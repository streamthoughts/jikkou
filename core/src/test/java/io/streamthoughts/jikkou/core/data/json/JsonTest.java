/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
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