/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.data.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.TreeMap;
import org.jetbrains.annotations.Nullable;

public final class Json {

    private Json() {}

    private static final ObjectMapper OBJECT_MAPPER = JsonMapper
            .builder()
            .nodeFactory(new SortingNodeFactory())
            .configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true)
            .build();

    /**
     * Static utility method to normalize a JSON string.
     *
     * @param json the json string
     * @return the normalized json string, or {@code null} if the passed json was {@code null};
     * @throws RuntimeException if json parsing failed.
     */
    public static @Nullable String normalize(@Nullable String json) {
        if (json == null) return null;

        try {
            JsonNode jsonNode = OBJECT_MAPPER.readTree(json);
            return OBJECT_MAPPER.writeValueAsString(jsonNode);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    static class SortingNodeFactory extends JsonNodeFactory {
        @Override
        public ObjectNode objectNode() {
            return new ObjectNode(this, new TreeMap<>());
        }
    }
}
