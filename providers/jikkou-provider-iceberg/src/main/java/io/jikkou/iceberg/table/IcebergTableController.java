/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.jikkou.iceberg.table;

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
import io.jikkou.iceberg.table.handlers.CreateTableChangeHandler;
import io.jikkou.iceberg.table.handlers.DeleteTableChangeHandler;
import io.jikkou.iceberg.table.handlers.UpdateTableChangeHandler;
import io.jikkou.iceberg.table.models.V1IcebergTable;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;

/**
 * Reconciles Iceberg table resources.
 */
@Title("Reconcile Iceberg tables")
@Description("Reconciles Iceberg table resources to ensure they match the desired state.")
@ControllerConfiguration(
    supportedModes = {CREATE, DELETE, UPDATE, FULL}
)
@SupportedResource(type = V1IcebergTable.class)
@SupportedResource(apiVersion = ApiVersions.ICEBERG_V1BETA1, kind = "IcebergTableChange")
public final class IcebergTableController
        extends ContextualExtension
        implements Controller<V1IcebergTable> {

    public interface Config {
        ConfigProperty<List<Pattern>> TABLES_EXCL = ConfigProperty
            .ofList("tables.deletion.exclude")
            .displayName("Tables Deletion Exclude")
            .description("Regex patterns matching table names to exclude from deletion.")
            .map(list -> list.stream().map(Pattern::compile).toList())
            .defaultValue(List.of());

        ConfigProperty<Boolean> DELETE_PURGE_ENABLED = ConfigProperty
            .ofBoolean("delete-purge")
            .displayName("Delete Purge")
            .description("Whether to purge underlying data files when dropping a table.")
            .defaultValue(false);

        ConfigProperty<Boolean> DELETE_ORPHAN_COLUMNS = ConfigProperty
            .ofBoolean("delete-orphan-columns")
            .displayName("Delete Orphan Columns")
            .description("Whether to drop columns that exist in the live table but are absent from the resource spec.")
            .defaultValue(false);

        ConfigProperty<Boolean> DELETE_ORPHAN_TABLES = ConfigProperty
            .ofBoolean("delete-orphans")
            .displayName("Delete Orphan Tables")
            .description("Whether to drop tables that exist in the catalog but are not defined in any resource.")
            .defaultValue(false);
    }

    private CatalogFactory catalogFactory;

    /**
     * Creates a new {@link IcebergTableController} instance.
     */
    public IcebergTableController() {
    }

    /**
     * Creates a new {@link IcebergTableController} instance.
     *
     * @param catalogFactory the catalog factory.
     */
    public IcebergTableController(@NotNull final CatalogFactory catalogFactory) {
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
        boolean purge = Config.DELETE_PURGE_ENABLED.get(context.configuration());

        List<ChangeHandler> handlers = List.of(
            new CreateTableChangeHandler(catalogFactory),
            new UpdateTableChangeHandler(catalogFactory),
            new DeleteTableChangeHandler(catalogFactory, purge),
            new ChangeHandler.None(TableChangeDescription::new)
        );
        return executor.applyChanges(handlers);
    }

    /** {@inheritDoc} */
    @Override
    public List<ResourceChange> plan(@NotNull final Collection<V1IcebergTable> resources,
                                     @NotNull final ReconciliationContext context) {
        Selector selector = context.selector();

        List<V1IcebergTable> allExpected = resources.stream().toList();

        IcebergTableCollector collector = new IcebergTableCollector(catalogFactory);
        List<V1IcebergTable> allActual = collector
            .listAll(context.configuration(), selector)
            .getItems();

        // Enrich actual with labels from expected for label-based selector support
        Controller.enrichLabelsFromExpected(allActual, allExpected);

        List<V1IcebergTable> expected = allExpected.stream().filter(selector::apply).toList();
        List<V1IcebergTable> actual = allActual.stream().filter(selector::apply).toList();

        List<Pattern> exclusionPatterns = Config.TABLES_EXCL.get(context.configuration());
        boolean deleteOrphanTables = Config.DELETE_ORPHAN_TABLES.get(context.configuration());
        boolean deleteOrphanColumns = Config.DELETE_ORPHAN_COLUMNS.get(context.configuration());
        boolean deletePurge = Config.DELETE_PURGE_ENABLED.get(context.configuration());

        TableChangeComputer computer = new TableChangeComputer(
            exclusionPatterns,
            deleteOrphanTables,
            deleteOrphanColumns,
            deletePurge
        );
        return computer.computeChanges(actual, expected);
    }
}
