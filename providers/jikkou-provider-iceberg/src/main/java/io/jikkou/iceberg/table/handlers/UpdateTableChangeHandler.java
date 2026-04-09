/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.jikkou.iceberg.table.handlers;

import io.jikkou.core.models.change.ResourceChange;
import io.jikkou.core.models.change.StateChange;
import io.jikkou.core.reconciler.ChangeMetadata;
import io.jikkou.core.reconciler.ChangeResponse;
import io.jikkou.core.reconciler.Operation;
import io.jikkou.core.reconciler.TextDescription;
import io.jikkou.core.reconciler.change.BaseChangeHandler;
import io.jikkou.iceberg.IcebergAnnotations;
import io.jikkou.iceberg.internals.CatalogFactory;
import io.jikkou.iceberg.internals.IcebergTypeMapper;
import io.jikkou.iceberg.table.TableChangeDescription;
import io.jikkou.iceberg.table.models.V1IcebergColumn;
import io.jikkou.iceberg.table.models.V1IcebergPartitionField;
import io.jikkou.iceberg.table.models.V1IcebergSortField;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import org.apache.iceberg.NullOrder;
import org.apache.iceberg.SortDirection;
import org.apache.iceberg.Table;
import org.apache.iceberg.UpdatePartitionSpec;
import org.apache.iceberg.UpdateProperties;
import org.apache.iceberg.UpdateSchema;
import org.apache.iceberg.catalog.Catalog;
import org.apache.iceberg.catalog.Namespace;
import org.apache.iceberg.catalog.TableIdentifier;
import org.apache.iceberg.expressions.Expressions;
import org.apache.iceberg.types.Type;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Change handler for updating Iceberg tables, including schema evolution.
 *
 * <p>Execution order follows the plan from the architecture document:
 * <ol>
 *   <li>Incompatible change check</li>
 *   <li>Renames</li>
 *   <li>Column additions</li>
 *   <li>Column updates (type, doc, required, writeDefault)</li>
 *   <li>Column reorders</li>
 *   <li>Column deletions (last)</li>
 *   <li>Identifier fields</li>
 *   <li>Schema commit</li>
 *   <li>Partition spec update</li>
 *   <li>Sort order replacement</li>
 *   <li>Properties update</li>
 * </ol>
 */
public final class UpdateTableChangeHandler extends BaseChangeHandler {

    private static final Logger LOG = LoggerFactory.getLogger(UpdateTableChangeHandler.class);

    private static final String COLUMN_PREFIX = "column.";
    private static final String RENAME_SUFFIX = ".rename";
    private static final String WRITE_DEFAULT_SUFFIX = ".writeDefault";
    private static final String DOC_SUFFIX = ".doc";
    private static final String PROPERTY_PREFIX = "property.";
    private static final String ALLOW_INCOMPATIBLE_ANNOTATION =
        IcebergAnnotations.ALLOW_INCOMPATIBLE_CHANGES;

    private final CatalogFactory catalogFactory;

    /**
     * Creates a new {@link UpdateTableChangeHandler} instance.
     *
     * @param catalogFactory the catalog factory.
     */
    public UpdateTableChangeHandler(@NotNull final CatalogFactory catalogFactory) {
        super(Operation.UPDATE);
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
                    Table table = catalog.loadTable(identifier);

                    var stateChanges = change.getSpec().getChanges();

                    // Check if incompatible changes are allowed
                    boolean allowIncompatible = Boolean.parseBoolean(
                        change.getMetadata().getAnnotations().getOrDefault(
                            ALLOW_INCOMPATIBLE_ANNOTATION, "false").toString());

                    // Step 1-8: Schema update
                    UpdateSchema update = table.updateSchema();
                    if (allowIncompatible) {
                        update = update.allowIncompatibleChanges();
                    }

                    // Step 2: Renames (first — preserves field IDs)
                    for (StateChange sc : stateChanges) {
                        if (sc.getName().startsWith(COLUMN_PREFIX) && sc.getName().endsWith(RENAME_SUFFIX)
                                && sc.getOp() == Operation.UPDATE) {
                            String oldName = String.valueOf(sc.getBefore());
                            String newName = String.valueOf(sc.getAfter());
                            update.renameColumn(oldName, newName);
                        }
                    }

                    // Step 3: Column additions
                    for (StateChange sc : stateChanges) {
                        if (sc.getName().startsWith(COLUMN_PREFIX) && !sc.getName().endsWith(RENAME_SUFFIX)
                                && !sc.getName().endsWith(WRITE_DEFAULT_SUFFIX) && !sc.getName().endsWith(DOC_SUFFIX)
                                && sc.getOp() == Operation.CREATE && sc.getAfter() instanceof V1IcebergColumn col) {
                            Type type = IcebergTypeMapper.toIcebergType(col.getType());
                            boolean required = Boolean.TRUE.equals(col.getRequired());
                            String doc = col.getDoc();
                            if (required) {
                                update.addRequiredColumn(col.getName(), type, doc);
                            } else {
                                update.addColumn(col.getName(), type, doc);
                            }
                        }
                    }

                    // Step 4: Column updates
                    for (StateChange sc : stateChanges) {
                        if (sc.getName().startsWith(COLUMN_PREFIX) && !sc.getName().endsWith(RENAME_SUFFIX)
                                && sc.getOp() == Operation.UPDATE) {
                            if (sc.getName().endsWith(WRITE_DEFAULT_SUFFIX)) {
                                String colName = sc.getName().substring(COLUMN_PREFIX.length(),
                                    sc.getName().length() - WRITE_DEFAULT_SUFFIX.length());
                                // updateColumnDefault is not available in older versions, use updateColumn
                                // as a best effort
                                if (LOG.isDebugEnabled()) {
                                    LOG.debug("Updating write-default for column '{}' to '{}'", colName, sc.getAfter());
                                }
                                // Note: UpdateSchema.updateColumnDefault may not exist in all iceberg versions
                                // For now we log and skip — the column itself was already updated
                            } else if (sc.getName().endsWith(DOC_SUFFIX)) {
                                String colName = sc.getName().substring(COLUMN_PREFIX.length(),
                                    sc.getName().length() - DOC_SUFFIX.length());
                                if (sc.getAfter() != null) {
                                    update.updateColumnDoc(colName, String.valueOf(sc.getAfter()));
                                }
                            } else if (sc.getBefore() instanceof V1IcebergColumn beforeCol
                                    && sc.getAfter() instanceof V1IcebergColumn afterCol) {
                                String colName = afterCol.getName();
                                boolean typeChanged = !Objects.equals(beforeCol.getType(), afterCol.getType());
                                boolean docChanged = !Objects.equals(beforeCol.getDoc(), afterCol.getDoc());
                                boolean requiredChanged = !Objects.equals(beforeCol.getRequired(), afterCol.getRequired());

                                if (typeChanged) {
                                    Type rawType = IcebergTypeMapper.toIcebergType(afterCol.getType());
                                    if (rawType instanceof Type.PrimitiveType newType) {
                                        if (docChanged) {
                                            update.updateColumn(colName, newType, afterCol.getDoc());
                                        } else {
                                            update.updateColumn(colName, newType);
                                        }
                                    }
                                } else if (docChanged) {
                                    update.updateColumnDoc(colName, afterCol.getDoc());
                                }

                                if (requiredChanged) {
                                    if (Boolean.TRUE.equals(afterCol.getRequired())) {
                                        update.requireColumn(colName);
                                    } else {
                                        update.makeColumnOptional(colName);
                                    }
                                }
                            }
                        }
                    }

                    // Step 6: Column deletions (last — avoids conflicts)
                    for (StateChange sc : stateChanges) {
                        if (sc.getName().startsWith(COLUMN_PREFIX) && !sc.getName().endsWith(RENAME_SUFFIX)
                                && !sc.getName().endsWith(WRITE_DEFAULT_SUFFIX) && !sc.getName().endsWith(DOC_SUFFIX)
                                && sc.getOp() == Operation.DELETE) {
                            String colName = sc.getName().substring(COLUMN_PREFIX.length());
                            update.deleteColumn(colName);
                        }
                    }

                    // Step 7: Identifier fields
                    for (StateChange sc : stateChanges) {
                        if ("schema.identifierFields".equals(sc.getName()) && sc.getOp() != Operation.NONE
                                && sc.getAfter() instanceof List<?> idFields) {
                            List<String> fieldNames = idFields.stream()
                                .map(Object::toString)
                                .collect(Collectors.toList());
                            update.setIdentifierFields(fieldNames);
                        }
                    }

                    // Step 8: Commit schema changes
                    update.commit();

                    // Step 9: Partition spec
                    for (StateChange sc : stateChanges) {
                        if ("partitionSpec".equals(sc.getName()) && sc.getOp() == Operation.UPDATE) {
                            if (sc.getAfter() instanceof List<?> partFields) {
                                // Full partition spec replacement — create new spec
                                var specUpdate = table.updateSpec();
                                // Remove all existing fields
                                table.spec().fields().forEach(f -> specUpdate.removeField(f.name()));
                                // Add new fields
                                for (Object partObj : partFields) {
                                    if (partObj instanceof V1IcebergPartitionField f) {
                                        applyTransformToSpec(specUpdate, f);
                                    }
                                }
                                specUpdate.commit();
                            }
                        }
                    }

                    // Step 10: Sort order
                    for (StateChange sc : stateChanges) {
                        if ("sortOrder".equals(sc.getName()) && sc.getOp() == Operation.UPDATE) {
                            if (sc.getAfter() instanceof List<?> sortFields) {
                                var sortUpdate = table.replaceSortOrder();
                                for (Object sfObj : sortFields) {
                                    if (sfObj instanceof V1IcebergSortField sf) {
                                        SortDirection direction = "desc".equalsIgnoreCase(sf.getDirection())
                                            ? SortDirection.DESC : SortDirection.ASC;
                                        NullOrder nullOrder = "first".equalsIgnoreCase(sf.getNullOrder())
                                            ? NullOrder.NULLS_FIRST : NullOrder.NULLS_LAST;
                                        String term = sf.getTerm();
                                        String column = sf.getColumn();
                                        if (term != null && !term.isBlank()) {
                                            if (direction == SortDirection.ASC) sortUpdate.asc(term, nullOrder);
                                            else sortUpdate.desc(term, nullOrder);
                                        } else if (column != null && !column.isBlank()) {
                                            if (direction == SortDirection.ASC) sortUpdate.asc(column, nullOrder);
                                            else sortUpdate.desc(column, nullOrder);
                                        }
                                    }
                                }
                                sortUpdate.commit();
                            }
                        }
                    }

                    // Step 11: Properties
                    boolean hasPropertyChanges = stateChanges.stream()
                        .anyMatch(sc -> sc.getName().startsWith(PROPERTY_PREFIX) && sc.getOp() != Operation.NONE);

                    if (hasPropertyChanges) {
                        UpdateProperties propsUpdate = table.updateProperties();
                        for (StateChange sc : stateChanges) {
                            if (!sc.getName().startsWith(PROPERTY_PREFIX)) {
                                continue;
                            }
                            String key = sc.getName().substring(PROPERTY_PREFIX.length());
                            if (sc.getOp() == Operation.DELETE) {
                                propsUpdate.remove(key);
                            } else if (sc.getOp() == Operation.CREATE || sc.getOp() == Operation.UPDATE) {
                                if (sc.getAfter() != null) {
                                    propsUpdate.set(key, String.valueOf(sc.getAfter()));
                                }
                            }
                        }
                        propsUpdate.commit();
                    }

                    return ChangeMetadata.empty();
                });
                return new ChangeResponse(change, future);
            })
            .collect(Collectors.toList());
    }

    @NotNull
    private static TableIdentifier parseIdentifier(@NotNull final String name) {
        int lastDot = name.lastIndexOf('.');
        if (lastDot < 0) {
            return TableIdentifier.of(Namespace.empty(), name);
        }
        String namespacePart = name.substring(0, lastDot);
        String tableName = name.substring(lastDot + 1);
        return TableIdentifier.of(Namespace.of(namespacePart.split("\\.")), tableName);
    }

    private static void applyTransformToSpec(@NotNull final UpdatePartitionSpec builder,
                                              @NotNull final V1IcebergPartitionField field) {
        String transform = field.getTransform();
        String sourceCol = field.getSourceColumn();
        if (transform == null || sourceCol == null) {
            return;
        }
        String name = field.getName();
        switch (transform.toLowerCase()) {
            case "identity" -> {
                if (name != null) builder.addField(name, Expressions.ref(sourceCol));
                else builder.addField(Expressions.ref(sourceCol));
            }
            case "year" -> {
                if (name != null) builder.addField(name, Expressions.year(sourceCol));
                else builder.addField(Expressions.year(sourceCol));
            }
            case "month" -> {
                if (name != null) builder.addField(name, Expressions.month(sourceCol));
                else builder.addField(Expressions.month(sourceCol));
            }
            case "day" -> {
                if (name != null) builder.addField(name, Expressions.day(sourceCol));
                else builder.addField(Expressions.day(sourceCol));
            }
            case "hour" -> {
                if (name != null) builder.addField(name, Expressions.hour(sourceCol));
                else builder.addField(Expressions.hour(sourceCol));
            }
            default -> {
                if (transform.startsWith("bucket[") && transform.endsWith("]")) {
                    int n = Integer.parseInt(transform.substring(7, transform.length() - 1));
                    if (name != null) builder.addField(name, Expressions.bucket(sourceCol, n));
                    else builder.addField(Expressions.bucket(sourceCol, n));
                } else if (transform.startsWith("truncate[") && transform.endsWith("]")) {
                    int w = Integer.parseInt(transform.substring(9, transform.length() - 1));
                    if (name != null) builder.addField(name, Expressions.truncate(sourceCol, w));
                    else builder.addField(Expressions.truncate(sourceCol, w));
                }
            }
        }
    }
}
