/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.jikkou.iceberg.namespace.handlers;

import io.jikkou.core.exceptions.JikkouRuntimeException;
import io.jikkou.core.models.change.ResourceChange;
import io.jikkou.core.reconciler.ChangeMetadata;
import io.jikkou.core.reconciler.ChangeResponse;
import io.jikkou.core.reconciler.Operation;
import io.jikkou.core.reconciler.TextDescription;
import io.jikkou.core.reconciler.change.BaseChangeHandler;
import io.jikkou.iceberg.internals.CatalogFactory;
import io.jikkou.iceberg.namespace.NamespaceChangeDescription;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.apache.iceberg.catalog.Catalog;
import org.apache.iceberg.catalog.Namespace;
import org.apache.iceberg.catalog.SupportsNamespaces;
import org.jetbrains.annotations.NotNull;

/**
 * Change handler for deleting Iceberg namespaces.
 */
public final class DeleteNamespaceChangeHandler extends BaseChangeHandler {

    private final CatalogFactory catalogFactory;

    /**
     * Creates a new {@link DeleteNamespaceChangeHandler} instance.
     *
     * @param catalogFactory the catalog factory.
     */
    public DeleteNamespaceChangeHandler(@NotNull final CatalogFactory catalogFactory) {
        super(Operation.DELETE);
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
        // Sort by namespace depth (descending) so child namespaces are deleted before parents.
        List<ResourceChange> sorted = new ArrayList<>(changes);
        sorted.sort(Comparator.<ResourceChange>comparingInt(
            c -> namespaceDepth(c.getMetadata().getName())).reversed());

        // Chain futures sequentially to ensure child namespaces are dropped before parents.
        List<ChangeResponse> responses = new ArrayList<>(sorted.size());
        CompletableFuture<Void> previous = CompletableFuture.completedFuture(null);
        for (ResourceChange change : sorted) {
            CompletableFuture<ChangeMetadata> future = previous.thenApplyAsync(ignored -> {
                Catalog catalog = catalogFactory.createCatalog();
                if (!(catalog instanceof SupportsNamespaces ns)) {
                    throw new JikkouRuntimeException(
                        "Catalog '" + catalog.name() + "' does not support namespace operations.");
                }
                String name = change.getMetadata().getName();
                Namespace namespace = Namespace.of(name.split("\\."));
                ns.dropNamespace(namespace);
                return ChangeMetadata.empty();
            });
            responses.add(new ChangeResponse(change, future));
            previous = future.thenApply(ignored -> null);
        }
        return responses;
    }

    private static int namespaceDepth(@NotNull final String name) {
        int depth = 1;
        for (int i = 0; i < name.length(); i++) {
            if (name.charAt(i) == '.') depth++;
        }
        return depth;
    }
}
