/*
 * Copyright 2021 StreamThoughts.
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
package io.streamthoughts.jikkou.kafka.control.change;

import static io.streamthoughts.jikkou.api.control.ChangeType.ADD;
import static io.streamthoughts.jikkou.api.control.ChangeType.DELETE;
import static io.streamthoughts.jikkou.api.control.ChangeType.NONE;
import static io.streamthoughts.jikkou.api.control.ChangeType.UPDATE;

import io.streamthoughts.jikkou.JikkouMetadataAnnotations;
import io.streamthoughts.jikkou.api.model.ConfigValue;
import io.streamthoughts.jikkou.api.model.Configs;
import io.streamthoughts.jikkou.api.model.ObjectMeta;
import io.streamthoughts.jikkou.kafka.adapters.KafkaConfigsAdapter;
import io.streamthoughts.jikkou.kafka.internals.KafkaTopics;
import io.streamthoughts.jikkou.kafka.models.V1KafkaTopic;
import io.streamthoughts.jikkou.kafka.models.V1KafkaTopicSpec;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.kafka.clients.admin.ConfigEntry;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class TopicChangeComputerTest {

    public static final String CONFIG_PROP = "config.prop";
    public static final String TEST_TOPIC = "Test";

    public static final String ANY_VALUE = "???";

    @Test
    void shouldNotReturnDeleteChangesForTopicDeleteFalse() {
        // GIVEN
        var topic = V1KafkaTopic.builder()
                .withMetadata(ObjectMeta.builder()
                        .withName(TEST_TOPIC)
                        .withAnnotation(JikkouMetadataAnnotations.JIKKOU_IO_DELETE, false)
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
                .collect(Collectors.toMap(TopicChange::getName, it -> it));

        // THEN
        Assertions.assertTrue(changes.isEmpty());
    }

    @Test
    void shouldReturnDeleteChangesForExistingTopicDeleteTrue() {

        // GIVEN
        var topic = V1KafkaTopic.builder()
                .withMetadata(ObjectMeta.builder()
                        .withName(TEST_TOPIC)
                        .withAnnotation(JikkouMetadataAnnotations.JIKKOU_IO_DELETE, true)
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
        List<V1KafkaTopic> expectedState = List.of(topic);

        // WHEN
        var changes = new TopicChangeComputer()
                .computeChanges(actualState, expectedState)
                .stream()
                .collect(Collectors.toMap(TopicChange::getName, it -> it));

        // THEN
        TopicChange change = changes.get(TEST_TOPIC);
        Assertions.assertNotNull(change);
        Assertions.assertEquals(DELETE, change.getChangeType());
        Assertions.assertFalse(change.hasConfigEntryChanges());
    }

    @Test
    void shouldReturnNoChangesForNotExistingTopicDeleteTrue() {

        // GIVEN
        var topic = V1KafkaTopic.builder()
                .withMetadata(ObjectMeta.builder()
                        .withName(TEST_TOPIC)
                        .withAnnotation(JikkouMetadataAnnotations.JIKKOU_IO_DELETE, true)
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
                .collect(Collectors.toMap(TopicChange::getName, it -> it));

        // THEN
        TopicChange change = changes.get(TEST_TOPIC);
        Assertions.assertNull(change);
    }

    @Test
    void shouldReturnChangesWhenTopicDoesNotExist() {
        // GIVEN
        var topic = V1KafkaTopic.builder()
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
        List<V1KafkaTopic> expectedState = List.of(topic);

        // WHEN
        var changes = new TopicChangeComputer()
                .computeChanges(actualState, expectedState)
                .stream()
                .collect(Collectors.toMap(TopicChange::getName, it -> it));

        // THEN
        TopicChange change = changes.get(TEST_TOPIC);
        Assertions.assertEquals(ADD, change.getChangeType());
    }

    @Test
    void shouldReturnChangesForTopicWithUpdatedConfigEntry() {

        // GIVEN
        Configs actualConfig = Configs.empty();
        actualConfig.add(new ConfigValue(CONFIG_PROP, "actual-value"));

        var topicBefore = V1KafkaTopic.builder()
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
        List<V1KafkaTopic> actualState = List.of(topicBefore);

        Configs expectedConfig = Configs.empty();
        expectedConfig.add(new ConfigValue(CONFIG_PROP, "expected-value"));

        var topicAfter = V1KafkaTopic.builder()
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

        List<V1KafkaTopic> expectedState = List.of(topicAfter);

        // WHEN
        var changes = new TopicChangeComputer()
                .computeChanges(actualState, expectedState)
                .stream()
                .collect(Collectors.toMap(TopicChange::getName, it -> it));

        // THEN
        TopicChange change = changes.get(TEST_TOPIC);
        Assertions.assertEquals(UPDATE, change.getChangeType());
        Assertions.assertTrue(change.hasConfigEntryChanges());

        Assertions.assertEquals(UPDATE, change.getConfigs().get(CONFIG_PROP).getChangeType());
        Assertions.assertEquals("actual-value", change.getConfigs().get(CONFIG_PROP).getValueChange().getBefore());
        Assertions.assertEquals("expected-value", change.getConfigs().get(CONFIG_PROP).getValueChange().getAfter());
    }

    @Test
    void shouldReturnChangesForTopicWithNewConfigEntry() {

        // GIVEN
        Configs actualConfig = Configs.empty();
        var topicBefore = V1KafkaTopic.builder()
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

        List<V1KafkaTopic> actualState = List.of(topicBefore);

        Configs expectedConfig = Configs.empty();
        expectedConfig.add(new ConfigValue(CONFIG_PROP, "expected-value"));
        var topicAfter = V1KafkaTopic.builder()
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

        List<V1KafkaTopic> expectedState = List.of(topicAfter);

        // WHEN
        var changes = new TopicChangeComputer()
                .computeChanges(actualState, expectedState)
                .stream()
                .collect(Collectors.toMap(TopicChange::getName, it -> it));

        // THEN
        TopicChange change = changes.get(TEST_TOPIC);
        Assertions.assertEquals(UPDATE, change.getChangeType());
        Assertions.assertTrue(change.hasConfigEntryChanges());

        Assertions.assertEquals(ADD, change.getConfigs().get(CONFIG_PROP).getChangeType());
        Assertions.assertEquals("expected-value", change.getConfigs().get(CONFIG_PROP).getValueChange().getAfter());
        Assertions.assertNull(change.getConfigs().get(CONFIG_PROP).getValueChange().getBefore());
    }

    @Test
    void shouldReturnNoneChangesForEqualTopics() {

        // GIVEN
        Configs configs = Configs.empty();
        configs.add(new ConfigValue(CONFIG_PROP, ANY_VALUE));

        var topic = V1KafkaTopic.builder()
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

        List<V1KafkaTopic> actualState = List.of(topic);
        List<V1KafkaTopic> expectedState = List.of(topic);

        // WHEN
        var changes = new TopicChangeComputer()
                .computeChanges(actualState, expectedState)
                .stream()
                .collect(Collectors.toMap(TopicChange::getName, it -> it));

        // THEN
        TopicChange change = changes.get(TEST_TOPIC);
        Assertions.assertEquals(NONE, change.getChangeType());
        Assertions.assertFalse(change.hasConfigEntryChanges());

        Assertions.assertEquals(NONE, change.getConfigs().get(CONFIG_PROP).getChangeType());
        Assertions.assertEquals(ANY_VALUE, change.getConfigs().get(CONFIG_PROP).getValueChange().getBefore());
        Assertions.assertEquals(ANY_VALUE, change.getConfigs().get(CONFIG_PROP).getValueChange().getAfter());
    }

    @Test
    void shouldReturnUpdateChangesForConfigDeleteOrphansTrue() {
        // GIVEN
        Configs configsBefore = Configs.empty();

        configsBefore.add(new ConfigValue(CONFIG_PROP, "orphan", true, true));
        var topicBefore = V1KafkaTopic.builder()
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

        List<V1KafkaTopic> actualState = List.of(topicBefore);

        Configs configsAfter = Configs.empty();
        var topicAfter = V1KafkaTopic.builder()
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

        List<V1KafkaTopic> expectedState = List.of(topicAfter);

        TopicChangeComputer changeComputer = new TopicChangeComputer(true);

        // WHEN

        var changes = changeComputer
                .computeChanges(actualState, expectedState)
                .stream()
                .collect(Collectors.toMap(TopicChange::getName, it -> it));

        // THEN
        TopicChange change = changes.get(TEST_TOPIC);
        Assertions.assertEquals(UPDATE, change.getChangeType());
        Assertions.assertTrue(change.hasConfigEntryChanges());

        Assertions.assertEquals(DELETE, change.getConfigs().get(CONFIG_PROP).getChangeType());
        Assertions.assertEquals("orphan", change.getConfigs().get(CONFIG_PROP).getValueChange().getBefore());
        Assertions.assertNull(change.getConfigs().get(CONFIG_PROP).getValueChange().getAfter());
    }

    @Test
    void shouldReturnNoneChangesForConfigDeleteOrphansFalse() {
        // GIVEN
        Configs configsBefore = Configs.empty();

        ConfigEntry mkConfigEntry = Mockito.mock(ConfigEntry.class);
        Mockito.when(mkConfigEntry.name()).thenReturn("CONFIG_PROP");
        Mockito.when(mkConfigEntry.value()).thenReturn("orphan");
        Mockito.when(mkConfigEntry.source()).thenReturn(ConfigEntry.ConfigSource.DYNAMIC_BROKER_CONFIG);

        configsBefore.add(KafkaConfigsAdapter.of(mkConfigEntry));
        var topicBefore = V1KafkaTopic.builder()
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

        List<V1KafkaTopic> actualState = List.of(topicBefore);

        Configs configsAfter = Configs.empty();
        var topicAfter = V1KafkaTopic.builder()
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
        List<V1KafkaTopic> expectedState = List.of(topicAfter);

        TopicChangeComputer changeComputer = new TopicChangeComputer(false);

        // WHEN

        Map<String, TopicChange> changes = changeComputer
                .computeChanges(actualState, expectedState)
                .stream()
                .collect(Collectors.toMap(TopicChange::getName, it -> it));

        // THEN
        TopicChange change = changes.get(TEST_TOPIC);
        Assertions.assertEquals(NONE, change.getChangeType());
        Assertions.assertFalse(change.hasConfigEntryChanges());
    }

    @Test
    void shouldReturnNoneChangesForTopicWithDefaultPartitions() {
        // GIVEN
        var topicBefore = V1KafkaTopic.builder()
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
        List<V1KafkaTopic> actualState = List.of(topicBefore);

        var topicAfter = V1KafkaTopic.builder()
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

        List<V1KafkaTopic> expectedState = List.of(topicAfter);

        TopicChangeComputer changeComputer = new TopicChangeComputer(false);

        // WHEN
        Map<String, TopicChange> changes = changeComputer
                .computeChanges(actualState, expectedState)
                .stream()
                .collect(Collectors.toMap(TopicChange::getName, it -> it));

        // THEN
        TopicChange change = changes.get(TEST_TOPIC);
        Assertions.assertEquals(NONE, change.getChangeType());
        Assertions.assertEquals(NONE, change.getPartitions().getChangeType());
    }

    @Test
    void shouldReturnNoneChangesForTopicWithDefaultReplication() {
        // GIVEN
        var topicBefore = V1KafkaTopic.builder()
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

        List<V1KafkaTopic> actualState = List.of(topicBefore);

        var topicAfter = V1KafkaTopic.builder()
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

        List<V1KafkaTopic> expectedState = List.of(topicAfter);
        TopicChangeComputer changeComputer = new TopicChangeComputer(false);

        // WHEN
        Map<String, TopicChange> changes = changeComputer
                .computeChanges(actualState, expectedState)
                .stream()
                .collect(Collectors.toMap(TopicChange::getName, it -> it));

        // THEN
        TopicChange change = changes.get(TEST_TOPIC);
        Assertions.assertEquals(NONE, change.getChangeType());
        Assertions.assertEquals(NONE, change.getReplicas().getChangeType());
    }
}