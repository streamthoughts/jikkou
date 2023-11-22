/*
 * Copyright 2023 The original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.streamthoughts.jikkou.kafka.connect.api;

import io.streamthoughts.jikkou.core.config.Configuration;
import io.streamthoughts.jikkou.kafka.connect.AbstractKafkaConnectorIT;
import io.streamthoughts.jikkou.kafka.connect.api.data.ConnectCluster;
import io.streamthoughts.jikkou.kafka.connect.api.data.ConnectorStatusResponse;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
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
class KafkaConnectApiTest extends AbstractKafkaConnectorIT {

    public static final Map<String, Object> CONNECTOR_CONFIG = Map.of(
            "name", "test",
            "connector.class", "FileStreamSink",
            "tasks.max", "1",
            "topics", "connect-test",
            "file", "/tmp/test.sink.txt"
    );
    KafkaConnectApi api;

    @BeforeEach
    void beforeEach() {
        KafkaConnectClientConfig configuration = new KafkaConnectClientConfig(Configuration.builder()
                .with("name", KAFKA_CONNECTOR_NAME)
                .with(KafkaConnectClientConfig.KAFKA_CONNECT_URL.key(), getConnectUrl())
                .build());
        this.api = KafkaConnectApiFactory.create(configuration);
    }

    @Test
    void shouldGetConnectCluster() {
        ConnectCluster cluster = api.getConnectCluster();
        Assertions.assertNotNull(cluster.kafkaClusterId());
        Assertions.assertNotNull(cluster.version());
        Assertions.assertNotNull(cluster.commit());
    }

    @Test
    void shouldListConnectors() throws Exception {
        deployFilestreamSinkConnectorAndWait();
        List<String> connectors = api.listConnectors();
        Assertions.assertEquals(List.of("test"), connectors);
    }

    @Test
    void shouldGetConnectorStatus() throws Exception {
        deployFilestreamSinkConnectorAndWait();
        ConnectorStatusResponse response = api.getConnectorStatus("test");

        ConnectorStatusResponse expected = new ConnectorStatusResponse(
                KAFKA_CONNECTOR_NAME,
                new ConnectorStatusResponse.ConnectorStatus("RUNNING", "kafka-connect:8083"),
                List.of(new ConnectorStatusResponse.TaskStatus(0, "RUNNING", "kafka-connect:8083"))
        );
        Assertions.assertEquals(expected, response);
    }

    @Test
    void shouldGetConnectorConfig() throws Exception {
        deployFilestreamSinkConnectorAndWait();
        Map<String, Object> config = api.getConnectorConfig("test");
        Assertions.assertEquals(
                new TreeMap<>(CONNECTOR_CONFIG),
                new TreeMap<>(config)
        );
    }

    @Test
    void shouldCreateOrUpdateConnector() {
        Assertions.assertNotNull(api.createOrUpdateConnector("test", CONNECTOR_CONFIG));
    }
}
