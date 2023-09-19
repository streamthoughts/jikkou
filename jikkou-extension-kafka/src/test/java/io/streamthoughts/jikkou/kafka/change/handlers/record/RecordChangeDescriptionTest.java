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
package io.streamthoughts.jikkou.kafka.change.handlers.record;

import com.fasterxml.jackson.databind.node.TextNode;
import io.streamthoughts.jikkou.api.change.ChangeType;
import io.streamthoughts.jikkou.api.change.ValueChange;
import io.streamthoughts.jikkou.api.model.GenericResourceChange;
import io.streamthoughts.jikkou.kafka.change.RecordChange;
import io.streamthoughts.jikkou.kafka.model.DataFormat;
import io.streamthoughts.jikkou.kafka.model.DataHandle;
import io.streamthoughts.jikkou.kafka.model.KafkaRecordHeader;
import io.streamthoughts.jikkou.kafka.models.KafkaRecordData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class RecordChangeDescriptionTest {

    @Test
    void shouldGetTextualDescription() {
        // Given
        RecordChange change = RecordChange
                .builder()
                .withTopic("test")
                .withChangeType(ChangeType.ADD)
                .withKeyFormat(DataFormat.STRING)
                .withValueFormat(DataFormat.STRING)
                .withRecord(ValueChange.withAfterValue(KafkaRecordData
                        .builder()
                        .withHeader(new KafkaRecordHeader("k", "v"))
                        .withKey(new DataHandle(new TextNode("key")))
                        .withValue(new DataHandle(new TextNode("value")))
                        .build()
                ))
                .build();

        // When
        RecordChangeDescription description = new RecordChangeDescription(new GenericResourceChange<>(change));
        String textual = description.textual();

        // Then
        Assertions.assertEquals("Add record for key '\"key\"' into topic 'test'", textual);
    }
}