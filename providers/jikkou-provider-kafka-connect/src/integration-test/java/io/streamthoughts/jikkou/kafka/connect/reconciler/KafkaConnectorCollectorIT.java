/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.kafka.connect.reconciler;

import io.streamthoughts.jikkou.core.config.Configuration;
import io.streamthoughts.jikkou.core.extension.DefaultExtensionContext;
import io.streamthoughts.jikkou.core.extension.DefaultExtensionDescriptorFactory;
import io.streamthoughts.jikkou.core.extension.ExtensionContext;
import io.streamthoughts.jikkou.core.extension.ExtensionDescriptor;
import io.streamthoughts.jikkou.core.models.ObjectMeta;
import io.streamthoughts.jikkou.core.selector.Selectors;
import io.streamthoughts.jikkou.kafka.connect.AbstractKafkaConnectorIT;
import io.streamthoughts.jikkou.kafka.connect.api.KafkaConnectClientConfig;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@Tag("integration")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class KafkaConnectorCollectorIT extends AbstractKafkaConnectorIT {

    private ExtensionContext context;

    @BeforeEach
    public void beforeEach() {
        DefaultExtensionDescriptorFactory descriptorFactory = new DefaultExtensionDescriptorFactory();
        ExtensionDescriptor<KafkaConnectorCollector> descriptor = descriptorFactory
                .make(KafkaConnectorCollector.class, KafkaConnectorCollector::new);

        Configuration configuration = Configuration.from(Map.of(
                "kafkaConnect.clusters", List.of(Map.of(
                        KafkaConnectClientConfig.KAFKA_CONNECT_NAME.key(), KAFKA_CONNECTOR_NAME,
                        KafkaConnectClientConfig.KAFKA_CONNECT_URL.key(), getConnectUrl()

                ))
        ));
        context = new DefaultExtensionContext(null, descriptor, configuration);
    }

    @Test
    void shouldThrowExceptionForInvalidClusterName() {
        // Given
        KafkaConnectorCollector collector = new KafkaConnectorCollector();
        collector.init(context);

        // When / Then
        Assertions.assertThrows(KafkaConnectClusterNotFoundException.class, () -> collector.listAll(
                Configuration.from(Map.of(
                        KafkaConnectorCollector.EXPAND_STATUS_CONFIG, false,
                        KafkaConnectorCollector.CONNECT_CLUSTER_CONFIG, "dummy"
                )),
                Selectors.NO_SELECTOR)
        );
    }

    @Test
    void shouldCollectAllConnectorsWithExpandStatusFalse() throws URISyntaxException, IOException, InterruptedException {
        // Given
        KafkaConnectorCollector collector = new KafkaConnectorCollector();
        collector.init(context);

        deployFilestreamSinkConnectorAndWait();

        // When
        List<V1KafkaConnector> results = collector.listAll(
                        Configuration.of(KafkaConnectorCollector.EXPAND_STATUS_CONFIG, false),
                        Selectors.NO_SELECTOR)
                .getItems();

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
        Assertions.assertEquals(List.of(expected), results);
    }

    @Test
    void shouldCollectAllConnectorsWithExpandStatusTrue() throws URISyntaxException, IOException, InterruptedException {
        // Given
        KafkaConnectorCollector collector = new KafkaConnectorCollector();
        collector.init(context);

        deployFilestreamSinkConnectorAndWait();

        // When
        List<V1KafkaConnector> results = collector.listAll(
                        Configuration.of(KafkaConnectorCollector.EXPAND_STATUS_CONFIG, true),
                        Selectors.NO_SELECTOR)
                .getItems();

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
                                List.of(new ConnectorStatusResponse.TaskStatus(0, "RUNNING", "kafka-connect:8083"))
                        ))
                        .build()
                )
                .build();
        Assertions.assertEquals(List.of(expected), results);
    }
}