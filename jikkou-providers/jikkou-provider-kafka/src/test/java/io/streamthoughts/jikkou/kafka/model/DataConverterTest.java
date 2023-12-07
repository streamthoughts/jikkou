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