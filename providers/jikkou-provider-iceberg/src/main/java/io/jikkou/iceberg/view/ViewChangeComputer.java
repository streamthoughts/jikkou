/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.jikkou.iceberg.view;

import io.jikkou.core.models.change.GenericResourceChange;
import io.jikkou.core.models.change.ResourceChange;
import io.jikkou.core.models.change.ResourceChangeSpec;
import io.jikkou.core.models.change.StateChange;
import io.jikkou.core.reconciler.Operation;
import io.jikkou.core.reconciler.change.ChangeComputer;
import io.jikkou.core.reconciler.change.ChangeComputerBuilder;
import io.jikkou.core.reconciler.change.ResourceChangeComputer;
import io.jikkou.core.reconciler.change.ResourceChangeFactory;
import io.jikkou.iceberg.view.models.V1IcebergView;
import io.jikkou.iceberg.view.models.V1IcebergViewQuery;
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
 * Computes changes for Iceberg view resources.
 *
 * <p>Change detection covers: queries, defaultNamespace, defaultCatalog, and properties.
 * Schema is excluded — it is engine-inferred and read-only.
 */
public final class ViewChangeComputer extends ResourceChangeComputer<String, V1IcebergView> {

    /**
     * Creates a new {@link ViewChangeComputer} instance with default settings.
     */
    public ViewChangeComputer() {
        this(List.of(), false);
    }

    /**
     * Creates a new {@link ViewChangeComputer} instance.
     *
     * @param exclusionPatterns  view name patterns to exclude from deletion.
     * @param deleteOrphanViews  whether to delete views absent from the desired state.
     */
    public ViewChangeComputer(@NotNull final List<Pattern> exclusionPatterns,
                              final boolean deleteOrphanViews) {
        super(
            ChangeComputerBuilder.KeyMapper.byName(),
            new ViewChangeFactory(exclusionPatterns),
            deleteOrphanViews
        );
    }

    /**
     * Factory that produces {@link ResourceChange} objects for view changes.
     */
    public static final class ViewChangeFactory extends ResourceChangeFactory<String, V1IcebergView> {

        private static final String PROPERTY_PREFIX = "property.";

        private final List<Pattern> exclusionPatterns;

        public ViewChangeFactory(@NotNull final List<Pattern> exclusionPatterns) {
            this.exclusionPatterns = exclusionPatterns;
        }

        /** {@inheritDoc} */
        @Override
        public ResourceChange createChangeForCreate(@NotNull final String key,
                                                    @NotNull final V1IcebergView after) {
            List<StateChange> changes = new ArrayList<>();

            // Queries
            List<V1IcebergViewQuery> queries = getQueries(after);
            changes.add(StateChange.create("queries", queries.isEmpty() ? null : queries));

            // Default namespace
            String defaultNamespace = getDefaultNamespace(after);
            if (defaultNamespace != null) {
                changes.add(StateChange.create("defaultNamespace", defaultNamespace));
            }

            // Default catalog
            String defaultCatalog = getDefaultCatalog(after);
            if (defaultCatalog != null) {
                changes.add(StateChange.create("defaultCatalog", defaultCatalog));
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
                                                    @NotNull final V1IcebergView before) {
            String viewName = before.getMetadata().getName();

            if (!exclusionPatterns.isEmpty()) {
                boolean excluded = exclusionPatterns.stream()
                    .anyMatch(p -> p.matcher(viewName).matches());
                if (excluded) {
                    return null;
                }
            }

            return buildResourceChange(before, Operation.DELETE, Collections.emptyList());
        }

        /** {@inheritDoc} */
        @Override
        public ResourceChange createChangeForUpdate(@NotNull final String key,
                                                    @NotNull final V1IcebergView before,
                                                    @NotNull final V1IcebergView after) {
            List<StateChange> changes = new ArrayList<>();

            // Compare queries as a whole list
            List<V1IcebergViewQuery> beforeQueries = getQueries(before);
            List<V1IcebergViewQuery> afterQueries = getQueries(after);
            if (!Objects.equals(beforeQueries, afterQueries)) {
                changes.add(StateChange.with("queries", beforeQueries, afterQueries));
            } else {
                changes.add(StateChange.none("queries", beforeQueries));
            }

            // Compare default namespace
            String beforeNs = getDefaultNamespace(before);
            String afterNs = getDefaultNamespace(after);
            if (!Objects.equals(beforeNs, afterNs)) {
                changes.add(StateChange.with("defaultNamespace", beforeNs, afterNs));
            }

            // Compare default catalog
            String beforeCat = getDefaultCatalog(before);
            String afterCat = getDefaultCatalog(after);
            if (!Objects.equals(beforeCat, afterCat)) {
                changes.add(StateChange.with("defaultCatalog", beforeCat, afterCat));
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
        private static List<V1IcebergViewQuery> getQueries(@NotNull final V1IcebergView view) {
            return Optional.ofNullable(view.getSpec())
                .map(V1IcebergViewQuery -> view.getSpec().getQueries())
                .orElse(Collections.emptyList());
        }

        @Nullable
        private static String getDefaultNamespace(@NotNull final V1IcebergView view) {
            return Optional.ofNullable(view.getSpec())
                .map(s -> s.getDefaultNamespace())
                .orElse(null);
        }

        @Nullable
        private static String getDefaultCatalog(@NotNull final V1IcebergView view) {
            return Optional.ofNullable(view.getSpec())
                .map(s -> s.getDefaultCatalog())
                .orElse(null);
        }

        @NotNull
        private static Map<String, String> getProperties(@NotNull final V1IcebergView view) {
            return Optional.ofNullable(view.getSpec())
                .map(s -> s.getProperties())
                .orElse(Collections.emptyMap());
        }

        @NotNull
        private static ResourceChange buildResourceChange(@NotNull final V1IcebergView resource,
                                                          @NotNull final Operation op,
                                                          @NotNull final List<StateChange> changes) {
            return GenericResourceChange.builder(V1IcebergView.class)
                .withMetadata(resource.getMetadata())
                .withSpec(ResourceChangeSpec.builder()
                    .withOperation(op)
                    .withChanges(changes)
                    .build())
                .build();
        }
    }
}
