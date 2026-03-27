/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.iceberg.namespace;

import io.streamthoughts.jikkou.core.annotation.Description;
import io.streamthoughts.jikkou.core.annotation.SupportedResource;
import io.streamthoughts.jikkou.core.annotation.Title;
import io.streamthoughts.jikkou.core.config.Configuration;
import io.streamthoughts.jikkou.core.exceptions.JikkouRuntimeException;
import io.streamthoughts.jikkou.core.extension.ContextualExtension;
import io.streamthoughts.jikkou.core.extension.ExtensionContext;
import io.streamthoughts.jikkou.core.models.ObjectMeta;
import io.streamthoughts.jikkou.core.models.ResourceList;
import io.streamthoughts.jikkou.core.reconciler.Collector;
import io.streamthoughts.jikkou.core.selector.Selector;
import io.streamthoughts.jikkou.iceberg.IcebergAnnotations;
import io.streamthoughts.jikkou.iceberg.IcebergExtensionProvider;
import io.streamthoughts.jikkou.iceberg.internals.CatalogFactory;
import io.streamthoughts.jikkou.iceberg.namespace.models.V1IcebergNamespace;
import io.streamthoughts.jikkou.iceberg.namespace.models.V1IcebergNamespaceList;
import io.streamthoughts.jikkou.iceberg.namespace.models.V1IcebergNamespaceSpec;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.iceberg.catalog.Catalog;
import org.apache.iceberg.catalog.Namespace;
import org.apache.iceberg.catalog.SupportsNamespaces;
import org.jetbrains.annotations.NotNull;

/**
 * Collects Iceberg namespace resources from the configured catalog.
 */
@Title("Collect Iceberg namespaces")
@Description("Collects all Iceberg namespace resources from the configured catalog.")
@SupportedResource(type = V1IcebergNamespace.class)
public final class IcebergNamespaceCollector extends ContextualExtension implements Collector<V1IcebergNamespace> {

    private CatalogFactory catalogFactory;

    /**
     * Creates a new {@link IcebergNamespaceCollector} instance.
     */
    public IcebergNamespaceCollector() {
    }

    /**
     * Creates a new {@link IcebergNamespaceCollector} instance.
     *
     * @param catalogFactory the catalog factory.
     */
    public IcebergNamespaceCollector(@NotNull final CatalogFactory catalogFactory) {
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
    public ResourceList<V1IcebergNamespace> listAll(@NotNull final Configuration configuration,
                                                     @NotNull final Selector selector) {
        Catalog catalog = catalogFactory.createCatalog();

        if (!(catalog instanceof SupportsNamespaces ns)) {
            throw new JikkouRuntimeException(
                "Catalog '" + catalog.name() + "' (type='" + catalogFactory.catalogType() + "') " +
                "does not support namespace operations. " +
                "Catalog must implement SupportsNamespaces to collect namespaces.");
        }

        List<V1IcebergNamespace> items = new ArrayList<>();
        collectNamespacesRecursive(ns, Namespace.empty(), items);
        return new V1IcebergNamespaceList.Builder().withItems(items).build();
    }

    private static void collectNamespacesRecursive(@NotNull final SupportsNamespaces ns,
                                                    @NotNull final Namespace parent,
                                                    @NotNull final List<V1IcebergNamespace> items) {
        List<Namespace> namespaces = ns.listNamespaces(parent);
        for (Namespace namespace : namespaces) {
            Map<String, String> properties = new LinkedHashMap<>(ns.loadNamespaceMetadata(namespace));
            String name = namespace.toString();

            // Extract catalog-managed location into annotation
            ObjectMeta.ObjectMetaBuilder metaBuilder = ObjectMeta.builder().withName(name);
            String location = properties.remove("location");
            if (location != null) {
                metaBuilder.withAnnotation(IcebergAnnotations.NAMESPACE_LOCATION, location);
            }

            V1IcebergNamespaceSpec spec = new V1IcebergNamespaceSpec();
            spec.setProperties(properties.isEmpty() ? null : properties);

            V1IcebergNamespace resource = V1IcebergNamespace.builder()
                .withMetadata(metaBuilder.build())
                .withSpec(spec)
                .build();

            items.add(resource);

            // Recurse into child namespaces
            collectNamespacesRecursive(ns, namespace, items);
        }
    }
}
