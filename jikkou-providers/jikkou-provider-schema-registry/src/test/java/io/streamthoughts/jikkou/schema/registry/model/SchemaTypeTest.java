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