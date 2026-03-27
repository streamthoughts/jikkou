/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.iceberg.health;

import static io.streamthoughts.jikkou.iceberg.health.IcebergCatalogHealthIndicator.HEALTH_INDICATOR_NAME;

import io.streamthoughts.jikkou.core.annotation.Description;
import io.streamthoughts.jikkou.core.annotation.Named;
import io.streamthoughts.jikkou.core.annotation.Title;
import io.streamthoughts.jikkou.core.exceptions.ConfigException;
import io.streamthoughts.jikkou.core.extension.ExtensionContext;
import io.streamthoughts.jikkou.core.health.Health;
import io.streamthoughts.jikkou.core.health.HealthIndicator;
import io.streamthoughts.jikkou.iceberg.IcebergExtensionProvider;
import io.streamthoughts.jikkou.iceberg.internals.CatalogFactory;
import java.time.Duration;
import org.apache.iceberg.catalog.Catalog;
import org.apache.iceberg.catalog.Namespace;
import org.apache.iceberg.catalog.SupportsNamespaces;
import org.jetbrains.annotations.NotNull;

/**
 * Health indicator for the Apache Iceberg catalog.
 */
@Named(HEALTH_INDICATOR_NAME)
@Title("IcebergCatalogHealthIndicator allows checking whether the Iceberg catalog is reachable.")
@Description("Get the health of the Apache Iceberg catalog")
public final class IcebergCatalogHealthIndicator implements HealthIndicator {

    public static final String HEALTH_INDICATOR_NAME = "iceberg";

    private CatalogFactory catalogFactory;

    /**
     * Creates a new {@link IcebergCatalogHealthIndicator} instance.
     */
    public IcebergCatalogHealthIndicator() {
    }

    /**
     * Creates a new {@link IcebergCatalogHealthIndicator} instance.
     *
     * @param catalogFactory the catalog factory.
     */
    public IcebergCatalogHealthIndicator(@NotNull final CatalogFactory catalogFactory) {
        this.catalogFactory = catalogFactory;
    }

    /** {@inheritDoc} */
    @Override
    public void init(@NotNull final ExtensionContext context) throws ConfigException {
        this.catalogFactory = context.<IcebergExtensionProvider>provider().catalogFactory();
    }

    /** {@inheritDoc} */
    @Override
    public Health getHealth(@NotNull final Duration timeout) {
        Health.Builder builder = Health.builder().name(HEALTH_INDICATOR_NAME);
        try {
            Catalog catalog = catalogFactory.createCatalog();
            builder = builder
                .details("catalog.name", catalogFactory.catalogName())
                .details("catalog.type", catalogFactory.catalogType());

            if (catalog instanceof SupportsNamespaces ns) {
                ns.listNamespaces(Namespace.empty());
            } else {
                catalog.listTables(Namespace.empty());
            }
            builder = builder.up();
        } catch (Exception e) {
            builder = builder.down().exception(e);
        }
        return builder.build();
    }
}
