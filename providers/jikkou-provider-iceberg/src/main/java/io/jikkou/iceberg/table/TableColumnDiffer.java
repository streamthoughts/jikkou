/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.jikkou.iceberg.table;

import io.jikkou.core.models.change.StateChange;
import io.jikkou.core.reconciler.Operation;
import io.jikkou.iceberg.table.models.V1IcebergColumn;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.jetbrains.annotations.NotNull;

/**
 * Computes column-level diff between two column lists using a two-pass algorithm.
 *
 * <p>Pass 1 — Resolve renames (via {@code previousName} field)
 * <p>Pass 2 — Standard column diff on remaining columns
 */
public final class TableColumnDiffer {

    private static final String COLUMN_PREFIX = "column.";
    private static final String RENAME_SUFFIX = ".rename";

    private final boolean deleteOrphanColumns;

    /**
     * Creates a new {@link TableColumnDiffer} instance.
     *
     * @param deleteOrphanColumns whether to emit DELETE changes for columns in the actual schema
     *                            that are absent from the desired schema.
     */
    public TableColumnDiffer(final boolean deleteOrphanColumns) {
        this.deleteOrphanColumns = deleteOrphanColumns;
    }

    /**
     * Computes column-level {@link StateChange} objects between the actual and desired column lists.
     *
     * @param actual  the columns currently in the live table.
     * @param desired the columns from the resource specification.
     * @return list of state changes (renames, adds, drops, updates, or none).
     * @throws IllegalArgumentException if a {@code previousName} refers to a column that does not
     *                                  exist in the actual schema and the new name is also absent.
     */
    @NotNull
    public List<StateChange> computeChanges(@NotNull final List<V1IcebergColumn> actual,
                                            @NotNull final List<V1IcebergColumn> desired) {
        List<StateChange> changes = new ArrayList<>();

        // Index actual columns by name for quick lookup
        Map<String, V1IcebergColumn> actualByName = new LinkedHashMap<>();
        for (V1IcebergColumn col : actual) {
            actualByName.put(col.getName(), col);
        }

        // Index desired columns by name for quick lookup
        Map<String, V1IcebergColumn> desiredByName = new LinkedHashMap<>();
        for (V1IcebergColumn col : desired) {
            desiredByName.put(col.getName(), col);
        }

        // Pass 1: Resolve renames
        Set<String> handledActual = new HashSet<>();
        Set<String> handledDesired = new HashSet<>();

        for (V1IcebergColumn desiredCol : desired) {
            String previousName = desiredCol.getPreviousName();
            if (previousName == null || previousName.isBlank()) {
                continue;
            }
            String newName = desiredCol.getName();

            if (actualByName.containsKey(previousName) && !actualByName.containsKey(newName)) {
                // Rename from previousName to newName
                changes.add(StateChange.builder()
                    .withName(COLUMN_PREFIX + previousName + RENAME_SUFFIX)
                    .withOp(Operation.UPDATE)
                    .withBefore(previousName)
                    .withAfter(newName)
                    .build());
                handledActual.add(previousName);
                handledDesired.add(newName);
            } else if (!actualByName.containsKey(previousName) && actualByName.containsKey(newName)) {
                // Rename already applied — previousName is stale, ignore silently
                // (the new name already exists in the actual schema)
            } else if (!actualByName.containsKey(previousName) && !actualByName.containsKey(newName)) {
                // Rename source not found AND new name also absent — error
                throw new IllegalArgumentException(
                    "Column rename source '" + previousName + "' not found in live table, " +
                    "and new name '" + newName + "' also absent. " +
                    "Cannot apply rename for column '" + newName + "'.");
            }
            // else: previousName and newName both in actual — ambiguous, treat as non-rename
        }

        // Pass 2: Standard diff on remaining (unhandled) columns
        for (V1IcebergColumn desiredCol : desired) {
            String name = desiredCol.getName();
            if (handledDesired.contains(name)) {
                continue;
            }

            if (!actualByName.containsKey(name)) {
                // ADD
                changes.add(StateChange.create(COLUMN_PREFIX + name, desiredCol));
            } else {
                // UPDATE or NONE — compare fields
                V1IcebergColumn actualCol = actualByName.get(name);
                List<StateChange> fieldChanges = compareColumn(name, actualCol, desiredCol);
                changes.addAll(fieldChanges);
                handledActual.add(name);
            }
        }

        // Handle drops (actual columns absent from desired, and not already handled by rename)
        if (deleteOrphanColumns) {
            for (V1IcebergColumn actualCol : actual) {
                String name = actualCol.getName();
                if (!handledActual.contains(name) && !desiredByName.containsKey(name)) {
                    changes.add(StateChange.delete(COLUMN_PREFIX + name, actualCol));
                }
            }
        }

        return changes;
    }

    @NotNull
    private static List<StateChange> compareColumn(@NotNull final String name,
                                                   @NotNull final V1IcebergColumn actual,
                                                   @NotNull final V1IcebergColumn desired) {
        List<StateChange> changes = new ArrayList<>();

        // Compare type
        boolean typeChanged = !Objects.equals(actual.getType(), desired.getType());
        // Compare doc
        boolean docChanged = !Objects.equals(actual.getDoc(), desired.getDoc());
        // Compare required
        boolean requiredChanged = !Objects.equals(actual.getRequired(), desired.getRequired());
        // Compare writeDefault
        boolean writeDefaultChanged = !Objects.equals(actual.getWriteDefault(), desired.getWriteDefault());

        if (typeChanged || docChanged || requiredChanged) {
            // Emit a column-level UPDATE with before/after
            StateChange sc = StateChange.with(COLUMN_PREFIX + name, actual, desired);
            changes.add(sc);
        } else if (writeDefaultChanged) {
            changes.add(StateChange.with(
                COLUMN_PREFIX + name + ".writeDefault",
                actual.getWriteDefault(),
                desired.getWriteDefault()));
        } else {
            // No change
            changes.add(StateChange.none(COLUMN_PREFIX + name, actual));
        }

        return changes;
    }
}
