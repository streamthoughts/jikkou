/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.jikkou.iceberg.internals;

import io.jikkou.iceberg.table.models.V1IcebergColumn;
import io.jikkou.iceberg.table.models.V1IcebergPartitionField;
import io.jikkou.iceberg.table.models.V1IcebergSortField;
import io.jikkou.iceberg.table.models.V1IcebergTable;
import java.util.Map;
import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.Schema;
import org.apache.iceberg.SortOrder;
import org.apache.iceberg.Table;
import org.apache.iceberg.catalog.TableIdentifier;
import org.apache.iceberg.types.Types;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class IcebergTableConverterTest {

    @Test
    void shouldConvertSimpleTable() {
        Schema schema = new Schema(
            Types.NestedField.required(1, "id", Types.LongType.get()),
            Types.NestedField.optional(2, "name", Types.StringType.get(), "User name")
        );

        Table table = Mockito.mock(Table.class);
        Mockito.when(table.schema()).thenReturn(schema);
        Mockito.when(table.spec()).thenReturn(PartitionSpec.unpartitioned());
        Mockito.when(table.sortOrder()).thenReturn(SortOrder.unsorted());
        Mockito.when(table.properties()).thenReturn(Map.of("write.format.default", "parquet"));
        Mockito.when(table.location()).thenReturn("s3://bucket/ns/my_table");

        TableIdentifier identifier = TableIdentifier.of("ns", "my_table");

        V1IcebergTable result = IcebergTableConverter.toV1IcebergTable(table, identifier);

        Assertions.assertEquals("ns.my_table", result.getMetadata().getName());
        Assertions.assertEquals("s3://bucket/ns/my_table", result.getSpec().getLocation());
        Assertions.assertNotNull(result.getSpec().getSchema());
        Assertions.assertEquals(2, result.getSpec().getSchema().getColumns().size());

        V1IcebergColumn idCol = result.getSpec().getSchema().getColumns().get(0);
        Assertions.assertEquals("id", idCol.getName());
        Assertions.assertEquals("long", idCol.getType());
        Assertions.assertTrue(idCol.getRequired());

        V1IcebergColumn nameCol = result.getSpec().getSchema().getColumns().get(1);
        Assertions.assertEquals("name", nameCol.getName());
        Assertions.assertEquals("string", nameCol.getType());
        Assertions.assertFalse(nameCol.getRequired());
        Assertions.assertEquals("User name", nameCol.getDoc());

        Assertions.assertEquals("parquet", result.getSpec().getProperties().get("write.format.default"));
    }

    @Test
    void shouldReturnNullPartitionFieldsForUnpartitionedTable() {
        Schema schema = new Schema(
            Types.NestedField.required(1, "id", Types.LongType.get())
        );

        Table table = Mockito.mock(Table.class);
        Mockito.when(table.schema()).thenReturn(schema);
        Mockito.when(table.spec()).thenReturn(PartitionSpec.unpartitioned());
        Mockito.when(table.sortOrder()).thenReturn(SortOrder.unsorted());
        Mockito.when(table.properties()).thenReturn(Map.of());
        Mockito.when(table.location()).thenReturn("s3://bucket/t");

        V1IcebergTable result = IcebergTableConverter.toV1IcebergTable(
            table, TableIdentifier.of("ns", "t"));

        Assertions.assertNull(result.getSpec().getPartitionFields());
        Assertions.assertNull(result.getSpec().getSortFields());
        Assertions.assertNull(result.getSpec().getProperties());
    }

    @Test
    void shouldConvertPartitionedTable() {
        Schema schema = new Schema(
            Types.NestedField.required(1, "id", Types.LongType.get()),
            Types.NestedField.optional(2, "event_time", Types.TimestampType.withoutZone())
        );

        PartitionSpec partitionSpec = PartitionSpec.builderFor(schema)
            .day("event_time")
            .build();

        Table table = Mockito.mock(Table.class);
        Mockito.when(table.schema()).thenReturn(schema);
        Mockito.when(table.spec()).thenReturn(partitionSpec);
        Mockito.when(table.sortOrder()).thenReturn(SortOrder.unsorted());
        Mockito.when(table.properties()).thenReturn(Map.of());
        Mockito.when(table.location()).thenReturn("s3://bucket/t");

        V1IcebergTable result = IcebergTableConverter.toV1IcebergTable(
            table, TableIdentifier.of("ns", "t"));

        Assertions.assertNotNull(result.getSpec().getPartitionFields());
        Assertions.assertEquals(1, result.getSpec().getPartitionFields().size());

        V1IcebergPartitionField pf = result.getSpec().getPartitionFields().get(0);
        Assertions.assertEquals("event_time", pf.getSourceColumn());
        Assertions.assertEquals("day", pf.getTransform());
    }

    @Test
    void shouldConvertSortedTable() {
        Schema schema = new Schema(
            Types.NestedField.required(1, "id", Types.LongType.get()),
            Types.NestedField.optional(2, "created_at", Types.TimestampType.withoutZone())
        );

        SortOrder sortOrder = SortOrder.builderFor(schema)
            .asc("created_at")
            .build();

        Table table = Mockito.mock(Table.class);
        Mockito.when(table.schema()).thenReturn(schema);
        Mockito.when(table.spec()).thenReturn(PartitionSpec.unpartitioned());
        Mockito.when(table.sortOrder()).thenReturn(sortOrder);
        Mockito.when(table.properties()).thenReturn(Map.of());
        Mockito.when(table.location()).thenReturn("s3://bucket/t");

        V1IcebergTable result = IcebergTableConverter.toV1IcebergTable(
            table, TableIdentifier.of("ns", "t"));

        Assertions.assertNotNull(result.getSpec().getSortFields());
        Assertions.assertEquals(1, result.getSpec().getSortFields().size());

        V1IcebergSortField sf = result.getSpec().getSortFields().get(0);
        Assertions.assertEquals("created_at", sf.getColumn());
        Assertions.assertEquals("asc", sf.getDirection());
        Assertions.assertEquals("first", sf.getNullOrder());
    }

    @Test
    void shouldConvertIdentifierFields() {
        Schema schema = new Schema(
            Types.NestedField.required(1, "id", Types.LongType.get()),
            Types.NestedField.optional(2, "name", Types.StringType.get())
        );
        schema = new Schema(schema.columns(), schema.identifierFieldIds());

        // Create a schema with identifier fields
        Schema schemaWithIds = new Schema(
            schema.columns(),
            java.util.Set.of(1)
        );

        Table table = Mockito.mock(Table.class);
        Mockito.when(table.schema()).thenReturn(schemaWithIds);
        Mockito.when(table.spec()).thenReturn(PartitionSpec.unpartitioned());
        Mockito.when(table.sortOrder()).thenReturn(SortOrder.unsorted());
        Mockito.when(table.properties()).thenReturn(Map.of());
        Mockito.when(table.location()).thenReturn("s3://bucket/t");

        V1IcebergTable result = IcebergTableConverter.toV1IcebergTable(
            table, TableIdentifier.of("ns", "t"));

        Assertions.assertNotNull(result.getSpec().getSchema().getIdentifierFields());
        Assertions.assertEquals(1, result.getSpec().getSchema().getIdentifierFields().size());
        Assertions.assertEquals("id", result.getSpec().getSchema().getIdentifierFields().get(0));
    }
}
