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
package io.streamthoughts.jikkou.kafka.change;

import com.fasterxml.jackson.databind.node.TextNode;
import io.streamthoughts.jikkou.core.models.CoreAnnotations;
import io.streamthoughts.jikkou.core.models.HasMetadataChange;
import io.streamthoughts.jikkou.core.models.ObjectMeta;
import io.streamthoughts.jikkou.core.reconcilier.ChangeType;
import io.streamthoughts.jikkou.core.reconcilier.change.ValueChange;
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
    void shouldReturnNoneChangeForIdentityResource() {
        // Given
        // When
        KafkaTableRecordChangeComputer computer = new KafkaTableRecordChangeComputer();
        List<KafkaTableRecordChange> actual = computer.computeChanges(List.of(KAFKA_TABLE_RECORD), List.of(KAFKA_TABLE_RECORD))
                .stream()
                .map(HasMetadataChange::getChange)
                .toList();
        // Then
        List<KafkaTableRecordChange> expected = List.of(KafkaTableRecordChange
                .builder()
                .withChangeType(ChangeType.NONE)
                .withRecord(ValueChange.none(KAFKA_TABLE_RECORD.getSpec()))
                .build()
        );
        Assertions.assertEquals(expected, actual);
    }

    @Test
    void shouldReturnAddChangeForNewResource() {
        // Given

        // When
        KafkaTableRecordChangeComputer computer = new KafkaTableRecordChangeComputer();

        List<KafkaTableRecordChange> actual = computer.computeChanges(List.of(), List.of(KAFKA_TABLE_RECORD))
                .stream()
                .map(HasMetadataChange::getChange)
                .toList();

        // Then
        List<KafkaTableRecordChange> expected = List.of(KafkaTableRecordChange
                .builder()
                .withChangeType(ChangeType.ADD)
                .withRecord(ValueChange.withAfterValue(KAFKA_TABLE_RECORD.getSpec()))
                .build()
        );
        Assertions.assertEquals(expected, actual);
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

        List<KafkaTableRecordChange> actual = computer.computeChanges(before, after)
                .stream()
                .map(HasMetadataChange::getChange)
                .toList();

        // Then
        List<KafkaTableRecordChange> expected = List.of(KafkaTableRecordChange
                .builder()
                .withChangeType(ChangeType.DELETE)
                .withRecord(ValueChange.withBeforeValue(KAFKA_TABLE_RECORD.getSpec()))
                .build()
        );
        Assertions.assertEquals(expected, actual);
    }

}