/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.jikkou.iceberg.internals;

import io.jikkou.core.models.ObjectMeta;
import io.jikkou.iceberg.table.models.V1IcebergColumn;
import io.jikkou.iceberg.table.models.V1IcebergPartitionField;
import io.jikkou.iceberg.table.models.V1IcebergSchema;
import io.jikkou.iceberg.table.models.V1IcebergSortField;
import io.jikkou.iceberg.table.models.V1IcebergTable;
import io.jikkou.iceberg.table.models.V1IcebergTableSpec;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.iceberg.PartitionField;
import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.Schema;
import org.apache.iceberg.SortField;
import org.apache.iceberg.SortOrder;
import org.apache.iceberg.Table;
import org.apache.iceberg.catalog.TableIdentifier;
import org.apache.iceberg.types.Types;
import org.jetbrains.annotations.NotNull;

/**
 * Converts Apache Iceberg {@link Table} objects to {@link V1IcebergTable} resources.
 */
public final class IcebergTableConverter {

    private IcebergTableConverter() {
        // utility class
    }

    /**
     * Converts an Iceberg {@link Table} to a {@link V1IcebergTable} resource.
     *
     * @param table      the Iceberg table.
     * @param identifier the table identifier (namespace + name).
     * @return the corresponding resource.
     */
    @NotNull
    public static V1IcebergTable toV1IcebergTable(@NotNull final Table table,
                                                  @NotNull final TableIdentifier identifier) {
        String resourceName = identifier.toString();

        V1IcebergSchema schema = convertSchema(table.schema());
        List<V1IcebergPartitionField> partitionFields = convertPartitionSpec(table.spec());
        List<V1IcebergSortField> sortFields = convertSortOrder(table.sortOrder());
        Map<String, String> properties = table.properties();
        String location = table.location();

        V1IcebergTableSpec spec = new V1IcebergTableSpec();
        spec.setLocation(location);
        spec.setSchema(schema);
        spec.setPartitionFields(partitionFields.isEmpty() ? null : partitionFields);
        spec.setSortFields(sortFields.isEmpty() ? null : sortFields);
        spec.setProperties(properties.isEmpty() ? null : properties);

        return V1IcebergTable.builder()
            .withMetadata(ObjectMeta.builder()
                .withName(resourceName)
                .build())
            .withSpec(spec)
            .build();
    }

    @NotNull
    private static V1IcebergSchema convertSchema(@NotNull final Schema schema) {
        List<V1IcebergColumn> columns = schema.columns().stream()
            .map(IcebergTableConverter::convertColumn)
            .collect(Collectors.toList());

        List<String> identifierFields = schema.identifierFieldIds().stream()
            .map(id -> schema.findField(id).name())
            .collect(Collectors.toList());

        V1IcebergSchema result = new V1IcebergSchema();
        result.setColumns(columns);
        if (!identifierFields.isEmpty()) {
            result.setIdentifierFields(identifierFields);
        }
        return result;
    }

    @NotNull
    private static V1IcebergColumn convertColumn(@NotNull final Types.NestedField field) {
        // Note: "default" is a Java reserved word; the generated class field is "_default".
        // Use the all-args constructor to avoid Lombok setter issues with reserved-word field names.
        return new V1IcebergColumn(
            field.name(),
            IcebergTypeMapper.fromIcebergType(field.type()),
            field.isRequired(),
            field.doc(),
            field.initialDefault(),
            field.writeDefault(),
            null  // previousName is only declared in specs, not read from a live table
        );
    }

    @NotNull
    private static List<V1IcebergPartitionField> convertPartitionSpec(@NotNull final PartitionSpec spec) {
        if (spec.isUnpartitioned()) {
            return List.of();
        }
        List<V1IcebergPartitionField> fields = new ArrayList<>();
        for (PartitionField partitionField : spec.fields()) {
            V1IcebergPartitionField field = new V1IcebergPartitionField();
            // Get the source column name from the schema
            Types.NestedField sourceField = spec.schema().findField(partitionField.sourceId());
            if (sourceField != null) {
                field.setSourceColumn(sourceField.name());
            }
            field.setTransform(partitionField.transform().toString());
            fields.add(field);
        }
        return fields;
    }

    @NotNull
    private static List<V1IcebergSortField> convertSortOrder(@NotNull final SortOrder sortOrder) {
        if (sortOrder.isUnsorted()) {
            return List.of();
        }
        Schema schema = sortOrder.schema();
        List<V1IcebergSortField> fields = new ArrayList<>();
        for (SortField sortField : sortOrder.fields()) {
            V1IcebergSortField field = new V1IcebergSortField();
            // SortField exposes fieldId(); look up the column name in the sort order's schema.
            Types.NestedField schemaField = schema != null ? schema.findField(sortField.sourceId()) : null;
            if (schemaField != null) {
                field.setColumn(schemaField.name());
            } else {
                // Fallback: use the transform string representation as the term
                field.setTerm(sortField.transform().toString() + "(" + sortField.sourceId() + ")");
            }
            field.setDirection(sortField.direction().toString().toLowerCase());
            // Normalize to short form ("first"/"last") matching user-facing spec format
            String nullOrderStr = sortField.nullOrder().toString().toLowerCase();
            if (nullOrderStr.startsWith("nulls ")) {
                nullOrderStr = nullOrderStr.substring("nulls ".length());
            }
            field.setNullOrder(nullOrderStr);
            fields.add(field);
        }
        return fields;
    }
}
