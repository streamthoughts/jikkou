/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.jikkou.iceberg.table.handlers;

import io.jikkou.core.models.change.ResourceChange;
import io.jikkou.core.models.change.StateChange;
import io.jikkou.core.models.change.StateChangeList;
import io.jikkou.core.reconciler.ChangeMetadata;
import io.jikkou.core.reconciler.ChangeResponse;
import io.jikkou.core.reconciler.Operation;
import io.jikkou.core.reconciler.TextDescription;
import io.jikkou.core.reconciler.change.BaseChangeHandler;
import io.jikkou.iceberg.internals.CatalogFactory;
import io.jikkou.iceberg.internals.IcebergTypeMapper;
import io.jikkou.iceberg.table.TableChangeDescription;
import io.jikkou.iceberg.table.models.V1IcebergColumn;
import io.jikkou.iceberg.table.models.V1IcebergPartitionField;
import io.jikkou.iceberg.table.models.V1IcebergSortField;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import org.apache.iceberg.NullOrder;
import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.Schema;
import org.apache.iceberg.SortDirection;
import org.apache.iceberg.SortOrder;
import org.apache.iceberg.catalog.Catalog;
import org.apache.iceberg.catalog.Namespace;
import org.apache.iceberg.catalog.TableIdentifier;
import org.apache.iceberg.types.Type;
import org.apache.iceberg.types.Types;
import org.jetbrains.annotations.NotNull;

/**
 * Change handler for creating Iceberg tables.
 */
public final class CreateTableChangeHandler extends BaseChangeHandler {

    private final CatalogFactory catalogFactory;

    /**
     * Creates a new {@link CreateTableChangeHandler} instance.
     *
     * @param catalogFactory the catalog factory.
     */
    public CreateTableChangeHandler(@NotNull final CatalogFactory catalogFactory) {
        super(Operation.CREATE);
        this.catalogFactory = catalogFactory;
    }

    /** {@inheritDoc} */
    @Override
    public TextDescription describe(@NotNull final ResourceChange change) {
        return new TableChangeDescription(change);
    }

    /** {@inheritDoc} */
    @Override
    @NotNull
    public List<ChangeResponse> handleChanges(@NotNull final List<ResourceChange> changes) {
        return changes.stream()
            .map(change -> {
                CompletableFuture<ChangeMetadata> future = CompletableFuture.supplyAsync(() -> {
                    Catalog catalog = catalogFactory.createCatalog();
                    String resourceName = change.getMetadata().getName();
                    TableIdentifier identifier = parseIdentifier(resourceName);

                    StateChangeList<? extends StateChange> data = change.getSpec().getChanges();

                    // Build schema
                    Schema schema = buildSchema(data);

                    // Build partition spec
                    PartitionSpec partitionSpec = buildPartitionSpec(schema, data);

                    // Build sort order
                    SortOrder sortOrder = buildSortOrder(schema, data);

                    // Extract location
                    String location = extractStringValue(data, "location");

                    // Extract properties
                    Map<String, String> properties = extractProperties(data);

                    // Create the table
                    Catalog.TableBuilder builder = catalog.buildTable(identifier, schema)
                        .withPartitionSpec(partitionSpec)
                        .withSortOrder(sortOrder)
                        .withProperties(properties);

                    if (location != null && !location.isEmpty()) {
                        builder = builder.withLocation(location);
                    }

                    builder.create();
                    return ChangeMetadata.empty();
                });
                return new ChangeResponse(change, future);
            })
            .collect(Collectors.toList());
    }

    @NotNull
    static TableIdentifier parseIdentifier(@NotNull final String name) {
        int lastDot = name.lastIndexOf('.');
        if (lastDot < 0) {
            return TableIdentifier.of(Namespace.empty(), name);
        }
        String namespacePart = name.substring(0, lastDot);
        String tableName = name.substring(lastDot + 1);
        return TableIdentifier.of(Namespace.of(namespacePart.split("\\.")), tableName);
    }

    @NotNull
    static Schema buildSchema(@NotNull final StateChangeList<? extends StateChange> data) {
        Object columnsObj = null;
        for (StateChange sc : data) {
            if ("schema.columns".equals(sc.getName())) {
                columnsObj = sc.getAfter();
                break;
            }
        }

        List<Types.NestedField> fields = new ArrayList<>();
        int fieldId = 1;
        if (columnsObj instanceof List<?> cols) {
            for (Object colObj : cols) {
                if (colObj instanceof V1IcebergColumn col) {
                    Type type = IcebergTypeMapper.toIcebergType(col.getType());
                    boolean required = Boolean.TRUE.equals(col.getRequired());
                    String doc = col.getDoc();
                    Types.NestedField field = required
                        ? Types.NestedField.required(fieldId++, col.getName(), type, doc)
                        : Types.NestedField.optional(fieldId++, col.getName(), type, doc);
                    fields.add(field);
                }
            }
        }
        return new Schema(fields);
    }

    @NotNull
    static PartitionSpec buildPartitionSpec(@NotNull final Schema schema,
                                             @NotNull final StateChangeList<? extends StateChange> data) {
        Object partObj = null;
        for (StateChange sc : data) {
            if ("partitionSpec".equals(sc.getName())) {
                partObj = sc.getAfter();
                break;
            }
        }

        if (!(partObj instanceof List<?> parts) || parts.isEmpty()) {
            return PartitionSpec.unpartitioned();
        }

        PartitionSpec.Builder builder = PartitionSpec.builderFor(schema);
        for (Object partObjItem : parts) {
            if (partObjItem instanceof V1IcebergPartitionField field) {
                applyTransform(builder, field);
            }
        }
        return builder.build();
    }

    static void applyTransform(@NotNull final PartitionSpec.Builder builder,
                                @NotNull final V1IcebergPartitionField field) {
        String transform = field.getTransform();
        String sourceCol = field.getSourceColumn();
        if (transform == null || sourceCol == null) {
            return;
        }
        // Note: custom partition field names (field.getName()) are not exposed in the public PartitionSpec.Builder API.
        switch (transform.toLowerCase()) {
            case "identity" -> builder.identity(sourceCol);
            case "year" -> builder.year(sourceCol);
            case "month" -> builder.month(sourceCol);
            case "day" -> builder.day(sourceCol);
            case "hour" -> builder.hour(sourceCol);
            case "void" -> builder.alwaysNull(sourceCol);
            default -> {
                if (transform.startsWith("bucket[") && transform.endsWith("]")) {
                    int n = Integer.parseInt(transform.substring(7, transform.length() - 1));
                    builder.bucket(sourceCol, n);
                } else if (transform.startsWith("truncate[") && transform.endsWith("]")) {
                    int w = Integer.parseInt(transform.substring(9, transform.length() - 1));
                    builder.truncate(sourceCol, w);
                }
            }
        }
    }

    @NotNull
    static SortOrder buildSortOrder(@NotNull final Schema schema,
                                     @NotNull final StateChangeList<? extends StateChange> data) {
        Object sortObj = null;
        for (StateChange sc : data) {
            if ("sortOrder".equals(sc.getName())) {
                sortObj = sc.getAfter();
                break;
            }
        }

        if (!(sortObj instanceof List<?> sortFields) || sortFields.isEmpty()) {
            return SortOrder.unsorted();
        }

        SortOrder.Builder builder = SortOrder.builderFor(schema);
        for (Object sfObj : sortFields) {
            if (sfObj instanceof V1IcebergSortField sf) {
                SortDirection direction = "desc".equalsIgnoreCase(sf.getDirection())
                    ? SortDirection.DESC : SortDirection.ASC;
                NullOrder nullOrder = "first".equalsIgnoreCase(sf.getNullOrder())
                    ? NullOrder.NULLS_FIRST : NullOrder.NULLS_LAST;

                String term = sf.getTerm();
                String column = sf.getColumn();

                if (term != null && !term.isBlank()) {
                    if (direction == SortDirection.ASC) builder.asc(term, nullOrder);
                    else builder.desc(term, nullOrder);
                } else if (column != null && !column.isBlank()) {
                    if (direction == SortDirection.ASC) builder.asc(column, nullOrder);
                    else builder.desc(column, nullOrder);
                }
            }
        }
        return builder.build();
    }

    @NotNull
    static Map<String, String> extractProperties(@NotNull final StateChangeList<? extends StateChange> data) {
        Map<String, String> properties = new HashMap<>();
        for (StateChange sc : data) {
            if (sc.getName().startsWith("property.") && sc.getAfter() != null) {
                String key = sc.getName().substring("property.".length());
                properties.put(key, String.valueOf(sc.getAfter()));
            }
        }
        return properties;
    }

    static String extractStringValue(@NotNull final StateChangeList<? extends StateChange> data,
                                      @NotNull final String name) {
        for (StateChange sc : data) {
            if (name.equals(sc.getName()) && sc.getAfter() != null) {
                return String.valueOf(sc.getAfter());
            }
        }
        return null;
    }
}
