/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.jikkou.iceberg.table;

import io.jikkou.core.models.change.GenericResourceChange;
import io.jikkou.core.models.change.ResourceChange;
import io.jikkou.core.models.change.ResourceChangeSpec;
import io.jikkou.core.models.change.StateChange;
import io.jikkou.core.reconciler.Operation;
import io.jikkou.core.reconciler.change.ChangeComputer;
import io.jikkou.core.reconciler.change.ChangeComputerBuilder;
import io.jikkou.core.reconciler.change.ResourceChangeComputer;
import io.jikkou.core.reconciler.change.ResourceChangeFactory;
import io.jikkou.iceberg.table.models.V1IcebergColumn;
import io.jikkou.iceberg.table.models.V1IcebergPartitionField;
import io.jikkou.iceberg.table.models.V1IcebergSortField;
import io.jikkou.iceberg.table.models.V1IcebergTable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Computes changes for Iceberg table resources.
 */
public final class TableChangeComputer extends ResourceChangeComputer<String, V1IcebergTable> {

    /**
     * Creates a new {@link TableChangeComputer} instance with default settings.
     */
    public TableChangeComputer() {
        this(List.of(), false, false, false);
    }

    /**
     * Creates a new {@link TableChangeComputer} instance.
     *
     * @param exclusionPatterns     table name patterns to exclude from deletion.
     * @param deleteOrphanTables    whether to delete tables absent from the desired state.
     * @param deleteOrphanColumns   whether to delete columns absent from the desired schema.
     * @param deletePurgeEnabled    whether to purge underlying data on table deletion.
     */
    public TableChangeComputer(@NotNull final List<Pattern> exclusionPatterns,
                               final boolean deleteOrphanTables,
                               final boolean deleteOrphanColumns,
                               final boolean deletePurgeEnabled) {
        super(
            ChangeComputerBuilder.KeyMapper.byName(),
            new TableChangeFactory(exclusionPatterns, deleteOrphanColumns, deletePurgeEnabled),
            deleteOrphanTables
        );
    }

    /**
     * Factory that produces {@link ResourceChange} objects for table changes.
     */
    public static final class TableChangeFactory extends ResourceChangeFactory<String, V1IcebergTable> {

        private static final String PROPERTY_PREFIX = "property.";

        private final List<Pattern> exclusionPatterns;
        private final boolean deleteOrphanColumns;
        private final boolean deletePurgeEnabled;

        /**
         * Creates a new {@link TableChangeFactory} instance.
         */
        public TableChangeFactory(@NotNull final List<Pattern> exclusionPatterns,
                                  final boolean deleteOrphanColumns,
                                  final boolean deletePurgeEnabled) {
            this.exclusionPatterns = exclusionPatterns;
            this.deleteOrphanColumns = deleteOrphanColumns;
            this.deletePurgeEnabled = deletePurgeEnabled;
        }

        /** {@inheritDoc} */
        @Override
        public ResourceChange createChangeForCreate(@NotNull final String key,
                                                    @NotNull final V1IcebergTable after) {
            List<StateChange> changes = new ArrayList<>();

            // Schema columns
            List<V1IcebergColumn> columns = getColumns(after);
            changes.add(StateChange.create("schema.columns", columns.isEmpty() ? null : columns));

            // Partition spec
            List<V1IcebergPartitionField> partitions = getPartitionSpec(after);
            if (!partitions.isEmpty()) {
                changes.add(StateChange.create("partitionSpec", partitions));
            }

            // Sort order
            List<V1IcebergSortField> sortFields = getSortOrder(after);
            if (!sortFields.isEmpty()) {
                changes.add(StateChange.create("sortOrder", sortFields));
            }

            // Location
            String location = getLocation(after);
            if (location != null) {
                changes.add(StateChange.create("location", location));
            }

            // Properties
            getProperties(after).forEach((k, v) ->
                changes.add(StateChange.create(PROPERTY_PREFIX + k, v)));

            return buildResourceChange(after, Operation.CREATE, changes);
        }

        /** {@inheritDoc} */
        @Override
        @Nullable
        public ResourceChange createChangeForDelete(@NotNull final String key,
                                                    @NotNull final V1IcebergTable before) {
            String tableName = before.getMetadata().getName();

            // Check exclusion patterns
            if (!exclusionPatterns.isEmpty()) {
                boolean excluded = exclusionPatterns.stream()
                    .anyMatch(p -> p.matcher(tableName).matches());
                if (excluded) {
                    return null;
                }
            }

            return buildResourceChange(before, Operation.DELETE, Collections.emptyList());
        }

        /** {@inheritDoc} */
        @Override
        public ResourceChange createChangeForUpdate(@NotNull final String key,
                                                    @NotNull final V1IcebergTable before,
                                                    @NotNull final V1IcebergTable after) {
            List<StateChange> changes = new ArrayList<>();

            // Compute column-level changes
            TableColumnDiffer differ = new TableColumnDiffer(deleteOrphanColumns);
            List<V1IcebergColumn> beforeColumns = getColumns(before);
            List<V1IcebergColumn> afterColumns = getColumns(after);
            changes.addAll(differ.computeChanges(beforeColumns, afterColumns));

            // Compare partition spec — normalize before-side to match user-spec style
            List<V1IcebergPartitionField> beforePartitions = getPartitionSpec(before);
            List<V1IcebergPartitionField> afterPartitions = getPartitionSpec(after);
            List<V1IcebergPartitionField> normalizedBefore = normalizePartitionFields(beforePartitions, afterPartitions);
            if (!Objects.equals(normalizedBefore, afterPartitions)) {
                changes.add(StateChange.with("partitionSpec", beforePartitions, afterPartitions));
            } else {
                changes.add(StateChange.none("partitionSpec", beforePartitions));
            }

            // Compare sort order — normalize before-side to match user-spec style
            List<V1IcebergSortField> beforeSort = getSortOrder(before);
            List<V1IcebergSortField> afterSort = getSortOrder(after);
            List<V1IcebergSortField> normalizedBeforeSort = normalizeSortFields(beforeSort, afterSort);
            if (!Objects.equals(normalizedBeforeSort, afterSort)) {
                changes.add(StateChange.with("sortOrder", beforeSort, afterSort));
            }

            // Compare location — only emit change if user explicitly specified a location
            String beforeLocation = getLocation(before);
            String afterLocation = getLocation(after);
            if (afterLocation != null && !Objects.equals(beforeLocation, afterLocation)) {
                changes.add(StateChange.with("location", beforeLocation, afterLocation));
            }

            // Compare properties
            Map<String, String> beforeProps = getProperties(before);
            Map<String, String> afterProps = getProperties(after);
            ChangeComputer<Map.Entry<String, String>, StateChange> propComputer = ChangeComputer
                .<String, Map.Entry<String, String>, StateChange>builder()
                .withKeyMapper(Map.Entry::getKey)
                .withChangeFactory((propKey, beforeEntry, afterEntry) -> {
                    String bVal = beforeEntry != null ? beforeEntry.getValue() : null;
                    String aVal = afterEntry != null ? afterEntry.getValue() : null;
                    return Optional.of(StateChange.with(PROPERTY_PREFIX + propKey, bVal, aVal));
                })
                .withDeleteOrphans(false)
                .build();
            changes.addAll(propComputer.computeChanges(
                new ArrayList<>(beforeProps.entrySet()),
                new ArrayList<>(afterProps.entrySet())));

            boolean hasChanged = changes.stream().anyMatch(c -> c.getOp() != Operation.NONE);
            Operation op = hasChanged ? Operation.UPDATE : Operation.NONE;

            return buildResourceChange(before, op, changes);
        }

        @NotNull
        private static List<V1IcebergColumn> getColumns(@NotNull final V1IcebergTable table) {
            return Optional.ofNullable(table.getSpec())
                .map(s -> s.getSchema())
                .map(schema -> schema.getColumns())
                .orElse(Collections.emptyList());
        }

        @NotNull
        private static List<V1IcebergPartitionField> getPartitionSpec(@NotNull final V1IcebergTable table) {
            return Optional.ofNullable(table.getSpec())
                .map(s -> s.getPartitionFields())
                .orElse(Collections.emptyList());
        }

        @NotNull
        private static List<V1IcebergSortField> getSortOrder(@NotNull final V1IcebergTable table) {
            return Optional.ofNullable(table.getSpec())
                .map(s -> s.getSortFields())
                .orElse(Collections.emptyList());
        }

        @Nullable
        private static String getLocation(@NotNull final V1IcebergTable table) {
            return Optional.ofNullable(table.getSpec())
                .map(s -> s.getLocation())
                .orElse(null);
        }

        @NotNull
        private static Map<String, String> getProperties(@NotNull final V1IcebergTable table) {
            return Optional.ofNullable(table.getSpec())
                .map(s -> s.getProperties())
                .orElse(Collections.emptyMap());
        }

        /**
         * Normalizes before-side partition fields for comparison: if the after-side field at the
         * same index has no name, clears the name from the before-side copy so auto-generated
         * names don't cause false positives.
         */
        @NotNull
        private static List<V1IcebergPartitionField> normalizePartitionFields(
                @NotNull final List<V1IcebergPartitionField> before,
                @NotNull final List<V1IcebergPartitionField> after) {
            if (before.size() != after.size()) {
                return before;
            }
            List<V1IcebergPartitionField> result = new ArrayList<>(before.size());
            for (int i = 0; i < before.size(); i++) {
                V1IcebergPartitionField bf = before.get(i);
                V1IcebergPartitionField af = after.get(i);
                if (af.getName() == null && bf.getName() != null) {
                    V1IcebergPartitionField copy = new V1IcebergPartitionField();
                    copy.setSourceColumn(bf.getSourceColumn());
                    copy.setTransform(bf.getTransform());
                    // leave name null
                    result.add(copy);
                } else {
                    result.add(bf);
                }
            }
            return result;
        }

        /**
         * Normalizes before-side sort fields for comparison: converts long-form nullOrder
         * (e.g. "nulls last") to short form ("last") to match user-spec conventions.
         */
        @NotNull
        private static List<V1IcebergSortField> normalizeSortFields(
                @NotNull final List<V1IcebergSortField> before,
                @NotNull final List<V1IcebergSortField> after) {
            if (before.size() != after.size()) {
                return before;
            }
            List<V1IcebergSortField> result = new ArrayList<>(before.size());
            for (V1IcebergSortField bf : before) {
                String nullOrder = bf.getNullOrder();
                if (nullOrder != null && nullOrder.startsWith("nulls ")) {
                    V1IcebergSortField copy = new V1IcebergSortField();
                    copy.setColumn(bf.getColumn());
                    copy.setTerm(bf.getTerm());
                    copy.setDirection(bf.getDirection());
                    copy.setNullOrder(nullOrder.substring("nulls ".length()));
                    result.add(copy);
                } else {
                    result.add(bf);
                }
            }
            return result;
        }

        @NotNull
        private static ResourceChange buildResourceChange(@NotNull final V1IcebergTable resource,
                                                          @NotNull final Operation op,
                                                          @NotNull final List<StateChange> changes) {
            return GenericResourceChange.builder(V1IcebergTable.class)
                .withMetadata(resource.getMetadata())
                .withSpec(ResourceChangeSpec.builder()
                    .withOperation(op)
                    .withChanges(changes)
                    .build())
                .build();
        }
    }
}
