/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.kafka.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.streamthoughts.jikkou.kafka.internals.DataSerde;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class DataConverterTest {

    public static final Map<String, Object> EMPTY_PROPS = Collections.emptyMap();
    public static final String TOPIC = "topic";

    @Test
    void shouldSerializeDeserializeStringForNonNull() {
        // Given
        DataSerde serde = DataType.STRING.getDataSerde();
        DataHandle value = DataHandle.ofString("value");

        // When / Then
        Optional<ByteBuffer> serialized = serde.serialize(
                TOPIC,
                value,
                EMPTY_PROPS,
                false
        );
        Assertions.assertTrue(serialized.isPresent());

        // When / Then
        Optional<DataHandle> deserialize = serde.deserialize(TOPIC, serialized.get(), EMPTY_PROPS, false);
        Assertions.assertTrue(deserialize.isPresent());
        Assertions.assertEquals(deserialize.get(), value);
    }

    @Test
    void shouldSerializeDeserializeBinaryForNonNull() {
        // Given
        String encoded = Base64.getEncoder().encodeToString("value".getBytes(StandardCharsets.UTF_8));
        DataSerde serde = DataType.BINARY.getDataSerde();
        DataHandle value = DataHandle.ofString(encoded);

        // When / Then
        Optional<ByteBuffer> serialized = serde.serialize(
                TOPIC,
                value,
                EMPTY_PROPS,
                false
        );
        Assertions.assertTrue(serialized.isPresent());

        // When / Then
        Optional<DataHandle> deserialize = serde.deserialize(TOPIC, serialized.get(), EMPTY_PROPS, false);
        Assertions.assertTrue(deserialize.isPresent());
        Assertions.assertEquals(deserialize.get(), value);
    }

    @Test
    void shouldSerializeDeserializeJsonForNonNull() throws IOException {
        // Given
        DataSerde serde = DataType.JSON.getDataSerde();
        JsonNode jsonNode = new ObjectMapper().readTree("""
                {
                    "key": "value"
                }
                """);
        DataHandle value = new DataHandle(jsonNode);

        // When / Then
        Optional<ByteBuffer> serialized = serde.serialize(
                TOPIC,
                value,
                EMPTY_PROPS,
                false
        );
        Assertions.assertTrue(serialized.isPresent());

        // When / Then
        Optional<DataHandle> deserialize = serde.deserialize(TOPIC, serialized.get(), EMPTY_PROPS, false);
        Assertions.assertTrue(deserialize.isPresent());
        Assertions.assertEquals(deserialize.get(), value);
    }
}