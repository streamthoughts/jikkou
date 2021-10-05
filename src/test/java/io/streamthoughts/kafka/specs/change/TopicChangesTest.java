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
package io.streamthoughts.kafka.specs.change;

import io.streamthoughts.kafka.specs.resources.ConfigValue;
import io.streamthoughts.kafka.specs.resources.Configs;
import io.streamthoughts.kafka.specs.model.V1TopicObject;
import org.apache.kafka.clients.admin.ConfigEntry;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.List;

import static io.streamthoughts.kafka.specs.change.Change.OperationType.*;

public class TopicChangesTest {

    public static final ConfigEntry DUMMY_CONFIG_ENTRY = new ConfigEntry("???", "???");

    public static final String CONFIG_PROP = "config.prop";
    public static final String TEST_TOPIC = "Test";

    @Test
    public void testReturnDeleteTopicChange() {

        // Given
        Configs configs = Configs.empty();
        configs.add(new ConfigValue(CONFIG_PROP, "???", DUMMY_CONFIG_ENTRY));
        V1TopicObject topic = new V1TopicObject(TEST_TOPIC, 1, (short) 1, configs);

        List<V1TopicObject> beforeTopicStates = List.of(topic);
        List<V1TopicObject> afterTopicStates  = Collections.emptyList();

        // When
        TopicChanges changes = TopicChanges.computeChanges(beforeTopicStates, afterTopicStates);

        // Then
        TopicChange change = changes.get(TEST_TOPIC);
        Assertions.assertEquals(DELETE, change.getOperation());
        Assertions.assertFalse(change.hasConfigEntryChanges());
    }

    @Test
    public void testReturnAddTopicChange() {

        // Given
        Configs configs = Configs.empty();
        configs.add(new ConfigValue(CONFIG_PROP, "???", DUMMY_CONFIG_ENTRY));
        V1TopicObject topic = new V1TopicObject(TEST_TOPIC, 1, (short) 1, configs);

        List<V1TopicObject> beforeTopicStates = Collections.emptyList();
        List<V1TopicObject> afterTopicStates  = List.of(topic);

        // When
        TopicChanges changes = TopicChanges.computeChanges(beforeTopicStates, afterTopicStates);

        // Then
        TopicChange change = changes.get(TEST_TOPIC);
        Assertions.assertEquals(ADD, change.getOperation());
        Assertions.assertTrue(change.hasConfigEntryChanges());
    }

    @Test
    public void testReturnUpdateTopicConfigEntryChange() {

        // Given
        Configs configsBefore = Configs.empty();
        configsBefore.add(new ConfigValue(CONFIG_PROP, "before", DUMMY_CONFIG_ENTRY));
        V1TopicObject topicBefore = new V1TopicObject(TEST_TOPIC, 1, (short) 1, configsBefore);

        List<V1TopicObject> beforeTopicStates = List.of(topicBefore);

        Configs configsAfter = Configs.empty();
        configsAfter.add(new ConfigValue(CONFIG_PROP, "after", DUMMY_CONFIG_ENTRY));
        V1TopicObject topicAfter = new V1TopicObject(TEST_TOPIC, 1, (short) 1, configsAfter);
        List<V1TopicObject> afterTopicStates  = List.of(topicAfter);

        // When
        TopicChanges changes = TopicChanges.computeChanges(beforeTopicStates, afterTopicStates);

        // Then
        TopicChange change = changes.get(TEST_TOPIC);
        Assertions.assertEquals(UPDATE, change.getOperation());
        Assertions.assertTrue(change.hasConfigEntryChanges());

        Assertions.assertEquals(UPDATE, change.getConfigs().get(CONFIG_PROP).getOperation());
        Assertions.assertEquals("before", change.getConfigs().get(CONFIG_PROP).getBefore());
        Assertions.assertEquals("after", change.getConfigs().get(CONFIG_PROP).getAfter());
    }

    @Test
    public void testReturnNoneTopicConfigEntryChange() {

        // Given
        Configs configs = Configs.empty();
        configs.add(new ConfigValue(CONFIG_PROP, "???", DUMMY_CONFIG_ENTRY));
        V1TopicObject topic = new V1TopicObject(TEST_TOPIC, 1, (short) 1, configs);

        List<V1TopicObject> beforeTopicStates = List.of(topic);
        List<V1TopicObject> afterTopicStates  = List.of(topic);

        // When
        TopicChanges changes = TopicChanges.computeChanges(beforeTopicStates, afterTopicStates);

        // Then
        TopicChange change = changes.get(TEST_TOPIC);
        Assertions.assertEquals(NONE, change.getOperation());
        Assertions.assertFalse(change.hasConfigEntryChanges());

        Assertions.assertEquals(NONE, change.getConfigs().get(CONFIG_PROP).getOperation());
        Assertions.assertEquals("???", change.getConfigs().get(CONFIG_PROP).getBefore());
        Assertions.assertEquals("???", change.getConfigs().get(CONFIG_PROP).getAfter());
    }

    @Test
    public void testReturnDeleteTopicConfigEntryChangeGivenOrphanDynamicTopicConfig() {
        // Given
        Configs configsBefore = Configs.empty();

        ConfigEntry mkConfigEntry = Mockito.mock(ConfigEntry.class);
        Mockito.when(mkConfigEntry.source()).thenReturn(ConfigEntry.ConfigSource.DYNAMIC_TOPIC_CONFIG);

        configsBefore.add(new ConfigValue(CONFIG_PROP, "before", mkConfigEntry));
        V1TopicObject topicBefore = new V1TopicObject(TEST_TOPIC, 1, (short) 1, configsBefore);

        List<V1TopicObject> beforeTopicStates = List.of(topicBefore);

        Configs configsAfter = Configs.empty();
        V1TopicObject topicAfter = new V1TopicObject(TEST_TOPIC, 1, (short) 1, configsAfter);
        List<V1TopicObject> afterTopicStates  = List.of(topicAfter);

        // When
        TopicChanges changes = TopicChanges.computeChanges(beforeTopicStates, afterTopicStates);

        // Then
        TopicChange change = changes.get(TEST_TOPIC);
        Assertions.assertEquals(UPDATE, change.getOperation());
        Assertions.assertTrue(change.hasConfigEntryChanges());

        Assertions.assertEquals(DELETE, change.getConfigs().get(CONFIG_PROP).getOperation());
        Assertions.assertEquals("before", change.getConfigs().get(CONFIG_PROP).getBefore());
        Assertions.assertNull(change.getConfigs().get(CONFIG_PROP).getAfter());
    }

    @Test
    public void testReturnDeleteTopicConfigEntryChangeGivenOrphanDynamicBrokerConfig() {
        // Given
        Configs configsBefore = Configs.empty();

        ConfigEntry mkConfigEntry = Mockito.mock(ConfigEntry.class);
        Mockito.when(mkConfigEntry.source()).thenReturn(ConfigEntry.ConfigSource.DYNAMIC_BROKER_CONFIG);

        configsBefore.add(new ConfigValue(CONFIG_PROP, "before", mkConfigEntry));
        V1TopicObject topicBefore = new V1TopicObject(TEST_TOPIC, 1, (short) 1, configsBefore);

        List<V1TopicObject> beforeTopicStates = List.of(topicBefore);

        Configs configsAfter = Configs.empty();
        V1TopicObject topicAfter = new V1TopicObject(TEST_TOPIC, 1, (short) 1, configsAfter);
        List<V1TopicObject> afterTopicStates  = List.of(topicAfter);

        // When
        TopicChanges changes = TopicChanges.computeChanges(beforeTopicStates, afterTopicStates);

        // Then
        TopicChange change = changes.get(TEST_TOPIC);
        Assertions.assertEquals(NONE, change.getOperation());
        Assertions.assertFalse(change.hasConfigEntryChanges());
    }
}