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
package io.streamthoughts.jikkou.kafka;

import io.streamthoughts.jikkou.kafka.internals.KafkaBrokersReady;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.CreateTopicsOptions;
import org.apache.kafka.clients.admin.NewTopic;
import org.junit.jupiter.api.BeforeEach;
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
public class AbstractKafkaIntegrationTest {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractKafkaIntegrationTest.class);
    private static final Network KAFKA_NETWORK = Network.newNetwork();

    public static final String CONFLUENT_PLATFORM_VERSION = "7.4.0";
    public static final int DEFAULT_NUM_PARTITIONS = 1;
    public static final short DEFAULT_REPLICATION_FACTOR = (short) 1;

    @Container
    final KafkaContainer kafka = new KafkaContainer(
            DockerImageName.parse("confluentinc/cp-kafka").withTag(CONFLUENT_PLATFORM_VERSION)).withKraft()
            .withNetwork(KAFKA_NETWORK)
            .withEnv("KAFKA_TRANSACTION_STATE_LOG_MIN_ISR", "1")
            .withEnv("KAFKA_TRANSACTION_STATE_LOG_REPLICATION_FACTOR", "1")
            .withLogConsumer(new Slf4jLogConsumer(LOG));


    @BeforeEach
    public void beforeEach() {
        new KafkaBrokersReady().waitForBrokers(AdminClient.create(clientConfig()));
    }

    public String getBootstrapServers() {
        return kafka.getBootstrapServers();
    }

    public int getBrokerPort() {
        return kafka.getFirstMappedPort();
    }

    public String getBrokerHost() {
        return kafka.getHost();
    }

    public Map<String, Object> clientConfig() {
        return Map.of(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, getBootstrapServers());
    }

    public void createTopic(final String topicName) {
        createTopic(topicName, Collections.emptyMap());
    }

    public void createTopic(final String topicName, final Map<String, String> config) {
        try(var client = AdminClient.create(clientConfig())) {
            try {
                client.createTopics(List.of(
                        new NewTopic(topicName, DEFAULT_NUM_PARTITIONS, DEFAULT_REPLICATION_FACTOR).configs(config)),
                        new CreateTopicsOptions()
                ).all().get();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
