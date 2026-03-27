/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.iceberg.table.handlers;

import io.streamthoughts.jikkou.core.models.change.ResourceChange;
import io.streamthoughts.jikkou.core.reconciler.ChangeMetadata;
import io.streamthoughts.jikkou.core.reconciler.ChangeResponse;
import io.streamthoughts.jikkou.core.reconciler.Operation;
import io.streamthoughts.jikkou.core.reconciler.TextDescription;
import io.streamthoughts.jikkou.core.reconciler.change.BaseChangeHandler;
import io.streamthoughts.jikkou.iceberg.internals.CatalogFactory;
import io.streamthoughts.jikkou.iceberg.table.TableChangeDescription;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import org.apache.iceberg.catalog.Catalog;
import org.apache.iceberg.catalog.Namespace;
import org.apache.iceberg.catalog.TableIdentifier;
import org.jetbrains.annotations.NotNull;

/**
 * Change handler for deleting Iceberg tables.
 */
public final class DeleteTableChangeHandler extends BaseChangeHandler {

    private final CatalogFactory catalogFactory;
    private final boolean purge;

    /**
     * Creates a new {@link DeleteTableChangeHandler} instance.
     *
     * @param catalogFactory the catalog factory.
     * @param purge          whether to purge underlying data files when dropping the table.
     */
    public DeleteTableChangeHandler(@NotNull final CatalogFactory catalogFactory,
                                    final boolean purge) {
        super(Operation.DELETE);
        this.catalogFactory = catalogFactory;
        this.purge = purge;
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
                    catalog.dropTable(identifier, purge);
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
}
