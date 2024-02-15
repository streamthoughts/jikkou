/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.kafka.change.record;

import io.streamthoughts.jikkou.core.models.change.GenericResourceChange;
import io.streamthoughts.jikkou.core.models.change.ResourceChange;
import io.streamthoughts.jikkou.core.models.change.ResourceChangeSpec;
import io.streamthoughts.jikkou.core.models.change.StateChange;
import io.streamthoughts.jikkou.core.reconciler.Operation;
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

class KafkaTableRecordChangeHandlerTest {

    static final String KAFKA_TOPIC_TEST = "test";

    @Test
    void shouldMapChangeToKafkaRecordForAddChangeType() {
        // Given
        ResourceChange change = GenericResourceChange
                .builder()
                .withSpec(ResourceChangeSpec
                        .builder()
                        .withOperation(Operation.CREATE)
                        .withChange(StateChange.create("record",
                                V1KafkaTableRecordSpec
                                        .builder()
                                        .withTopic(KAFKA_TOPIC_TEST)
                                        .withHeader(new KafkaRecordHeader("k", "v"))
                                        .withKey(new DataValue(DataType.STRING, DataHandle.ofString("key")))
                                        .withValue(new DataValue(DataType.STRING, DataHandle.ofString("value")))
                                        .build()))
                        .build()
                )
                .build();
        // When
        KafkaRecord<ByteBuffer, ByteBuffer> actual = KafkaTableRecordChangeHandler.toKafkaRecord(change);

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