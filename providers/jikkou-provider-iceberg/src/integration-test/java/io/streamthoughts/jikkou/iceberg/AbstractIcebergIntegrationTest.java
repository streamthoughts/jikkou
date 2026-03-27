/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.iceberg;

import io.streamthoughts.jikkou.iceberg.internals.CatalogFactory;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.TestMethodOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Base class for Iceberg integration tests using a PostgreSQL-backed JDBC catalog.
 */
@Testcontainers
@Tag("integration")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public abstract class AbstractIcebergIntegrationTest {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractIcebergIntegrationTest.class);

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine")
        .withDatabaseName("iceberg_catalog")
        .withUsername("iceberg")
        .withPassword("iceberg")
        .withLogConsumer(new Slf4jLogConsumer(LOG));

    protected CatalogFactory catalogFactory;
    private Path warehouseDir;

    @BeforeEach
    void setUpCatalog() throws IOException {
        warehouseDir = Files.createTempDirectory("iceberg-warehouse-");
        catalogFactory = new CatalogFactory(
            "test-catalog",
            "jdbc",
            POSTGRES.getJdbcUrl(),
            warehouseDir.toUri().toString(),
            Map.of(
                "jdbc.user", POSTGRES.getUsername(),
                "jdbc.password", POSTGRES.getPassword()
            )
        );
    }

    protected String warehouseLocation() {
        return warehouseDir.toUri().toString();
    }
}
