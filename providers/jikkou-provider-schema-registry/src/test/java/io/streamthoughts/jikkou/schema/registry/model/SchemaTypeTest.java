/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.schema.registry.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class SchemaTypeTest {

    @Test
    void shouldReturnAvro() {
        // Given - When
        SchemaType res = SchemaType.getForNameIgnoreCase("avro");
        // Then
        Assertions.assertEquals( SchemaType.AVRO, res);
    }

    @Test
    void shouldReturnJson() {
        // Given - When
        SchemaType res = SchemaType.getForNameIgnoreCase("json");
        // Then
        Assertions.assertEquals( SchemaType.JSON, res);
    }

    @Test
    void shouldReturnProtobuf() {
        // Given - When
        SchemaType res = SchemaType.getForNameIgnoreCase("protobuf");
        // Then
        Assertions.assertEquals(SchemaType.PROTOBUF, res);
    }
}