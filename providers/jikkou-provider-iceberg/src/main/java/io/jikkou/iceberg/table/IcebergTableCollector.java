/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.jikkou.iceberg.table;

import io.jikkou.core.annotation.Description;
import io.jikkou.core.annotation.SupportedResource;
import io.jikkou.core.annotation.Title;
import io.jikkou.core.config.Configuration;
import io.jikkou.core.exceptions.JikkouRuntimeException;
import io.jikkou.core.extension.ContextualExtension;
import io.jikkou.core.extension.ExtensionContext;
import io.jikkou.core.models.ResourceList;
import io.jikkou.core.reconciler.Collector;
import io.jikkou.core.selector.Selector;
import io.jikkou.iceberg.IcebergExtensionProvider;
import io.jikkou.iceberg.internals.CatalogFactory;
import io.jikkou.iceberg.internals.IcebergTableConverter;
import io.jikkou.iceberg.table.models.V1IcebergTable;
import io.jikkou.iceberg.table.models.V1IcebergTableList;
import java.util.ArrayList;
import java.util.List;
import org.apache.iceberg.Table;
import org.apache.iceberg.catalog.Catalog;
import org.apache.iceberg.catalog.Namespace;
import org.apache.iceberg.catalog.SupportsNamespaces;
import org.apache.iceberg.catalog.TableIdentifier;
import org.jetbrains.annotations.NotNull;

/**
 * Collects Iceberg table resources from the configured catalog.
 */
@Title("Collect Iceberg tables")
@Description("Collects all Iceberg table resources from the configured catalog.")
@SupportedResource(type = V1IcebergTable.class)
public final class IcebergTableCollector extends ContextualExtension implements Collector<V1IcebergTable> {

    private CatalogFactory catalogFactory;

    /**
     * Creates a new {@link IcebergTableCollector} instance.
     */
    public IcebergTableCollector() {
    }

    /**
     * Creates a new {@link IcebergTableCollector} instance.
     *
     * @param catalogFactory the catalog factory.
     */
    public IcebergTableCollector(@NotNull final CatalogFactory catalogFactory) {
        this.catalogFactory = catalogFactory;
    }

    /** {@inheritDoc} */
    @Override
    public void init(@NotNull final ExtensionContext context) {
        super.init(context);
        if (catalogFactory == null) {
            catalogFactory = context.<IcebergExtensionProvider>provider().catalogFactory();
        }
    }

    /** {@inheritDoc} */
    @Override
    public ResourceList<V1IcebergTable> listAll(@NotNull final Configuration configuration,
                                                 @NotNull final Selector selector) {
        Catalog catalog = catalogFactory.createCatalog();

        List<Namespace> namespaces = listNamespaces(catalog);
        List<V1IcebergTable> items = new ArrayList<>();

        for (Namespace namespace : namespaces) {
            List<TableIdentifier> tableIdentifiers;
            try {
                tableIdentifiers = catalog.listTables(namespace);
            } catch (Exception e) {
                throw new JikkouRuntimeException(
                    "Failed to list tables in namespace '" + namespace + "'", e);
            }

            for (TableIdentifier identifier : tableIdentifiers) {
                try {
                    Table table = catalog.loadTable(identifier);
                    V1IcebergTable resource = IcebergTableConverter.toV1IcebergTable(table, identifier);
                    items.add(resource);
                } catch (Exception e) {
                    throw new JikkouRuntimeException(
                        "Failed to load table '" + identifier + "'", e);
                }
            }
        }

        return new V1IcebergTableList.Builder().withItems(items).build();
    }

    @NotNull
    private static List<Namespace> listNamespaces(@NotNull final Catalog catalog) {
        if (catalog instanceof SupportsNamespaces ns) {
            try {
                List<Namespace> result = new ArrayList<>();
                collectNamespacesRecursive(ns, Namespace.empty(), result);
                return result;
            } catch (Exception e) {
                // Fallback: some catalogs support SupportsNamespaces but fail on root listing
                // In that case, try to list tables at the root namespace
                return List.of(Namespace.empty());
            }
        }
        // If catalog does not support namespaces, list tables at root
        return List.of(Namespace.empty());
    }

    private static void collectNamespacesRecursive(@NotNull final SupportsNamespaces ns,
                                                    @NotNull final Namespace parent,
                                                    @NotNull final List<Namespace> result) {
        List<Namespace> children = ns.listNamespaces(parent);
        for (Namespace child : children) {
            result.add(child);
            collectNamespacesRecursive(ns, child, result);
        }
    }
}
