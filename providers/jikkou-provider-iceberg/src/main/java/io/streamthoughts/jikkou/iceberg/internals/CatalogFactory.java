/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.iceberg.internals;

import java.util.HashMap;
import java.util.Map;
import org.apache.hadoop.conf.Configuration;
import org.apache.iceberg.CatalogProperties;
import org.apache.iceberg.CatalogUtil;
import org.apache.iceberg.catalog.Catalog;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Factory for creating Apache Iceberg {@link Catalog} instances from provider configuration.
 */
public final class CatalogFactory {

    private final String catalogName;
    private final String catalogType;
    private final String catalogUri;
    private final String warehouse;
    private final Map<String, Object> extraProperties;

    /**
     * Creates a new {@link CatalogFactory} instance.
     *
     * @param catalogName      the catalog name.
     * @param catalogType      the catalog type (e.g. "rest", "hive", "glue").
     * @param catalogUri       the catalog URI (optional).
     * @param warehouse        the warehouse location (optional).
     * @param extraProperties  additional catalog properties passed through to the catalog implementation.
     */
    public CatalogFactory(@NotNull final String catalogName,
                          @NotNull final String catalogType,
                          @Nullable final String catalogUri,
                          @Nullable final String warehouse,
                          @NotNull final Map<String, Object> extraProperties) {
        this.catalogName = catalogName;
        this.catalogType = catalogType;
        this.catalogUri = catalogUri;
        this.warehouse = warehouse;
        this.extraProperties = extraProperties;
    }

    /**
     * Creates and returns a new Iceberg {@link Catalog} instance.
     *
     * @return a configured catalog.
     */
    public Catalog createCatalog() {
        Map<String, String> props = new HashMap<>();

        // Start with extra passthrough properties
        extraProperties.forEach((k, v) -> props.put(k, String.valueOf(v)));

        // Inject top-level catalog config keys
        if (catalogUri != null && !catalogUri.isEmpty()) {
            props.put(CatalogProperties.URI, catalogUri);
        }
        if (warehouse != null && !warehouse.isEmpty()) {
            props.put(CatalogProperties.WAREHOUSE_LOCATION, warehouse);
        }
        props.put(CatalogUtil.ICEBERG_CATALOG_TYPE, catalogType);

        return CatalogUtil.buildIcebergCatalog(catalogName, props, new Configuration());
    }

    /**
     * @return the catalog name.
     */
    public String catalogName() {
        return catalogName;
    }

    /**
     * @return the catalog type.
     */
    public String catalogType() {
        return catalogType;
    }
}
