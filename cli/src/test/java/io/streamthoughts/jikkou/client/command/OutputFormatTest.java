/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.client.command;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class OutputFormatTest {

    @Test
    void shouldSerializeToJson() throws IOException {
        // Given
        Map<String, String> data = Map.of("key", "value");
        ByteArrayOutputStream os = new ByteArrayOutputStream();

        // When
        OutputFormat.JSON.serialize(data, os);

        // Then
        String result = os.toString(StandardCharsets.UTF_8);
        Assertions.assertTrue(result.contains("\"key\""));
        Assertions.assertTrue(result.contains("\"value\""));
    }

    @Test
    void shouldSerializeToYaml() throws IOException {
        // Given
        Map<String, String> data = Map.of("key", "value");
        ByteArrayOutputStream os = new ByteArrayOutputStream();

        // When
        OutputFormat.YAML.serialize(data, os);

        // Then
        String result = os.toString(StandardCharsets.UTF_8);
        Assertions.assertTrue(result.contains("key:"));
        Assertions.assertTrue(result.contains("value"));
    }

    @Test
    void shouldThrowUnsupportedOperationExceptionWhenSerializingAsTable() {
        // Given
        Map<String, String> data = Map.of("key", "value");
        ByteArrayOutputStream os = new ByteArrayOutputStream();

        // When / Then
        Assertions.assertThrows(UnsupportedOperationException.class,
                () -> OutputFormat.TABLE.serialize(data, os));
    }

    @Test
    void shouldSerializeListToJson() throws IOException {
        // Given
        List<String> data = List.of("a", "b", "c");
        ByteArrayOutputStream os = new ByteArrayOutputStream();

        // When
        OutputFormat.JSON.serialize(data, os);

        // Then
        String result = os.toString(StandardCharsets.UTF_8);
        Assertions.assertTrue(result.contains("\"a\""));
        Assertions.assertTrue(result.contains("\"b\""));
        Assertions.assertTrue(result.contains("\"c\""));
    }

    @Test
    void shouldSerializeListToYaml() throws IOException {
        // Given
        List<String> data = List.of("a", "b", "c");
        ByteArrayOutputStream os = new ByteArrayOutputStream();

        // When
        OutputFormat.YAML.serialize(data, os);

        // Then
        String result = os.toString(StandardCharsets.UTF_8);
        Assertions.assertTrue(result.contains("- \"a\""));
        Assertions.assertTrue(result.contains("- \"b\""));
        Assertions.assertTrue(result.contains("- \"c\""));
    }
}
