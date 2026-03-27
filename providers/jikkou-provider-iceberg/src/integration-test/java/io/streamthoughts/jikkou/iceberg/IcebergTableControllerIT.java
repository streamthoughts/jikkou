/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.iceberg;

import io.streamthoughts.jikkou.core.ReconciliationContext;
import io.streamthoughts.jikkou.core.ReconciliationMode;
import io.streamthoughts.jikkou.core.config.Configuration;
import io.streamthoughts.jikkou.core.models.CoreAnnotations;
import io.streamthoughts.jikkou.core.models.ObjectMeta;
import io.streamthoughts.jikkou.core.models.ResourceList;
import io.streamthoughts.jikkou.core.models.change.ResourceChange;
import io.streamthoughts.jikkou.core.reconciler.ChangeResult;
import io.streamthoughts.jikkou.core.reconciler.Operation;
import io.streamthoughts.jikkou.core.selector.Selectors;
import io.streamthoughts.jikkou.iceberg.namespace.models.V1IcebergNamespace;
import io.streamthoughts.jikkou.iceberg.namespace.models.V1IcebergNamespaceSpec;
import io.streamthoughts.jikkou.iceberg.table.models.V1IcebergColumn;
import io.streamthoughts.jikkou.iceberg.table.models.V1IcebergPartitionField;
import io.streamthoughts.jikkou.iceberg.table.models.V1IcebergSchema;
import io.streamthoughts.jikkou.iceberg.table.models.V1IcebergSortField;
import io.streamthoughts.jikkou.iceberg.table.models.V1IcebergTable;
import io.streamthoughts.jikkou.iceberg.table.models.V1IcebergTableSpec;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class IcebergTableControllerIT extends BaseExtensionProviderIT {

    @BeforeEach
    void createTestNamespace() {
        // Ensure the namespace exists for all table tests
        V1IcebergNamespaceSpec spec = new V1IcebergNamespaceSpec();
        V1IcebergNamespace ns = V1IcebergNamespace.builder()
            .withMetadata(ObjectMeta.builder().withName("test_ns").build())
            .withSpec(spec)
            .build();

        api.reconcile(ResourceList.of(List.of(ns)), ReconciliationMode.CREATE,
            ReconciliationContext.builder().dryRun(false).build());
    }

    @Test
    void shouldCreateTable() {
        // GIVEN
        V1IcebergTable table = table("test_ns.events",
            List.of(col("id", "long", true), col("name", "string", false)),
            null, null, null);

        // WHEN
        List<ChangeResult> results = api.reconcile(
            ResourceList.of(List.of(table)), ReconciliationMode.CREATE,
            ReconciliationContext.builder().dryRun(false).build()).results();

        // THEN
        Assertions.assertEquals(1, results.size());
        Assertions.assertEquals(Operation.CREATE, results.get(0).change().getSpec().getOp());

        ResourceList<V1IcebergTable> actual = listTables();
        Assertions.assertTrue(actual.stream()
            .anyMatch(t -> "test_ns.events".equals(t.getMetadata().getName())));
    }

    @Test
    void shouldCreateTableWithPartitionsAndSortOrder() {
        // GIVEN
        V1IcebergPartitionField pf = new V1IcebergPartitionField();
        pf.setSourceColumn("event_time");
        pf.setTransform("day");

        V1IcebergSortField sf = new V1IcebergSortField();
        sf.setColumn("event_time");
        sf.setDirection("asc");
        sf.setNullOrder("last");

        V1IcebergTable table = table("test_ns.partitioned",
            List.of(
                col("id", "long", true),
                col("event_time", "timestamp", false)
            ),
            List.of(pf), List.of(sf), null);

        // WHEN
        List<ChangeResult> results = api.reconcile(
            ResourceList.of(List.of(table)), ReconciliationMode.CREATE,
            ReconciliationContext.builder().dryRun(false).build()).results();

        // THEN
        Assertions.assertEquals(1, results.size());
        Assertions.assertEquals(Operation.CREATE, results.get(0).change().getSpec().getOp());

        V1IcebergTable actual = listTables().stream()
            .filter(t -> "test_ns.partitioned".equals(t.getMetadata().getName()))
            .findFirst()
            .orElseThrow(() -> new AssertionError("Table not found"));

        Assertions.assertNotNull(actual.getSpec().getPartitionFields());
        Assertions.assertEquals(1, actual.getSpec().getPartitionFields().size());
        Assertions.assertEquals("day", actual.getSpec().getPartitionFields().get(0).getTransform());

        Assertions.assertNotNull(actual.getSpec().getSortFields());
        Assertions.assertEquals(1, actual.getSpec().getSortFields().size());
        Assertions.assertEquals("asc", actual.getSpec().getSortFields().get(0).getDirection());
    }

    @Test
    void shouldAddColumnToExistingTable() {
        // GIVEN - create initial table
        V1IcebergTable table = table("test_ns.evolving",
            List.of(col("id", "long", true)),
            null, null, null);
        api.reconcile(ResourceList.of(List.of(table)), ReconciliationMode.CREATE,
            ReconciliationContext.builder().dryRun(false).build());

        // WHEN - add a column
        V1IcebergTable updated = table("test_ns.evolving",
            List.of(col("id", "long", true), col("email", "string", false)),
            null, null, null);
        List<ChangeResult> results = api.reconcile(
            ResourceList.of(List.of(updated)), ReconciliationMode.UPDATE,
            ReconciliationContext.builder().dryRun(false).build()).results();

        // THEN
        Map<String, ResourceChange> byName = results.stream()
            .map(ChangeResult::change)
            .collect(Collectors.toMap(c -> c.getMetadata().getName(), c -> c));
        Assertions.assertEquals(Operation.UPDATE, byName.get("test_ns.evolving").getSpec().getOp());

        V1IcebergTable actual = listTables().stream()
            .filter(t -> "test_ns.evolving".equals(t.getMetadata().getName()))
            .findFirst()
            .orElseThrow();
        Assertions.assertEquals(2, actual.getSpec().getSchema().getColumns().size());
    }

    @Test
    void shouldUpdateTableProperties() {
        // GIVEN
        V1IcebergTable table = table("test_ns.props_test",
            List.of(col("id", "long", true)),
            null, null, Map.of("write.format.default", "parquet"));
        api.reconcile(ResourceList.of(List.of(table)), ReconciliationMode.CREATE,
            ReconciliationContext.builder().dryRun(false).build());

        // WHEN - change property
        V1IcebergTable updated = table("test_ns.props_test",
            List.of(col("id", "long", true)),
            null, null, Map.of("write.format.default", "orc"));
        List<ChangeResult> results = api.reconcile(
            ResourceList.of(List.of(updated)), ReconciliationMode.UPDATE,
            ReconciliationContext.builder().dryRun(false).build()).results();

        // THEN
        Assertions.assertEquals(1, results.size());
        Assertions.assertEquals(Operation.UPDATE, results.get(0).change().getSpec().getOp());

        V1IcebergTable actual = listTables().stream()
            .filter(t -> "test_ns.props_test".equals(t.getMetadata().getName()))
            .findFirst()
            .orElseThrow();
        Assertions.assertEquals("orc", actual.getSpec().getProperties().get("write.format.default"));
    }

    @Test
    void shouldDeleteTable() {
        // GIVEN
        V1IcebergTable table = table("test_ns.to_delete",
            List.of(col("id", "long", true)),
            null, null, null);
        api.reconcile(ResourceList.of(List.of(table)), ReconciliationMode.CREATE,
            ReconciliationContext.builder().dryRun(false).build());

        // WHEN - annotate with jikkou.io/delete for DELETE mode
        V1IcebergSchema deleteSchema = new V1IcebergSchema();
        deleteSchema.setColumns(List.of(col("id", "long", true)));
        V1IcebergTableSpec deleteSpec = new V1IcebergTableSpec();
        deleteSpec.setSchema(deleteSchema);
        V1IcebergTable toDelete = V1IcebergTable.builder()
            .withMetadata(ObjectMeta.builder()
                .withName("test_ns.to_delete")
                .withAnnotation(CoreAnnotations.JIKKOU_IO_DELETE, true)
                .build())
            .withSpec(deleteSpec)
            .build();

        List<ChangeResult> results = api.reconcile(
            ResourceList.of(List.of(toDelete)), ReconciliationMode.DELETE,
            ReconciliationContext.builder().dryRun(false).build()).results();

        // THEN
        Assertions.assertEquals(1, results.size());
        Assertions.assertEquals(Operation.DELETE, results.get(0).change().getSpec().getOp());
        Assertions.assertFalse(listTables().stream()
            .anyMatch(t -> "test_ns.to_delete".equals(t.getMetadata().getName())));
    }

    @Test
    void shouldProduceNoChangesForIdenticalTable() {
        // GIVEN
        V1IcebergTable table = table("test_ns.idempotent_tbl",
            List.of(col("id", "long", true), col("name", "string", false)),
            null, null, Map.of("write.format.default", "parquet"));
        api.reconcile(ResourceList.of(List.of(table)), ReconciliationMode.CREATE,
            ReconciliationContext.builder().dryRun(false).build());

        // WHEN - re-apply same spec
        List<ChangeResult> results = api.reconcile(
            ResourceList.of(List.of(table)), ReconciliationMode.UPDATE,
            ReconciliationContext.builder().dryRun(false).build()).results();

        // THEN
        Assertions.assertEquals(1, results.size());
        Assertions.assertEquals(Operation.NONE, results.get(0).change().getSpec().getOp());
    }

    @Test
    void shouldDeleteOrphanTablesInFullMode() {
        // GIVEN - create two tables
        V1IcebergTable t1 = table("test_ns.keep",
            List.of(col("id", "long", true)), null, null, null);
        V1IcebergTable t2 = table("test_ns.orphan",
            List.of(col("id", "long", true)), null, null, null);
        api.reconcile(ResourceList.of(List.of(t1, t2)), ReconciliationMode.CREATE,
            ReconciliationContext.builder().dryRun(false).build());

        // WHEN - apply FULL mode with only t1 and delete-orphans=true
        List<ChangeResult> results = api.reconcile(
            ResourceList.of(List.of(t1)), ReconciliationMode.FULL,
            ReconciliationContext.builder()
                .dryRun(false)
                .configuration(Configuration.of("delete-orphans", true))
                .build()).results();

        // THEN
        boolean hasDelete = results.stream()
            .map(r -> r.change().getSpec().getOp())
            .anyMatch(op -> op == Operation.DELETE);
        Assertions.assertTrue(hasDelete);
        Assertions.assertFalse(listTables().stream()
            .anyMatch(t -> "test_ns.orphan".equals(t.getMetadata().getName())));
    }

    private ResourceList<V1IcebergTable> listTables() {
        return api.listResources(V1IcebergTable.class, Selectors.NO_SELECTOR, Configuration.empty());
    }

    private static V1IcebergColumn col(String name, String type, boolean required) {
        V1IcebergColumn c = new V1IcebergColumn();
        c.setName(name);
        c.setType(type);
        c.setRequired(required);
        return c;
    }

    private static V1IcebergTable table(String name,
                                         List<V1IcebergColumn> columns,
                                         List<V1IcebergPartitionField> partitions,
                                         List<V1IcebergSortField> sortFields,
                                         Map<String, String> properties) {
        V1IcebergSchema schema = new V1IcebergSchema();
        schema.setColumns(columns);

        V1IcebergTableSpec spec = new V1IcebergTableSpec();
        spec.setSchema(schema);
        spec.setPartitionFields(partitions);
        spec.setSortFields(sortFields);
        spec.setProperties(properties != null && !properties.isEmpty() ? properties : null);

        return V1IcebergTable.builder()
            .withMetadata(ObjectMeta.builder().withName(name).build())
            .withSpec(spec)
            .build();
    }
}
