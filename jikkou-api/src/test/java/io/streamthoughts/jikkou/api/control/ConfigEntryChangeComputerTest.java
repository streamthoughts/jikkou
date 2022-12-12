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
package io.streamthoughts.jikkou.api.control;

import io.streamthoughts.jikkou.api.model.Configs;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ConfigEntryChangeComputerTest {

    @Test
    void should_compute_change_given_added_config() {
        // Given
        Configs actualStates = Configs.empty();
        Configs expectedStates = Configs.of("k1", "v1");
        ConfigEntryReconciliationConfig configuration = new ConfigEntryReconciliationConfig();

        // When
        List<ConfigEntryChange> changes = new ConfigEntryChangeComputer()
                .computeChanges(
                        actualStates,
                        expectedStates,
                        configuration
                );
        // Then
        Assertions.assertEquals(1, changes.size());
        Assertions.assertEquals(ChangeType.ADD, changes.get(0).getChange());
        Assertions.assertEquals(ChangeType.ADD, changes.get(0).getValueChange().type());
        Assertions.assertEquals("k1", changes.get(0).getKey());
        Assertions.assertNull(changes.get(0).getValueChange().getBefore());
        Assertions.assertEquals("v1", changes.get(0).getValueChange().getAfter());
    }

    @Test
    void should_not_compute_change_given_removed_config_and_deletion_false() {
        // Given
        Configs actualStates = Configs.of("k1", "v1");
        Configs expectedStates = Configs.empty();
        ConfigEntryReconciliationConfig configuration = new ConfigEntryReconciliationConfig()
                .withDeleteConfigOrphans(false);

        // When
        List<ConfigEntryChange> changes = new ConfigEntryChangeComputer()
                .computeChanges(
                        actualStates,
                        expectedStates,
                        configuration
                );
        // Then
        Assertions.assertEquals(0, changes.size());
    }

    @Test
    void should_compute_change_given_removed_config_and_deletion_true() {
        // Given
        Configs actualStates = Configs.of("k1", "v1");
        Configs expectedStates = Configs.empty();
        ConfigEntryReconciliationConfig configuration = new ConfigEntryReconciliationConfig()
                .withDeleteConfigOrphans(true);

        // When
        List<ConfigEntryChange> changes = new ConfigEntryChangeComputer()
                .computeChanges(
                        actualStates,
                        expectedStates,
                        configuration
                );
        // Then
        Assertions.assertEquals(1, changes.size());
        Assertions.assertEquals(ChangeType.DELETE, changes.get(0).getChange());
        Assertions.assertEquals(ChangeType.DELETE, changes.get(0).getValueChange().type());
        Assertions.assertEquals("k1", changes.get(0).getKey());
        Assertions.assertEquals("v1", changes.get(0).getValueChange().getBefore());
        Assertions.assertNull(changes.get(0).getValueChange().getAfter());
    }

    @Test
    void should_compute_change_given_updated_config() {
        // Given
        Configs actualStates = Configs.of("k1", "v1");
        Configs expectedStates = Configs.of("k1", "v2");
        ConfigEntryReconciliationConfig configuration = new ConfigEntryReconciliationConfig();

        // When
        List<ConfigEntryChange> changes = new ConfigEntryChangeComputer()
                .computeChanges(
                        actualStates,
                        expectedStates,
                        configuration
                );
        // Then
        Assertions.assertEquals(1, changes.size());
        Assertions.assertEquals(ChangeType.UPDATE, changes.get(0).getChange());
        Assertions.assertEquals(ChangeType.UPDATE, changes.get(0).getValueChange().type());
        Assertions.assertEquals("k1", changes.get(0).getKey());
        Assertions.assertEquals("v1", changes.get(0).getValueChange().getBefore());
        Assertions.assertEquals("v2", changes.get(0).getValueChange().getAfter());
    }
}