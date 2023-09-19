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
import com.fasterxml.jackson.databind.node.TextNode;
import io.streamthoughts.jikkou.kafka.internals.DataSerdes;
import io.streamthoughts.jikkou.kafka.internals.serdes.KafkaJsonSerdes;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.Optional;
import org.apache.kafka.common.serialization.LongSerializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;

public enum DataFormat {

    BINARY {
        @Override
        public DataSerdes getDataSerdes() {
            return new DataSerdes() {
                @Override
                public Optional<ByteBuffer> serialize(String topicName,
                                                      DataHandle data,
                                                      Map<String, Object> properties,
                                                      boolean isForRecordKey) {
                    if (data == null) return Optional.empty();
                    try(LongSerializer serializer = new LongSerializer()) {
                        byte[] bytes = serializer.serialize(topicName, data.value().asLong());
                        return Optional.of(ByteBuffer.wrap(bytes));
                    }
                }

                @Override
                public Optional<DataHandle> deserialize(String topicName,
                                                        ByteBuffer data,
                                                        Map<String, Object> properties,
                                                        boolean isForRecordKey) {
                    return Optional.empty();
                }
            };
        }
    },
    STRING {
        @Override
        public DataSerdes getDataSerdes() {
            return new DataSerdes() {
                @Override
                public Optional<ByteBuffer> serialize(String topicName,
                                                      DataHandle data,
                                                      Map<String, Object> properties,
                                                      boolean isForRecordKey) {
                    if (data == null) return Optional.empty();
                    try(StringSerializer serializer = new StringSerializer()) {
                        serializer.configure(properties, isForRecordKey);
                        byte[] bytes = serializer.serialize(topicName, data.value().asText());
                        return Optional.of(ByteBuffer.wrap(bytes));
                    }
                }

                @Override
                public Optional<DataHandle> deserialize(String topicName,
                                                        ByteBuffer data,
                                                        Map<String, Object> properties,
                                                        boolean isForRecordKey) {
                    if (data == null) return Optional.empty();
                    try(StringDeserializer deserializer = new StringDeserializer()) {
                        deserializer.configure(properties, isForRecordKey);
                        String output = deserializer.deserialize(topicName, data.array());
                        return Optional.ofNullable(output).map(TextNode::new).map(DataHandle::new);
                    }
                }
            };
        }
    },
    JSON {
        @Override
        public DataSerdes getDataSerdes() {
            return new DataSerdes() {
                @Override
                public Optional<ByteBuffer> serialize(String topicName,
                                                      DataHandle data,
                                                      Map<String, Object> properties,
                                                      boolean isForRecordKey) {
                    if (data == null) return Optional.empty();
                    try(KafkaJsonSerdes serdes = new KafkaJsonSerdes(properties, isForRecordKey)) {
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
                    try(KafkaJsonSerdes serdes = new KafkaJsonSerdes(properties, isForRecordKey)) {
                        JsonNode output = serdes.deserialize(topicName, data.array());
                        return Optional.ofNullable(output).map(DataHandle::new);
                    }
                }
            };
        }
    },
    LONG {
        @Override
        public DataSerdes getDataSerdes() {
            return new DataSerdes() {
                @Override
                public Optional<ByteBuffer> serialize(String topicName,
                                                      DataHandle data,
                                                      Map<String, Object> properties,
                                                      boolean isForRecordKey) {
                    if (data == null) return Optional.empty();
                    byte[] bytes = new LongSerializer().serialize(topicName, data.value().asLong());
                    return Optional.of(ByteBuffer.wrap(bytes));
                }

                @Override
                public Optional<DataHandle> deserialize(String topicName,
                                                        ByteBuffer data,
                                                        Map<String, Object> properties,
                                                        boolean isForRecordKey) {
                    return Optional.empty();
                }
            };
        }
    };

    public abstract DataSerdes getDataSerdes();
}
