/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.jikkou.iceberg.view;

import io.jikkou.core.models.ObjectMeta;
import io.jikkou.core.models.change.ResourceChange;
import io.jikkou.core.reconciler.Operation;
import io.jikkou.iceberg.view.models.V1IcebergView;
import io.jikkou.iceberg.view.models.V1IcebergViewQuery;
import io.jikkou.iceberg.view.models.V1IcebergViewSpec;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ViewChangeComputerTest {

    private static V1IcebergView view(String name,
                                       List<V1IcebergViewQuery> queries,
                                       String defaultNamespace,
                                       Map<String, String> properties) {
        V1IcebergViewSpec spec = new V1IcebergViewSpec();
        spec.setQueries(queries);
        spec.setDefaultNamespace(defaultNamespace);
        spec.setProperties(properties.isEmpty() ? null : properties);

        return V1IcebergView.builder()
            .withMetadata(ObjectMeta.builder().withName(name).build())
            .withSpec(spec)
            .build();
    }

    private static V1IcebergViewQuery query(String dialect, String sql) {
        return V1IcebergViewQuery.builder()
            .withDialect(dialect)
            .withSql(sql)
            .build();
    }

    @Test
    void shouldDetectCreateWhenViewOnlyInExpected() {
        V1IcebergView expected = view("db.my_view",
            List.of(query("spark", "SELECT 1")),
            "db",
            Map.of());

        ViewChangeComputer computer = new ViewChangeComputer();
        List<ResourceChange> changes = computer.computeChanges(List.of(), List.of(expected));

        Assertions.assertEquals(1, changes.size());
        Assertions.assertEquals(Operation.CREATE, changes.get(0).getSpec().getOp());
    }

    @Test
    void shouldDetectDeleteWhenViewOnlyInActual() {
        V1IcebergView actual = view("db.my_view",
            List.of(query("spark", "SELECT 1")),
            "db",
            Map.of());

        ViewChangeComputer computer = new ViewChangeComputer(List.of(), true);
        List<ResourceChange> changes = computer.computeChanges(List.of(actual), List.of());

        Assertions.assertEquals(1, changes.size());
        Assertions.assertEquals(Operation.DELETE, changes.get(0).getSpec().getOp());
    }

    @Test
    void shouldExcludeFromDeletion() {
        V1IcebergView actual = view("db.protected_view",
            List.of(query("spark", "SELECT 1")),
            "db",
            Map.of());

        ViewChangeComputer computer = new ViewChangeComputer(
            List.of(Pattern.compile("db\\.protected.*")), true);
        List<ResourceChange> changes = computer.computeChanges(List.of(actual), List.of());

        Assertions.assertTrue(changes.isEmpty());
    }

    @Test
    void shouldDetectNoChangeWhenIdentical() {
        V1IcebergView before = view("db.my_view",
            List.of(query("spark", "SELECT 1")),
            "db",
            Map.of("comment", "test"));

        V1IcebergView after = view("db.my_view",
            List.of(query("spark", "SELECT 1")),
            "db",
            Map.of("comment", "test"));

        ViewChangeComputer computer = new ViewChangeComputer();
        List<ResourceChange> changes = computer.computeChanges(List.of(before), List.of(after));

        Assertions.assertEquals(1, changes.size());
        Assertions.assertEquals(Operation.NONE, changes.get(0).getSpec().getOp());
    }

    @Test
    void shouldDetectUpdateWhenQueriesChanged() {
        V1IcebergView before = view("db.my_view",
            List.of(query("spark", "SELECT 1")),
            "db",
            Map.of());

        V1IcebergView after = view("db.my_view",
            List.of(query("spark", "SELECT 2")),
            "db",
            Map.of());

        ViewChangeComputer computer = new ViewChangeComputer();
        List<ResourceChange> changes = computer.computeChanges(List.of(before), List.of(after));

        Assertions.assertEquals(1, changes.size());
        Assertions.assertEquals(Operation.UPDATE, changes.get(0).getSpec().getOp());
    }

    @Test
    void shouldDetectUpdateWhenDefaultNamespaceChanged() {
        V1IcebergView before = view("db.my_view",
            List.of(query("spark", "SELECT 1")),
            "db",
            Map.of());

        V1IcebergView after = view("db.my_view",
            List.of(query("spark", "SELECT 1")),
            "other_db",
            Map.of());

        ViewChangeComputer computer = new ViewChangeComputer();
        List<ResourceChange> changes = computer.computeChanges(List.of(before), List.of(after));

        Assertions.assertEquals(1, changes.size());
        Assertions.assertEquals(Operation.UPDATE, changes.get(0).getSpec().getOp());
    }

    @Test
    void shouldDetectUpdateWhenPropertyChanged() {
        V1IcebergView before = view("db.my_view",
            List.of(query("spark", "SELECT 1")),
            "db",
            Map.of("comment", "old"));

        V1IcebergView after = view("db.my_view",
            List.of(query("spark", "SELECT 1")),
            "db",
            Map.of("comment", "new"));

        ViewChangeComputer computer = new ViewChangeComputer();
        List<ResourceChange> changes = computer.computeChanges(List.of(before), List.of(after));

        Assertions.assertEquals(1, changes.size());
        Assertions.assertEquals(Operation.UPDATE, changes.get(0).getSpec().getOp());
    }

    @Test
    void shouldNotDeleteOrphansWhenDisabled() {
        V1IcebergView actual = view("db.orphan_view",
            List.of(query("spark", "SELECT 1")),
            "db",
            Map.of());

        ViewChangeComputer computer = new ViewChangeComputer(List.of(), false);
        List<ResourceChange> changes = computer.computeChanges(List.of(actual), List.of());

        Assertions.assertTrue(changes.isEmpty());
    }
}
