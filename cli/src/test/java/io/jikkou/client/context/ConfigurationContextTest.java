/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.jikkou.client.context;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jikkou.core.exceptions.JikkouRuntimeException;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ConfigurationContextTest {

    @Test
    void shouldTolerateEmptyConfigFile(@TempDir Path tempDir) throws Exception {
        // Given an existing but empty config file
        File configFile = Files.createFile(tempDir.resolve("config")).toFile();

        ConfigurationContext context = new ConfigurationContext(configFile, new ObjectMapper());

        // Then it is treated as if no configuration existed
        assertFalse(context.isExists());
        assertEquals(ConfigurationContext.EMPTY_CONTEXT, context.getCurrentContextName());
        assertTrue(context.getContexts().isEmpty());
        assertNotNull(context.getCurrentContext());
    }

    @Test
    void shouldTolerateWhitespaceOnlyConfigFile(@TempDir Path tempDir) throws Exception {
        File configFile = tempDir.resolve("config").toFile();
        Files.writeString(configFile.toPath(), "  \n\t  \n");

        ConfigurationContext context = new ConfigurationContext(configFile, new ObjectMapper());

        assertFalse(context.isExists());
        assertTrue(context.getContexts().isEmpty());
    }

    @Test
    void shouldStillFailOnMalformedJson(@TempDir Path tempDir) throws Exception {
        File configFile = tempDir.resolve("config").toFile();
        Files.writeString(configFile.toPath(), "{ not valid json");

        ConfigurationContext context = new ConfigurationContext(configFile, new ObjectMapper());

        assertTrue(context.isExists());
        assertThrows(JikkouRuntimeException.class, context::getContexts);
    }
}
