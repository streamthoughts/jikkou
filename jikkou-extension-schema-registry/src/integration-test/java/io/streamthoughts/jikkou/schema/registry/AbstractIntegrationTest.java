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
package io.streamthoughts.jikkou.schema.registry;

import io.streamthoughts.jikkou.rest.client.RestClientBuilder;
import io.streamthoughts.jikkou.schema.registry.api.AsyncSchemaRegistryApi;
import io.streamthoughts.jikkou.schema.registry.api.SchemaRegistryApi;
import io.streamthoughts.jikkou.schema.registry.api.SchemaRegistryClientConfig;
import io.streamthoughts.jikkou.schema.registry.api.SchemaRegistryContainer;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.TestMethodOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
@Tag("integration")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AbstractIntegrationTest {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractIntegrationTest.class);

    public static final String TEST_SUBJECT = "test";

    public static final String AVRO_SCHEMA = """
            {
              "namespace": "example.avro",
              "type": "record",
              "name": "User",
              "fields": [
                 {"name": "name", "type": "string"},
                 {"name": "favorite_number",  "type": ["int", "null"]},
                 {"name": "favorite_color", "type": ["string", "null"]}
              ]
            }
            """;

    private static final Network KAFKA_NETWORK = Network.newNetwork();

    public static final String CONFLUENT_PLATFORM_VERSION = "7.4.0";
    @Container
    final KafkaContainer kafkaContainer = new KafkaContainer(
            DockerImageName.parse("confluentinc/cp-kafka").withTag(CONFLUENT_PLATFORM_VERSION)).withKraft()
            .withNetwork(KAFKA_NETWORK)
            .withEnv("KAFKA_TRANSACTION_STATE_LOG_MIN_ISR", "1")
            .withEnv("KAFKA_TRANSACTION_STATE_LOG_REPLICATION_FACTOR", "1")
            .withLogConsumer(new Slf4jLogConsumer(LOG));

    @Container
    final SchemaRegistryContainer schemaRegistry =
            new SchemaRegistryContainer(CONFLUENT_PLATFORM_VERSION)
                    .withNetwork(KAFKA_NETWORK)
                    .withKafka(kafkaContainer)
                    .withLogConsumer(new Slf4jLogConsumer(LOG))
                    .dependsOn(kafkaContainer);

    public AsyncSchemaRegistryApi getAsyncSchemaRegistryApi() {
        SchemaRegistryApi api = RestClientBuilder
                .newBuilder()
                .baseUri(schemaRegistry.getSchemaRegistryUrl())
                .build(SchemaRegistryApi.class);
        return new AsyncSchemaRegistryApi(api);
    }

    public SchemaRegistryClientConfig getSchemaRegistryClientConfiguration() {
        return new SchemaRegistryClientConfig(
                SchemaRegistryClientConfig.SCHEMA_REGISTRY_URL.asConfiguration(schemaRegistry.getSchemaRegistryUrl())
        );
    }
}
