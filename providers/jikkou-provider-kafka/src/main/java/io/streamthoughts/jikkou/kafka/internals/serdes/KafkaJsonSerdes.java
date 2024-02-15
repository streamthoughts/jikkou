/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
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