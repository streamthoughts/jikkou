/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.schema.registry.model;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.streamthoughts.jikkou.common.utils.IOUtils;
import io.streamthoughts.jikkou.core.annotation.Reflectable;
import java.io.IOException;
import java.util.Map;

@JsonDeserialize(using = SchemaHandle.SchemaHandleDeserializer.class)
@Reflectable
public final class SchemaHandle {

    private final String value;

    /**
     * Creates a new {@link SchemaHandle} instance.
     *
     * @param value    the schema string.
     */
    public SchemaHandle(String value) {
        this.value = value;
    }

    @JsonValue
    public String value() {
        return value;
    }

    /** {@inheritDoc} **/
    @Override
    public String toString() {
        return value;
    }

    public static class SchemaHandleDeserializer extends JsonDeserializer<SchemaHandle> {

        /**
         * {@inheritDoc}
         **/
        @Override
        public SchemaHandle deserialize(JsonParser jsonParser,
                                        DeserializationContext deserializationContext) throws IOException {

            JsonToken jsonToken = jsonParser.currentToken();
            if (jsonToken.isScalarValue()) {
                return new SchemaHandle(jsonParser.getText());
            }
            if (jsonToken.isStructStart()) {
                @SuppressWarnings("unchecked")
                Map<String, Object> map = jsonParser.readValueAs(Map.class);
                Object ref = map.get("$ref");
                if (ref != null) {
                    String location = ref.toString();
                    return new SchemaHandle(IOUtils.readTextFile(location));
                }
            }
            return null;
        }
    }
}
