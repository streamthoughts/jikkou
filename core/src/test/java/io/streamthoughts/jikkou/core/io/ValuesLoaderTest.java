/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.io;

import io.streamthoughts.jikkou.core.io.reader.ValuesReaderOptions;
import io.streamthoughts.jikkou.core.models.NamedValueSet;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Tests for {@link ValuesLoader}.
 * These tests verify the fix for GitHub issue #535:
 * "Directory as source for `--values-files`"
 */
class ValuesLoaderTest {

    @TempDir
    Path tempDir;

    @Test
    void shouldLoadValuesFromSingleFile() throws IOException {
        // Given
        Path valuesFile = tempDir.resolve("values.yaml");
        Files.writeString(valuesFile, """
            key1: value1
            key2: value2
            """);

        // When
        NamedValueSet result = ValuesLoader.loadFromLocations(
                List.of(valuesFile.toString()),
                ValuesReaderOptions.of("**/*.{yaml,yml}"));

        // Then
        Map<String, Object> values = result.asMap();
        Assertions.assertEquals("value1", values.get("key1"));
        Assertions.assertEquals("value2", values.get("key2"));
    }

    @Test
    void shouldLoadValuesFromDirectory() throws IOException {
        // Given
        Path valuesDir = Files.createDirectories(tempDir.resolve("values"));
        Files.writeString(valuesDir.resolve("values1.yaml"), """
            team: teamA
            environment: dev
            """);
        Files.writeString(valuesDir.resolve("values2.yml"), """
            region: us-east-1
            """);

        // When
        NamedValueSet result = ValuesLoader.loadFromLocations(
                List.of(valuesDir.toString()),
                ValuesReaderOptions.of("**/*.{yaml,yml}"));

        // Then
        Map<String, Object> values = result.asMap();
        Assertions.assertEquals("teamA", values.get("team"));
        Assertions.assertEquals("dev", values.get("environment"));
        Assertions.assertEquals("us-east-1", values.get("region"));
    }

    /**
     * Test case from GitHub issue #535:
     * configurations/cluster1/resources/(teamA|teamB|...)/(values1|values2|...).yml
     */
    @Test
    void shouldLoadValuesRecursivelyFromNestedDirectories() throws IOException {
        // Given: Simulate the user's use case from issue #535
        // /data/configurations/cluster1/resources/(teamA|teamB|...)/(values1|values2|...).yml
        Path resources = Files.createDirectories(tempDir.resolve("configurations/cluster1/resources"));
        Path teamA = Files.createDirectories(resources.resolve("teamA"));
        Path teamB = Files.createDirectories(resources.resolve("teamB"));
        Path teamC = Files.createDirectories(resources.resolve("teamC"));

        Files.writeString(teamA.resolve("values1.yml"), """
            teamA:
              topic1:
                partitions: 3
                replication: 2
            """);
        Files.writeString(teamA.resolve("values2.yml"), """
            teamA:
              topic2:
                partitions: 6
            """);
        Files.writeString(teamB.resolve("values1.yml"), """
            teamB:
              topic1:
                partitions: 12
            """);
        Files.writeString(teamC.resolve("config.yaml"), """
            teamC:
              enabled: true
            """);

        // When: Load from the top-level resources directory
        NamedValueSet result = ValuesLoader.loadFromLocations(
                List.of(resources.toString()),
                ValuesReaderOptions.of("**/*.{yaml,yml}"));

        // Then: All values from all teams should be loaded
        Map<String, Object> values = result.asMap();
        Assertions.assertNotNull(values.get("teamA"), "teamA values should be loaded");
        Assertions.assertNotNull(values.get("teamB"), "teamB values should be loaded");
        Assertions.assertNotNull(values.get("teamC"), "teamC values should be loaded");
    }

    @Test
    void shouldLoadValuesFromDeeplyNestedDirectories() throws IOException {
        // Given: Multiple levels of nesting
        Path level1 = Files.createDirectories(tempDir.resolve("level1"));
        Path level2 = Files.createDirectories(level1.resolve("level2"));
        Path level3 = Files.createDirectories(level2.resolve("level3"));
        Path level4 = Files.createDirectories(level3.resolve("level4"));

        Files.writeString(level1.resolve("config1.yaml"), "level1: value1");
        Files.writeString(level2.resolve("config2.yaml"), "level2: value2");
        Files.writeString(level3.resolve("config3.yaml"), "level3: value3");
        Files.writeString(level4.resolve("config4.yaml"), "level4: value4");

        // When
        NamedValueSet result = ValuesLoader.loadFromLocations(
                List.of(tempDir.toString()),
                ValuesReaderOptions.of("**/*.{yaml,yml}"));

        // Then
        Map<String, Object> values = result.asMap();
        Assertions.assertEquals("value1", values.get("level1"));
        Assertions.assertEquals("value2", values.get("level2"));
        Assertions.assertEquals("value3", values.get("level3"));
        Assertions.assertEquals("value4", values.get("level4"));
    }

    @Test
    void shouldMergeValuesFromMultipleDirectories() throws IOException {
        // Given: Two separate value directories
        Path dir1 = Files.createDirectories(tempDir.resolve("dir1"));
        Path dir2 = Files.createDirectories(tempDir.resolve("dir2"));

        Files.writeString(dir1.resolve("values.yaml"), """
            common:
              setting1: fromDir1
            unique1: value1
            """);
        Files.writeString(dir2.resolve("values.yaml"), """
            common:
              setting2: fromDir2
            unique2: value2
            """);

        // When: Load from both directories
        NamedValueSet result = ValuesLoader.loadFromLocations(
                List.of(dir1.toString(), dir2.toString()),
                ValuesReaderOptions.of("**/*.{yaml,yml}"));

        // Then: Values from both directories should be present
        Map<String, Object> values = result.asMap();
        Assertions.assertNotNull(values.get("common"));
        Assertions.assertNotNull(values.get("unique1"));
        Assertions.assertNotNull(values.get("unique2"));
    }

    @Test
    void shouldIgnoreNonYamlFiles() throws IOException {
        // Given
        Path dir = Files.createDirectories(tempDir.resolve("mixed"));
        Files.writeString(dir.resolve("valid.yaml"), "valid: true");
        Files.writeString(dir.resolve("readme.txt"), "This is a readme");
        Files.writeString(dir.resolve("config.json"), "{\"json\": true}");

        // When
        NamedValueSet result = ValuesLoader.loadFromLocations(
                List.of(dir.toString()),
                ValuesReaderOptions.of("**/*.{yaml,yml}"));

        // Then
        Map<String, Object> values = result.asMap();
        Assertions.assertEquals(1, values.size());
        Assertions.assertEquals(true, values.get("valid"));
    }

    @Test
    void shouldReturnEmptySetForEmptyDirectory() {
        // Given: Empty directory (tempDir is already empty)

        // When
        NamedValueSet result = ValuesLoader.loadFromLocations(
                List.of(tempDir.toString()),
                ValuesReaderOptions.of("**/*.{yaml,yml}"));

        // Then
        Assertions.assertTrue(result.isEmpty());
    }

    @Test
    void shouldLoadValuesFromFilesInRootAndSubdirectories() throws IOException {
        // Given: Files in both root and subdirectories
        Path subDir = Files.createDirectories(tempDir.resolve("subdir"));

        Files.writeString(tempDir.resolve("root.yaml"), "rootKey: rootValue");
        Files.writeString(subDir.resolve("sub.yaml"), "subKey: subValue");

        // When
        NamedValueSet result = ValuesLoader.loadFromLocations(
                List.of(tempDir.toString()),
                ValuesReaderOptions.of("**/*.{yaml,yml}"));

        // Then: Both files should be loaded
        Map<String, Object> values = result.asMap();
        Assertions.assertEquals("rootValue", values.get("rootKey"));
        Assertions.assertEquals("subValue", values.get("subKey"));
    }

    @Test
    void shouldSupportMixedFileAndDirectoryLocations() throws IOException {
        // Given: A single file and a directory
        Path singleFile = tempDir.resolve("single.yaml");
        Path valuesDir = Files.createDirectories(tempDir.resolve("values"));

        Files.writeString(singleFile, "singleFile: true");
        Files.writeString(valuesDir.resolve("dir.yaml"), "fromDir: true");

        // When: Load from both a file and a directory
        NamedValueSet result = ValuesLoader.loadFromLocations(
                List.of(singleFile.toString(), valuesDir.toString()),
                ValuesReaderOptions.of("**/*.{yaml,yml}"));

        // Then
        Map<String, Object> values = result.asMap();
        Assertions.assertEquals(true, values.get("singleFile"));
        Assertions.assertEquals(true, values.get("fromDir"));
    }

    /**
     * Test case from GitHub issue #535 comment:
     * Multiple files with the same top-level key should be deep-merged.
     */
    @Test
    @SuppressWarnings("unchecked")
    void shouldDeepMergeValuesFromMultipleFilesWithSameTopLevelKey() throws IOException {
        // Given: Multiple files with the same top-level key 'topics'
        Path resources = Files.createDirectories(tempDir.resolve("resources"));

        Files.writeString(resources.resolve("a.yaml"), """
            topics:
              team_a_1:
                partitions: 6
              team_a_2:
                partitions: 8
            """);
        Files.writeString(resources.resolve("b.yaml"), """
            topics:
              team_b_1:
                partitions: 7
              team_b_2:
                partitions: 5
            """);

        // When
        NamedValueSet result = ValuesLoader.loadFromLocations(
                List.of(resources.toString()),
                ValuesReaderOptions.of("**/*.{yaml,yml}"));

        // Then: All topics from both files should be present (deep merged)
        Map<String, Object> values = result.asMap();
        Assertions.assertNotNull(values.get("topics"), "topics key should exist");

        Map<String, Object> topics = (Map<String, Object>) values.get("topics");
        Assertions.assertEquals(4, topics.size(), "Should have 4 topics from both files");
        Assertions.assertNotNull(topics.get("team_a_1"), "team_a_1 should be present");
        Assertions.assertNotNull(topics.get("team_a_2"), "team_a_2 should be present");
        Assertions.assertNotNull(topics.get("team_b_1"), "team_b_1 should be present");
        Assertions.assertNotNull(topics.get("team_b_2"), "team_b_2 should be present");
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldDeepMergeNestedMaps() throws IOException {
        // Given: Files with nested structures that should be merged
        Path resources = Files.createDirectories(tempDir.resolve("resources"));

        Files.writeString(resources.resolve("base.yaml"), """
            config:
              database:
                host: localhost
                port: 5432
              logging:
                level: INFO
            """);
        Files.writeString(resources.resolve("override.yaml"), """
            config:
              database:
                username: admin
              cache:
                enabled: true
            """);

        // When
        NamedValueSet result = ValuesLoader.loadFromLocations(
                List.of(resources.toString()),
                ValuesReaderOptions.of("**/*.{yaml,yml}"));

        // Then: Nested maps should be deep merged
        Map<String, Object> values = result.asMap();
        Map<String, Object> config = (Map<String, Object>) values.get("config");
        Map<String, Object> database = (Map<String, Object>) config.get("database");

        // Original values should be preserved
        Assertions.assertEquals("localhost", database.get("host"));
        Assertions.assertEquals(5432, database.get("port"));
        // New values should be added
        Assertions.assertEquals("admin", database.get("username"));
        // logging should still exist
        Assertions.assertNotNull(config.get("logging"));
        // cache should be added
        Assertions.assertNotNull(config.get("cache"));
    }
}
