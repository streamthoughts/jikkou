/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.kafka.change.topic;

import static io.streamthoughts.jikkou.core.reconciler.Operation.CREATE;
import static io.streamthoughts.jikkou.core.reconciler.Operation.DELETE;
import static io.streamthoughts.jikkou.core.reconciler.Operation.NONE;
import static io.streamthoughts.jikkou.core.reconciler.Operation.UPDATE;

import io.streamthoughts.jikkou.core.models.ConfigValue;
import io.streamthoughts.jikkou.core.models.Configs;
import io.streamthoughts.jikkou.core.models.CoreAnnotations;
import io.streamthoughts.jikkou.core.models.ObjectMeta;
import io.streamthoughts.jikkou.core.models.change.GenericResourceChange;
import io.streamthoughts.jikkou.core.models.change.ResourceChange;
import io.streamthoughts.jikkou.core.models.change.ResourceChangeSpec;
import io.streamthoughts.jikkou.core.models.change.StateChange;
import io.streamthoughts.jikkou.core.reconciler.Operation;
import io.streamthoughts.jikkou.kafka.adapters.KafkaConfigsAdapter;
import io.streamthoughts.jikkou.kafka.change.topics.TopicChange;
import io.streamthoughts.jikkou.kafka.change.topics.TopicChangeComputer;
import io.streamthoughts.jikkou.kafka.internals.KafkaTopics;
import io.streamthoughts.jikkou.kafka.models.V1KafkaTopic;
import io.streamthoughts.jikkou.kafka.models.V1KafkaTopicSpec;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.kafka.clients.admin.ConfigEntry;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class TopicChangeComputerTest {

    static final String CONFIG_PROP = "config.prop";
    static final String TEST_TOPIC = "Test";
    static final String ANY_VALUE = "???";

    @Test
    void shouldNotReturnDeleteChangesForTopicDeleteFalse() {
        // GIVEN
        var topic = V1KafkaTopic.builder()
                .withMetadata(ObjectMeta.builder()
                        .withName(TEST_TOPIC)
                        .withAnnotation(CoreAnnotations.JIKKOU_IO_DELETE, false)
                        .build()
                )
                .withSpec(V1KafkaTopicSpec
                        .builder()
                        .withPartitions(1)
                        .withReplicas((short) 1)
                        .withConfigs(Configs.empty())
                        .build())
                .build();

        List<V1KafkaTopic> actualState = List.of(topic);
        List<V1KafkaTopic> expectedState = Collections.emptyList();

        // WHEN
        var changes = new TopicChangeComputer()
                .computeChanges(actualState, expectedState)
                .stream()
                .collect(Collectors.toMap(change -> change.getMetadata().getName(), it -> it));

        // THEN
        Assertions.assertTrue(changes.isEmpty());
    }

    @Test
    void shouldReturnDeleteChangesForExistingTopicDeleteTrue() {

        // GIVEN
        V1KafkaTopic resource = V1KafkaTopic.builder()
                .withMetadata(ObjectMeta.builder()
                        .withName(TEST_TOPIC)
                        .withAnnotation(CoreAnnotations.JIKKOU_IO_DELETE, true)
                        .build()
                )
                .withSpec(V1KafkaTopicSpec
                        .builder()
                        .withPartitions(1)
                        .withReplicas((short) 1)
                        .withConfigs(Configs.empty())
                        .build())
                .build();

        // WHEN
        List<ResourceChange> changes = new TopicChangeComputer().computeChanges(List.of(resource), List.of(resource));

        // THEN
        ResourceChange expected = GenericResourceChange
                .builder(V1KafkaTopic.class)
                .withMetadata(resource.getMetadata())
                .withSpec(ResourceChangeSpec
                        .builder()
                        .withOperation(DELETE)
                        .withChange(StateChange.delete(TopicChange.PARTITIONS, 1))
                        .withChange(StateChange.delete(TopicChange.REPLICAS, (short)1))
                        .build()
                )
                .build();
        Assertions.assertEquals(List.of(expected), changes);
    }

    @Test
    void shouldReturnNoChangesForNotExistingTopicDeleteTrue() {
        // GIVEN
        var topic = V1KafkaTopic.builder()
                .withMetadata(ObjectMeta.builder()
                        .withName(TEST_TOPIC)
                        .withAnnotation(CoreAnnotations.JIKKOU_IO_DELETE, true)
                        .build()
                )
                .withSpec(V1KafkaTopicSpec
                        .builder()
                        .withPartitions(1)
                        .withReplicas((short) 1)
                        .withConfigs(Configs.empty())
                        .build())
                .build();

        List<V1KafkaTopic> actualState = List.of();
        List<V1KafkaTopic> expectedState = List.of(topic);

        // WHEN
        var changes = new TopicChangeComputer()
                .computeChanges(actualState, expectedState)
                .stream()
                .collect(Collectors.toMap(change -> change.getMetadata().getName(), it -> it));

        // THEN
        ResourceChange change = changes.get(TEST_TOPIC);
        Assertions.assertNull(change);
    }

    @Test
    void shouldReturnChangesWhenTopicDoesNotExist() {
        // GIVEN
        V1KafkaTopic resource = V1KafkaTopic.builder()
                .withMetadata(ObjectMeta.builder()
                        .withName(TEST_TOPIC)
                        .build()
                )
                .withSpec(V1KafkaTopicSpec
                        .builder()
                        .withPartitions(1)
                        .withReplicas((short) 1)
                        .withConfigs(Configs.empty())
                        .build())
                .build();
        List<V1KafkaTopic> actualState = Collections.emptyList();
        List<V1KafkaTopic> expectedState = List.of(resource);

        // WHEN
        List<ResourceChange> changes = new TopicChangeComputer().computeChanges(actualState, expectedState);

        // THEN
        Assertions.assertEquals(List.of(GenericResourceChange
                        .builder(V1KafkaTopic.class)
                        .withMetadata(resource.getMetadata())
                        .withSpec(ResourceChangeSpec
                                .builder()
                                .withOperation(CREATE)
                                .withChange(StateChange.create("partitions", 1))
                                .withChange(StateChange.create("replicas", (short) 1))
                                .build()
                        )
                        .build()
                ),
                changes
        );
    }

    @Test
    void shouldReturnChangesForTopicWithUpdatedConfigEntry() {

        // GIVEN
        Configs actualConfig = Configs.empty();
        actualConfig.add(new ConfigValue(CONFIG_PROP, "actual-value"));

        V1KafkaTopic before = V1KafkaTopic.builder()
                .withMetadata(ObjectMeta.builder()
                        .withName(TEST_TOPIC)
                        .build()
                )
                .withSpec(V1KafkaTopicSpec
                        .builder()
                        .withPartitions(1)
                        .withReplicas((short) 1)
                        .withConfigs(actualConfig)
                        .build())
                .build();
        List<V1KafkaTopic> actualState = List.of(before);

        Configs expectedConfig = Configs.empty();
        expectedConfig.add(new ConfigValue(CONFIG_PROP, "expected-value"));

        var topicAfter = V1KafkaTopic.builder()
                .withMetadata(before.getMetadata())
                .withSpec(V1KafkaTopicSpec
                        .builder()
                        .withPartitions(1)
                        .withReplicas((short) 1)
                        .withConfigs(expectedConfig)
                        .build())
                .build();

        List<V1KafkaTopic> expectedState = List.of(topicAfter);

        // WHEN
        var changes = new TopicChangeComputer()
                .computeChanges(actualState, expectedState)
                .stream()
                .collect(Collectors.toMap(change -> change.getMetadata().getName(), it -> it));

        // THEN
        ResourceChange change = changes.get(TEST_TOPIC);
        Assertions.assertEquals(UPDATE, change.getSpec().getOp());
    }

    @Test
    void shouldReturnUpdateChangesForTopicWithNewConfigEntry() {

        // GIVEN
        V1KafkaTopic before = V1KafkaTopic.builder()
                .withMetadata(ObjectMeta.builder()
                        .withName(TEST_TOPIC)
                        .build()
                )
                .withSpec(V1KafkaTopicSpec
                        .builder()
                        .withPartitions(1)
                        .withReplicas((short) 1)
                        .withConfigs(Configs.empty())
                        .build())
                .build();

        Configs expectedConfig = Configs.empty();
        expectedConfig.add(new ConfigValue(CONFIG_PROP, ANY_VALUE));
        V1KafkaTopic after = V1KafkaTopic.builder()
                .withMetadata(ObjectMeta.builder()
                        .withName(TEST_TOPIC)
                        .build()
                )
                .withSpec(V1KafkaTopicSpec
                        .builder()
                        .withPartitions(1)
                        .withReplicas((short) 1)
                        .withConfigs(expectedConfig)
                        .build())
                .build();

        // WHEN
        List<ResourceChange> changes = new TopicChangeComputer().computeChanges(List.of(before), List.of(after));

        // THEN
        ResourceChange expected = GenericResourceChange
                .builder(V1KafkaTopic.class)
                .withMetadata(after.getMetadata())
                .withSpec(ResourceChangeSpec
                        .builder()
                        .withOperation(UPDATE)
                        .withChange(StateChange.none(TopicChange.PARTITIONS, 1))
                        .withChange(StateChange.none(TopicChange.REPLICAS, (short)1))
                        .withChange(StateChange.create(TopicChange.CONFIG_PREFIX + CONFIG_PROP, ANY_VALUE))
                        .build()
                )
                .build();
        Assertions.assertEquals(List.of(expected), changes);
    }

    @Test
    void shouldReturnNoneChangesForEqualTopics() {

        // GIVEN
        Configs configs = Configs.empty();
        configs.add(new ConfigValue(CONFIG_PROP, ANY_VALUE));

        V1KafkaTopic resource = V1KafkaTopic.builder()
                .withMetadata(ObjectMeta.builder()
                        .withName(TEST_TOPIC)
                        .build()
                )
                .withSpec(V1KafkaTopicSpec
                        .builder()
                        .withPartitions(1)
                        .withReplicas((short) 1)
                        .withConfigs(configs)
                        .build())
                .build();

        // WHEN
        List<ResourceChange> changes = new TopicChangeComputer().computeChanges(List.of(resource), List.of(resource));

        // THEN
        ResourceChange expected = GenericResourceChange
                .builder(V1KafkaTopic.class)
                .withMetadata(resource.getMetadata())
                .withSpec(ResourceChangeSpec
                        .builder()
                        .withOperation(Operation.NONE)
                        .withChange(StateChange.none(TopicChange.PARTITIONS, 1))
                        .withChange(StateChange.none(TopicChange.REPLICAS, (short)1))
                        .withChange(StateChange.none(TopicChange.CONFIG_PREFIX + CONFIG_PROP, ANY_VALUE))
                        .build()
                )
                .build();
        Assertions.assertEquals(List.of(expected), changes);
    }

    @Test
    void shouldReturnUpdateChangesForConfigDeleteOrphansTrue() {
        // GIVEN
        Configs configsBefore = Configs.empty();
        configsBefore.add(new ConfigValue(CONFIG_PROP, ANY_VALUE, true, true));
        V1KafkaTopic before = V1KafkaTopic.builder()
                .withMetadata(ObjectMeta.builder()
                        .withName(TEST_TOPIC)
                        .build()
                )
                .withSpec(V1KafkaTopicSpec
                        .builder()
                        .withPartitions(1)
                        .withReplicas((short) 1)
                        .withConfigs(configsBefore)
                        .build())
                .build();

        V1KafkaTopic after = V1KafkaTopic.builder()
                .withMetadata(ObjectMeta.builder()
                        .withName(TEST_TOPIC)
                        .build()
                )
                .withSpec(V1KafkaTopicSpec
                        .builder()
                        .withPartitions(1)
                        .withReplicas((short) 1)
                        .withConfigs(Configs.empty())
                        .build())
                .build();

        TopicChangeComputer changeComputer = new TopicChangeComputer(true);

        // WHEN
        List<ResourceChange> changes = changeComputer.computeChanges(List.of(before), List.of(after));

        // THEN
        ResourceChange expected = GenericResourceChange
                .builder(V1KafkaTopic.class)
                .withMetadata(after.getMetadata())
                .withSpec(ResourceChangeSpec
                        .builder()
                        .withOperation(UPDATE)
                        .withChange(StateChange.none(TopicChange.PARTITIONS, 1))
                        .withChange(StateChange.none(TopicChange.REPLICAS, (short)1))
                        .withChange(StateChange.delete(TopicChange.CONFIG_PREFIX + CONFIG_PROP, ANY_VALUE))
                        .build()
                )
                .build();
        Assertions.assertEquals(List.of(expected), changes);
    }

    @Test
    void shouldReturnNoneChangesForConfigDeleteOrphansFalse() {
        // GIVEN
        Configs configsBefore = Configs.empty();

        ConfigEntry mkConfigEntry = Mockito.mock(ConfigEntry.class);
        Mockito.when(mkConfigEntry.name()).thenReturn(CONFIG_PROP);
        Mockito.when(mkConfigEntry.value()).thenReturn(ANY_VALUE);
        Mockito.when(mkConfigEntry.source()).thenReturn(ConfigEntry.ConfigSource.DYNAMIC_BROKER_CONFIG);

        configsBefore.add(KafkaConfigsAdapter.of(mkConfigEntry));
        V1KafkaTopic before = V1KafkaTopic.builder()
                .withMetadata(ObjectMeta.builder()
                        .withName(TEST_TOPIC)
                        .build()
                )
                .withSpec(V1KafkaTopicSpec
                        .builder()
                        .withPartitions(1)
                        .withReplicas((short) 1)
                        .withConfigs(configsBefore)
                        .build())
                .build();

        Configs configsAfter = Configs.empty();
        V1KafkaTopic after = V1KafkaTopic.builder()
                .withMetadata(ObjectMeta.builder()
                        .withName(TEST_TOPIC)
                        .build()
                )
                .withSpec(V1KafkaTopicSpec
                        .builder()
                        .withPartitions(1)
                        .withReplicas((short) 1)
                        .withConfigs(configsAfter)
                        .build())
                .build();

        TopicChangeComputer changeComputer = new TopicChangeComputer(false);

        // WHEN
        List<ResourceChange> changes = changeComputer.computeChanges(List.of(before), List.of(after));

        // THEN
        ResourceChange expected = GenericResourceChange
                .builder(V1KafkaTopic.class)
                .withMetadata(after.getMetadata())
                .withSpec(ResourceChangeSpec
                        .builder()
                        .withOperation(NONE)
                        .withChange(StateChange.none(TopicChange.PARTITIONS,1))
                        .withChange(StateChange.none(TopicChange.REPLICAS, (short)1))
                        .build()
                )
                .build();
        Assertions.assertEquals(List.of(expected), changes);
    }

    @Test
    void shouldReturnNoneChangesForTopicWithDefaultPartitions() {
        // GIVEN
        V1KafkaTopic before = V1KafkaTopic.builder()
                .withMetadata(ObjectMeta.builder()
                        .withName(TEST_TOPIC)
                        .build()
                )
                .withSpec(V1KafkaTopicSpec
                        .builder()
                        .withPartitions(1)
                        .withReplicas((short) 1)
                        .build())
                .build();

        V1KafkaTopic after = V1KafkaTopic.builder()
                .withMetadata(ObjectMeta.builder()
                        .withName(TEST_TOPIC)
                        .build()
                )
                .withSpec(V1KafkaTopicSpec
                        .builder()
                        .withPartitions(KafkaTopics.NO_NUM_PARTITIONS)
                        .withReplicas((short) 1)
                        .build())
                .build();

        TopicChangeComputer changeComputer = new TopicChangeComputer(false);

        // WHEN
        List<ResourceChange> changes = changeComputer.computeChanges(List.of(before), List.of(after));

        // THEN
        ResourceChange expected = GenericResourceChange
                .builder(V1KafkaTopic.class)
                .withMetadata(after.getMetadata())
                .withSpec(ResourceChangeSpec
                        .builder()
                        .withOperation(NONE)
                        .withChange(StateChange.none(TopicChange.PARTITIONS, KafkaTopics.NO_NUM_PARTITIONS))
                        .withChange(StateChange.none(TopicChange.REPLICAS, (short)1))
                        .build()
                )
                .build();
        Assertions.assertEquals(List.of(expected), changes);
    }

    @Test
    void shouldReturnNoneChangesForTopicWithDefaultReplication() {
        // GIVEN
        V1KafkaTopic before = V1KafkaTopic.builder()
                .withMetadata(ObjectMeta.builder()
                        .withName(TEST_TOPIC)
                        .build()
                )
                .withSpec(V1KafkaTopicSpec
                        .builder()
                        .withPartitions(1)
                        .withReplicas((short) 1)
                        .build())
                .build();

        V1KafkaTopic after = V1KafkaTopic.builder()
                .withMetadata(ObjectMeta.builder()
                        .withName(TEST_TOPIC)
                        .build()
                )
                .withSpec(V1KafkaTopicSpec
                        .builder()
                        .withPartitions(1)
                        .withReplicas(KafkaTopics.NO_REPLICATION_FACTOR)
                        .build())
                .build();

        TopicChangeComputer changeComputer = new TopicChangeComputer(false);

        // WHEN
        List<ResourceChange> changes = changeComputer.computeChanges(List.of(before), List.of(after));

        // THEN
        ResourceChange expected = GenericResourceChange
                .builder(V1KafkaTopic.class)
                .withMetadata(after.getMetadata())
                .withSpec(ResourceChangeSpec
                        .builder()
                        .withOperation(NONE)
                        .withChange(StateChange.none(TopicChange.PARTITIONS, 1))
                        .withChange(StateChange.none(TopicChange.REPLICAS, KafkaTopics.NO_REPLICATION_FACTOR))
                        .build()
                )
                .build();
        Assertions.assertEquals(List.of(expected), changes);
    }
}