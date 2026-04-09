/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.jikkou.iceberg.view;

import io.jikkou.core.models.change.ResourceChange;
import io.jikkou.core.models.change.StateChange;
import io.jikkou.core.reconciler.Operation;
import io.jikkou.core.reconciler.TextDescription;
import io.jikkou.iceberg.view.models.V1IcebergViewQuery;
import java.util.List;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;

/**
 * Provides human-readable descriptions for Iceberg view changes.
 */
public final class ViewChangeDescription implements TextDescription {

    private static final String PROPERTY_PREFIX = "property.";

    private final ResourceChange change;

    /**
     * Creates a new {@link ViewChangeDescription} instance.
     *
     * @param change the resource change.
     */
    public ViewChangeDescription(@NotNull final ResourceChange change) {
        this.change = change;
    }

    /** {@inheritDoc} */
    @Override
    public String textual() {
        String name = change.getMetadata().getName();
        Operation op = change.getSpec().getOp();

        return switch (op) {
            case CREATE -> buildCreateDescription(name);
            case DELETE -> "Delete view '" + name + "'";
            case UPDATE -> buildUpdateDescription(name);
            default -> op + " view '" + name + "'";
        };
    }

    private String buildCreateDescription(@NotNull final String name) {
        var changes = change.getSpec().getChanges();

        int queryCount = 0;
        List<String> dialects = List.of();

        for (StateChange sc : changes) {
            if ("queries".equals(sc.getName()) && sc.getAfter() instanceof List<?> queries) {
                queryCount = queries.size();
                dialects = queries.stream()
                    .filter(q -> q instanceof V1IcebergViewQuery)
                    .map(q -> ((V1IcebergViewQuery) q).getDialect())
                    .collect(Collectors.toList());
            }
        }

        StringBuilder sb = new StringBuilder("Create view '").append(name).append("'");
        sb.append(" (queries=").append(queryCount);
        if (!dialects.isEmpty()) {
            sb.append(", dialects=[").append(String.join(", ", dialects)).append("]");
        }
        sb.append(")");
        return sb.toString();
    }

    private String buildUpdateDescription(@NotNull final String name) {
        var changes = change.getSpec().getChanges();

        boolean queriesChanged = changes.stream()
            .anyMatch(c -> "queries".equals(c.getName()) && c.getOp() != Operation.NONE);

        boolean defaultNamespaceChanged = changes.stream()
            .anyMatch(c -> "defaultNamespace".equals(c.getName()) && c.getOp() != Operation.NONE);

        boolean defaultCatalogChanged = changes.stream()
            .anyMatch(c -> "defaultCatalog".equals(c.getName()) && c.getOp() != Operation.NONE);

        List<String> changedProperties = changes.stream()
            .filter(c -> c.getName().startsWith(PROPERTY_PREFIX))
            .filter(c -> c.getOp() != Operation.NONE)
            .map(c -> c.getName().substring(PROPERTY_PREFIX.length()))
            .collect(Collectors.toList());

        StringBuilder sb = new StringBuilder("Update view '").append(name).append("'");
        List<String> aspects = new java.util.ArrayList<>();
        if (queriesChanged) aspects.add("queries");
        if (defaultNamespaceChanged) aspects.add("defaultNamespace");
        if (defaultCatalogChanged) aspects.add("defaultCatalog");
        if (!changedProperties.isEmpty()) {
            aspects.add("properties=[" + String.join(", ", changedProperties) + "]");
        }

        if (!aspects.isEmpty()) {
            sb.append(" (").append(String.join(", ", aspects)).append(")");
        }
        return sb.toString();
    }
}
