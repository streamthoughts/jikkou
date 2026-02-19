/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.kafka.reconciler;

import io.streamthoughts.jikkou.common.utils.CollectionUtils;
import io.streamthoughts.jikkou.core.ReconciliationContext;
import io.streamthoughts.jikkou.core.ReconciliationMode;
import io.streamthoughts.jikkou.core.config.Configuration;
import io.streamthoughts.jikkou.core.exceptions.JikkouRuntimeException;
import io.streamthoughts.jikkou.core.io.Jackson;
import io.streamthoughts.jikkou.core.io.ResourceLoader;
import io.streamthoughts.jikkou.core.io.reader.ResourceReaderFactory;
import io.streamthoughts.jikkou.core.models.ConfigValue;
import io.streamthoughts.jikkou.core.models.ObjectMeta;
import io.streamthoughts.jikkou.core.models.ResourceList;
import io.streamthoughts.jikkou.core.models.change.ResourceChange;
import io.streamthoughts.jikkou.core.reconciler.ChangeResult;
import io.streamthoughts.jikkou.core.reconciler.DefaultChangeResult;
import io.streamthoughts.jikkou.core.reconciler.Operation;
import io.streamthoughts.jikkou.core.selector.ExpressionOperator;
import io.streamthoughts.jikkou.core.selector.LabelSelector;
import io.streamthoughts.jikkou.core.selector.PreparedExpression;
import io.streamthoughts.jikkou.core.selector.Selectors;
import io.streamthoughts.jikkou.kafka.BaseExtensionProviderIT;
import io.streamthoughts.jikkou.kafka.collections.V1KafkaTopicList;
import io.streamthoughts.jikkou.kafka.models.V1KafkaTopic;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class AdminClientKafkaTopicControllerIT extends BaseExtensionProviderIT {

    public static final String CLASSPATH_RESOURCE_TOPICS = "test-kafka-topics.yaml";
    public static final String CLASSPATH_RESOURCE_TOPIC_ALL_DELETE = "test-kafka-topics-with-all-delete.yaml";
    public static final String CLASSPATH_RESOURCE_TOPIC_SINGLE_DELETE = "test-kafka-topics-with-single-delete.yaml";
    public static final String CLASSPATH_RESOURCE_TOPICS_WITH_LABELS = "test-kafka-topics-with-labels.yaml";
    public static final String TOPIC_TEST_A = "topic-test-A";
    public static final String TOPIC_TEST_B = "topic-test-B";
    public static final String TOPIC_TEST_C = "topic-test-C";

    private final ResourceLoader loader = new ResourceLoader(new ResourceReaderFactory(Jackson.YAML_OBJECT_MAPPER));

    @Test
    public void shouldReconcileKafkaTopicsGivenModeCreate() {
        // GIVEN
        var resources = loader
            .loadFromClasspath(CLASSPATH_RESOURCE_TOPICS);

        var context = ReconciliationContext.builder()
            .dryRun(false)
            .build();

        // WHEN
        ResourceList<V1KafkaTopic> initialTopicList = listResources();
        List<? extends ChangeResult> results = api.reconcile(resources, ReconciliationMode.CREATE, context).results();
        ResourceList<V1KafkaTopic> actualTopicList = listResources();

        // THEN
        Assertions.assertEquals(
            0,
            initialTopicList.size(),
            "Invalid number of topics [before reconciliation]");
        Assertions.assertEquals(
            2, actualTopicList.size(),
            "Invalid number of topics [after reconciliation]");
        Assertions.assertEquals(
            2,
            results.size(),
            "Invalid number of changes");

        V1KafkaTopicList expectedTopicList = resources.getAllByClass(V1KafkaTopicList.class).get(0);
        Assertions.assertEquals(
            expectedTopicList.getItems().size(),
            actualTopicList.size()
        );

        Map<String, V1KafkaTopic> actualByTopicName = actualTopicList.keyByName();
        Map<String, V1KafkaTopic> expectedByTopicName = expectedTopicList.keyByName();

        expectedByTopicName.forEach((topicName, expected) -> {
            V1KafkaTopic actual = actualByTopicName.get(topicName);
            Assertions.assertEquals(expected.getSpec().getPartitions(), actual.getSpec().getPartitions());
            Assertions.assertEquals(expected.getSpec().getReplicas(), actual.getSpec().getReplicas());

            // Explicitly validate each config because Redpanda returns additional config properties.
            Map<String, ConfigValue> expectedConfigByName = CollectionUtils.keyBy(expected.getSpec().getConfigs(), ConfigValue::name);
            Map<String, ConfigValue> actualConfigByName = CollectionUtils.keyBy(actual.getSpec().getConfigs(), ConfigValue::name);

            expectedConfigByName.forEach((configName, expectedConfigValue) -> {
                ConfigValue actualConfigValue = actualConfigByName.get(configName);
                Assertions.assertEquals(expectedConfigValue, actualConfigValue);
            });
        });
    }

    @Test
    public void shouldReconcileKafkaTopicsForModeDeleteWithDeleteAnnotations() {

        // GIVEN
        createTopic(TOPIC_TEST_A);
        createTopic(TOPIC_TEST_B);

        var resources = loader
            .loadFromClasspath(CLASSPATH_RESOURCE_TOPIC_ALL_DELETE);

        var context = ReconciliationContext.builder()
            .dryRun(false)
            .build();

        // WHEN
        ResourceList<V1KafkaTopic> initialTopicList = listResources();
        List<ChangeResult> results = api.reconcile(resources, ReconciliationMode.DELETE, context).results();
        ResourceList<V1KafkaTopic> actualTopicList = listResources();

        // THEN
        Assertions.assertEquals(
            2,
            initialTopicList.size(),
            "Invalid number of topics [before reconciliation]");
        Assertions.assertEquals(
            0, actualTopicList.size(),
            "Invalid number of topics [after reconciliation]");
        Assertions.assertEquals(
            2,
            results.size(),
            "Invalid number of changes");

        ChangeResult change = results.iterator().next();
        Assertions.assertEquals(DefaultChangeResult.Status.CHANGED, change.status());
        Assertions.assertEquals(Operation.DELETE, change.change().getSpec().getOp());
    }

    @Test
    public void shouldReconcileKafkaTopicsForModeUpdate() {

        // GIVEN
        createTopic(TOPIC_TEST_A);

        var resources = loader
            .loadFromClasspath(CLASSPATH_RESOURCE_TOPICS);

        var context = ReconciliationContext.builder()
            .dryRun(false)
            .build();

        // WHEN
        ResourceList<V1KafkaTopic> initialTopicList = listResources();
        List<ChangeResult> results = api.reconcile(resources, ReconciliationMode.UPDATE, context).results();
        ResourceList<V1KafkaTopic> actualTopicList = listResources();

        // THEN
        Assertions.assertEquals(
            1,
            initialTopicList.size(),
            "Invalid number of topics [before reconciliation]");
        Assertions.assertEquals(
            2, actualTopicList.size(),
            "Invalid number of topics [after reconciliation]");
        Assertions.assertEquals(
            2,
            results.size(),
            "Invalid number of changes");

        Map<String, ResourceChange> changeKeyedByTopicName = results.stream()
            .map(ChangeResult::change)
            .collect(Collectors.toMap(o -> o.getMetadata().getName(), o -> o));

        Assertions.assertNotNull(changeKeyedByTopicName.get(TOPIC_TEST_A));
        Assertions.assertEquals(Operation.UPDATE, changeKeyedByTopicName.get(TOPIC_TEST_A).getSpec().getOp());
        Assertions.assertNotNull(changeKeyedByTopicName.get(TOPIC_TEST_B));
        Assertions.assertEquals(Operation.CREATE, changeKeyedByTopicName.get(TOPIC_TEST_B).getSpec().getOp());
    }

    @Test
    public void shouldReconcileKafkaTopicsGivenModeApply() {

        // GIVEN
        createTopic(TOPIC_TEST_C);

        var resources = loader
            .loadFromClasspath(CLASSPATH_RESOURCE_TOPICS);

        var context = ReconciliationContext.builder()
            .dryRun(false)
            .build();

        // WHEN
        ResourceList<V1KafkaTopic> initialTopicList = listResources();
        List<ChangeResult> results = api.reconcile(resources, ReconciliationMode.FULL, context).results();
        ResourceList<V1KafkaTopic> actualTopicList = listResources();

        // THEN
        Assertions.assertEquals(
            1,
            initialTopicList.size(),
            "Invalid number of topics [before reconciliation]: " +
                topicNames(initialTopicList)
        );
        Assertions.assertEquals(
            3, actualTopicList.size(),
            "Invalid number of topics [after reconciliation]:" +
                topicNames(actualTopicList)
        );
        Assertions.assertEquals(
            2,
            results.size(),
            "Invalid number of changes");

        boolean delete = results.stream()
            .map(it -> it.change().getSpec().getOp())
            .anyMatch(it -> it.equals(Operation.DELETE));

        Assertions.assertFalse(delete);
    }

    @Test
    public void shouldReconcileKafkaTopicForModeApplyAndDeleteOrphansTrue() throws InterruptedException {

        // GIVEN
        createTopic(TOPIC_TEST_C);

        var resources = loader
            .loadFromClasspath(CLASSPATH_RESOURCE_TOPIC_SINGLE_DELETE);

        var context = ReconciliationContext.builder()
            .dryRun(false)
            .build();

        // WHEN
        ResourceList<V1KafkaTopic> initialTopicList = listResources();
        List<ChangeResult> results = api.reconcile(resources, ReconciliationMode.FULL, context).results();

        Thread.sleep(500); // Let's wait for KRaft to remove topic
        ResourceList<V1KafkaTopic> actualTopicList = listResources();

        // THEN
        Assertions.assertEquals(
            1,
            initialTopicList.size(),
            "Invalid number of topics [before reconciliation]: " +
                topicNames(initialTopicList)
        );
        Assertions.assertEquals(
            2, actualTopicList.size(),
            "Invalid number of topics [after reconciliation]: " +
                topicNames(actualTopicList)
        );
        Assertions.assertEquals(
            3,
            results.size(),
            "Invalid number of changes");

        Assertions.assertEquals(1, initialTopicList.size());
        boolean delete = results.stream()
            .map(it -> it.change().getSpec().getOp())
            .anyMatch(it -> it.equals(Operation.DELETE));

        Assertions.assertTrue(delete);
    }

    @Test
    public void shouldReconcileExistingTopicsWithLabelSelectorWithoutCreatingDuplicates() {
        // GIVEN - Create topics that already exist on the cluster
        createTopic(TOPIC_TEST_A);
        createTopic(TOPIC_TEST_B);

        // Load resources that define the same topics but with user-defined labels
        var resources = loader.loadFromClasspath(CLASSPATH_RESOURCE_TOPICS_WITH_LABELS);

        // Build a label selector: team IN (my-service)
        LabelSelector labelSelector = new LabelSelector(
                new PreparedExpression("team", ExpressionOperator.IN, List.of("my-service"))
        );

        var context = ReconciliationContext.builder()
                .selector(Selectors.allMatch(List.of(labelSelector)))
                .dryRun(false)
                .build();

        // WHEN
        List<ChangeResult> results = api.reconcile(resources, ReconciliationMode.UPDATE, context).results();

        // THEN - Both topics should be UPDATE or NONE, never CREATE
        Map<String, ResourceChange> changeByName = results.stream()
                .map(ChangeResult::change)
                .collect(Collectors.toMap(c -> c.getMetadata().getName(), c -> c));

        Assertions.assertEquals(2, changeByName.size(), "Expected changes for both topics");

        // Neither topic should be CREATE — they already exist and the selector must not filter them out
        changeByName.forEach((name, change) -> {
            Assertions.assertNotEquals(
                    Operation.CREATE,
                    change.getSpec().getOp(),
                    "Topic '" + name + "' should not be CREATE (it already exists on the cluster)"
            );
        });
    }

    @Test
    public void shouldThrowWhenLabelSelectorUsedWithDeleteOrphans() {
        // GIVEN
        var resources = loader.loadFromClasspath(CLASSPATH_RESOURCE_TOPICS_WITH_LABELS);

        LabelSelector labelSelector = new LabelSelector(
                new PreparedExpression("team", ExpressionOperator.IN, List.of("my-service"))
        );

        var context = ReconciliationContext.builder()
                .selector(Selectors.allMatch(List.of(labelSelector)))
                .configuration(Configuration.of("delete-orphans", true))
                .dryRun(false)
                .build();

        // WHEN / THEN
        Assertions.assertThrows(JikkouRuntimeException.class, () ->
                api.reconcile(resources, ReconciliationMode.FULL, context)
        );
    }

    @NotNull
    private static List<String> topicNames(ResourceList<V1KafkaTopic> items) {
        return items.stream().map(V1KafkaTopic::getMetadata).map(ObjectMeta::getName).toList();
    }

    private ResourceList<V1KafkaTopic> listResources() {
        return api.listResources(V1KafkaTopic.class, Selectors.NO_SELECTOR, Configuration.empty());
    }
}