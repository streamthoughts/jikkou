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

import io.streamthoughts.jikkou.api.model.ConfigValue;
import io.streamthoughts.jikkou.api.model.Configs;
import io.streamthoughts.jikkou.kafka.adapters.KafkaConfigsAdapter;
import io.streamthoughts.jikkou.kafka.internals.KafkaConstants;
import io.streamthoughts.jikkou.kafka.models.V1KafkaTopicObject;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.kafka.clients.admin.ConfigEntry;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class TopicChangeComputerTest {
    
    public static final String CONFIG_PROP = "config.prop";
    public static final String TEST_TOPIC = "Test";

    public static final KafkaTopicReconciliationConfig DEFAULT_TOPIC_CHANGE_OPTIONS = new KafkaTopicReconciliationConfig();

    @Test
    public void should_not_return_delete_changes_for_internal_topics_given_delete_topic_orphans_options_true() {

        // Given
        var topic = V1KafkaTopicObject.builder()
                .withName("__consumer_offsets")
                .withPartitions(1)
                .withReplicationFactor((short)1)
                .withConfigs(Configs.empty())
                .build();

        List<V1KafkaTopicObject> actualState = List.of(topic);
        List<V1KafkaTopicObject> expectedState  = Collections.emptyList();
        KafkaTopicReconciliationConfig options = DEFAULT_TOPIC_CHANGE_OPTIONS.withDeleteTopicOrphans(true);

        // When
        var changes = new TopicChangeComputer()
                .computeChanges(actualState, expectedState, options)
                .stream()
                .collect(Collectors.toMap(TopicChange::getKey, it -> it));

        // Then
        Assertions.assertTrue(changes.isEmpty());
    }

    @Test
    public void should_return_delete_changes_for_internal_topics_given_delete_topic_orphans_options_true() {

        // Given
        var topic = V1KafkaTopicObject.builder()
                .withName("__consumer_offsets")
                .withPartitions(1)
                .withReplicationFactor((short)1)
                .withConfigs(Configs.empty())
                .build();

        List<V1KafkaTopicObject> actualState = List.of(topic);
        List<V1KafkaTopicObject> expectedState  = Collections.emptyList();
        KafkaTopicReconciliationConfig options = DEFAULT_TOPIC_CHANGE_OPTIONS
                .withDeleteTopicOrphans(true)
                .withExcludeInternalTopics(false);

        // When
        var changes = new TopicChangeComputer()
                .computeChanges(actualState, expectedState, options)
                .stream()
                .collect(Collectors.toMap(TopicChange::getKey, it -> it));

        // Then
        Assertions.assertFalse(changes.isEmpty());
        var topicChange = changes.get("__consumer_offsets");
        Assertions.assertNotNull(topicChange);
        Assertions.assertEquals(DELETE, topicChange.getChange());
    }

    @Test
    public void should_not_return_delete_changes_given_delete_topic_orphans_options_false() {

        // Given
        var topic = V1KafkaTopicObject.builder()
                .withName(TEST_TOPIC)
                .withPartitions(1)
                .withReplicationFactor((short)1)
                .withConfigs(Configs.empty())
                .build();

        List<V1KafkaTopicObject> actualState = List.of(topic);
        List<V1KafkaTopicObject> expectedState  = Collections.emptyList();

        // When
        var changes = new TopicChangeComputer()
                .computeChanges(actualState, expectedState, DEFAULT_TOPIC_CHANGE_OPTIONS.withDeleteTopicOrphans(false))
                .stream()
                .collect(Collectors.toMap(TopicChange::getKey, it -> it));

        // Then
        Assertions.assertTrue(changes.isEmpty());
    }

    @Test
    public void should_return_delete_changes_given_delete_topic_orphans_options_true() {

        // Given
        var topic = V1KafkaTopicObject.builder()
                .withName(TEST_TOPIC)
                .withPartitions(1)
                .withReplicationFactor((short)1)
                .withConfigs(Configs.empty())
                .build();

        List<V1KafkaTopicObject> actualState = List.of(topic);
        List<V1KafkaTopicObject> expectedState  = Collections.emptyList();

        var options = new KafkaTopicReconciliationConfig().withDeleteTopicOrphans(true);

        // When
        var changes = new TopicChangeComputer()
                .computeChanges(actualState, expectedState, options)
                .stream()
                .collect(Collectors.toMap(TopicChange::getKey, it -> it));

        // Then
        TopicChange change = changes.get(TEST_TOPIC);
        Assertions.assertNotNull(change);
        Assertions.assertEquals(DELETE, change.getChange());
        Assertions.assertFalse(change.hasConfigEntryChanges());
    }

    @Test
    public void should_return_add_changes_when_topic_not_exist() {
        // Given
        var topic = V1KafkaTopicObject.builder()
                .withName(TEST_TOPIC)
                .withPartitions(1)
                .withReplicationFactor((short)1)
                .withConfigs(Configs.empty())
                .build();

        List<V1KafkaTopicObject> actualState = Collections.emptyList();
        List<V1KafkaTopicObject> expectedState  = List.of(topic);

        // When
        var changes = new TopicChangeComputer()
                .computeChanges(actualState, expectedState, DEFAULT_TOPIC_CHANGE_OPTIONS)
                .stream()
                .collect(Collectors.toMap(TopicChange::getKey, it -> it));

        // Then
        TopicChange change = changes.get(TEST_TOPIC);
        Assertions.assertEquals(ADD, change.getChange());
    }

    @Test
    public void should_return_update_changes_given_topic_with_updated_config_entry() {

        // Given
        Configs actualConfig = Configs.empty();
        actualConfig.add(new ConfigValue(CONFIG_PROP, "actual-value"));

        var topicBefore = V1KafkaTopicObject.builder()
                .withName(TEST_TOPIC)
                .withPartitions(1)
                .withReplicationFactor((short)1)
                .withConfigs(actualConfig)
                .build();

        List<V1KafkaTopicObject> actualState = List.of(topicBefore);

        Configs expectedConfig = Configs.empty();
        expectedConfig.add(new ConfigValue(CONFIG_PROP, "expected-value"));

        var topicAfter = V1KafkaTopicObject.builder()
                .withName(TEST_TOPIC)
                .withPartitions(1)
                .withReplicationFactor((short)1)
                .withConfigs(expectedConfig)
                .build();

        List<V1KafkaTopicObject> expectedState  = List.of(topicAfter);

        // When
        var changes = new TopicChangeComputer()
                .computeChanges(actualState, expectedState, new KafkaTopicReconciliationConfig())
                .stream()
                .collect(Collectors.toMap(TopicChange::getKey, it -> it));

        // Then
        TopicChange change = changes.get(TEST_TOPIC);
        Assertions.assertEquals(UPDATE, change.getChange());
        Assertions.assertTrue(change.hasConfigEntryChanges());

        Assertions.assertEquals(UPDATE, change.getConfigs().get(CONFIG_PROP).getChange());
        Assertions.assertEquals("actual-value", change.getConfigs().get(CONFIG_PROP).getValueChange().getBefore());
        Assertions.assertEquals("expected-value", change.getConfigs().get(CONFIG_PROP).getValueChange().getAfter());
    }

    @Test
    public void should_return_update_changes_given_topic_with_added_config_entry() {

        // Given
        Configs actualConfig = Configs.empty();
        var topicBefore = V1KafkaTopicObject.builder()
                .withName(TEST_TOPIC)
                .withPartitions(1)
                .withReplicationFactor((short)1)
                .withConfigs(actualConfig)
                .build();

        List<V1KafkaTopicObject> actualState = List.of(topicBefore);

        Configs expectedConfig = Configs.empty();
        expectedConfig.add(new ConfigValue(CONFIG_PROP, "expected-value"));

        var topicAfter = V1KafkaTopicObject.builder()
                .withName(TEST_TOPIC)
                .withPartitions(1)
                .withReplicationFactor((short)1)
                .withConfigs(expectedConfig)
                .build();

        List<V1KafkaTopicObject> expectedState  = List.of(topicAfter);

        // When
        var changes = new TopicChangeComputer()
                .computeChanges(actualState, expectedState, new KafkaTopicReconciliationConfig())
                .stream()
                .collect(Collectors.toMap(TopicChange::getKey, it -> it));

        // Then
        TopicChange change = changes.get(TEST_TOPIC);
        Assertions.assertEquals(UPDATE, change.getChange());
        Assertions.assertTrue(change.hasConfigEntryChanges());

        Assertions.assertEquals(ADD, change.getConfigs().get(CONFIG_PROP).getChange());
        Assertions.assertEquals("expected-value", change.getConfigs().get(CONFIG_PROP).getValueChange().getAfter());
        Assertions.assertNull(change.getConfigs().get(CONFIG_PROP).getValueChange().getBefore());
    }

    @Test
    public void should_return_none_changes_given_identical_topic() {

        // Given
        Configs configs = Configs.empty();
        configs.add(new ConfigValue(CONFIG_PROP, "???"));

        var topic = V1KafkaTopicObject.builder()
                .withName(TEST_TOPIC)
                .withPartitions(1)
                .withReplicationFactor((short)1)
                .withConfigs(configs)
                .build();


        List<V1KafkaTopicObject> actualState = List.of(topic);
        List<V1KafkaTopicObject> expectedState  = List.of(topic);

        // When
        var changes = new TopicChangeComputer()
                .computeChanges(actualState, expectedState, new KafkaTopicReconciliationConfig())
                .stream()
                .collect(Collectors.toMap(TopicChange::getKey, it -> it));

        // Then
        TopicChange change = changes.get(TEST_TOPIC);
        Assertions.assertEquals(NONE, change.getChange());
        Assertions.assertFalse(change.hasConfigEntryChanges());

        Assertions.assertEquals(NONE, change.getConfigs().get(CONFIG_PROP).getChange());
        Assertions.assertEquals("???", change.getConfigs().get(CONFIG_PROP).getValueChange().getBefore());
        Assertions.assertEquals("???", change.getConfigs().get(CONFIG_PROP).getValueChange().getAfter());
    }

    @Test
    public void should_return_update_changes_given_topic_with_deleted_config_entry_and_delete_config_orphans_true() {
        // Given
        Configs configsBefore = Configs.empty();

        configsBefore.add(new ConfigValue(CONFIG_PROP, "orphan", true, true));
        var topicBefore = V1KafkaTopicObject.builder()
                .withName(TEST_TOPIC)
                .withPartitions(1)
                .withReplicationFactor((short)1)
                .withConfigs(configsBefore)
                .build();

        List<V1KafkaTopicObject> actualState = List.of(topicBefore);

        Configs configsAfter = Configs.empty();
        var topicAfter = V1KafkaTopicObject.builder()
                .withName(TEST_TOPIC)
                .withPartitions(1)
                .withReplicationFactor((short)1)
                .withConfigs(configsAfter)
                .build();

        List<V1KafkaTopicObject> expectedState  = List.of(topicAfter);

        // When
        var changes = new TopicChangeComputer()
                .computeChanges(actualState, expectedState, DEFAULT_TOPIC_CHANGE_OPTIONS.withDeleteConfigOrphans(true))
                .stream()
                .collect(Collectors.toMap(TopicChange::getKey, it -> it));

        // Then
        TopicChange change = changes.get(TEST_TOPIC);
        Assertions.assertEquals(UPDATE, change.getChange());
        Assertions.assertTrue(change.hasConfigEntryChanges());

        Assertions.assertEquals(DELETE, change.getConfigs().get(CONFIG_PROP).getChange());
        Assertions.assertEquals("orphan", change.getConfigs().get(CONFIG_PROP).getValueChange().getBefore());
        Assertions.assertNull(change.getConfigs().get(CONFIG_PROP).getValueChange().getAfter());
    }

    @Test
    public void should_return_none_changes_given_topic_with_deleted_config_entry_and_delete_config_orphans_false() {
        // Given
        Configs configsBefore = Configs.empty();

        ConfigEntry mkConfigEntry = Mockito.mock(ConfigEntry.class);
        Mockito.when(mkConfigEntry.name()).thenReturn("CONFIG_PROP");
        Mockito.when(mkConfigEntry.value()).thenReturn("orphan");
        Mockito.when(mkConfigEntry.source()).thenReturn(ConfigEntry.ConfigSource.DYNAMIC_BROKER_CONFIG);

        configsBefore.add(KafkaConfigsAdapter.of(mkConfigEntry));
        var topicBefore = V1KafkaTopicObject.builder()
                .withName(TEST_TOPIC)
                .withPartitions(1)
                .withReplicationFactor((short)1)
                .withConfigs(configsBefore)
                .build();

        List<V1KafkaTopicObject> actualState = List.of(topicBefore);

        Configs configsAfter = Configs.empty();
        var topicAfter = V1KafkaTopicObject.builder()
                .withName(TEST_TOPIC)
                .withPartitions(1)
                .withReplicationFactor((short)1)
                .withConfigs(configsAfter)
                .build();

        List<V1KafkaTopicObject> expectedState  = List.of(topicAfter);

        // When
        Map<String, TopicChange> changes = new TopicChangeComputer()
                .computeChanges(actualState, expectedState, DEFAULT_TOPIC_CHANGE_OPTIONS.withDeleteConfigOrphans(false))
                .stream()
                .collect(Collectors.toMap(TopicChange::getKey, it -> it));

        // Then
        TopicChange change = changes.get(TEST_TOPIC);
        Assertions.assertEquals(NONE, change.getChange());
        Assertions.assertFalse(change.hasConfigEntryChanges());
    }

    @Test
    public void should_return_none_changes_given_topic_with_default_partitions() {
        // Given
        var topicBefore = V1KafkaTopicObject.builder()
                .withName(TEST_TOPIC)
                .withPartitions(1)
                .withReplicationFactor((short)1)
                .build();

        List<V1KafkaTopicObject> actualState = List.of(topicBefore);

        var topicAfter = V1KafkaTopicObject.builder()
                .withName(TEST_TOPIC)
                .withPartitions(KafkaConstants.NO_NUM_PARTITIONS)
                .withReplicationFactor((short)1)
                .build();

        List<V1KafkaTopicObject> expectedState  = List.of(topicAfter);

        // When
        Map<String, TopicChange> changes = new TopicChangeComputer()
                .computeChanges(actualState, expectedState, DEFAULT_TOPIC_CHANGE_OPTIONS.withDeleteConfigOrphans(false))
                .stream()
                .collect(Collectors.toMap(TopicChange::getKey, it -> it));

        // Then
        TopicChange change = changes.get(TEST_TOPIC);
        Assertions.assertEquals(NONE, change.getChange());
        Assertions.assertEquals(NONE, change.getPartitions().get().type());
    }

    @Test
    public void should_return_none_changes_given_topic_with_default_replication() {
        // Given
        var topicBefore = V1KafkaTopicObject.builder()
                .withName(TEST_TOPIC)
                .withPartitions(1)
                .withReplicationFactor((short)1)
                .build();

        List<V1KafkaTopicObject> actualState = List.of(topicBefore);

        var topicAfter = V1KafkaTopicObject.builder()
                .withName(TEST_TOPIC)
                .withPartitions(1)
                .withReplicationFactor(KafkaConstants.NO_REPLICATION_FACTOR)
                .build();

        List<V1KafkaTopicObject> expectedState  = List.of(topicAfter);

        // When
        Map<String, TopicChange> changes = new TopicChangeComputer()
                .computeChanges(actualState, expectedState, DEFAULT_TOPIC_CHANGE_OPTIONS.withDeleteConfigOrphans(false))
                .stream()
                .collect(Collectors.toMap(TopicChange::getKey, it -> it));

        // Then
        TopicChange change = changes.get(TEST_TOPIC);
        Assertions.assertEquals(NONE, change.getChange());
        Assertions.assertEquals(NONE, change.getReplicationFactor().get().type());
    }
}