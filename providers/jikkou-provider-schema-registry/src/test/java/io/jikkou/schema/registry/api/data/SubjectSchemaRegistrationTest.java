/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.jikkou.schema.registry.api.data;

import com.fasterxml.jackson.databind.JsonNode;
import io.jikkou.core.data.SchemaType;
import io.jikkou.core.io.Jackson;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class SubjectSchemaRegistrationTest {

    @Test
    void shouldSerializeIdAndVersionAsIntegers() throws Exception {
        // Given
        SubjectSchemaRegistration registration = new SubjectSchemaRegistration(1001, 1, "schema", SchemaType.PROTOBUF);

        // When
        JsonNode json = Jackson.JSON_OBJECT_MAPPER.readTree(Jackson.JSON_OBJECT_MAPPER.writeValueAsString(registration));

        // Then
        Assertions.assertTrue(json.get("id").isInt());
        Assertions.assertEquals(1001, json.get("id").intValue());
        Assertions.assertTrue(json.get("version").isInt());
        Assertions.assertEquals(1, json.get("version").intValue());
    }

    @Test
    void shouldOmitIdAndVersionWhenNull() throws Exception {
        // Given
        SubjectSchemaRegistration registration = new SubjectSchemaRegistration(null, null, "schema", SchemaType.AVRO);

        // When
        JsonNode json = Jackson.JSON_OBJECT_MAPPER.readTree(Jackson.JSON_OBJECT_MAPPER.writeValueAsString(registration));

        // Then
        Assertions.assertFalse(json.has("id"));
        Assertions.assertFalse(json.has("version"));
    }
}
