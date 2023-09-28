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

import io.streamthoughts.jikkou.api.change.ChangeType;
import io.streamthoughts.jikkou.api.change.ValueChange;
import io.streamthoughts.jikkou.api.model.GenericResourceChange;
import io.streamthoughts.jikkou.kafka.change.KafkaTableRecordChange;
import io.streamthoughts.jikkou.kafka.model.DataHandle;
import io.streamthoughts.jikkou.kafka.model.DataType;
import io.streamthoughts.jikkou.kafka.model.DataValue;
import io.streamthoughts.jikkou.kafka.model.KafkaRecordHeader;
import io.streamthoughts.jikkou.kafka.models.V1KafkaTableRecordSpec;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class KafkaTableKafkaTableRecordChangeDescriptionTest {

    @Test
    void shouldGetTextualDescription() {
        // Given
        KafkaTableRecordChange change = KafkaTableRecordChange
                .builder()
                .withTopic("test")
                .withChangeType(ChangeType.ADD)
                .withRecord(ValueChange.withAfterValue(V1KafkaTableRecordSpec
                        .builder()
                        .withHeader(new KafkaRecordHeader("k", "v"))
                        .withKey(new DataValue(DataType.STRING, DataHandle.ofString("key")))
                        .withValue(new DataValue(DataType.STRING, DataHandle.ofString("value")))
                        .build()
                ))
                .build();

        // When
        KafkaTableRecordChangeDescription description = new KafkaTableRecordChangeDescription(new GenericResourceChange<>(change));
        String textual = description.textual();

        // Then
        Assertions.assertEquals("Add record for key '\"key\"' into topic 'test'", textual);
    }
}