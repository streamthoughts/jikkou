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
package io.streamthoughts.jikkou.schema.registry.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.streamthoughts.jikkou.api.io.Jackson;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class SchemaHandleTest {

    @Test
    void shouldDeserializeForStringValue() throws JsonProcessingException {
        // Given
        var json = "\"schema\"";
        // When
        ObjectMapper om = Jackson.JSON_OBJECT_MAPPER;
        SchemaHandle result = om.readValue(json, SchemaHandle.class);

        // Then
        Assertions.assertEquals("schema", result.value());
    }

    @Test
    void shouldDeserializeForRefValue() throws IOException {
        // Given
        Path temp = Files.createTempFile(null, ".txt");
        Files.writeString(temp, "schema");

        var json = "{ \"$ref\": \"" + temp + "\" }";

        // When
        ObjectMapper om = Jackson.JSON_OBJECT_MAPPER;
        SchemaHandle result = om.readValue(json, SchemaHandle.class);

        // Then
        Assertions.assertEquals("schema", result.value());
    }
}