/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.common.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
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
}