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
package io.streamthoughts.jikkou.api.control;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


class JsonValueChangeTest {

    @Test
    void shouldGetNoneChangeGivenTwoEqualsJson() {
        // Given
        var json = """
        { "filed1": "value1", "field2": "value2", "field3": { "field4": "value4"} }
        """;
        // When
        JsonValueChange change = JsonValueChange.with(json, json);

        // Then
        Assertions.assertEquals(change.getChangeType(), ChangeType.NONE);
    }

    @Test
    void shouldGetNoneChangeGivenTwoEqualsCanonicalJson() {
        // Given
        var json1 = """
        { "field1": "value1", "field2": "value2", "field3": { "field4": "value4"} }
        """;
        var json2 = """
        { "field3": { "field4": "value4"}, "field2": "value2", "field1": "value1" } }
        """;
        // When
        JsonValueChange change = JsonValueChange.with(json1, json2);

        // Then
        Assertions.assertEquals(ChangeType.NONE, change.getChangeType());
    }

    @Test
    void shouldGetUpdateChangeGivenTwoDistinctJson() {
        // Given
        var json1 = """
         { "filed1": "value1", "field2": "value2" }
        """;
        var json2 = """
         { "filed1": "value1", "field2": "value2", "field3": { "field4": "value4"} }
        """;
        // When
        JsonValueChange change = JsonValueChange.with(json1, json2);

        // Then
        Assertions.assertEquals(ChangeType.UPDATE, change.getChangeType());
    }

    @Test
    void shouldGetAddChangeGivenNewJson() {
        // Given
        var json = """
         { "filed1": "value1", "field2": "value2" }
        """;
        // When
        JsonValueChange change = JsonValueChange.with(json, null);

        // Then
        Assertions.assertEquals(ChangeType.ADD, change.getChangeType());
    }

    @Test
    void shouldGetDeleteChangeGivenNewJson() {
        // Given
        var json = """
         { "filed1": "value1", "field2": "value2" }
        """;
        // When
        JsonValueChange change = JsonValueChange.with( null, json);

        // Then
        Assertions.assertEquals(ChangeType.DELETE, change.getChangeType());
    }
}