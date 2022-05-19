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
package io.streamthoughts.jikkou.api.change;

import io.streamthoughts.jikkou.api.resources.ConfigValue;
import io.streamthoughts.jikkou.api.resources.Configs;
import io.streamthoughts.jikkou.api.model.V1TopicObject;
import org.apache.kafka.clients.admin.ConfigEntry;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static io.streamthoughts.jikkou.api.change.Change.OperationType.*;

public class TopicChangeComputerTest {

    public static final ConfigEntry DUMMY_CONFIG_ENTRY = new ConfigEntry("???", "???");

    public static final String CONFIG_PROP = "config.prop";
    public static final String TEST_TOPIC = "Test";

    public static final TopicChangeOptions DEFAULT_TOPIC_CHANGE_OPTIONS = new TopicChangeOptions();

    @Test
    public void should_not_return_delete_changes_for_internal_topics_given_delete_topic_orphans_options_true() {

        // Given
        V1TopicObject topic = new V1TopicObject("__consumer_offsets", 1, (short) 1, Configs.empty());

        List<V1TopicObject> actualState = List.of(topic);
        List<V1TopicObject> expectedState  = Collections.emptyList();
        TopicChangeOptions options = DEFAULT_TOPIC_CHANGE_OPTIONS.withDeleteTopicOrphans(true);

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
        V1TopicObject topic = new V1TopicObject("__consumer_offsets", 1, (short) 1, Configs.empty());

        List<V1TopicObject> actualState = List.of(topic);
        List<V1TopicObject> expectedState  = Collections.emptyList();
        TopicChangeOptions options = DEFAULT_TOPIC_CHANGE_OPTIONS
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
        Assertions.assertEquals(DELETE, topicChange.getOperation());
    }

    @Test
    public void should_not_return_delete_changes_given_delete_topic_orphans_options_false() {

        // Given
        V1TopicObject topic = new V1TopicObject(TEST_TOPIC, 1, (short) 1, Configs.empty());

        List<V1TopicObject> actualState = List.of(topic);
        List<V1TopicObject> expectedState  = Collections.emptyList();

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
        V1TopicObject topic = new V1TopicObject(TEST_TOPIC, 1, (short) 1, Configs.empty());

        List<V1TopicObject> actualState = List.of(topic);
        List<V1TopicObject> expectedState  = Collections.emptyList();

        var options = new TopicChangeOptions().withDeleteTopicOrphans(true);

        // When
        var changes = new TopicChangeComputer()
                .computeChanges(actualState, expectedState, options)
                .stream()
                .collect(Collectors.toMap(TopicChange::getKey, it -> it));

        // Then
        TopicChange change = changes.get(TEST_TOPIC);
        Assertions.assertNotNull(change);
        Assertions.assertEquals(DELETE, change.getOperation());
        Assertions.assertFalse(change.hasConfigEntryChanges());
    }

    @Test
    public void should_return_add_changes_when_topic_not_exist() {
        // Given
        V1TopicObject topic = new V1TopicObject(TEST_TOPIC, 1, (short) 1, Configs.empty());

        List<V1TopicObject> actualState = Collections.emptyList();
        List<V1TopicObject> expectedState  = List.of(topic);

        // When
        var changes = new TopicChangeComputer()
                .computeChanges(actualState, expectedState, DEFAULT_TOPIC_CHANGE_OPTIONS)
                .stream()
                .collect(Collectors.toMap(TopicChange::getKey, it -> it));

        // Then
        TopicChange change = changes.get(TEST_TOPIC);
        Assertions.assertEquals(ADD, change.getOperation());
    }

    @Test
    public void should_return_update_changes_given_topic_with_updated_config_entry() {

        // Given
        Configs actualConfig = Configs.empty();
        actualConfig.add(new ConfigValue(CONFIG_PROP, "actual-value", DUMMY_CONFIG_ENTRY));
        V1TopicObject topicBefore = new V1TopicObject(TEST_TOPIC, 1, (short) 1, actualConfig);

        List<V1TopicObject> actualState = List.of(topicBefore);

        Configs expectedConfig = Configs.empty();
        expectedConfig.add(new ConfigValue(CONFIG_PROP, "expected-value", DUMMY_CONFIG_ENTRY));
        V1TopicObject topicAfter = new V1TopicObject(TEST_TOPIC, 1, (short) 1, expectedConfig);
        List<V1TopicObject> expectedState  = List.of(topicAfter);

        // When
        var changes = new TopicChangeComputer()
                .computeChanges(actualState, expectedState, new TopicChangeOptions())
                .stream()
                .collect(Collectors.toMap(TopicChange::getKey, it -> it));

        // Then
        TopicChange change = changes.get(TEST_TOPIC);
        Assertions.assertEquals(UPDATE, change.getOperation());
        Assertions.assertTrue(change.hasConfigEntryChanges());

        Assertions.assertEquals(UPDATE, change.getConfigs().get(CONFIG_PROP).getOperation());
        Assertions.assertEquals("actual-value", change.getConfigs().get(CONFIG_PROP).getBefore());
        Assertions.assertEquals("expected-value", change.getConfigs().get(CONFIG_PROP).getAfter());
    }

    @Test
    public void should_return_update_changes_given_topic_with_added_config_entry() {

        // Given
        Configs actualConfig = Configs.empty();
        V1TopicObject topicBefore = new V1TopicObject(TEST_TOPIC, 1, (short) 1, actualConfig);

        List<V1TopicObject> actualState = List.of(topicBefore);

        Configs expectedConfig = Configs.empty();
        expectedConfig.add(new ConfigValue(CONFIG_PROP, "expected-value", DUMMY_CONFIG_ENTRY));
        V1TopicObject topicAfter = new V1TopicObject(TEST_TOPIC, 1, (short) 1, expectedConfig);
        List<V1TopicObject> expectedState  = List.of(topicAfter);

        // When
        var changes = new TopicChangeComputer()
                .computeChanges(actualState, expectedState, new TopicChangeOptions())
                .stream()
                .collect(Collectors.toMap(TopicChange::getKey, it -> it));

        // Then
        TopicChange change = changes.get(TEST_TOPIC);
        Assertions.assertEquals(UPDATE, change.getOperation());
        Assertions.assertTrue(change.hasConfigEntryChanges());

        Assertions.assertEquals(ADD, change.getConfigs().get(CONFIG_PROP).getOperation());
        Assertions.assertEquals("expected-value", change.getConfigs().get(CONFIG_PROP).getAfter());
        Assertions.assertNull(change.getConfigs().get(CONFIG_PROP).getBefore());
    }

    @Test
    public void should_return_none_changes_given_identical_topic() {

        // Given
        Configs configs = Configs.empty();
        configs.add(new ConfigValue(CONFIG_PROP, "???", DUMMY_CONFIG_ENTRY));
        V1TopicObject topic = new V1TopicObject(TEST_TOPIC, 1, (short) 1, configs);

        List<V1TopicObject> actualState = List.of(topic);
        List<V1TopicObject> expectedState  = List.of(topic);

        // When
        var changes = new TopicChangeComputer()
                .computeChanges(actualState, expectedState, new TopicChangeOptions())
                .stream()
                .collect(Collectors.toMap(TopicChange::getKey, it -> it));

        // Then
        TopicChange change = changes.get(TEST_TOPIC);
        Assertions.assertEquals(NONE, change.getOperation());
        Assertions.assertFalse(change.hasConfigEntryChanges());

        Assertions.assertEquals(NONE, change.getConfigs().get(CONFIG_PROP).getOperation());
        Assertions.assertEquals("???", change.getConfigs().get(CONFIG_PROP).getBefore());
        Assertions.assertEquals("???", change.getConfigs().get(CONFIG_PROP).getAfter());
    }

    @Test
    public void should_return_update_changes_given_topic_with_deleted_config_entry_and_delete_config_orphans_true() {
        // Given
        Configs configsBefore = Configs.empty();

        ConfigEntry mkConfigEntry = Mockito.mock(ConfigEntry.class);
        Mockito.when(mkConfigEntry.source()).thenReturn(ConfigEntry.ConfigSource.DYNAMIC_TOPIC_CONFIG);

        configsBefore.add(new ConfigValue(CONFIG_PROP, "orphan", mkConfigEntry));
        V1TopicObject topicBefore = new V1TopicObject(TEST_TOPIC, 1, (short) 1, configsBefore);

        List<V1TopicObject> actualState = List.of(topicBefore);

        Configs configsAfter = Configs.empty();
        V1TopicObject topicAfter = new V1TopicObject(TEST_TOPIC, 1, (short) 1, configsAfter);
        List<V1TopicObject> expectedState  = List.of(topicAfter);

        // When
        var changes = new TopicChangeComputer()
                .computeChanges(actualState, expectedState, DEFAULT_TOPIC_CHANGE_OPTIONS.withDeleteConfigOrphans(true))
                .stream()
                .collect(Collectors.toMap(TopicChange::getKey, it -> it));

        // Then
        TopicChange change = changes.get(TEST_TOPIC);
        Assertions.assertEquals(UPDATE, change.getOperation());
        Assertions.assertTrue(change.hasConfigEntryChanges());

        Assertions.assertEquals(DELETE, change.getConfigs().get(CONFIG_PROP).getOperation());
        Assertions.assertEquals("orphan", change.getConfigs().get(CONFIG_PROP).getBefore());
        Assertions.assertNull(change.getConfigs().get(CONFIG_PROP).getAfter());
    }

    @Test
    public void should_return_none_changes_given_topic_with_deleted_config_entry_and_delete_config_orphans_false() {
        // Given
        Configs configsBefore = Configs.empty();

        ConfigEntry mkConfigEntry = Mockito.mock(ConfigEntry.class);
        Mockito.when(mkConfigEntry.source()).thenReturn(ConfigEntry.ConfigSource.DYNAMIC_BROKER_CONFIG);

        configsBefore.add(new ConfigValue(CONFIG_PROP, "orphan", mkConfigEntry));
        V1TopicObject topicBefore = new V1TopicObject(TEST_TOPIC, 1, (short) 1, configsBefore);

        List<V1TopicObject> actualState = List.of(topicBefore);

        Configs configsAfter = Configs.empty();
        V1TopicObject topicAfter = new V1TopicObject(TEST_TOPIC, 1, (short) 1, configsAfter);
        List<V1TopicObject> expectedState  = List.of(topicAfter);

        // When
        Map<String, TopicChange> changes = new TopicChangeComputer()
                .computeChanges(actualState, expectedState, DEFAULT_TOPIC_CHANGE_OPTIONS.withDeleteConfigOrphans(false))
                .stream()
                .collect(Collectors.toMap(TopicChange::getKey, it -> it));

        // Then
        TopicChange change = changes.get(TEST_TOPIC);
        Assertions.assertEquals(NONE, change.getOperation());
        Assertions.assertFalse(change.hasConfigEntryChanges());
    }
}