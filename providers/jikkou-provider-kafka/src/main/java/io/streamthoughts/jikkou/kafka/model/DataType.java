/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.kafka.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.TextNode;
import io.streamthoughts.jikkou.kafka.internals.DataSerde;
import io.streamthoughts.jikkou.kafka.internals.serdes.KafkaJsonSerdes;
import java.nio.ByteBuffer;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;

/**
 * The data type supported for a key/value Kafka record.
 */
public enum DataType {

    BINARY {
        /** {@inheritDoc} **/
        @Override
        public DataSerde getDataSerde() {
            return new BinarySerde();
        }
    },
    STRING {
        /** {@inheritDoc} **/
        @Override
        public DataSerde getDataSerde() {
            return new StringSerde();
        }
    },
    JSON {
        /** {@inheritDoc} **/
        @Override
        public DataSerde getDataSerde() {
            return new JsonSerde();
        }
    };

    /**
     * Gets the {@link DataSerde} for this format.
     *
     * @return  a new {@link DataSerde}.
     */
    public abstract DataSerde getDataSerde();

    /**
     * Data Serde for Json.
     */
    static class JsonSerde implements DataSerde {
        @Override
        public Optional<ByteBuffer> serialize(String topicName,
                                              DataHandle data,
                                              Map<String, Object> properties,
                                              boolean isForRecordKey) {
            if (data == null || data.isNull()) return Optional.empty();
            try (KafkaJsonSerdes serdes = new KafkaJsonSerdes(properties, isForRecordKey)) {
                byte[] bytes = serdes.serialize(topicName, data.value());
                return Optional.of(ByteBuffer.wrap(bytes));
            }
        }

        @Override
        public Optional<DataHandle> deserialize(String topicName,
                                                ByteBuffer data,
                                                Map<String, Object> properties,
                                                boolean isForRecordKey) {
            if (data == null) return Optional.empty();
            try (KafkaJsonSerdes serdes = new KafkaJsonSerdes(properties, isForRecordKey)) {
                JsonNode output = serdes.deserialize(topicName, data.array());
                return Optional.ofNullable(output).map(DataHandle::new);
            }
        }
    }

    /**
     * Data Serde for binary content encoded in base64.
     */
    static class BinarySerde implements DataSerde {
        @Override
        public Optional<ByteBuffer> serialize(String topicName,
                                              DataHandle data,
                                              Map<String, Object> properties,
                                              boolean isForRecordKey) {
            if (data == null || data.isNull()) return Optional.empty();
            String encoded = data.value().asText();
            return Optional.of(ByteBuffer.wrap(Base64.getDecoder().decode(encoded)));
        }

        @Override
        public Optional<DataHandle> deserialize(String topicName,
                                                ByteBuffer data,
                                                Map<String, Object> properties,
                                                boolean isForRecordKey) {
            if (data == null) return Optional.empty();
            byte[] array = data.array();
            return Optional
                    .of(Base64.getEncoder().encodeToString(array))
                    .map(TextNode::new)
                    .map(DataHandle::new);
        }
    }

    /**
     * Data Serde for Long.
     */
    static class StringSerde implements DataSerde {
        /** {@inheritDoc} **/
        @Override
        public Optional<ByteBuffer> serialize(String topicName,
                                              DataHandle data,
                                              Map<String, Object> properties,
                                              boolean isForRecordKey) {
            if (data == null || data.isNull()) return Optional.empty();
            try (StringSerializer serializer = new StringSerializer()) {
                serializer.configure(properties, isForRecordKey);
                byte[] bytes = serializer.serialize(topicName, data.value().asText());
                return Optional.of(ByteBuffer.wrap(bytes));
            }
        }
        /** {@inheritDoc} **/
        @Override
        public Optional<DataHandle> deserialize(String topicName,
                                                ByteBuffer data,
                                                Map<String, Object> properties,
                                                boolean isForRecordKey) {
            if (data == null) return Optional.empty();
            try (StringDeserializer deserializer = new StringDeserializer()) {
                deserializer.configure(properties, isForRecordKey);
                String output = deserializer.deserialize(topicName, data.array());
                return Optional
                        .ofNullable(output)
                        .map(TextNode::new)
                        .map(DataHandle::new);
            }
        }
    }
}
