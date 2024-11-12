/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.kafka.connect.reconciler;

import io.streamthoughts.jikkou.core.config.Configuration;
import io.streamthoughts.jikkou.core.models.ObjectMeta;
import io.streamthoughts.jikkou.core.models.ResourceList;
import io.streamthoughts.jikkou.core.selector.Selectors;
import io.streamthoughts.jikkou.kafka.connect.BaseExtensionProviderIT;
import io.streamthoughts.jikkou.kafka.connect.api.data.ConnectorStatusResponse;
import io.streamthoughts.jikkou.kafka.connect.exception.KafkaConnectClusterNotFoundException;
import io.streamthoughts.jikkou.kafka.connect.models.KafkaConnectorState;
import io.streamthoughts.jikkou.kafka.connect.models.V1KafkaConnector;
import io.streamthoughts.jikkou.kafka.connect.models.V1KafkaConnectorSpec;
import io.streamthoughts.jikkou.kafka.connect.models.V1KafkaConnectorStatus;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@Tag("integration")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class KafkaConnectorCollectorIT extends BaseExtensionProviderIT {

    @Test
    void shouldThrowExceptionForInvalidClusterName() {
        // When / Then
        Assertions.assertThrows(KafkaConnectClusterNotFoundException.class, () -> {
            api.listResources(V1KafkaConnector.class, Selectors.NO_SELECTOR, Configuration.from(Map.of(
                KafkaConnectorCollector.Config.EXPAND_STATUS.key(), false,
                KafkaConnectorCollector.Config.CONNECT_CLUSTER.key(), "dummy"
            )));
        });
    }

    @Test
    void shouldCollectAllConnectorsWithExpandStatusFalse() throws URISyntaxException, IOException, InterruptedException {
        // Given
        deployFilestreamSinkConnectorAndWait();

        // When
        ResourceList<V1KafkaConnector> resources = api.listResources(
            V1KafkaConnector.class,
            Selectors.NO_SELECTOR,
            Configuration.of(KafkaConnectorCollector.Config.EXPAND_STATUS.key(), false)
        );

        // Then
        V1KafkaConnector expected = V1KafkaConnector
            .builder()
            .withMetadata(ObjectMeta
                .builder()
                .withName(KAFKA_CONNECTOR_NAME)
                .withLabel("kafka.jikkou.io/connect-cluster", KAFKA_CONNECTOR_NAME)
                .build()
            )
            .withSpec(V1KafkaConnectorSpec
                .builder()
                .withConnectorClass("FileStreamSink")
                .withTasksMax(1)
                .withConfig(Map.of(
                    "topics", "connect-test",
                    "file", "/tmp/test.sink.txt"
                ))
                .withState(KafkaConnectorState.RUNNING)
                .build()
            )
            .build();
        Assertions.assertEquals(List.of(expected), resources.getItems());
    }

    @Test
    void shouldCollectAllConnectorsWithExpandStatusTrue() throws URISyntaxException, IOException, InterruptedException {
        // Given
        deployFilestreamSinkConnectorAndWait();

        // When
        ResourceList<V1KafkaConnector> resources = api.listResources(
            V1KafkaConnector.class,
            Selectors.NO_SELECTOR,
            Configuration.of(KafkaConnectorCollector.Config.EXPAND_STATUS.key(), true)
        );

        // Then
        V1KafkaConnector expected = V1KafkaConnector
            .builder()
            .withMetadata(ObjectMeta
                .builder()
                .withName(KAFKA_CONNECTOR_NAME)
                .withLabel("kafka.jikkou.io/connect-cluster", KAFKA_CONNECTOR_NAME)
                .build()
            )
            .withSpec(V1KafkaConnectorSpec
                .builder()
                .withConnectorClass("FileStreamSink")
                .withTasksMax(1)
                .withConfig(Map.of(
                    "topics", "connect-test",
                    "file", "/tmp/test.sink.txt"
                ))
                .withState(KafkaConnectorState.RUNNING)
                .build()
            )
            .withStatus(V1KafkaConnectorStatus
                .builder()
                .withConnectorStatus(new ConnectorStatusResponse(
                    KAFKA_CONNECTOR_NAME,
                    new ConnectorStatusResponse.ConnectorStatus("RUNNING", "kafka-connect:8083"),
                    List.of(new ConnectorStatusResponse.TaskStatus(0, "RUNNING", "kafka-connect:8083", null))
                ))
                .build()
            )
            .build();
        Assertions.assertEquals(List.of(expected), resources.getItems());
    }
}