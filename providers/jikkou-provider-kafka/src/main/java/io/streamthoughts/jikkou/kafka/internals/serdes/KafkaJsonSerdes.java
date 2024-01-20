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
package io.streamthoughts.jikkou.kafka.internals.serdes;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.streamthoughts.jikkou.core.config.ConfigProperty;
import io.streamthoughts.jikkou.core.config.Configuration;
import java.util.Map;
import org.apache.commons.lang.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serializer;

/**
 * Serialize objects to UTF-8 JSON. This works with any object which is serializable with Jackson.
 */
public class KafkaJsonSerdes implements Serializer<JsonNode>, Deserializer<JsonNode> {

    private ObjectMapper objectMapper;

    private static final ConfigProperty<Boolean> PRETTY_PRINT = ConfigProperty.ofBoolean("json.pretty.print")
            .description("Whether JSON output should be indented (\"pretty-printed\")")
            .orElse(true);

    /**
     * Default constructor needed by Kafka
     */
    public KafkaJsonSerdes() {
    }

    public KafkaJsonSerdes(Map<String, ?> config, boolean isKey) {
        configure(config, isKey);
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public void configure(Map<String, ?> config, boolean isKey) {
        objectMapper = new ObjectMapper();
        objectMapper.configure(
                SerializationFeature.INDENT_OUTPUT,
                PRETTY_PRINT.get(Configuration.from(config))
        );
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public JsonNode deserialize(String topic, byte[] data) {
        if (data == null) {
            return null;
        }
        try {
            return objectMapper.readTree(data);
        } catch (Exception e) {
            throw new SerializationException("Error deserializing JSON message", e);
        }
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public byte[] serialize(String topic, JsonNode data) {
        if (data == null) {
            return null;
        }

        try {
            return objectMapper.writeValueAsBytes(data);
        } catch (Exception e) {
            throw new SerializationException("Error serializing JSON message", e);
        }
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public void close() {
    }
}