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
package io.streamthoughts.jikkou.kafka.change.record;

import com.fasterxml.jackson.databind.node.TextNode;
import io.streamthoughts.jikkou.core.models.CoreAnnotations;
import io.streamthoughts.jikkou.core.models.ObjectMeta;
import io.streamthoughts.jikkou.core.models.change.GenericResourceChange;
import io.streamthoughts.jikkou.core.models.change.ResourceChange;
import io.streamthoughts.jikkou.core.models.change.ResourceChangeSpec;
import io.streamthoughts.jikkou.core.models.change.StateChange;
import io.streamthoughts.jikkou.core.reconciler.Operation;
import io.streamthoughts.jikkou.kafka.model.DataHandle;
import io.streamthoughts.jikkou.kafka.model.DataType;
import io.streamthoughts.jikkou.kafka.model.DataValue;
import io.streamthoughts.jikkou.kafka.model.KafkaRecordHeader;
import io.streamthoughts.jikkou.kafka.models.V1KafkaTableRecord;
import io.streamthoughts.jikkou.kafka.models.V1KafkaTableRecordSpec;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class KafkaTableRecordChangeComputerTest {

    static final String TEST_TOPIC_NAME = "test";

    static final V1KafkaTableRecord KAFKA_TABLE_RECORD = V1KafkaTableRecord
            .builder()
            .withSpec(V1KafkaTableRecordSpec
                    .builder()
                    .withTopic(TEST_TOPIC_NAME)
                    .withHeader(new KafkaRecordHeader("k", "v"))
                    .withKey(new DataValue(DataType.STRING, new DataHandle(new TextNode("key"))))
                    .withValue(new DataValue(DataType.STRING, new DataHandle(new TextNode("value"))))
                    .build())
            .build();

    @Test
    void shouldReturnNoneChangeForExistingResource() {
        // Given
        List<V1KafkaTableRecord> resources = List.of(KAFKA_TABLE_RECORD);
        KafkaTableRecordChangeComputer computer = new KafkaTableRecordChangeComputer();

        // When
        List<ResourceChange> actual = computer.computeChanges(resources, resources);

        // Then
        ResourceChange expected = GenericResourceChange
                .builder(V1KafkaTableRecord.class)
                .withSpec(ResourceChangeSpec
                        .builder()
                        .withOperation(Operation.NONE)
                        .withChange(StateChange.none("record", KAFKA_TABLE_RECORD.getSpec()))
                        .build()
                )
                .build();
        Assertions.assertEquals(List.of(expected), actual);
    }

    @Test
    void shouldReturnCreateChangeForNewResource() {
        // Given

        // When
        KafkaTableRecordChangeComputer computer = new KafkaTableRecordChangeComputer();

        List<ResourceChange> actual = computer.computeChanges(List.of(), List.of(KAFKA_TABLE_RECORD))
                .stream()
                .toList();

        // Then
        ResourceChange expected = GenericResourceChange
                .builder(V1KafkaTableRecord.class)
                .withSpec(ResourceChangeSpec
                        .builder()
                        .withOperation(Operation.CREATE)
                        .withChange(StateChange.create("record", KAFKA_TABLE_RECORD.getSpec()))
                        .build()
                )
                .build();
        Assertions.assertEquals(List.of(expected), actual);
    }

    @Test
    void shouldReturnDeleteChangeForNewResource() {
        // Given
        List<V1KafkaTableRecord> before = List.of(KAFKA_TABLE_RECORD);

        V1KafkaTableRecord recordToDelete = KAFKA_TABLE_RECORD.withMetadata(ObjectMeta
                .builder()
                .withAnnotation(CoreAnnotations.JIKKOU_IO_DELETE, true)
                .build()
        );
        List<V1KafkaTableRecord> after = List.of(recordToDelete);

        // When
        KafkaTableRecordChangeComputer computer = new KafkaTableRecordChangeComputer();

        List<ResourceChange> actual = computer.computeChanges(before, after)
                .stream()
                .toList();

        // Then
        ResourceChange expected = GenericResourceChange
                .builder(V1KafkaTableRecord.class)
                .withSpec(ResourceChangeSpec
                        .builder()
                        .withOperation(Operation.DELETE)
                        .withChange(StateChange.delete("record", KAFKA_TABLE_RECORD.getSpec()))
                        .build()
                )
                .build();
        Assertions.assertEquals(List.of(expected), actual);
    }

}