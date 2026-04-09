/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.jikkou.iceberg.namespace;

import static io.jikkou.core.ReconciliationMode.CREATE;
import static io.jikkou.core.ReconciliationMode.DELETE;
import static io.jikkou.core.ReconciliationMode.FULL;
import static io.jikkou.core.ReconciliationMode.UPDATE;

import io.jikkou.core.ReconciliationContext;
import io.jikkou.core.annotation.Description;
import io.jikkou.core.annotation.SupportedResource;
import io.jikkou.core.annotation.Title;
import io.jikkou.core.extension.ContextualExtension;
import io.jikkou.core.extension.ExtensionContext;
import io.jikkou.core.models.change.ResourceChange;
import io.jikkou.core.reconciler.ChangeExecutor;
import io.jikkou.core.reconciler.ChangeHandler;
import io.jikkou.core.reconciler.ChangeResult;
import io.jikkou.core.reconciler.Controller;
import io.jikkou.core.reconciler.annotations.ControllerConfiguration;
import io.jikkou.core.selector.Selector;
import io.jikkou.iceberg.ApiVersions;
import io.jikkou.iceberg.IcebergExtensionProvider;
import io.jikkou.iceberg.internals.CatalogFactory;
import io.jikkou.iceberg.namespace.handlers.CreateNamespaceChangeHandler;
import io.jikkou.iceberg.namespace.handlers.DeleteNamespaceChangeHandler;
import io.jikkou.iceberg.namespace.handlers.UpdateNamespaceChangeHandler;
import io.jikkou.iceberg.namespace.models.V1IcebergNamespace;
import java.util.Collection;
import java.util.List;
import org.jetbrains.annotations.NotNull;

/**
 * Reconciles Iceberg namespace resources.
 */
@Title("Reconcile Iceberg namespaces")
@Description("Reconciles Iceberg namespace resources to ensure they match the desired state.")
@ControllerConfiguration(
    supportedModes = {CREATE, DELETE, UPDATE, FULL}
)
@SupportedResource(type = V1IcebergNamespace.class)
@SupportedResource(apiVersion = ApiVersions.ICEBERG_V1BETA1, kind = "IcebergNamespaceChange")
public final class IcebergNamespaceController
        extends ContextualExtension
        implements Controller<V1IcebergNamespace> {

    private CatalogFactory catalogFactory;

    /**
     * Creates a new {@link IcebergNamespaceController} instance.
     */
    public IcebergNamespaceController() {
    }

    /**
     * Creates a new {@link IcebergNamespaceController} instance.
     *
     * @param catalogFactory the catalog factory.
     */
    public IcebergNamespaceController(@NotNull final CatalogFactory catalogFactory) {
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
    public List<ChangeResult> execute(@NotNull final ChangeExecutor executor,
                                      @NotNull final ReconciliationContext context) {
        List<ChangeHandler> handlers = List.of(
            new CreateNamespaceChangeHandler(catalogFactory),
            new UpdateNamespaceChangeHandler(catalogFactory),
            new DeleteNamespaceChangeHandler(catalogFactory),
            new ChangeHandler.None(NamespaceChangeDescription::new)
        );
        return executor.applyChanges(handlers);
    }

    /** {@inheritDoc} */
    @Override
    public List<ResourceChange> plan(@NotNull final Collection<V1IcebergNamespace> resources,
                                     @NotNull final ReconciliationContext context) {
        Selector selector = context.selector();

        List<V1IcebergNamespace> allExpected = resources.stream().toList();

        IcebergNamespaceCollector collector = new IcebergNamespaceCollector(catalogFactory);
        List<V1IcebergNamespace> allActual = collector
            .listAll(context.configuration(), selector)
            .getItems();

        // Enrich actual with labels from expected for label-based selector support
        Controller.enrichLabelsFromExpected(allActual, allExpected);

        List<V1IcebergNamespace> expected = allExpected.stream().filter(selector::apply).toList();
        List<V1IcebergNamespace> actual = allActual.stream().filter(selector::apply).toList();

        // deleteOrphans=false by default for namespaces (namespaces may contain tables)
        NamespaceChangeComputer computer = new NamespaceChangeComputer(false);
        return computer.computeChanges(actual, expected);
    }
}
