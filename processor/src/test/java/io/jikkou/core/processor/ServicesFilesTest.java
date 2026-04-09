/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.jikkou.core.processor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class ServicesFilesTest {

    @Test
    void shouldReturnCorrectPath() {
        String path = ServicesFiles.getPath("com.example.MyService");
        assertEquals("META-INF/services/com.example.MyService", path);
    }

    @Test
    void shouldReadEmptyServiceFile() throws IOException {
        InputStream input = toInputStream("");
        Set<String> result = ServicesFiles.readServiceFile(input);
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldReadSingleServiceEntry() throws IOException {
        InputStream input = toInputStream("com.example.ServiceA\n");
        Set<String> result = ServicesFiles.readServiceFile(input);
        assertEquals(Set.of("com.example.ServiceA"), result);
    }

    @Test
    void shouldReadMultipleServiceEntries() throws IOException {
        String content = "com.example.ServiceA\ncom.example.ServiceB\ncom.example.ServiceC\n";
        InputStream input = toInputStream(content);
        Set<String> result = ServicesFiles.readServiceFile(input);
        assertEquals(Set.of("com.example.ServiceA", "com.example.ServiceB", "com.example.ServiceC"), result);
    }

    @Test
    void shouldIgnoreComments() throws IOException {
        String content = "# This is a comment\ncom.example.ServiceA\n# Another comment\ncom.example.ServiceB\n";
        InputStream input = toInputStream(content);
        Set<String> result = ServicesFiles.readServiceFile(input);
        assertEquals(Set.of("com.example.ServiceA", "com.example.ServiceB"), result);
    }

    @Test
    void shouldIgnoreInlineComments() throws IOException {
        String content = "com.example.ServiceA # inline comment\n";
        InputStream input = toInputStream(content);
        Set<String> result = ServicesFiles.readServiceFile(input);
        assertEquals(Set.of("com.example.ServiceA"), result);
    }

    @Test
    void shouldTrimWhitespace() throws IOException {
        String content = "  com.example.ServiceA  \n  \n\tcom.example.ServiceB\t\n";
        InputStream input = toInputStream(content);
        Set<String> result = ServicesFiles.readServiceFile(input);
        assertEquals(Set.of("com.example.ServiceA", "com.example.ServiceB"), result);
    }

    @Test
    void shouldSkipBlankLines() throws IOException {
        String content = "\n\ncom.example.ServiceA\n\n\ncom.example.ServiceB\n\n";
        InputStream input = toInputStream(content);
        Set<String> result = ServicesFiles.readServiceFile(input);
        assertEquals(Set.of("com.example.ServiceA", "com.example.ServiceB"), result);
    }

    @Test
    void shouldSkipCommentOnlyLines() throws IOException {
        String content = "# only comments\n# another comment\n";
        InputStream input = toInputStream(content);
        Set<String> result = ServicesFiles.readServiceFile(input);
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldDeduplicateEntries() throws IOException {
        String content = "com.example.ServiceA\ncom.example.ServiceA\n";
        InputStream input = toInputStream(content);
        Set<String> result = ServicesFiles.readServiceFile(input);
        assertEquals(1, result.size());
        assertTrue(result.contains("com.example.ServiceA"));
    }

    @Test
    void shouldWriteServiceFile() throws IOException {
        Collection<String> services = List.of("com.example.ServiceA", "com.example.ServiceB");
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ServicesFiles.writeServiceFile(services, output);
        String result = output.toString(StandardCharsets.UTF_8);
        assertEquals("com.example.ServiceA\ncom.example.ServiceB\n", result);
    }

    @Test
    void shouldWriteEmptyServiceFile() throws IOException {
        Collection<String> services = List.of();
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ServicesFiles.writeServiceFile(services, output);
        String result = output.toString(StandardCharsets.UTF_8);
        assertEquals("", result);
    }

    @Test
    void shouldRoundTripServiceFile() throws IOException {
        Collection<String> original = List.of("com.example.Alpha", "com.example.Beta");
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ServicesFiles.writeServiceFile(original, output);

        InputStream input = new ByteArrayInputStream(output.toByteArray());
        Set<String> readBack = ServicesFiles.readServiceFile(input);
        assertEquals(Set.of("com.example.Alpha", "com.example.Beta"), readBack);
    }

    private static InputStream toInputStream(String content) {
        return new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
    }
}
