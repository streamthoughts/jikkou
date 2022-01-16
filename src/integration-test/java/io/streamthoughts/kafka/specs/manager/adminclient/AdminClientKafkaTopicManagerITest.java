/*
 * Copyright 2022 StreamThoughts.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.streamthoughts.kafka.specs.manager.adminclient;

import io.streamthoughts.kafka.specs.change.TopicChangeOptions;
import io.streamthoughts.kafka.specs.config.JikkouConfig;
import io.streamthoughts.kafka.specs.io.SpecFileLoader;
import io.streamthoughts.kafka.specs.manager.KafkaResourceManager;
import io.streamthoughts.kafka.specs.manager.KafkaResourceOperationContext;
import io.streamthoughts.kafka.specs.manager.KafkaTopicManager;
import io.streamthoughts.kafka.specs.manager.TopicDescribeOptions;
import io.streamthoughts.kafka.specs.model.V1SpecFile;
import io.streamthoughts.kafka.specs.model.V1TopicObject;
import io.streamthoughts.kafka.specs.resources.ConfigValue;
import io.streamthoughts.kafka.specs.resources.Named;
import io.streamthoughts.kafka.testcontainer.RedpandaContainerConfig;
import io.streamthoughts.kafka.testcontainer.RedpandaKafkaContainer;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

@Testcontainers
@Tag("IntegrationTest")
public class AdminClientKafkaTopicManagerITest {

    private static final Logger LOG = LoggerFactory.getLogger(AdminClientKafkaTopicManagerITest.class);

    @Container
    public RedpandaKafkaContainer kafka = new RedpandaKafkaContainer(new RedpandaContainerConfig());

    private KafkaTopicManager manager;

    @BeforeEach
    public void setUp() {
        var config = JikkouConfig
                .builder()
                .withCLIConfigParams(Map.of("adminClient.bootstrap.servers", kafka.getBootstrapServers()))
                .getOrCreate();
        manager = new AdminClientKafkaTopicManager();
        manager.configure(config);
    }


    @Test
    public void should_create_kafka_topics() {
        // Given
        InputStream topics = getTopicSpecFileInputStream();
        V1SpecFile file = SpecFileLoader.newForYaml().load(topics);

        // When
        manager.update(
                KafkaResourceManager.UpdateMode.CREATE,
                List.of(file.specs()),
                KafkaResourceOperationContext.with(new TopicChangeOptions(), false)
        );

        // Then
        List<V1TopicObject> actualTopics = manager.describe(new TopicDescribeOptions()
                .withDescribeStaticBrokerConfigs(false)
                .withDescribeDynamicBrokerConfigs(false)
                .withDescribeDefaultConfigs(false)
        );

        List<V1TopicObject> expectedTopics = file.specs().topics();
        Assertions.assertEquals(expectedTopics.size(), actualTopics.size());

        Map<String, V1TopicObject> actualByTopicName = Named.keyByName(actualTopics);
        Map<String, V1TopicObject> expectedByTopicName = Named.keyByName(expectedTopics);

        expectedByTopicName.forEach((topicName, expected) -> {
            V1TopicObject actual = actualByTopicName.get(topicName);
            Assertions.assertEquals(expected.partitions(), actual.partitions());
            Assertions.assertEquals(expected.replicationFactor(), actual.replicationFactor());

            // Explicitly validate each config because Redpanda returns additional config properties.
            Map<String, ConfigValue> expectedConfigByName = Named.keyByName(expected.configs());
            Map<String, ConfigValue> actualConfigByName = Named.keyByName(actual.configs());

            expectedConfigByName.forEach((configName, expectedConfigValue) -> {
                ConfigValue actualConfigValue = actualConfigByName.get(configName);
                Assertions.assertEquals(expectedConfigValue, actualConfigValue);
            });
        });

    }

    private @NotNull InputStream getTopicSpecFileInputStream() {
        InputStream topics = getClass().getClassLoader().getResourceAsStream("./topics-test.yaml");
        assert topics != null;
        return topics;
    }
}