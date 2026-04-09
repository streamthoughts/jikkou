/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.jikkou.iceberg.table;

import io.jikkou.core.models.ObjectMeta;
import io.jikkou.core.models.change.ResourceChange;
import io.jikkou.core.reconciler.Operation;
import io.jikkou.iceberg.table.models.V1IcebergColumn;
import io.jikkou.iceberg.table.models.V1IcebergPartitionField;
import io.jikkou.iceberg.table.models.V1IcebergSchema;
import io.jikkou.iceberg.table.models.V1IcebergSortField;
import io.jikkou.iceberg.table.models.V1IcebergTable;
import io.jikkou.iceberg.table.models.V1IcebergTableSpec;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class TableChangeComputerTest {

    private static V1IcebergColumn col(String name, String type) {
        V1IcebergColumn c = new V1IcebergColumn();
        c.setName(name);
        c.setType(type);
        c.setRequired(false);
        return c;
    }

    private static V1IcebergTable table(String name, String location, Map<String, String> properties,
                                         List<V1IcebergColumn> columns) {
        return table(name, location, properties, columns, null, null);
    }

    private static V1IcebergTable table(String name, String location, Map<String, String> properties,
                                         List<V1IcebergColumn> columns,
                                         List<V1IcebergPartitionField> partitions,
                                         List<V1IcebergSortField> sortFields) {
        V1IcebergSchema schema = new V1IcebergSchema();
        schema.setColumns(columns);

        V1IcebergTableSpec spec = new V1IcebergTableSpec();
        spec.setLocation(location);
        spec.setProperties(properties);
        spec.setSchema(schema);
        spec.setPartitionFields(partitions);
        spec.setSortFields(sortFields);

        return V1IcebergTable.builder()
            .withMetadata(ObjectMeta.builder().withName(name).build())
            .withSpec(spec)
            .build();
    }

    private static V1IcebergPartitionField partition(String sourceColumn, String transform, String name) {
        V1IcebergPartitionField f = new V1IcebergPartitionField();
        f.setSourceColumn(sourceColumn);
        f.setTransform(transform);
        f.setName(name);
        return f;
    }

    private static V1IcebergSortField sort(String column, String direction, String nullOrder) {
        V1IcebergSortField f = new V1IcebergSortField();
        f.setColumn(column);
        f.setDirection(direction);
        f.setNullOrder(nullOrder);
        return f;
    }

    /**
     * When a table exists in the catalog with a location and extra properties,
     * re-applying the same user spec (without location, with a subset of properties)
     * should produce NONE — no spurious update.
     */
    @Test
    void shouldNotDetectSpuriousUpdateWhenCatalogInjectsExtraPropertiesAndLocation() {
        // "before" = what the catalog returns (has location + extra catalog-managed properties)
        V1IcebergTable before = table("ns.my_table",
            "s3://bucket/ns/my_table",
            Map.of("write.format.default", "parquet", "gc.enabled", "true"),
            List.of(col("id", "long")));

        // "after" = what the user specifies (no location, only user-managed properties)
        V1IcebergTable after = table("ns.my_table",
            null,
            Map.of("write.format.default", "parquet"),
            List.of(col("id", "long")));

        TableChangeComputer computer = new TableChangeComputer();
        List<ResourceChange> changes = computer.computeChanges(List.of(before), List.of(after));

        Assertions.assertEquals(1, changes.size());
        Assertions.assertEquals(Operation.NONE, changes.get(0).getSpec().getOp(),
            "Expected NONE but got " + changes.get(0).getSpec().getOp()
            + " with changes: " + changes.get(0).getSpec().getChanges());
    }

    /**
     * When the user explicitly sets a different location, it should be detected as UPDATE.
     */
    @Test
    void shouldDetectLocationChangeWhenUserExplicitlySetsIt() {
        V1IcebergTable before = table("ns.my_table",
            "s3://bucket/old-path",
            Map.of(),
            List.of(col("id", "long")));

        V1IcebergTable after = table("ns.my_table",
            "s3://bucket/new-path",
            Map.of(),
            List.of(col("id", "long")));

        TableChangeComputer computer = new TableChangeComputer();
        List<ResourceChange> changes = computer.computeChanges(List.of(before), List.of(after));

        Assertions.assertEquals(1, changes.size());
        Assertions.assertEquals(Operation.UPDATE, changes.get(0).getSpec().getOp());
    }

    /**
     * When the user explicitly adds a new property, it should be detected as UPDATE.
     */
    @Test
    void shouldDetectNewPropertyAsUpdate() {
        V1IcebergTable before = table("ns.my_table", null,
            Map.of("write.format.default", "parquet"),
            List.of(col("id", "long")));

        V1IcebergTable after = table("ns.my_table", null,
            Map.of("write.format.default", "parquet", "write.parquet.compression-codec", "zstd"),
            List.of(col("id", "long")));

        TableChangeComputer computer = new TableChangeComputer();
        List<ResourceChange> changes = computer.computeChanges(List.of(before), List.of(after));

        Assertions.assertEquals(1, changes.size());
        Assertions.assertEquals(Operation.UPDATE, changes.get(0).getSpec().getOp());
    }

    /**
     * When the catalog returns an auto-generated partition field name but the user spec
     * omits it, the comparison should produce NONE.
     */
    @Test
    void shouldNotDetectSpuriousUpdateWhenPartitionFieldNameIsAutoGenerated() {
        // Catalog returns partition with auto-generated name
        V1IcebergTable before = table("ns.t", null, Map.of(), List.of(col("id", "long")),
            List.of(partition("event_time", "day", "event_time_day")),
            null);

        // User spec omits partition name
        V1IcebergTable after = table("ns.t", null, Map.of(), List.of(col("id", "long")),
            List.of(partition("event_time", "day", null)),
            null);

        TableChangeComputer computer = new TableChangeComputer();
        List<ResourceChange> changes = computer.computeChanges(List.of(before), List.of(after));

        Assertions.assertEquals(1, changes.size());
        Assertions.assertEquals(Operation.NONE, changes.get(0).getSpec().getOp());
    }

    /**
     * When the catalog returns nullOrder as "nulls last" but the user spec uses "last",
     * the comparison should produce NONE.
     */
    @Test
    void shouldNotDetectSpuriousUpdateWhenSortOrderNullOrderDiffersOnlyInPrefix() {
        // Catalog returns long-form "nulls last"
        V1IcebergTable before = table("ns.t", null, Map.of(), List.of(col("id", "long")),
            null,
            List.of(sort("event_time", "asc", "nulls last")));

        // User spec uses short-form "last"
        V1IcebergTable after = table("ns.t", null, Map.of(), List.of(col("id", "long")),
            null,
            List.of(sort("event_time", "asc", "last")));

        TableChangeComputer computer = new TableChangeComputer();
        List<ResourceChange> changes = computer.computeChanges(List.of(before), List.of(after));

        Assertions.assertEquals(1, changes.size());
        Assertions.assertEquals(Operation.NONE, changes.get(0).getSpec().getOp());
    }

    /**
     * A real sort order change (e.g. asc → desc) should still be detected.
     */
    @Test
    void shouldDetectRealSortOrderChange() {
        V1IcebergTable before = table("ns.t", null, Map.of(), List.of(col("id", "long")),
            null,
            List.of(sort("event_time", "asc", "last")));

        V1IcebergTable after = table("ns.t", null, Map.of(), List.of(col("id", "long")),
            null,
            List.of(sort("event_time", "desc", "first")));

        TableChangeComputer computer = new TableChangeComputer();
        List<ResourceChange> changes = computer.computeChanges(List.of(before), List.of(after));

        Assertions.assertEquals(1, changes.size());
        Assertions.assertEquals(Operation.UPDATE, changes.get(0).getSpec().getOp());
    }
}
