/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.http.client.serdes;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public interface JSON {

    /**
     * Deserialize the given JSON string to Java object.
     *
     * @param <T>        Type
     * @param body       The JSON string
     * @param returnType The type to deserialize into
     * @return The deserialized Java object
     */
    <T> T deserialize(String body, Class<T> returnType);

    /**
     * Serialize the given Java object into JSON string.
     *
     * @param obj Object
     * @return String representation of the JSON
     */
    String serialize(Object obj);

    static JSON getDefault() {
        return new Jackson();
    }

    class Jackson implements JSON {

        private final ObjectMapper objectMapper;

        public Jackson() {
            this(new ObjectMapper());
        }

        public Jackson(ObjectMapper objectMapper) {
            this.objectMapper = objectMapper;
        }

        @Override
        public <T> T deserialize(String body, Class<T> returnType) {
            try {
                return objectMapper.readValue(body, returnType);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public String serialize(Object obj) {
            try {
                return objectMapper.writeValueAsString(obj);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
