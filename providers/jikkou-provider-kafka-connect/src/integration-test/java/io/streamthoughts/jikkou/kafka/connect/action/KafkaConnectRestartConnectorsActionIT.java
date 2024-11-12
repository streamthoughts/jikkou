/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.kafka.connect.action;

import io.streamthoughts.jikkou.core.action.ExecutionError;
import io.streamthoughts.jikkou.core.action.ExecutionResult;
import io.streamthoughts.jikkou.core.action.ExecutionStatus;
import io.streamthoughts.jikkou.core.config.Configuration;
import io.streamthoughts.jikkou.core.extension.ExtensionContext;
import io.streamthoughts.jikkou.core.models.ApiActionResultSet;
import io.streamthoughts.jikkou.kafka.connect.BaseExtensionProviderIT;
import io.streamthoughts.jikkou.kafka.connect.models.V1KafkaConnector;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@Tag("integration")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class KafkaConnectRestartConnectorsActionIT extends BaseExtensionProviderIT {

    private ExtensionContext context;

    @BeforeEach
    void beforeEach() throws Exception {
        deployFilestreamSinkConnectorAndWait();
    }

    @Test
    void shouldSucceedToRestartAllConnectorsForAllClusters() {
        // WHEN
        ApiActionResultSet<V1KafkaConnector> resultSet = api.execute(KafkaConnectRestartConnectorsAction.NAME, Configuration.empty());

        // THEN
        Assertions.assertNotNull(resultSet);
        Assertions.assertEquals(1, resultSet.results().size());
        ExecutionResult<V1KafkaConnector> result = resultSet.results().getFirst();
        Assertions.assertEquals(ExecutionStatus.SUCCEEDED, result.status());
        Assertions.assertNotNull(result.data());
    }

    @Test
    void shouldSuccessfullyRestartForSpecificConnector() {
        // WHEN
        ApiActionResultSet<V1KafkaConnector> resultSet = api.execute(
            KafkaConnectRestartConnectorsAction.NAME,
            Configuration.of(
                KafkaConnectRestartConnectorsAction.Config.CONNECTOR_NAME.key(), "test",
                KafkaConnectRestartConnectorsAction.Config.INCLUDE_TASKS.key(), true
            ));

        // THEN
        Assertions.assertNotNull(resultSet);
        Assertions.assertEquals(1, resultSet.results().size());
        ExecutionResult<V1KafkaConnector> result = resultSet.results().getFirst();
        Assertions.assertEquals(ExecutionStatus.SUCCEEDED, result.status());
        Assertions.assertNotNull(result.data());
    }

    @Test
    void shouldFailToRestartUnknownConnector() {
        // WHEN
        ApiActionResultSet<V1KafkaConnector> resultSet = api.execute(
            KafkaConnectRestartConnectorsAction.NAME,
            Configuration.of(
                KafkaConnectRestartConnectorsAction.Config.CONNECTOR_NAME.key(), "dummy",
                KafkaConnectRestartConnectorsAction.Config.INCLUDE_TASKS.key(), true
            ));

        // THEN
        Assertions.assertNotNull(resultSet);
        Assertions.assertEquals(1, resultSet.results().size());
        ExecutionResult<V1KafkaConnector> result = resultSet.results().getFirst();
        Assertions.assertEquals(ExecutionStatus.FAILED, result.status());
        Assertions.assertEquals(List.of(new ExecutionError("Unknown connector: dummy", 404)), result.errors());
    }
}