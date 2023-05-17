/*
 * Copyright 2023 StreamThoughts.
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
package io.streamthoughts.jikkou.api.control;

import io.streamthoughts.jikkou.api.model.ConfigValue;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ConfigEntryChangeComputerTest {

    public static final List<ConfigValue> EMPTY_LIST = Collections.emptyList();

    @Test
    void shouldGetAddChangeForNewValue() {
        // Given
        ConfigEntryChangeComputer computer = new ConfigEntryChangeComputer();
        computer.isConfigDeletionEnabled(true);
        List<ConfigValue> expected = List.of(new ConfigValue("key", "value"));

        // When
        List<ConfigEntryChange> changes = computer.computeChanges(EMPTY_LIST, expected);

        // Then
        Assertions.assertNotNull(changes);
        Assertions.assertEquals(1, changes.size());
        Assertions.assertEquals(ChangeType.ADD, changes.iterator().next().getChangeType());
    }

    @Test
    void shouldGetAddChangeForNewValueAndExistingNullValue() {
        // Given
        ConfigEntryChangeComputer computer = new ConfigEntryChangeComputer();
        computer.isConfigDeletionEnabled(true);
        List<ConfigValue> expected = List.of(new ConfigValue("key", "value"));
        List<ConfigValue> actual = List.of(new ConfigValue("key", null));

        // When
        List<ConfigEntryChange> changes = computer.computeChanges(actual, expected);

        // Then
        Assertions.assertNotNull(changes);
        Assertions.assertEquals(1, changes.size());
        Assertions.assertEquals(ChangeType.ADD, changes.iterator().next().getChangeType());
    }

    @Test
    void shouldGetUpdateChangeForNewValue() {
        // Given
        ConfigEntryChangeComputer computer = new ConfigEntryChangeComputer();
        computer.isConfigDeletionEnabled(true);
        ConfigValue newConfigValue = new ConfigValue("key", "new");
        ConfigValue oldConfigValue = new ConfigValue("key", "old");

        // When
        List<ConfigEntryChange> changes = computer.computeChanges(
                List.of(oldConfigValue),
                List.of(newConfigValue)
        );

        // Then
        Assertions.assertNotNull(changes);
        Assertions.assertEquals(1, changes.size());

        ConfigEntryChange change = changes.iterator().next();
        Assertions.assertEquals(ChangeType.UPDATE, change.getChangeType());
        Assertions.assertEquals(oldConfigValue.value(), change.getValueChange().getBefore());
        Assertions.assertEquals(newConfigValue.value(), change.getValueChange().getAfter());
    }

    @Test
    void shouldGetDeleteChangeForMissingValueGivenDeletionTrue() {
        // Given
        ConfigEntryChangeComputer computer = new ConfigEntryChangeComputer();
        computer.isConfigDeletionEnabled(true);

        // When
        List<ConfigValue> actual = List.of(new ConfigValue("key", "value"));

        List<ConfigEntryChange> changes = computer.computeChanges(actual, EMPTY_LIST);

        // Then
        Assertions.assertNotNull(changes);
        Assertions.assertEquals(1, changes.size());
        Assertions.assertEquals(ChangeType.DELETE, changes.iterator().next().getChangeType());
    }

    @Test
    void shouldGetNoDeleteChangeForMissingValueGivenDeletionFalse() {
        // Given
        ConfigEntryChangeComputer computer = new ConfigEntryChangeComputer();
        computer.isConfigDeletionEnabled(false);

        // When
        List<ConfigValue> actual = List.of(new ConfigValue("key", "value"));

        List<ConfigEntryChange> changes = computer.computeChanges(actual, EMPTY_LIST);

        // Then
        Assertions.assertNotNull(changes);
        Assertions.assertTrue(changes.isEmpty());
    }

}