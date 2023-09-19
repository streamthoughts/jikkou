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
import io.streamthoughts.jikkou.JikkouMetadataAnnotations;
import io.streamthoughts.jikkou.api.change.ChangeType;
import io.streamthoughts.jikkou.api.change.ValueChange;
import io.streamthoughts.jikkou.api.model.HasMetadataChange;
import io.streamthoughts.jikkou.api.model.ObjectMeta;
import io.streamthoughts.jikkou.kafka.model.DataFormat;
import io.streamthoughts.jikkou.kafka.model.DataHandle;
import io.streamthoughts.jikkou.kafka.model.KafkaRecordHeader;
import io.streamthoughts.jikkou.kafka.models.KafkaRecordData;
import io.streamthoughts.jikkou.kafka.models.V1KafkaTableRecord;
import io.streamthoughts.jikkou.kafka.models.V1KafkaTableRecordSpec;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class RecordChangeComputerTest {

    static final String TEST_TOPIC_NAME = "test";

    static final KafkaRecordData KAFKA_RECORD_DATA = KafkaRecordData
            .builder()
            .withHeader(new KafkaRecordHeader("k", "v"))
            .withKey(new DataHandle(new TextNode("key")))
            .withValue(new DataHandle(new TextNode("value")))
            .build();

    static final V1KafkaTableRecord KAFKA_TABLE_RECORD = V1KafkaTableRecord
            .builder()
            .withMetadata(ObjectMeta.builder()
                    .withName(TEST_TOPIC_NAME)
                    .build()
            )
            .withSpec(V1KafkaTableRecordSpec
                    .builder()
                    .withKeyFormat(DataFormat.STRING)
                    .withValueFormat(DataFormat.STRING)
                    .withRecord(KAFKA_RECORD_DATA)
                    .build()
            )
            .build();


    @Test
    void shouldReturnNoneChangeForIdentityResource() {
        // Given
        // When
        RecordChangeComputer computer = new RecordChangeComputer();
        List<RecordChange> actual = computer.computeChanges(List.of(KAFKA_TABLE_RECORD), List.of(KAFKA_TABLE_RECORD))
                .stream()
                .map(HasMetadataChange::getChange)
                .toList();
        // Then
        List<RecordChange> expected = List.of(RecordChange
                .builder()
                .withTopic(TEST_TOPIC_NAME)
                .withChangeType(ChangeType.NONE)
                .withKeyFormat(DataFormat.STRING)
                .withValueFormat(DataFormat.STRING)
                .withRecord(ValueChange.none(KAFKA_RECORD_DATA))
                .build()
        );
        Assertions.assertEquals(expected, actual);
    }

    @Test
    void shouldReturnAddChangeForNewResource() {
        // Given

        // When
        RecordChangeComputer computer = new RecordChangeComputer();

        List<RecordChange> actual = computer.computeChanges(List.of(), List.of(KAFKA_TABLE_RECORD))
                .stream()
                .map(HasMetadataChange::getChange)
                .toList();

        // Then
        List<RecordChange> expected = List.of(RecordChange
                .builder()
                .withTopic(TEST_TOPIC_NAME)
                .withChangeType(ChangeType.ADD)
                .withKeyFormat(DataFormat.STRING)
                .withValueFormat(DataFormat.STRING)
                .withRecord(ValueChange.withAfterValue(KAFKA_RECORD_DATA))
                .build()
        );
        Assertions.assertEquals(expected, actual);
    }

    @Test
    void shouldReturnDeleteChangeForNewResource() {
        // Given
        List<V1KafkaTableRecord> before = List.of(KAFKA_TABLE_RECORD);

        V1KafkaTableRecord recordToDelete = KAFKA_TABLE_RECORD.withMetadata(
                KAFKA_TABLE_RECORD
                        .getMetadata().toBuilder()
                        .withAnnotation(JikkouMetadataAnnotations.JIKKOU_IO_DELETE, true)
                        .build()
        );
        List<V1KafkaTableRecord> after = List.of(recordToDelete);

        // When
        RecordChangeComputer computer = new RecordChangeComputer();

        List<RecordChange> actual = computer.computeChanges(before, after)
                .stream()
                .map(HasMetadataChange::getChange)
                .toList();

        // Then
        List<RecordChange> expected = List.of(RecordChange
                .builder()
                .withTopic(TEST_TOPIC_NAME)
                .withChangeType(ChangeType.DELETE)
                .withKeyFormat(DataFormat.STRING)
                .withValueFormat(DataFormat.STRING)
                .withRecord(ValueChange.withBeforeValue(KAFKA_RECORD_DATA))
                .build()
        );
        Assertions.assertEquals(expected, actual);
    }

}