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
package io.streamthoughts.jikkou.kafka.control;

import io.streamthoughts.jikkou.api.JikkouApi;
import io.streamthoughts.jikkou.api.ReconciliationContext;
import io.streamthoughts.jikkou.api.ReconciliationMode;
import io.streamthoughts.jikkou.api.SimpleJikkouApi;
import io.streamthoughts.jikkou.api.config.Configuration;
import io.streamthoughts.jikkou.api.control.ChangeResult;
import io.streamthoughts.jikkou.api.control.Description;
import io.streamthoughts.jikkou.api.io.ResourceDeserializer;
import io.streamthoughts.jikkou.api.io.ResourceLoader;
import io.streamthoughts.jikkou.api.model.ConfigValue;
import io.streamthoughts.jikkou.api.model.Nameable;
import io.streamthoughts.jikkou.api.model.ResourceList;
import io.streamthoughts.jikkou.api.testcontainer.RedpandaContainerConfig;
import io.streamthoughts.jikkou.api.testcontainer.RedpandaKafkaContainer;
import io.streamthoughts.jikkou.kafka.AdminClientContext;
import io.streamthoughts.jikkou.kafka.LegacyKafkaClusterResourceTypeResolver;
import io.streamthoughts.jikkou.kafka.control.change.KafkaTopicReconciliationConfig;
import io.streamthoughts.jikkou.kafka.control.change.TopicChange;
import io.streamthoughts.jikkou.kafka.models.V1KafkaTopicList;
import io.streamthoughts.jikkou.kafka.models.V1KafkaTopicObject;
import java.util.Collection;
import java.util.Map;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.admin.AdminClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@Tag("integration")
public class AdminClientKafkaTopicControllerIT {

    public static final String CLASSPATH_RESOURCE_TOPIC = "topics-test.yaml";
    public static final String ORPHAN_TOPIC = "orphan-topic";
    public static final String TOPIC_TEST_A = "topic-test-A";

    @Container
    public RedpandaKafkaContainer kafka = new RedpandaKafkaContainer(
            new RedpandaContainerConfig()
                    .withKafkaApiFixedExposedPort(9092)
                    .withAttachContainerOutputLog(true)
                    .withTransactionEnabled(false)
    );

    private volatile JikkouApi api;

    @BeforeAll
    public static void beforeAll() {
        ResourceDeserializer.registerKind(V1KafkaTopicList.class);
        ResourceDeserializer.registerResolverType(new LegacyKafkaClusterResourceTypeResolver());
    }

    @BeforeEach
    public void setUp() {
        var controller = new AdminClientKafkaTopicController(new AdminClientContext(() ->
                AdminClient.create(Map.of(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers())))
        );

        api = SimpleJikkouApi.builder()
                .withController(controller)
                .build();
    }


    @Test
    public void should_reconcile_kafka_topics_given_mode_create_only_with_default_options() {
        // Given
        ResourceList resourceList = ResourceLoader.create()
                .loadFromClasspath(CLASSPATH_RESOURCE_TOPIC);

        var resources = ResourceLoader.create()
                .loadFromClasspath(CLASSPATH_RESOURCE_TOPIC);

        var config = new KafkaTopicReconciliationConfig().asConfiguration();

        var context = ReconciliationContext.with(config, false);

        // When
        V1KafkaTopicList initialTopicList = getResource();
        Collection<ChangeResult<?>> results = api.apply(resources, ReconciliationMode.CREATE_ONLY, context);
        V1KafkaTopicList actualTopicList = getResource();

        // Then
        Assertions.assertEquals(
                0,
                initialTopicList.getSpec().getTopics().size(),
                "Invalid number of topics [before reconciliation]");
        Assertions.assertEquals(
                2, actualTopicList.getSpec().getTopics().size(),
                "Invalid number of topics [after reconciliation]");
        Assertions.assertEquals(
                2,
                results.size(),
                "Invalid number of changes");

        V1KafkaTopicList expectedTopicList = resourceList.getAllResourcesForClass(V1KafkaTopicList.class).get(0);
        Assertions.assertEquals(
                expectedTopicList.getSpec().getTopics().size(),
                actualTopicList.getSpec().getTopics().size()
        );

        Map<String, V1KafkaTopicObject> actualByTopicName = Nameable.keyByName(actualTopicList.getSpec().getTopics());
        Map<String, V1KafkaTopicObject> expectedByTopicName = Nameable.keyByName(expectedTopicList.getSpec().getTopics());

        expectedByTopicName.forEach((topicName, expected) -> {
            V1KafkaTopicObject actual = actualByTopicName.get(topicName);
            Assertions.assertEquals(expected.getPartitions(), actual.getPartitions());
            Assertions.assertEquals(expected.getReplicationFactor(), actual.getReplicationFactor());

            // Explicitly validate each config because Redpanda returns additional config properties.
            Map<String, ConfigValue> expectedConfigByName = Nameable.keyByName(expected.getConfigs());
            Map<String, ConfigValue> actualConfigByName = Nameable.keyByName(actual.getConfigs());

            expectedConfigByName.forEach((configName, expectedConfigValue) -> {
                ConfigValue actualConfigValue = actualConfigByName.get(configName);
                Assertions.assertEquals(expectedConfigValue, actualConfigValue);
            });
        });
    }

    @Test
    public void should_reconcile_kafka_topics_given_mode_delete_with_delete_orphans_true() {

        // Given
        kafka.createTopic(ORPHAN_TOPIC);

        var resources = ResourceLoader.create()
                .loadFromClasspath(CLASSPATH_RESOURCE_TOPIC);

        var config = new KafkaTopicReconciliationConfig()
                .withDeleteTopicOrphans(true)
                .asConfiguration();

        var context = ReconciliationContext.with(config, false);

        // When
        V1KafkaTopicList initialTopicList = getResource();
        Collection<ChangeResult<?>> results = api.apply(resources, ReconciliationMode.DELETE_ONLY, context);
        V1KafkaTopicList actualTopicList = getResource();

        // Then
        Assertions.assertEquals(
                1,
                initialTopicList.getSpec().getTopics().size(),
                "Invalid number of topics [before reconciliation]");
        Assertions.assertEquals(
                0, actualTopicList.getSpec().getTopics().size(),
                "Invalid number of topics [after reconciliation]");
        Assertions.assertEquals(
               1,
                results.size(),
                "Invalid number of changes");

        ChangeResult<?> change = results.iterator().next();
        Assertions.assertEquals(ChangeResult.Status.CHANGED, change.status());
        Assertions.assertEquals(Description.OperationType.DELETE, change.description().operation());
    }

    @Test
    public void should_reconcile_kafka_topics_given_mode_update_only_with_default_options() {

        // Given
        kafka.createTopic(TOPIC_TEST_A);

        var resources = ResourceLoader.create()
                .loadFromClasspath(CLASSPATH_RESOURCE_TOPIC);

        var configuration = new KafkaTopicReconciliationConfig().asConfiguration();

        var context = ReconciliationContext.with(configuration, false);

        // When
        V1KafkaTopicList initialTopicList = getResource();
        Collection<ChangeResult<?>> results = api.apply(resources, ReconciliationMode.UPDATE_ONLY, context);
        V1KafkaTopicList actualTopicList = getResource();

        // Then
        Assertions.assertEquals(
                1,
                initialTopicList.getSpec().getTopics().size(),
                "Invalid number of topics [before reconciliation]");
        Assertions.assertEquals(
                1, actualTopicList.getSpec().getTopics().size(),
                "Invalid number of topics [after reconciliation]");
        Assertions.assertEquals(
                1,
                results.size(),
                "Invalid number of changes");

        ChangeResult<?> change = results.iterator().next();
        Assertions.assertEquals(ChangeResult.Status.CHANGED, change.status(), change.toString());
        Assertions.assertEquals(TOPIC_TEST_A, ((TopicChange) change.resource()).getName());
        Assertions.assertEquals(Description.OperationType.ALTER, change.description().operation());
    }

    @Test
    public void should_reconcile_kafka_topics_given_mode_apply_with_delete_orphans_false() {

        // Given
        kafka.createTopic(ORPHAN_TOPIC);

        var resources = ResourceLoader.create()
                .loadFromClasspath(CLASSPATH_RESOURCE_TOPIC);

        var configuration = new KafkaTopicReconciliationConfig()
                .withExcludeInternalTopics(true)
                .withDeleteTopicOrphans(false)  // /!\ IMPORTANT
                .asConfiguration();

        var context = ReconciliationContext.with(configuration, false);

        // When
        V1KafkaTopicList initialTopicList = getResource();
        Collection<ChangeResult<?>> results = api.apply(resources, ReconciliationMode.APPLY_ALL, context);
        V1KafkaTopicList actualTopicList = getResource();

        // Then
        Assertions.assertEquals(
                1,
                initialTopicList.getSpec().getTopics().size(),
                "Invalid number of topics [before reconciliation]");
        Assertions.assertEquals(
                3, actualTopicList.getSpec().getTopics().size(),
                "Invalid number of topics [after reconciliation]");
        Assertions.assertEquals(
                2,
                results.size(),
                "Invalid number of changes");

        boolean delete = results.stream()
                .map(it -> it.description().operation())
                .anyMatch(it -> it.equals(Description.OperationType.DELETE));

        Assertions.assertFalse(delete);
    }

    @Test
    public void should_reconcile_kafka_topics_given_mode_apply_with_delete_orphans_true() {

        // Given
        kafka.createTopic(ORPHAN_TOPIC);

        var resources = ResourceLoader.create()
                .loadFromClasspath(CLASSPATH_RESOURCE_TOPIC);

        var configuration = new KafkaTopicReconciliationConfig()
                .withExcludeInternalTopics(true)
                .withDeleteTopicOrphans(true)
                .asConfiguration();

        var context = ReconciliationContext.with(configuration, false);

        // When
        V1KafkaTopicList initialTopicList = getResource();
        Collection<ChangeResult<?>> results = api.apply(resources, ReconciliationMode.APPLY_ALL, context);
        V1KafkaTopicList actualTopicList = getResource();

        // Then
        Assertions.assertEquals(
                1,
                initialTopicList.getSpec().getTopics().size(),
                "Invalid number of topics");
        Assertions.assertEquals(
                2, actualTopicList.getSpec().getTopics().size(),
                "Invalid number of topics");
        Assertions.assertEquals(
                3,
                results.size(),
                "Invalid number of changes");

        Assertions.assertEquals(1, initialTopicList.getSpec().getTopics().size());
        boolean delete = results.stream()
                .map(it -> it.description().operation())
                .anyMatch(it -> it.equals(Description.OperationType.DELETE));

        Assertions.assertTrue(delete);
    }

    private V1KafkaTopicList getResource() {
        Configuration configuration = new ConfigDescribeConfiguration()
                .withDescribeStaticBrokerConfigs(false)
                .withDescribeDynamicBrokerConfigs(false)
                .withDescribeDefaultConfigs(false)
                .asConfiguration();

        return api.getResource(V1KafkaTopicList.class, configuration);
    }
}