/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.kafka.connect.action;

import io.streamthoughts.jikkou.core.action.ExecutionError;
import io.streamthoughts.jikkou.core.action.ExecutionResult;
import io.streamthoughts.jikkou.core.action.ExecutionResultSet;
import io.streamthoughts.jikkou.core.action.ExecutionStatus;
import io.streamthoughts.jikkou.core.config.Configuration;
import io.streamthoughts.jikkou.core.extension.DefaultExtensionContext;
import io.streamthoughts.jikkou.core.extension.DefaultExtensionDescriptorFactory;
import io.streamthoughts.jikkou.core.extension.ExtensionContext;
import io.streamthoughts.jikkou.core.extension.ExtensionDescriptor;
import io.streamthoughts.jikkou.kafka.connect.AbstractKafkaConnectorIT;
import io.streamthoughts.jikkou.kafka.connect.api.KafkaConnectClientConfig;
import io.streamthoughts.jikkou.kafka.connect.models.V1KafkaConnector;
import java.util.List;
import java.util.Map;
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
class KafkaConnectRestartConnectorsActionIT extends AbstractKafkaConnectorIT {

    private ExtensionContext context;

    @BeforeEach
    void beforeEach() throws Exception {
        deployFilestreamSinkConnectorAndWait();

        DefaultExtensionDescriptorFactory descriptorFactory = new DefaultExtensionDescriptorFactory();
        ExtensionDescriptor<KafkaConnectRestartConnectorsAction> descriptor = descriptorFactory
                .make(KafkaConnectRestartConnectorsAction.class, KafkaConnectRestartConnectorsAction::new);

        Configuration configuration = Configuration.from(Map.of(
                "kafkaConnect.clusters", List.of(Map.of(
                        KafkaConnectClientConfig.KAFKA_CONNECT_NAME.key(), KAFKA_CONNECTOR_NAME,
                        KafkaConnectClientConfig.KAFKA_CONNECT_URL.key(), getConnectUrl(),
                        KafkaConnectClientConfig.KAFKA_CONNECT_DEBUG_LOGGING_ENABLED.key(), true

                ))
        ));
        context = new DefaultExtensionContext(null, descriptor, configuration);
    }

    @Test
    void shouldSucceedToRestartAllConnectorsForAllClusters() {
        // GIVEN
        KafkaConnectRestartConnectorsAction action = new KafkaConnectRestartConnectorsAction();
        action.init(context);

        // WHEN
        ExecutionResultSet<V1KafkaConnector> resultSet = action.execute(Configuration.empty());

        // THEN
        Assertions.assertNotNull(resultSet);
        Assertions.assertEquals(1, resultSet.results().size());
        ExecutionResult<V1KafkaConnector> result = resultSet.results().get(0);
        Assertions.assertEquals(ExecutionStatus.SUCCEEDED, result.status());
        Assertions.assertNotNull(result.data());
    }

    @Test
    void shouldSuccessfullyRestartForAllClusters() {
        // GIVEN
        KafkaConnectRestartConnectorsAction action = new KafkaConnectRestartConnectorsAction();
        action.init(context);

        // WHEN
        ExecutionResultSet<V1KafkaConnector> resultSet = action.execute(Configuration.empty());

        // THEN
        Assertions.assertNotNull(resultSet);
        Assertions.assertEquals(1, resultSet.results().size());
        ExecutionResult<V1KafkaConnector> result = resultSet.results().get(0);
        Assertions.assertEquals(ExecutionStatus.SUCCEEDED, result.status());
        Assertions.assertNotNull(result.data());
    }

    @Test
    void shouldSuccessfullyRestartForSpecificConnector() {
        // GIVEN
        KafkaConnectRestartConnectorsAction action = new KafkaConnectRestartConnectorsAction();
        action.init(context);

        // WHEN
        ExecutionResultSet<V1KafkaConnector> resultSet = action.execute(Configuration.of(
                KafkaConnectRestartConnectorsAction.CONNECTOR_NAME_CONFIG, "test",
                KafkaConnectRestartConnectorsAction.INCLUDE_TASKS_CONFIG, true
        ));

        // THEN
        Assertions.assertNotNull(resultSet);
        Assertions.assertEquals(1, resultSet.results().size());
        ExecutionResult<V1KafkaConnector> result = resultSet.results().get(0);
        Assertions.assertEquals(ExecutionStatus.SUCCEEDED, result.status());
        Assertions.assertNotNull(result.data());
    }

    @Test
    void shouldFailToRestartUnknownConnector() {
        // GIVEN
        KafkaConnectRestartConnectorsAction action = new KafkaConnectRestartConnectorsAction();
        action.init(context);

        // WHEN
        ExecutionResultSet<V1KafkaConnector> resultSet = action.execute(Configuration.of(
                KafkaConnectRestartConnectorsAction.CONNECTOR_NAME_CONFIG, "dummy"
        ));

        // THEN
        Assertions.assertNotNull(resultSet);
        Assertions.assertEquals(1, resultSet.results().size());
        ExecutionResult<V1KafkaConnector> result = resultSet.results().get(0);
        Assertions.assertEquals(ExecutionStatus.FAILED, result.status());
        Assertions.assertEquals(List.of(new ExecutionError("Unknown connector: dummy", 404)), result.errors());
    }
}