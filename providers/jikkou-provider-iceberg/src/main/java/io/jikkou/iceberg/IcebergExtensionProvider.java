/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.jikkou.iceberg;

import io.jikkou.core.annotation.Provider;
import io.jikkou.core.config.ConfigProperty;
import io.jikkou.core.config.Configuration;
import io.jikkou.core.exceptions.ConfigException;
import io.jikkou.core.extension.ExtensionRegistry;
import io.jikkou.core.models.change.GenericResourceChange;
import io.jikkou.core.models.change.ResourceChange;
import io.jikkou.core.resource.ResourceRegistry;
import io.jikkou.iceberg.health.IcebergCatalogHealthIndicator;
import io.jikkou.iceberg.internals.CatalogFactory;
import io.jikkou.iceberg.namespace.IcebergNamespaceCollector;
import io.jikkou.iceberg.namespace.IcebergNamespaceController;
import io.jikkou.iceberg.namespace.models.V1IcebergNamespace;
import io.jikkou.iceberg.namespace.models.V1IcebergNamespaceList;
import io.jikkou.iceberg.table.IcebergTableCollector;
import io.jikkou.iceberg.table.IcebergTableController;
import io.jikkou.iceberg.table.models.V1IcebergTable;
import io.jikkou.iceberg.table.models.V1IcebergTableList;
import io.jikkou.iceberg.view.IcebergViewCollector;
import io.jikkou.iceberg.view.IcebergViewController;
import io.jikkou.iceberg.view.models.V1IcebergView;
import io.jikkou.iceberg.view.models.V1IcebergViewList;
import io.jikkou.spi.BaseExtensionProvider;
import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.NotNull;

/**
 * Extension provider for Apache Iceberg catalogs.
 */
@Provider(
    name = "iceberg",
    description = "Extension provider for Apache Iceberg",
    tags = {"Apache Iceberg"}
)
public final class IcebergExtensionProvider extends BaseExtensionProvider {

    /**
     * Configuration properties for the Iceberg provider.
     */
    public interface Config {

        ConfigProperty<String> CATALOG_NAME = ConfigProperty
            .ofString("catalogName")
            .displayName("Catalog Name")
            .description("The name to assign to the catalog instance.")
            .defaultValue("default");

        ConfigProperty<String> CATALOG_TYPE = ConfigProperty
            .ofString("catalogType")
            .displayName("Catalog Type")
            .required(true)
            .description("The catalog implementation type: rest, hive, jdbc, glue, nessie, hadoop.");

        ConfigProperty<String> CATALOG_URI = ConfigProperty
            .ofString("catalogUri")
            .displayName("Catalog URI")
            .description("The catalog endpoint URI (REST URL, Hive metastore URI, JDBC connection URL, etc.).");

        ConfigProperty<String> WAREHOUSE = ConfigProperty
            .ofString("warehouse")
            .displayName("Warehouse")
            .description("The warehouse base location in the storage system (e.g. s3://bucket/warehouse).");

        ConfigProperty<Map<String, Object>> CATALOG_PROPERTIES = ConfigProperty
            .ofMap("catalogProperties")
            .displayName("Catalog Properties")
            .description("Passthrough properties for CatalogUtil.buildIcebergCatalog()." +
                " Covers catalog-specific config: AWS credentials, S3 endpoints, Nessie auth, JDBC options, etc.")
            .defaultValue(Map.of());

        ConfigProperty<Boolean> DEBUG_LOGGING = ConfigProperty
            .ofBoolean("debugLoggingEnabled")
            .displayName("Debug Logging")
            .description("Enable debug logging for catalog operations.")
            .defaultValue(false);
    }

    private CatalogFactory catalogFactory;

    /** {@inheritDoc} */
    @Override
    public List<ConfigProperty<?>> configProperties() {
        return List.of(
            Config.CATALOG_NAME,
            Config.CATALOG_TYPE,
            Config.CATALOG_URI,
            Config.WAREHOUSE,
            Config.CATALOG_PROPERTIES,
            Config.DEBUG_LOGGING
        );
    }

    /** {@inheritDoc} */
    @Override
    public void configure(@NotNull final Configuration configuration) throws ConfigException {
        super.configure(configuration);
        String catalogName = Config.CATALOG_NAME.get(configuration);
        String catalogType = Config.CATALOG_TYPE.get(configuration);
        String catalogUri = Config.CATALOG_URI.getOptional(configuration).orElse(null);
        String warehouse = Config.WAREHOUSE.getOptional(configuration).orElse(null);
        Map<String, Object> catalogProperties = Config.CATALOG_PROPERTIES.get(configuration);

        this.catalogFactory = new CatalogFactory(
            catalogName,
            catalogType,
            catalogUri,
            warehouse,
            catalogProperties
        );
    }

    /**
     * Returns the {@link CatalogFactory} built from provider configuration.
     *
     * @return the catalog factory.
     */
    public CatalogFactory catalogFactory() {
        return catalogFactory;
    }

    /** {@inheritDoc} */
    @Override
    public void registerExtensions(@NotNull final ExtensionRegistry registry) {
        // Collectors
        registry.register(IcebergNamespaceCollector.class, IcebergNamespaceCollector::new);
        registry.register(IcebergTableCollector.class, IcebergTableCollector::new);
        registry.register(IcebergViewCollector.class, IcebergViewCollector::new);

        // Controllers
        registry.register(IcebergNamespaceController.class, IcebergNamespaceController::new);
        registry.register(IcebergTableController.class, IcebergTableController::new);
        registry.register(IcebergViewController.class, IcebergViewController::new);

        // Health indicators
        registry.register(IcebergCatalogHealthIndicator.class, IcebergCatalogHealthIndicator::new);
    }

    /** {@inheritDoc} */
    @Override
    public void registerResources(@NotNull final ResourceRegistry registry) {
        registry.register(V1IcebergNamespace.class);
        registry.register(GenericResourceChange.class, ResourceChange.getResourceTypeOf(V1IcebergNamespace.class));
        registry.register(V1IcebergNamespaceList.class);
        registry.register(V1IcebergTable.class);
        registry.register(GenericResourceChange.class, ResourceChange.getResourceTypeOf(V1IcebergTable.class));
        registry.register(V1IcebergTableList.class);
        registry.register(V1IcebergView.class);
        registry.register(GenericResourceChange.class, ResourceChange.getResourceTypeOf(V1IcebergView.class));
        registry.register(V1IcebergViewList.class);
    }
}
