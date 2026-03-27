/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.iceberg.table;

import io.streamthoughts.jikkou.core.models.change.StateChange;
import io.streamthoughts.jikkou.core.reconciler.Operation;
import io.streamthoughts.jikkou.iceberg.table.models.V1IcebergColumn;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class TableColumnDifferTest {

    // Helper to build a minimal column
    private static V1IcebergColumn col(String name, String type) {
        V1IcebergColumn c = new V1IcebergColumn();
        c.setName(name);
        c.setType(type);
        c.setRequired(false);
        return c;
    }

    private static V1IcebergColumn colWithPrevious(String name, String type, String previousName) {
        V1IcebergColumn c = col(name, type);
        c.setPreviousName(previousName);
        return c;
    }

    private static V1IcebergColumn colRequired(String name, String type) {
        V1IcebergColumn c = col(name, type);
        c.setRequired(true);
        return c;
    }

    // --- No changes ---

    @Test
    void shouldReturnNoneWhenColumnsUnchanged() {
        var differ = new TableColumnDiffer(false);
        List<V1IcebergColumn> cols = List.of(col("id", "long"), col("name", "string"));

        List<StateChange> changes = differ.computeChanges(cols, cols);

        // All should be NONE
        Assertions.assertTrue(changes.stream().allMatch(c -> c.getOp() == Operation.NONE),
            "Expected all NONE changes, got: " + changes);
    }

    // --- Add column ---

    @Test
    void shouldEmitCreateForNewColumn() {
        var differ = new TableColumnDiffer(false);
        List<V1IcebergColumn> actual = List.of(col("id", "long"));
        List<V1IcebergColumn> desired = List.of(col("id", "long"), col("email", "string"));

        List<StateChange> changes = differ.computeChanges(actual, desired);

        StateChange emailChange = changes.stream()
            .filter(c -> "column.email".equals(c.getName()))
            .findFirst()
            .orElseThrow(() -> new AssertionError("Missing column.email change"));
        Assertions.assertEquals(Operation.CREATE, emailChange.getOp());
    }

    // --- Drop column ---

    @Test
    void shouldEmitDeleteForDroppedColumnWhenDeleteOrphansTrue() {
        var differ = new TableColumnDiffer(true);
        List<V1IcebergColumn> actual = List.of(col("id", "long"), col("old_col", "string"));
        List<V1IcebergColumn> desired = List.of(col("id", "long"));

        List<StateChange> changes = differ.computeChanges(actual, desired);

        StateChange dropChange = changes.stream()
            .filter(c -> "column.old_col".equals(c.getName()))
            .findFirst()
            .orElseThrow(() -> new AssertionError("Missing column.old_col drop change"));
        Assertions.assertEquals(Operation.DELETE, dropChange.getOp());
    }

    @Test
    void shouldNotEmitDeleteWhenDeleteOrphansFalse() {
        var differ = new TableColumnDiffer(false);
        List<V1IcebergColumn> actual = List.of(col("id", "long"), col("old_col", "string"));
        List<V1IcebergColumn> desired = List.of(col("id", "long"));

        List<StateChange> changes = differ.computeChanges(actual, desired);

        boolean hasDelete = changes.stream()
            .anyMatch(c -> c.getOp() == Operation.DELETE);
        Assertions.assertFalse(hasDelete, "Should not emit DELETE when deleteOrphanColumns=false");
    }

    // --- Update column ---

    @Test
    void shouldEmitUpdateForTypeChange() {
        var differ = new TableColumnDiffer(false);
        List<V1IcebergColumn> actual = List.of(col("count", "int"));
        List<V1IcebergColumn> desired = List.of(col("count", "long")); // int → long promotion

        List<StateChange> changes = differ.computeChanges(actual, desired);

        StateChange countChange = changes.stream()
            .filter(c -> "column.count".equals(c.getName()))
            .findFirst()
            .orElseThrow(() -> new AssertionError("Missing column.count change"));
        Assertions.assertEquals(Operation.UPDATE, countChange.getOp());
    }

    // --- Rename detection ---

    @Test
    void shouldDetectRenameViaPreviousName() {
        var differ = new TableColumnDiffer(false);
        List<V1IcebergColumn> actual = List.of(col("customer_id", "string"));
        List<V1IcebergColumn> desired = List.of(colWithPrevious("user_id", "string", "customer_id"));

        List<StateChange> changes = differ.computeChanges(actual, desired);

        StateChange renameChange = changes.stream()
            .filter(c -> c.getName().endsWith(".rename"))
            .findFirst()
            .orElseThrow(() -> new AssertionError("Missing rename change"));

        Assertions.assertEquals("column.customer_id.rename", renameChange.getName());
        Assertions.assertEquals(Operation.UPDATE, renameChange.getOp());
        Assertions.assertEquals("customer_id", renameChange.getBefore());
        Assertions.assertEquals("user_id", renameChange.getAfter());
    }

    @Test
    void shouldIgnoreStalePreviousNameWhenNewNameAlreadyExists() {
        var differ = new TableColumnDiffer(false);
        // Rename already applied: actual already has "user_id", customer_id gone
        List<V1IcebergColumn> actual = List.of(col("user_id", "string"));
        List<V1IcebergColumn> desired = List.of(colWithPrevious("user_id", "string", "customer_id"));

        // Should not throw, should produce NONE
        List<StateChange> changes = differ.computeChanges(actual, desired);

        boolean hasRename = changes.stream().anyMatch(c -> c.getName().endsWith(".rename"));
        Assertions.assertFalse(hasRename, "Should not emit rename when previousName is stale (new name already in actual)");
    }

    @Test
    void shouldThrowWhenPreviousNameAndNewNameBothAbsent() {
        var differ = new TableColumnDiffer(false);
        List<V1IcebergColumn> actual = List.of(col("other_col", "string"));
        // Neither "customer_id" nor "user_id" exist in actual
        List<V1IcebergColumn> desired = List.of(colWithPrevious("user_id", "string", "customer_id"));

        Assertions.assertThrows(
            IllegalArgumentException.class,
            () -> differ.computeChanges(actual, desired),
            "Should throw when rename source not found and new name also absent"
        );
    }

    // --- Mixed scenario ---

    @Test
    void shouldHandleMixedChanges() {
        var differ = new TableColumnDiffer(true);
        List<V1IcebergColumn> actual = List.of(
            col("id", "long"),
            col("old_name", "string"),
            col("to_drop", "int")
        );
        List<V1IcebergColumn> desired = List.of(
            col("id", "long"),
            colWithPrevious("new_name", "string", "old_name"),
            col("new_col", "boolean")
        );

        List<StateChange> changes = differ.computeChanges(actual, desired);

        // Rename
        Assertions.assertTrue(changes.stream()
            .anyMatch(c -> c.getName().equals("column.old_name.rename") && c.getOp() == Operation.UPDATE));

        // Add
        Assertions.assertTrue(changes.stream()
            .anyMatch(c -> c.getName().equals("column.new_col") && c.getOp() == Operation.CREATE));

        // Drop
        Assertions.assertTrue(changes.stream()
            .anyMatch(c -> c.getName().equals("column.to_drop") && c.getOp() == Operation.DELETE));
    }
}
