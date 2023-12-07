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
package io.streamthoughts.jikkou.core.data.json;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class JsonTest {

    @Test
    void shouldNormalizeJsonString() {
        // Given
        var json = """
                { "field1": "value1", "field2": "value2", "field3": { "field4": "value4"} }
                """;
        Assertions.assertEquals("{\"field1\":\"value1\",\"field2\":\"value2\",\"field3\":{\"field4\":\"value4\"}}", Json.normalize(json));
    }

    @Test
    void shouldNormalizeGivenTwoEqualsJson() {
        // Given
        var json1 = """
                { "field1": "value1", "field2": "value2", "field3": { "field4": "value4"} }
                """;
        var json2 = """
                { "field3": { "field4": "value4"}, "field2": "value2", "field1": "value1" } }
                """;
        // When
        Assertions.assertEquals(Json.normalize(json1), Json.normalize(json2));
    }
}