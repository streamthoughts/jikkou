/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.jikkou.iceberg.view;

import static io.jikkou.core.ReconciliationMode.CREATE;
import static io.jikkou.core.ReconciliationMode.DELETE;
import static io.jikkou.core.ReconciliationMode.FULL;
import static io.jikkou.core.ReconciliationMode.UPDATE;

import io.jikkou.core.ReconciliationContext;
import io.jikkou.core.annotation.Description;
import io.jikkou.core.annotation.SupportedResource;
import io.jikkou.core.annotation.Title;
import io.jikkou.core.config.ConfigProperty;
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
import io.jikkou.iceberg.view.handlers.CreateViewChangeHandler;
import io.jikkou.iceberg.view.handlers.DeleteViewChangeHandler;
import io.jikkou.iceberg.view.handlers.UpdateViewChangeHandler;
import io.jikkou.iceberg.view.models.V1IcebergView;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;

/**
 * Reconciles Iceberg view resources.
 */
@Title("Reconcile Iceberg views")
@Description("Reconciles Iceberg view resources to ensure they match the desired state.")
@ControllerConfiguration(
    supportedModes = {CREATE, DELETE, UPDATE, FULL}
)
@SupportedResource(type = V1IcebergView.class)
@SupportedResource(apiVersion = ApiVersions.ICEBERG_V1BETA1, kind = "IcebergViewChange")
public final class IcebergViewController
        extends ContextualExtension
        implements Controller<V1IcebergView> {

    public interface Config {
        ConfigProperty<List<Pattern>> VIEWS_EXCL = ConfigProperty
            .ofList("views.deletion.exclude")
            .displayName("Views Deletion Exclude")
            .description("Regex patterns matching view names to exclude from deletion.")
            .map(list -> list.stream().map(Pattern::compile).toList())
            .defaultValue(List.of());

        ConfigProperty<Boolean> DELETE_ORPHAN_VIEWS = ConfigProperty
            .ofBoolean("delete-orphans")
            .displayName("Delete Orphan Views")
            .description("Whether to drop views that exist in the catalog but are not defined in any resource.")
            .defaultValue(false);
    }

    private CatalogFactory catalogFactory;

    /**
     * Creates a new {@link IcebergViewController} instance.
     */
    public IcebergViewController() {
    }

    /**
     * Creates a new {@link IcebergViewController} instance.
     *
     * @param catalogFactory the catalog factory.
     */
    public IcebergViewController(@NotNull final CatalogFactory catalogFactory) {
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
            new CreateViewChangeHandler(catalogFactory),
            new UpdateViewChangeHandler(catalogFactory),
            new DeleteViewChangeHandler(catalogFactory),
            new ChangeHandler.None(ViewChangeDescription::new)
        );
        return executor.applyChanges(handlers);
    }

    /** {@inheritDoc} */
    @Override
    public List<ResourceChange> plan(@NotNull final Collection<V1IcebergView> resources,
                                     @NotNull final ReconciliationContext context) {
        Selector selector = context.selector();

        List<V1IcebergView> allExpected = resources.stream().toList();

        IcebergViewCollector collector = new IcebergViewCollector(catalogFactory);
        List<V1IcebergView> allActual = collector
            .listAll(context.configuration(), selector)
            .getItems();

        Controller.enrichLabelsFromExpected(allActual, allExpected);

        List<V1IcebergView> expected = allExpected.stream().filter(selector::apply).toList();
        List<V1IcebergView> actual = allActual.stream().filter(selector::apply).toList();

        List<Pattern> exclusionPatterns = Config.VIEWS_EXCL.get(context.configuration());
        boolean deleteOrphanViews = Config.DELETE_ORPHAN_VIEWS.get(context.configuration());

        ViewChangeComputer computer = new ViewChangeComputer(
            exclusionPatterns,
            deleteOrphanViews
        );
        return computer.computeChanges(actual, expected);
    }
}
