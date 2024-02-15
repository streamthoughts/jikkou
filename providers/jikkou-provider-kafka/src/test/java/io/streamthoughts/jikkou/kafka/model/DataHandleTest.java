/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.kafka.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.NullNode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class DataHandleTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Test
    void shouldReturnEmptyGivenNullValue() {
        Assertions.assertTrue(new DataHandle(null).isNull());
    }

    @Test
    void shouldReturnEmptyGivenNullNode() {
        Assertions.assertTrue(new DataHandle(NullNode.getInstance()).isNull());
    }

    @Test
    void shouldDeserializeDataHandleGivenJsonString() throws JsonProcessingException {
        // Given
        String content = "{\"foo\": \"bar\"}";
        // When
        DataHandle handle = OBJECT_MAPPER.readValue(content, DataHandle.class);
        // Then
        Assertions.assertNotNull(handle);
    }
}
