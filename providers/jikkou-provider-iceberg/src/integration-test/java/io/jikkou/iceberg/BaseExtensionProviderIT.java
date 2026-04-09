/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.jikkou.iceberg;

import io.jikkou.core.CoreExtensionProvider;
import io.jikkou.core.JikkouApi;
import io.jikkou.core.config.Configuration;
import io.jikkou.runtime.JikkouContext;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;

/**
 * Base integration test that sets up a full {@link JikkouApi} with the Iceberg provider
 * configured to use a PostgreSQL-backed JDBC catalog.
 */
public class BaseExtensionProviderIT extends AbstractIcebergIntegrationTest {

    protected JikkouApi api;

    @BeforeEach
    void initApi() {
        Configuration icebergConfig = Configuration.from(Map.of(
            "catalogName", "test-catalog",
            "catalogType", "jdbc",
            "catalogUri", POSTGRES.getJdbcUrl(),
            "warehouse", warehouseLocation(),
            "catalogProperties", Map.of(
                "jdbc.user", POSTGRES.getUsername(),
                "jdbc.password", POSTGRES.getPassword()
            )
        ));

        api = JikkouContext.defaultContext()
            .newApiBuilder()
            .register(new CoreExtensionProvider())
            .register(new IcebergExtensionProvider(), icebergConfig)
            .build()
            .enableBuiltInAnnotations(false);
    }
}
