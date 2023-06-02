/*
 * Copyright 2023 StreamThoughts.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.streamthoughts.jikkou.schema.registry.model;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.streamthoughts.jikkou.common.utils.IOUtils;
import java.io.IOException;
import java.util.Map;

@JsonDeserialize(using = SchemaHandle.SchemaHandleDeserializer.class)
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
                String location = map.get("$ref").toString();
                String schema = IOUtils.readTextFile(location);
                return new SchemaHandle(schema);
            }
            return null;
        }
    }
}