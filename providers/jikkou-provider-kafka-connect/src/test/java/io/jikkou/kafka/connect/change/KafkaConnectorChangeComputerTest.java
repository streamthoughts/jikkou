/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.jikkou.kafka.connect.change;

import io.jikkou.core.models.ObjectMeta;
import io.jikkou.core.models.change.GenericResourceChange;
import io.jikkou.core.models.change.ResourceChange;
import io.jikkou.core.models.change.ResourceChangeSpec;
import io.jikkou.core.models.change.StateChange;
import io.jikkou.core.reconciler.Operation;
import io.jikkou.kafka.connect.KafkaConnectExtensionProvider;
import io.jikkou.kafka.connect.models.KafkaConnectorState;
import io.jikkou.kafka.connect.models.V1KafkaConnector;
import io.jikkou.kafka.connect.models.V1KafkaConnectorSpec;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
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
    void shouldIgnoreStateChangeGivenNoDesiredState() {
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
                        .withConfig(Map.of("topics", "connect-test"))
                        .build()
                )
                .build();
        V1KafkaConnector oldConnector = newConnector
                .toBuilder()
                .withSpec(newConnector.getSpec().toBuilder().withState(KafkaConnectorState.PAUSED).build())
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
                        .withOperation(Operation.NONE)
                        .withChange(StateChange.none(KafkaConnectorChangeComputer.DATA_CONNECTOR_CLASS, "FileStreamSink"))
                        .withChange(StateChange.none(KafkaConnectorChangeComputer.DATA_TASKS_MAX, 1))
                        .withChange(StateChange.none(KafkaConnectorChangeComputer.DATA_STATE, KafkaConnectorState.PAUSED))
                        .withChange(StateChange.none("config.topics", "connect-test"))
                        .build()
                )
                .build();
        Assertions.assertEquals(List.of(expected), results);
    }

    @Test
    void shouldReturnFalseForConfigChangeGivenNoChange() {
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
        boolean configChange = KafkaConnectorChangeHandler.hasConfigChange(change);

        // Then
        Assertions.assertFalse(configChange);
    }

    @Test
    void shouldReturnTrueForConfigChangeGivenMaxTasksUpdate() {
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
        boolean configChange = KafkaConnectorChangeHandler.hasConfigChange(change);

        // Then
        Assertions.assertTrue(configChange);
    }

    @Test
    void shouldReturnTrueForConfigChangeGivenConnectorClassUpdate() {
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
        boolean configChange = KafkaConnectorChangeHandler.hasConfigChange(change);

        // Then
        Assertions.assertTrue(configChange);
    }

    @Test
    void shouldReturnFalseForConfigChangeGivenStateOnlyUpdate() {
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
        boolean configChange = KafkaConnectorChangeHandler.hasConfigChange(change);

        // Then
        Assertions.assertFalse(configChange);
    }

    private static final Pattern MASKED_VALUE_PATTERN =
            Pattern.compile(KafkaConnectExtensionProvider.DEFAULT_MASKED_CONFIG_VALUE_PATTERN);

    private static final String MASKED_VALUE = "\u2022\u2022\u2022\u2022\u2022\u2022\u2022\u2022";

    private static final String SECRET_REF = "${file:/path/secrets.properties:connection.password}";

    private static V1KafkaConnector newConnectorWithConfig(Map<String, Object> config) {
        return V1KafkaConnector
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
                        .withConfig(config)
                        .withState(KafkaConnectorState.RUNNING)
                        .build()
                )
                .build();
    }

    private static List<StateChange> computeConfigChanges(KafkaConnectorChangeComputer computer,
                                                          V1KafkaConnector actual,
                                                          V1KafkaConnector expected) {
        List<ResourceChange> results = computer.computeChanges(List.of(actual), List.of(expected));
        Assertions.assertEquals(1, results.size());
        return results.getFirst().getSpec().getChanges()
                .stream()
                .filter(change -> change.getName().startsWith(KafkaConnectorChangeComputer.DATA_CONFIG_PREFIX))
                .map(StateChange.class::cast)
                .toList();
    }

    @Test
    void shouldReturnNoneGivenMaskedActualValue() {
        V1KafkaConnector actual = newConnectorWithConfig(Map.of("topics", "test", "connection.password", MASKED_VALUE));
        V1KafkaConnector expected = newConnectorWithConfig(Map.of("topics", "test", "connection.password", SECRET_REF));

        KafkaConnectorChangeComputer computer = new KafkaConnectorChangeComputer(MASKED_VALUE_PATTERN);

        List<ResourceChange> results = computer.computeChanges(List.of(actual), List.of(expected));

        Assertions.assertEquals(Operation.NONE, results.getFirst().getSpec().getOp());
        Assertions.assertTrue(computeConfigChanges(computer, actual, expected)
                .contains(StateChange.none("config.connection.password", SECRET_REF)));
    }

    @Test
    void shouldReturnUpdateGivenMaskedActualValueAndNoPattern() {
        V1KafkaConnector actual = newConnectorWithConfig(Map.of("connection.password", MASKED_VALUE));
        V1KafkaConnector expected = newConnectorWithConfig(Map.of("connection.password", SECRET_REF));

        List<StateChange> changes = computeConfigChanges(new KafkaConnectorChangeComputer(), actual, expected);

        Assertions.assertEquals(
                List.of(StateChange.update("config.connection.password", MASKED_VALUE, SECRET_REF)),
                changes);
    }

    @Test
    void shouldReturnDeleteGivenMaskedActualValueRemovedFromExpected() {
        V1KafkaConnector actual = newConnectorWithConfig(Map.of("topics", "test", "connection.password", MASKED_VALUE));
        V1KafkaConnector expected = newConnectorWithConfig(Map.of("topics", "test"));

        List<StateChange> changes = computeConfigChanges(
                new KafkaConnectorChangeComputer(MASKED_VALUE_PATTERN), actual, expected);

        Assertions.assertTrue(changes.contains(StateChange.delete("config.connection.password", MASKED_VALUE)));
    }

    @Test
    void shouldReturnUpdateGivenUnmaskedActualValueChanged() {
        V1KafkaConnector actual = newConnectorWithConfig(Map.of("topics", "old", "connection.password", MASKED_VALUE));
        V1KafkaConnector expected = newConnectorWithConfig(Map.of("topics", "new", "connection.password", SECRET_REF));

        List<StateChange> changes = computeConfigChanges(
                new KafkaConnectorChangeComputer(MASKED_VALUE_PATTERN), actual, expected);

        Assertions.assertTrue(changes.contains(StateChange.update("config.topics", "old", "new")));
        Assertions.assertTrue(changes.contains(StateChange.none("config.connection.password", SECRET_REF)));
    }

    @Test
    void shouldSupportCustomMaskedValuePattern() {
        V1KafkaConnector actual = newConnectorWithConfig(Map.of("connection.password", "[hidden]"));
        V1KafkaConnector expected = newConnectorWithConfig(Map.of("connection.password", SECRET_REF));

        List<StateChange> changes = computeConfigChanges(
                new KafkaConnectorChangeComputer(Pattern.compile("^\\[hidden\\]$")), actual, expected);

        Assertions.assertEquals(List.of(StateChange.none("config.connection.password", SECRET_REF)), changes);
    }
}
