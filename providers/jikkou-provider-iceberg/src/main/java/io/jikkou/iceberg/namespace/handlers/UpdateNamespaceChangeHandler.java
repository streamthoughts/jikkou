/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.jikkou.iceberg.namespace.handlers;

import io.jikkou.core.exceptions.JikkouRuntimeException;
import io.jikkou.core.models.change.ResourceChange;
import io.jikkou.core.models.change.StateChange;
import io.jikkou.core.reconciler.ChangeMetadata;
import io.jikkou.core.reconciler.ChangeResponse;
import io.jikkou.core.reconciler.Operation;
import io.jikkou.core.reconciler.TextDescription;
import io.jikkou.core.reconciler.change.BaseChangeHandler;
import io.jikkou.iceberg.internals.CatalogFactory;
import io.jikkou.iceberg.namespace.NamespaceChangeDescription;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import org.apache.iceberg.catalog.Catalog;
import org.apache.iceberg.catalog.Namespace;
import org.apache.iceberg.catalog.SupportsNamespaces;
import org.jetbrains.annotations.NotNull;

/**
 * Change handler for updating Iceberg namespace properties.
 */
public final class UpdateNamespaceChangeHandler extends BaseChangeHandler {

    private static final String PROPERTY_PREFIX = "property.";

    private final CatalogFactory catalogFactory;

    /**
     * Creates a new {@link UpdateNamespaceChangeHandler} instance.
     *
     * @param catalogFactory the catalog factory.
     */
    public UpdateNamespaceChangeHandler(@NotNull final CatalogFactory catalogFactory) {
        super(Operation.UPDATE);
        this.catalogFactory = catalogFactory;
    }

    /** {@inheritDoc} */
    @Override
    public TextDescription describe(@NotNull final ResourceChange change) {
        return new NamespaceChangeDescription(change);
    }

    /** {@inheritDoc} */
    @Override
    @NotNull
    public List<ChangeResponse> handleChanges(@NotNull final List<ResourceChange> changes) {
        return changes.stream()
            .map(change -> {
                CompletableFuture<ChangeMetadata> future = CompletableFuture.supplyAsync(() -> {
                    Catalog catalog = catalogFactory.createCatalog();
                    if (!(catalog instanceof SupportsNamespaces ns)) {
                        throw new JikkouRuntimeException(
                            "Catalog '" + catalog.name() + "' does not support namespace operations.");
                    }
                    String name = change.getMetadata().getName();
                    Namespace namespace = Namespace.of(name.split("\\."));

                    Map<String, String> propsToSet = new HashMap<>();
                    Set<String> keysToRemove = new HashSet<>();

                    for (StateChange sc : change.getSpec().getChanges()) {
                        if (!sc.getName().startsWith(PROPERTY_PREFIX)) {
                            continue;
                        }
                        String key = sc.getName().substring(PROPERTY_PREFIX.length());
                        if (sc.getOp() == Operation.DELETE) {
                            keysToRemove.add(key);
                        } else if (sc.getOp() == Operation.CREATE || sc.getOp() == Operation.UPDATE) {
                            if (sc.getAfter() != null) {
                                propsToSet.put(key, String.valueOf(sc.getAfter()));
                            }
                        }
                    }

                    if (!propsToSet.isEmpty()) {
                        ns.setProperties(namespace, propsToSet);
                    }
                    if (!keysToRemove.isEmpty()) {
                        ns.removeProperties(namespace, keysToRemove);
                    }
                    return ChangeMetadata.empty();
                });
                return new ChangeResponse(change, future);
            })
            .collect(Collectors.toList());
    }
}
