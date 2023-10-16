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
package io.streamthoughts.jikkou.kafka.connect;

import static org.testcontainers.containers.wait.strategy.Wait.forHttp;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.TestMethodOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
@Tag("integration")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AbstractKafkaConnectorIT {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractKafkaConnectorIT.class);

    public static final String CONFLUENT_PLATFORM_VERSION = "7.5.0";
    private static final Network KAFKA_NETWORK = Network.newNetwork();
    public static final String KAFKA_CONNECTOR_NAME = "test";
    @Container
    final KafkaContainer kafka = new KafkaContainer(
            DockerImageName.parse("confluentinc/cp-kafka").withTag(CONFLUENT_PLATFORM_VERSION)).withKraft()
            .withNetwork(KAFKA_NETWORK)
            .withNetworkAliases("broker")
            .withEnv("KAFKA_TRANSACTION_STATE_LOG_MIN_ISR", "1")
            .withEnv("KAFKA_TRANSACTION_STATE_LOG_REPLICATION_FACTOR", "1")
            .withLogConsumer(new Slf4jLogConsumer(LOG));

    @Container
    final GenericContainer connect = new GenericContainer(
            DockerImageName.parse("confluentinc/cp-kafka-connect").withTag(CONFLUENT_PLATFORM_VERSION))
            .withEnv("CONNECT_BOOTSTRAP_SERVERS", "PLAINTEXT://broker:9092")
            .withEnv("CONNECT_REST_PORT", "8083")
            .withEnv("CONNECT_GROUP_ID", "kafka-connect")
            .withEnv("CONNECT_CONFIG_STORAGE_TOPIC", "_connect-configs")
            .withEnv("CONNECT_OFFSET_STORAGE_TOPIC", "_connect-offsets")
            .withEnv("CONNECT_STATUS_STORAGE_TOPIC", "_connect-status")
            .withEnv("CONNECT_KEY_CONVERTER", "org.apache.kafka.connect.storage.StringConverter")
            .withEnv("CONNECT_VALUE_CONVERTER", "org.apache.kafka.connect.storage.StringConverter")
            .withEnv("CONNECT_REST_ADVERTISED_HOST_NAME", "kafka-connect")
            .withEnv("CONNECT_CONFIG_STORAGE_REPLICATION_FACTOR", "1")
            .withEnv("CONNECT_OFFSET_STORAGE_REPLICATION_FACTOR", "1")
            .withEnv("CONNECT_STATUS_STORAGE_REPLICATION_FACTOR", "1")
            .withEnv("CONNECT_PLUGIN_PATH", "/usr/local/share/kafka/plugins,/usr/share/filestream-connectors")
            .withExposedPorts(8083)
            .withNetwork(KAFKA_NETWORK)
            .withNetworkAliases("kafka-connect")
            .dependsOn(kafka)
            .withLogConsumer(new Slf4jLogConsumer(LOG))
            .waitingFor(forHttp("/connector-plugins"));

    @NotNull
    protected String getConnectUrl() {
        return "http://localhost:" + connect.getFirstMappedPort();
    }


    protected void deployFilestreamSinkConnectorAndWait() throws URISyntaxException, IOException, InterruptedException {
        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest createOrUpdateConnectorConfig = HttpRequest.newBuilder()
                .uri(new URI(getConnectUrl() + "/connectors/test/config"))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString("""
                         {
                         "tasks.max":1,
                         "connector.class":"FileStreamSink",
                         "file": "/tmp/test.sink.txt",
                         "topics": "connect-test"
                        }
                         """))
                .build();
        HttpResponse<String> response = httpClient.send(createOrUpdateConnectorConfig, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 201) {
            HttpRequest getConnectors = HttpRequest.newBuilder()
                    .uri(new URI(getConnectUrl() + "/connectors"))
                    .header("Content-Type", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> connectors;
            do {
                connectors = httpClient.send(getConnectors, HttpResponse.BodyHandlers.ofString());
                Thread.sleep(100);
            } while (connectors.statusCode() == 200 && connectors.body().equals("[]"));
        }
        Thread.sleep(1000); // make sure tasks are running
    }
}
