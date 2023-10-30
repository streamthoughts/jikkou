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

import io.streamthoughts.jikkou.core.models.Configs;
import io.streamthoughts.jikkou.core.models.DefaultResourceChange;
import io.streamthoughts.jikkou.core.models.HasMetadataChange;
import io.streamthoughts.jikkou.core.models.ObjectMeta;
import io.streamthoughts.jikkou.core.reconcilier.Change;
import io.streamthoughts.jikkou.core.reconcilier.ChangeType;
import io.streamthoughts.jikkou.core.reconcilier.change.ConfigEntryChange;
import io.streamthoughts.jikkou.core.reconcilier.change.ValueChange;
import io.streamthoughts.jikkou.kafka.connect.models.KafkaConnectorState;
import io.streamthoughts.jikkou.kafka.connect.models.V1KafkaConnector;
import io.streamthoughts.jikkou.kafka.connect.models.V1KafkaConnectorSpec;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


class KafkaConnectorChangeComputerTest {

    @Test
    void shouldComputeChangeForNewConnector() {
        // Given
        V1KafkaConnector newConnector = V1KafkaConnector
                .builder()
                .withMetadata(ObjectMeta
                        .builder()
                        .withName("test")
                        .withLabel("kafka.jikkou.io/connect-cluster", "test")
                        .build()
                )
                .withSpec(V1KafkaConnectorSpec
                        .builder()
                        .withConnectorClass("FileStreamSink")
                        .withTasksMax(1)
                        .withConfig(Configs.of(
                                "topics", "connect-test",
                                "file", "/tmp/test.sink.txt"
                        ))
                        .withState(KafkaConnectorState.RUNNING)
                        .build()
                )
                .build();
        KafkaConnectorChangeComputer computer = new KafkaConnectorChangeComputer();

        // When
        List<HasMetadataChange<KafkaConnectorChange>> results = computer.computeChanges(
                Collections.emptyList(),
                List.of(newConnector));

        // Then
        DefaultResourceChange<Change> change = DefaultResourceChange
                .builder()
                .withMetadata(ObjectMeta
                    .builder()
                    .withName("test")
                    .withLabel("kafka.jikkou.io/connect-cluster", "test")
                    .build()
                )
                .withChange(new KafkaConnectorChange(
                    ChangeType.ADD,
                    "test",
                    ValueChange.withAfterValue("FileStreamSink"),
                    ValueChange.withAfterValue(1),
                    ValueChange.withAfterValue(KafkaConnectorState.RUNNING),
                    List.of(
                        new ConfigEntryChange("file", ValueChange.withAfterValue("/tmp/test.sink.txt")),
                        new ConfigEntryChange("topics", ValueChange.withAfterValue("connect-test"))
                    )
                ))
                .build();
        Assertions.assertEquals(List.of(change), results);
    }

    @Test
    void shouldComputeChangeForUpdateConnector() {
        // Given
        V1KafkaConnector newConnector = V1KafkaConnector
                .builder()
                .withMetadata(ObjectMeta
                        .builder()
                        .withName("test")
                        .withLabel("kafka.jikkou.io/connect-cluster", "test")
                        .build()
                )
                .withSpec(V1KafkaConnectorSpec
                        .builder()
                        .withConnectorClass("FileStreamSink")
                        .withTasksMax(1)
                        .withConfig(Configs.of(
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
                        .withName("test")
                        .withLabel("kafka.jikkou.io/connect-cluster", "test")
                        .build()
                )
                .withSpec(V1KafkaConnectorSpec
                        .builder()
                        .withConnectorClass("FileStreamSink")
                        .withTasksMax(2)
                        .withConfig(Configs.of(
                                "topics", "connect-test",
                                "file", "/tmp/test.sink.txt"
                        ))
                        .withState(KafkaConnectorState.RUNNING)
                        .build()
                )
                .build();
        KafkaConnectorChangeComputer computer = new KafkaConnectorChangeComputer();

        // When
        List<HasMetadataChange<KafkaConnectorChange>> results = computer.computeChanges(
                List.of(oldConnector),
                List.of(newConnector));

        // Then
        DefaultResourceChange<Change> change = DefaultResourceChange
                .builder()
                .withMetadata(ObjectMeta
                        .builder()
                        .withName("test")
                        .withLabel("kafka.jikkou.io/connect-cluster", "test")
                        .build()
                )
                .withChange(new KafkaConnectorChange(
                        ChangeType.UPDATE,
                        "test",
                        ValueChange.none("FileStreamSink"),
                        ValueChange.with(2, 1),
                        ValueChange.none(KafkaConnectorState.RUNNING),
                        List.of(
                                new ConfigEntryChange("file", ValueChange.none("/tmp/test.sink.txt")),
                                new ConfigEntryChange("topics", ValueChange.none("connect-test"))
                        )
                ))
                .build();
        Assertions.assertEquals(List.of(change), results);
    }

}