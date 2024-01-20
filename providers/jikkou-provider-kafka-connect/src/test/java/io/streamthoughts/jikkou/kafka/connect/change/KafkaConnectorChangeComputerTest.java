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

import io.streamthoughts.jikkou.core.models.ObjectMeta;
import io.streamthoughts.jikkou.core.models.change.GenericResourceChange;
import io.streamthoughts.jikkou.core.models.change.ResourceChange;
import io.streamthoughts.jikkou.core.models.change.ResourceChangeSpec;
import io.streamthoughts.jikkou.core.models.change.StateChange;
import io.streamthoughts.jikkou.core.reconciler.Operation;
import io.streamthoughts.jikkou.kafka.connect.models.KafkaConnectorState;
import io.streamthoughts.jikkou.kafka.connect.models.V1KafkaConnector;
import io.streamthoughts.jikkou.kafka.connect.models.V1KafkaConnectorSpec;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class KafkaConnectorChangeComputerTest {

    public static final String TEST_CONNECTOR_NAME = "test";

    @Test
    void shouldComputeChangeForNewConnector() {
        // Given
        V1KafkaConnector newConnector = V1KafkaConnector
                .builder()
                .withMetadata(ObjectMeta
                        .builder()
                        .withName(TEST_CONNECTOR_NAME)
                        .withLabel("kafka.jikkou.io/connect-cluster", TEST_CONNECTOR_NAME)
                        .build()
                )
                .withSpec(V1KafkaConnectorSpec
                        .builder()
                        .withConnectorClass("FileStreamSink")
                        .withTasksMax(1)
                        .withConfig(Map.of(
                                "topics", "connect-test",
                                "file", "/tmp/test.sink.txt"
                        ))
                        .withState(KafkaConnectorState.RUNNING)
                        .build()
                )
                .build();
        KafkaConnectorChangeComputer computer = new KafkaConnectorChangeComputer();

        // When
        List<ResourceChange> results = computer.computeChanges(
                Collections.emptyList(),
                List.of(newConnector));

        ResourceChange expected = GenericResourceChange
                .builder(V1KafkaConnector.class)
                .withMetadata(newConnector.getMetadata())
                .withSpec(ResourceChangeSpec
                        .builder()
                        .withOperation(Operation.CREATE)
                        .withChange(StateChange.create("connectorClass", "FileStreamSink"))
                        .withChange(StateChange.create("tasksMax", 1))
                        .withChange(StateChange.create(KafkaConnectorChangeComputer.DATA_STATE, KafkaConnectorState.RUNNING))
                        .withChange(StateChange.create("config.topics", "connect-test"))
                        .withChange(StateChange.create("config.file", "/tmp/test.sink.txt"))
                        .build()
                )
                .build();

        // Then
        Assertions.assertEquals(List.of(expected), results);
    }

    @Test
    void shouldComputeChangeForUpdateConnector() {
        // Given
        V1KafkaConnector newConnector = V1KafkaConnector
                .builder()
                .withMetadata(ObjectMeta
                        .builder()
                        .withName(TEST_CONNECTOR_NAME)
                        .withLabel("kafka.jikkou.io/connect-cluster", TEST_CONNECTOR_NAME)
                        .build()
                )
                .withSpec(V1KafkaConnectorSpec
                        .builder()
                        .withConnectorClass("FileStreamSink")
                        .withTasksMax(1)
                        .withConfig(Map.of(
                                "topics", "connect-test",
                                "file", "/tmp/test.sink.txt"
                        ))
                        .withState(KafkaConnectorState.RUNNING)
                        .build()
                )
                .build();
        V1KafkaConnector oldConnector = V1KafkaConnector
                .builder()
                .withMetadata(ObjectMeta
                        .builder()
                        .withName(TEST_CONNECTOR_NAME)
                        .withLabel("kafka.jikkou.io/connect-cluster", TEST_CONNECTOR_NAME)
                        .build()
                )
                .withSpec(V1KafkaConnectorSpec
                        .builder()
                        .withConnectorClass("FileStreamSink")
                        .withTasksMax(2)
                        .withConfig(Map.of(
                                "topics", "connect-test",
                                "file", "/tmp/test.sink.txt"
                        ))
                        .withState(KafkaConnectorState.RUNNING)
                        .build()
                )
                .build();
        KafkaConnectorChangeComputer computer = new KafkaConnectorChangeComputer();

        // When
        List<ResourceChange> results = computer.computeChanges(
                List.of(oldConnector),
                List.of(newConnector));

        // Then
        ResourceChange expected = GenericResourceChange
                .builder(V1KafkaConnector.class)
                .withMetadata(newConnector.getMetadata())
                .withSpec(ResourceChangeSpec
                        .builder()
                        .withOperation(Operation.UPDATE)
                        .withChange(StateChange.none(KafkaConnectorChangeComputer.DATA_CONNECTOR_CLASS, "FileStreamSink"))
                        .withChange(StateChange.update(KafkaConnectorChangeComputer.DATA_TASKS_MAX, 2, 1))
                        .withChange(StateChange.none(KafkaConnectorChangeComputer.DATA_STATE, KafkaConnectorState.RUNNING))
                        .withChange(StateChange.none("config.topics", "connect-test"))
                        .withChange(StateChange.none("config.file", "/tmp/test.sink.txt"))
                        .build()
                )
                .build();
        Assertions.assertEquals(List.of(expected), results);
    }

    @Test
    void shouldReturnFalseForStateOnlyGivenNoChange() {
        // Given
        ResourceChange change = GenericResourceChange
                .builder()
                .withSpec(ResourceChangeSpec
                        .builder()
                        .withOperation(Operation.NONE)
                        .withChange(StateChange.none(KafkaConnectorChangeComputer.DATA_CONNECTOR_CLASS, "connector.class"))
                        .withChange(StateChange.none(KafkaConnectorChangeComputer.DATA_TASKS_MAX, 1))
                        .withChange(StateChange.none(KafkaConnectorChangeComputer.DATA_STATE, KafkaConnectorState.RUNNING))
                        .build()
                )
                .build();
        // When
        boolean stateOnlyChange = KafkaConnectorChangeHandler.isStateOnlyChange(change);

        // Then
        Assertions.assertFalse(stateOnlyChange);
    }

    @Test
    void shouldReturnFalseForStateOnlyGivenMaxTasksUpdate() {
        // Given
        ResourceChange change = GenericResourceChange
                .builder()
                .withSpec(ResourceChangeSpec
                        .builder()
                        .withOperation(Operation.UPDATE)
                        .withChange(StateChange.none(KafkaConnectorChangeComputer.DATA_CONNECTOR_CLASS, "connector.class"))
                        .withChange(StateChange.with(KafkaConnectorChangeComputer.DATA_TASKS_MAX, 1, 2))
                        .withChange(StateChange.with(KafkaConnectorChangeComputer.DATA_STATE, KafkaConnectorState.RUNNING, KafkaConnectorState.PAUSED))
                        .build()
                )
                .build();
        // When
        boolean stateOnlyChange = KafkaConnectorChangeHandler.isStateOnlyChange(change);

        // Then
        Assertions.assertFalse(stateOnlyChange);
    }

    @Test
    void shouldReturnFalseForStateOnlyGivenConnectorClassUpdate() {
        // Given
        ResourceChange change = GenericResourceChange
                .builder()
                .withSpec(ResourceChangeSpec
                        .builder()
                        .withOperation(Operation.UPDATE)
                        .withChange(StateChange.with(KafkaConnectorChangeComputer.DATA_CONNECTOR_CLASS, "connector1.class", "connector2.class"))
                        .withChange(StateChange.none(KafkaConnectorChangeComputer.DATA_TASKS_MAX, 1 ))
                        .withChange(StateChange.with(KafkaConnectorChangeComputer.DATA_STATE, KafkaConnectorState.RUNNING, KafkaConnectorState.PAUSED))
                        .build()
                )
                .build();
        // When
        boolean stateOnlyChange = KafkaConnectorChangeHandler.isStateOnlyChange(change);

        // Then
        Assertions.assertFalse(stateOnlyChange);
    }

    @Test
    void shouldReturnTrueForStateOnly() {
        // Given
        ResourceChange change = GenericResourceChange
                .builder()
                .withSpec(ResourceChangeSpec
                        .builder()
                        .withOperation(Operation.UPDATE)
                        .withChange(StateChange.none(KafkaConnectorChangeComputer.DATA_CONNECTOR_CLASS, "connector.class"))
                        .withChange(StateChange.none(KafkaConnectorChangeComputer.DATA_TASKS_MAX, 1 ))
                        .withChange(StateChange.with(KafkaConnectorChangeComputer.DATA_STATE, KafkaConnectorState.RUNNING, KafkaConnectorState.PAUSED))
                        .build()
                )
                .build();
        // When
        boolean stateOnlyChange = KafkaConnectorChangeHandler.isStateOnlyChange(change);

        // Then
        Assertions.assertTrue(stateOnlyChange);
    }

}