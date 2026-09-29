/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.jikkou.common.utils;

import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class IOUtilsTest {

    @TempDir
    Path tempDir;

    @Test
    void shouldFindMatchingFiles_whenFilesAreInRootDirectory() throws IOException {
        // Given
        Files.createFile(tempDir.resolve("values1.yaml"));
        Files.createFile(tempDir.resolve("values2.yml"));
        Files.createFile(tempDir.resolve("other.txt"));

        // When
        List<Path> result = IOUtils.findMatching(tempDir, "**/*.{yaml,yml}");

        // Then
        Assertions.assertEquals(2, result.size());
        Assertions.assertTrue(result.stream().anyMatch(p -> p.getFileName().toString().equals("values1.yaml")));
        Assertions.assertTrue(result.stream().anyMatch(p -> p.getFileName().toString().equals("values2.yml")));
    }

    @Test
    void shouldFindMatchingFilesRecursively_whenFilesAreInSubdirectories() throws IOException {
        // Given
        Path teamA = Files.createDirectories(tempDir.resolve("teamA"));
        Path teamB = Files.createDirectories(tempDir.resolve("teamB"));
        Path nestedDir = Files.createDirectories(teamA.resolve("nested"));

        Files.createFile(teamA.resolve("values1.yaml"));
        Files.createFile(teamB.resolve("values2.yml"));
        Files.createFile(nestedDir.resolve("values3.yaml"));
        Files.createFile(tempDir.resolve("root.yaml"));
        Files.createFile(teamA.resolve("other.txt")); // Should not match

        // When
        List<Path> result = IOUtils.findMatching(tempDir, "**/*.{yaml,yml}");

        // Then
        Assertions.assertEquals(4, result.size());
        Assertions.assertTrue(result.stream().anyMatch(p -> p.getFileName().toString().equals("values1.yaml")));
        Assertions.assertTrue(result.stream().anyMatch(p -> p.getFileName().toString().equals("values2.yml")));
        Assertions.assertTrue(result.stream().anyMatch(p -> p.getFileName().toString().equals("values3.yaml")));
        Assertions.assertTrue(result.stream().anyMatch(p -> p.getFileName().toString().equals("root.yaml")));
    }

    @Test
    void shouldFindMatchingFilesRecursively_whenMultipleLevelsOfSubdirectories() throws IOException {
        Path resources = Files.createDirectories(tempDir.resolve("resources"));
        Path teamA = Files.createDirectories(resources.resolve("teamA"));
        Path teamB = Files.createDirectories(resources.resolve("teamB"));
        Path subTeam = Files.createDirectories(teamA.resolve("subteam"));

        Files.createFile(teamA.resolve("values1.yml"));
        Files.createFile(teamA.resolve("values2.yml"));
        Files.createFile(teamB.resolve("values1.yml"));
        Files.createFile(subTeam.resolve("values3.yaml"));

        // When
        List<Path> result = IOUtils.findMatching(tempDir, "**/*.{yaml,yml}");

        // Then
        Assertions.assertEquals(4, result.size());
    }

    @Test
    void shouldReturnEmptyList_whenNoFilesMatch() throws IOException {
        // Given
        Files.createFile(tempDir.resolve("file.txt"));
        Files.createFile(tempDir.resolve("file.json"));

        // When
        List<Path> result = IOUtils.findMatching(tempDir, "**/*.{yaml,yml}");

        // Then
        Assertions.assertTrue(result.isEmpty());
    }

    @Test
    void shouldReturnEmptyList_whenDirectoryIsEmpty() {
        // When
        List<Path> result = IOUtils.findMatching(tempDir, "**/*.{yaml,yml}");

        // Then
        Assertions.assertTrue(result.isEmpty());
    }

    @Test
    void shouldMatchFilesWithSpecificPattern() throws IOException {
        // Given
        Files.createFile(tempDir.resolve("config.yaml"));
        Files.createFile(tempDir.resolve("values.yaml"));
        Files.createFile(tempDir.resolve("data.yaml"));

        // When: Only match files starting with 'config'
        List<Path> result = IOUtils.findMatching(tempDir, "**/config*.yaml");

        // Then
        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals("config.yaml", result.getFirst().getFileName().toString());
    }

    @Test
    void shouldMatchFilesWithGlobPattern() throws IOException {
        // Given
        Path subDir = Files.createDirectories(tempDir.resolve("configs"));
        Files.createFile(subDir.resolve("app.yaml"));
        Files.createFile(subDir.resolve("app.yml"));
        Files.createFile(tempDir.resolve("app.yaml"));

        // When
        List<Path> result = IOUtils.findMatching(tempDir, "**/*.yaml");

        // Then
        Assertions.assertEquals(2, result.size());
    }

    @Test
    void shouldNotIncludeDirectoriesInResults() throws IOException {
        // Given
        Path subDir = Files.createDirectories(tempDir.resolve("subdir.yaml")); // Directory with .yaml suffix
        Files.createFile(subDir.resolve("file.yaml"));

        // When
        List<Path> result = IOUtils.findMatching(tempDir, "**/*.yaml");

        // Then
        Assertions.assertEquals(1, result.size());
        Assertions.assertTrue(Files.isRegularFile(result.getFirst()));
    }

    @Test
    void shouldSendBasicAuthHeader_whenHostMatchesConfig() throws Exception {
        // Given
        AtomicReference<String> authHeader = new AtomicReference<>();
        HttpServer server = newHttpServer(authHeader);
        try {
            Path configFile = tempDir.resolve("application.conf");
            Files.writeString(configFile, """
                    jikkou.io.http.authentications: [
                      { host: "127.0.0.1", username: "user", password: "pass" }
                    ]
                    """);

            // When
            String content = withConfigFile(configFile, () -> new String(
                    IOUtils.openStream(new URL("http://127.0.0.1:" + server.getAddress().getPort() + "/x"))
                            .readAllBytes(),
                    StandardCharsets.UTF_8));

            // Then
            Assertions.assertEquals("hello", content);
            String expected = "Basic "
                    + Base64.getEncoder().encodeToString("user:pass".getBytes(StandardCharsets.UTF_8));
            Assertions.assertEquals(expected, authHeader.get());
        } finally {
            server.stop(0);
        }
    }

    @Test
    void shouldNotSendAuthHeader_whenNoConfigEntry() throws Exception {
        // Given
        AtomicReference<String> authHeader = new AtomicReference<>();
        HttpServer server = newHttpServer(authHeader);
        try {
            Path configFile = tempDir.resolve("application.conf");
            Files.writeString(configFile, "jikkou.some.other.property = \"value\"\n");

            // When
            String content = withConfigFile(configFile, () -> new String(
                    IOUtils.openStream(new URL("http://127.0.0.1:" + server.getAddress().getPort() + "/x"))
                            .readAllBytes(),
                    StandardCharsets.UTF_8));

            // Then
            Assertions.assertEquals("hello", content);
            Assertions.assertNull(authHeader.get());
        } finally {
            server.stop(0);
        }
    }

    @Test
    void shouldThrowMeaningfulError_whenHostMatchesButCredentialsMissing() throws Exception {
        // Given
        AtomicReference<String> authHeader = new AtomicReference<>();
        HttpServer server = newHttpServer(authHeader);
        try {
            Path configFile = tempDir.resolve("application.conf");
            Files.writeString(configFile, """
                    jikkou.io.http.authentications: [
                      { host: "127.0.0.1", username: "user" }
                    ]
                    """);

            // When
            RuntimeException exception = Assertions.assertThrows(RuntimeException.class, () ->
                    withConfigFile(configFile, () ->
                            IOUtils.openStream(new URL("http://127.0.0.1:" + server.getAddress().getPort() + "/x"))));

            // Then
            Assertions.assertTrue(exception.getMessage().contains("password"));
            Assertions.assertTrue(exception.getMessage().contains("jikkou.io.http.authentications"));
        } finally {
            server.stop(0);
        }
    }

    private static HttpServer newHttpServer(AtomicReference<String> authHeader) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/x", exchange -> {
            authHeader.set(exchange.getRequestHeaders().getFirst("Authorization"));
            byte[] body = "hello".getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        server.start();
        return server;
    }

    private static <T> T withConfigFile(Path configFile, ThrowingSupplier<T> action) throws Exception {
        String previous = System.getProperty("config.file");
        System.setProperty("config.file", configFile.toString());
        IOUtils.resetHttpAuthConfig();
        try {
            return action.get();
        } finally {
            if (previous == null) {
                System.clearProperty("config.file");
            } else {
                System.setProperty("config.file", previous);
            }
            IOUtils.resetHttpAuthConfig();
        }
    }

    @FunctionalInterface
    private interface ThrowingSupplier<T> {
        T get() throws Exception;
    }
}