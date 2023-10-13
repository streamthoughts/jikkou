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
package io.streamthoughts.jikkou.kafka.connect.change;

import io.streamthoughts.jikkou.api.change.ChangeType;
import io.streamthoughts.jikkou.api.change.ValueChange;
import io.streamthoughts.jikkou.kafka.connect.models.KafkaConnectorState;
import java.util.Collections;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class KafkaConnectorChangeTest {

    @Test
    void shouldReturnFalseForStateOnlyGivenNoChange() {
        // Given
        KafkaConnectorChange change = new KafkaConnectorChange(
                ChangeType.NONE,
                "test",
                ValueChange.none("connector.class"),
                ValueChange.none(1),
                ValueChange.with(KafkaConnectorState.RUNNING, KafkaConnectorState.RUNNING),
                Collections.emptyList()
        );
        // When
        boolean stateOnlyChange = change.isStateOnlyChange();

        // Then
        Assertions.assertFalse(stateOnlyChange);
    }

    @Test
    void shouldReturnFalseForStateOnlyGivenMaxTasksUpdate() {
        // Given
        KafkaConnectorChange change = new KafkaConnectorChange(
                ChangeType.UPDATE,
                "test",
                ValueChange.none("connector.class"),
                ValueChange.with(1, 2), // UPDATE
                ValueChange.with(KafkaConnectorState.RUNNING, KafkaConnectorState.PAUSED),
                Collections.emptyList()
        );
        // When
        boolean stateOnlyChange = change.isStateOnlyChange();

        // Then
        Assertions.assertFalse(stateOnlyChange);
    }

    @Test
    void shouldReturnFalseForStateOnlyGivenMaxConnectorClassUpdate() {
        // Given
        KafkaConnectorChange change = new KafkaConnectorChange(
                ChangeType.UPDATE,
                "test",
                ValueChange.with("connector1.class", "connector2.class"), // UPDATE
                ValueChange.none(1),
                ValueChange.with(KafkaConnectorState.RUNNING, KafkaConnectorState.PAUSED),
                Collections.emptyList()
        );
        // When
        boolean stateOnlyChange = change.isStateOnlyChange();

        // Then
        Assertions.assertFalse(stateOnlyChange);
    }

    @Test
    void shouldReturnTrueForStateOnly() {
        // Given
        KafkaConnectorChange change = new KafkaConnectorChange(
                ChangeType.UPDATE,
                "test",
                ValueChange.none("connector.class"),
                ValueChange.none(1),
                ValueChange.with(KafkaConnectorState.RUNNING, KafkaConnectorState.PAUSED),
                Collections.emptyList()
        );
        // When
        boolean stateOnlyChange = change.isStateOnlyChange();

        // Then
        Assertions.assertTrue(stateOnlyChange);
    }

}