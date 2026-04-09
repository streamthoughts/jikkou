/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.jikkou.iceberg;

import io.jikkou.core.health.Health;
import io.jikkou.core.health.HealthStatus;
import io.jikkou.iceberg.health.IcebergCatalogHealthIndicator;
import java.time.Duration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class IcebergCatalogHealthIndicatorIT extends AbstractIcebergIntegrationTest {

    @Test
    void shouldReportHealthyWhenCatalogIsReachable() {
        IcebergCatalogHealthIndicator indicator = new IcebergCatalogHealthIndicator(catalogFactory);

        Health health = indicator.getHealth(Duration.ofSeconds(10));

        Assertions.assertEquals(HealthStatus.UP, health.getStatus());
        Assertions.assertEquals("test-catalog", health.getDetails().get("catalog.name"));
        Assertions.assertEquals("jdbc", health.getDetails().get("catalog.type"));
    }

    @Test
    void shouldReportUnhealthyWhenCatalogIsUnreachable() {
        var badFactory = new io.jikkou.iceberg.internals.CatalogFactory(
            "bad-catalog",
            "jdbc",
            "jdbc:postgresql://localhost:19999/nonexistent",
            "/tmp/bad-warehouse",
            java.util.Map.of(
                "jdbc.user", "bad",
                "jdbc.password", "bad"
            )
        );

        IcebergCatalogHealthIndicator indicator = new IcebergCatalogHealthIndicator(badFactory);

        Health health = indicator.getHealth(Duration.ofSeconds(5));

        Assertions.assertEquals(HealthStatus.DOWN, health.getStatus());
    }
}
