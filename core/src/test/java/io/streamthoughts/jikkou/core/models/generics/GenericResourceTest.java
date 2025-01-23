/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.models.generics;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.streamthoughts.jikkou.core.io.Jackson;
import io.streamthoughts.jikkou.core.models.ObjectMeta;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class GenericResourceTest {

    @Test
    void shouldDeserializeAnyValue() throws JsonProcessingException {
        // Given
        String yaml = """
            apiVersion: io.jikkou/v1
            kind: Test
            metadata:
                name: "test"
            spec: 
               field1: value1
               field2: value2
            """;

        // When
        GenericResource deserialized = Jackson.YAML_OBJECT_MAPPER.readValue(yaml, GenericResource.class);

        // Then
        GenericResource expected = new GenericResource(
            "io.jikkou/v1",
            "Test",
            ObjectMeta
                .builder()
                .withName("test")
                .build(),
            null,
            Map.of("spec", Map.of("field1", "value1", "field2", "value2"))
        );

        Assertions.assertEquals(expected, deserialized);
    }
}