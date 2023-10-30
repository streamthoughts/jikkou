/*
 * Copyright 2023 The original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.streamthoughts.jikkou.core.reconcilier.change;

import io.streamthoughts.jikkou.core.models.ConfigValue;
import io.streamthoughts.jikkou.core.models.DefaultResourceChange;
import io.streamthoughts.jikkou.core.models.HasMetadataChange;
import io.streamthoughts.jikkou.core.models.ObjectMeta;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ConfigEntryChangeComputerTest {

    public static final List<ConfigValue> EMPTY_LIST = Collections.emptyList();

    @Test
    void shouldGetAddChangeForNewValue() {
        // Given
        ConfigEntryChangeComputer computer = new ConfigEntryChangeComputer(true);
        List<ConfigValue> after = List.of(new ConfigValue("key", "value"));

        // When
        List<HasMetadataChange<ConfigEntryChange>> result = computer.computeChanges(EMPTY_LIST, after);

        // Then
        List<HasMetadataChange<ConfigEntryChange>> expected = after.stream()
                .map(it -> DefaultResourceChange.<ConfigEntryChange>builder()
                        .withMetadata(new ObjectMeta())
                        .withChange(new ConfigEntryChange(it.getName(), ValueChange.withAfterValue(it.value())))
                        .build()
                )
                .collect(Collectors.toList());
        Assertions.assertEquals(expected, result);
    }

    @Test
    void shouldGetAddChangeForNewValueAndExistingNullValue() {
        // Given
        ConfigEntryChangeComputer computer = new ConfigEntryChangeComputer(true);
        List<ConfigValue> before = List.of(new ConfigValue("key", null));
        List<ConfigValue> after = List.of(new ConfigValue("key", "value"));

        // When
        List<HasMetadataChange<ConfigEntryChange>> result = computer.computeChanges(before, after);

        // Then
        List<HasMetadataChange<ConfigEntryChange>> expected = after.stream()
                .map(it -> DefaultResourceChange.<ConfigEntryChange>builder()
                        .withMetadata(new ObjectMeta())
                        .withChange(new ConfigEntryChange(it.getName(), ValueChange.withAfterValue(it.value())))
                        .build()
                )
                .collect(Collectors.toList());
        Assertions.assertEquals(expected, result);
    }

    @Test
    void shouldGetUpdateChangeForNewValue() {
        // Given
        ConfigEntryChangeComputer computer = new ConfigEntryChangeComputer(true);
        ConfigValue newConfigValue = new ConfigValue("key", "new");
        ConfigValue oldConfigValue = new ConfigValue("key", "old");

        // When
        List<HasMetadataChange<ConfigEntryChange>> result = computer.computeChanges(
                List.of(oldConfigValue),
                List.of(newConfigValue)
        );

        // Then
        List<HasMetadataChange<ConfigEntryChange>> expected = List.of(
                        DefaultResourceChange.<ConfigEntryChange>builder()
                                .withMetadata(new ObjectMeta())
                                .withChange(new ConfigEntryChange("key", ValueChange.with( "old", "new")))
                                .build()
                );
        Assertions.assertEquals(expected, result);
    }

    @Test
    void shouldGetDeleteChangeForMissingValueGivenDeletionTrue() {
        // Given
        ConfigEntryChangeComputer computer = new ConfigEntryChangeComputer(true);

        // When
        List<ConfigValue> actual = List.of(new ConfigValue("key", "value"));

        List<HasMetadataChange<ConfigEntryChange>> result = computer.computeChanges(actual, EMPTY_LIST);

        // Then
        List<HasMetadataChange<ConfigEntryChange>> expected = List.of(
                DefaultResourceChange.<ConfigEntryChange>builder()
                        .withMetadata(new ObjectMeta())
                        .withChange(new ConfigEntryChange("key", ValueChange.withBeforeValue("value")))
                        .build()
        );
        Assertions.assertEquals(expected, result);
    }

    @Test
    void shouldGetNoDeleteChangeForMissingValueGivenDeletionFalse() {
        // Given
        ConfigEntryChangeComputer computer = new ConfigEntryChangeComputer(false);

        // When
        List<ConfigValue> actual = List.of(new ConfigValue("key", "value"));

        List<HasMetadataChange<ConfigEntryChange>> result = computer.computeChanges(actual, EMPTY_LIST);

        // Then
        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.isEmpty());
    }

}