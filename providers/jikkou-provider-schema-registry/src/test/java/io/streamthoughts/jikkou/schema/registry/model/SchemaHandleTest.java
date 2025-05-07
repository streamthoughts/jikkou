/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.schema.registry.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.streamthoughts.jikkou.core.data.SchemaHandle;
import io.streamthoughts.jikkou.core.io.Jackson;
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