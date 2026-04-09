/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.jikkou.iceberg.view.handlers;

import io.jikkou.core.exceptions.JikkouRuntimeException;
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
import io.jikkou.iceberg.table.models.V1IcebergColumn;
import io.jikkou.iceberg.view.ViewChangeDescription;
import io.jikkou.iceberg.view.models.V1IcebergViewQuery;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import org.apache.iceberg.Schema;
import org.apache.iceberg.catalog.Catalog;
import org.apache.iceberg.catalog.Namespace;
import org.apache.iceberg.catalog.TableIdentifier;
import org.apache.iceberg.catalog.ViewCatalog;
import org.apache.iceberg.types.Type;
import org.apache.iceberg.types.Types;
import org.apache.iceberg.view.ViewBuilder;
import org.jetbrains.annotations.NotNull;

/**
 * Change handler for creating Iceberg views.
 */
public final class CreateViewChangeHandler extends BaseChangeHandler {

    private final CatalogFactory catalogFactory;

    /**
     * Creates a new {@link CreateViewChangeHandler} instance.
     *
     * @param catalogFactory the catalog factory.
     */
    public CreateViewChangeHandler(@NotNull final CatalogFactory catalogFactory) {
        super(Operation.CREATE);
        this.catalogFactory = catalogFactory;
    }

    /** {@inheritDoc} */
    @Override
    public TextDescription describe(@NotNull final ResourceChange change) {
        return new ViewChangeDescription(change);
    }

    /** {@inheritDoc} */
    @Override
    @NotNull
    public List<ChangeResponse> handleChanges(@NotNull final List<ResourceChange> changes) {
        return changes.stream()
            .map(change -> {
                CompletableFuture<ChangeMetadata> future = CompletableFuture.supplyAsync(() -> {
                    Catalog catalog = catalogFactory.createCatalog();
                    if (!(catalog instanceof ViewCatalog viewCatalog)) {
                        throw new JikkouRuntimeException(
                            "The configured catalog does not support views (does not implement ViewCatalog)");
                    }

                    String resourceName = change.getMetadata().getName();
                    TableIdentifier identifier = parseIdentifier(resourceName);

                    StateChangeList<? extends StateChange> data = change.getSpec().getChanges();

                    // Build schema from columns if provided, otherwise use empty schema
                    Schema schema = buildSchema(data);

                    // Extract queries
                    List<V1IcebergViewQuery> queries = extractQueries(data);

                    // Extract defaults
                    String defaultNamespace = extractStringValue(data, "defaultNamespace");
                    String defaultCatalog = extractStringValue(data, "defaultCatalog");

                    // Extract properties
                    Map<String, String> properties = extractProperties(data);

                    // Build the view
                    ViewBuilder builder = viewCatalog.buildView(identifier)
                        .withSchema(schema)
                        .withDefaultNamespace(Namespace.of(defaultNamespace.split("\\.")))
                        .withProperties(properties);

                    if (defaultCatalog != null && !defaultCatalog.isEmpty()) {
                        builder.withDefaultCatalog(defaultCatalog);
                    }

                    for (V1IcebergViewQuery query : queries) {
                        builder.withQuery(query.getDialect(), query.getSql());
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
        String viewName = name.substring(lastDot + 1);
        return TableIdentifier.of(Namespace.of(namespacePart.split("\\.")), viewName);
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
    @SuppressWarnings("unchecked")
    static List<V1IcebergViewQuery> extractQueries(
            @NotNull final StateChangeList<? extends StateChange> data) {
        for (StateChange sc : data) {
            if ("queries".equals(sc.getName()) && sc.getAfter() instanceof List<?> queries) {
                return (List<V1IcebergViewQuery>) queries;
            }
        }
        return List.of();
    }

    @NotNull
    static Map<String, String> extractProperties(
            @NotNull final StateChangeList<? extends StateChange> data) {
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
