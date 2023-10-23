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

import io.streamthoughts.jikkou.core.change.ChangeType;
import io.streamthoughts.jikkou.core.change.ValueChange;
import io.streamthoughts.jikkou.core.models.GenericResourceChange;
import io.streamthoughts.jikkou.kafka.change.KafkaTableRecordChange;
import io.streamthoughts.jikkou.kafka.internals.KafkaRecord;
import io.streamthoughts.jikkou.kafka.model.DataHandle;
import io.streamthoughts.jikkou.kafka.model.DataType;
import io.streamthoughts.jikkou.kafka.model.DataValue;
import io.streamthoughts.jikkou.kafka.model.KafkaRecordHeader;
import io.streamthoughts.jikkou.kafka.models.V1KafkaTableRecordSpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class KafkaTableKafkaTableRecordChangeHandlerTest {

    @Test
    void shouldMapChangeToKafkaRecordForAddChangeType() {
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
        KafkaRecord< ByteBuffer, ByteBuffer> actual = KafkaTableRecordChangeHandler.toKafkaRecord(new GenericResourceChange<>(change));

        KafkaRecord<ByteBuffer, ByteBuffer> expected = KafkaRecord
                .<ByteBuffer, ByteBuffer>builder()
                .topic("test")
                .key(ByteBuffer.wrap("key".getBytes(StandardCharsets.UTF_8)))
                .value(ByteBuffer.wrap("value".getBytes(StandardCharsets.UTF_8)))
                .header("k", "v")
                .build();

        // Then
        Assertions.assertEquals(expected, actual);
    }
  
}