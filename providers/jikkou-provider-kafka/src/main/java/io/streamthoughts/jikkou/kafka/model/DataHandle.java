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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.TextNode;
import io.streamthoughts.jikkou.common.utils.IOUtils;
import io.streamthoughts.jikkou.core.annotation.Reflectable;
import java.io.IOException;
import java.util.Objects;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Used for wrapping a JSON value.
 * 
 * @param value the Json value.
 */
@JsonDeserialize(using = DataHandle.PayloadHandleDeserializer.class)
@Reflectable
public record DataHandle(JsonNode value) {

    public static DataHandle NULL = new DataHandle(NullNode.getInstance());

    /**
     * Static helper method to create a {@link DataHandle} for the given value.
     *
     * @param value a text value.
     * @return  a new {@link DataHandle} instance.
     */
    public static DataHandle ofString(@NotNull String value) {
        return new DataHandle(new TextNode(value));
    }

    /**
     * Creates a new {@link DataHandle} instance.
     *
     * @param value the JSON value.
     */
    @JsonCreator
    public DataHandle(@Nullable JsonNode value) {
        this.value = Optional.ofNullable(value).orElse(NullNode.getInstance());
    }

    /**
     * Gets the JSON string.
     *
     * @return  the raw json value.
     */
    @JsonValue
    public String rawValue() {
        return rawValue(true);
    }

    /**
     * Gets the JSON string.
     *
     * @param prettyString using pretty-printer.
     *
     * @return  the raw json value.
     */
    public String rawValue(boolean prettyString) {
        return isNull() ? null : prettyString ? value.toPrettyString() : value.toString();
    }

    /**
     * Check if this data contains null value.
     *
     * @return {@code true} if null, otherwise {@code false}.
     */
    public boolean isNull() {
        return value().isNull();
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public String toString() {
        return value().toString();
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (DataHandle) obj;
        return Objects.equals(this.value, that.value);
    }

    public static class PayloadHandleDeserializer extends JsonDeserializer<DataHandle> {

        private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

        private static final String REF_FIELD_NAME = "$ref";

        /**
         * {@inheritDoc}
         **/
        @Override
        public DataHandle deserialize(JsonParser jsonParser,
                                      DeserializationContext deserializationContext) throws IOException {
            JsonToken jsonToken = jsonParser.currentToken();
            String rawValue = null;
            if (jsonToken.isStructStart()) {
                JsonNode value = jsonParser.readValueAsTree();
                if (value.has(REF_FIELD_NAME)) {
                    rawValue = IOUtils.readTextFile(value.get(REF_FIELD_NAME).asText());
                } else {
                    rawValue = value.toString();
                }
            }

            if (jsonToken.isScalarValue()) {
                rawValue = jsonParser.getText();
            }

            if (rawValue != null) {
                return new DataHandle(OBJECT_MAPPER.readValue(rawValue, JsonNode.class));
            }
            return DataHandle.NULL;
        }
    }
}
