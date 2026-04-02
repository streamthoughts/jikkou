/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.iceberg.view;

import io.streamthoughts.jikkou.core.annotation.Description;
import io.streamthoughts.jikkou.core.annotation.SupportedResource;
import io.streamthoughts.jikkou.core.annotation.Title;
import io.streamthoughts.jikkou.core.config.Configuration;
import io.streamthoughts.jikkou.core.exceptions.JikkouRuntimeException;
import io.streamthoughts.jikkou.core.extension.ContextualExtension;
import io.streamthoughts.jikkou.core.extension.ExtensionContext;
import io.streamthoughts.jikkou.core.models.ResourceList;
import io.streamthoughts.jikkou.core.reconciler.Collector;
import io.streamthoughts.jikkou.core.selector.Selector;
import io.streamthoughts.jikkou.iceberg.IcebergExtensionProvider;
import io.streamthoughts.jikkou.iceberg.internals.CatalogFactory;
import io.streamthoughts.jikkou.iceberg.internals.IcebergViewConverter;
import io.streamthoughts.jikkou.iceberg.view.models.V1IcebergView;
import io.streamthoughts.jikkou.iceberg.view.models.V1IcebergViewList;
import java.util.ArrayList;
import java.util.List;
import org.apache.iceberg.catalog.Catalog;
import org.apache.iceberg.catalog.Namespace;
import org.apache.iceberg.catalog.SupportsNamespaces;
import org.apache.iceberg.catalog.TableIdentifier;
import org.apache.iceberg.catalog.ViewCatalog;
import org.apache.iceberg.view.View;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Collects Iceberg view resources from the configured catalog.
 */
@Title("Collect Iceberg views")
@Description("Collects all Iceberg view resources from the configured catalog.")
@SupportedResource(type = V1IcebergView.class)
public final class IcebergViewCollector extends ContextualExtension implements Collector<V1IcebergView> {

    private static final Logger LOG = LoggerFactory.getLogger(IcebergViewCollector.class);

    private CatalogFactory catalogFactory;

    /**
     * Creates a new {@link IcebergViewCollector} instance.
     */
    public IcebergViewCollector() {
    }

    /**
     * Creates a new {@link IcebergViewCollector} instance.
     *
     * @param catalogFactory the catalog factory.
     */
    public IcebergViewCollector(@NotNull final CatalogFactory catalogFactory) {
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
    public ResourceList<V1IcebergView> listAll(@NotNull final Configuration configuration,
                                                @NotNull final Selector selector) {
        Catalog catalog = catalogFactory.createCatalog();

        if (!(catalog instanceof ViewCatalog viewCatalog)) {
            LOG.warn("The configured catalog does not support views (does not implement ViewCatalog). "
                + "Returning empty list.");
            return new V1IcebergViewList.Builder().withItems(List.of()).build();
        }

        List<Namespace> namespaces = listNamespaces(catalog);
        List<V1IcebergView> items = new ArrayList<>();

        for (Namespace namespace : namespaces) {
            List<TableIdentifier> viewIdentifiers;
            try {
                viewIdentifiers = viewCatalog.listViews(namespace);
            } catch (Exception e) {
                throw new JikkouRuntimeException(
                    "Failed to list views in namespace '" + namespace + "'", e);
            }

            for (TableIdentifier identifier : viewIdentifiers) {
                try {
                    View view = viewCatalog.loadView(identifier);
                    V1IcebergView resource = IcebergViewConverter.toV1IcebergView(view, identifier);
                    items.add(resource);
                } catch (Exception e) {
                    throw new JikkouRuntimeException(
                        "Failed to load view '" + identifier + "'", e);
                }
            }
        }

        return new V1IcebergViewList.Builder().withItems(items).build();
    }

    @NotNull
    private static List<Namespace> listNamespaces(@NotNull final Catalog catalog) {
        if (catalog instanceof SupportsNamespaces ns) {
            try {
                List<Namespace> result = new ArrayList<>();
                collectNamespacesRecursive(ns, Namespace.empty(), result);
                return result;
            } catch (Exception e) {
                return List.of(Namespace.empty());
            }
        }
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
