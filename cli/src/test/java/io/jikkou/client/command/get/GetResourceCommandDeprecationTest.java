/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.jikkou.client.command.get;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.jikkou.client.command.FormatOptionsMixin;
import io.jikkou.client.command.ProviderOptionMixin;
import io.jikkou.client.command.SelectorOptionsMixin;
import io.jikkou.core.JikkouApi;
import io.jikkou.core.ListContext;
import io.jikkou.core.io.writer.ResourceWriter;
import io.jikkou.core.models.HasMetadata;
import io.jikkou.core.models.ResourceList;
import io.jikkou.core.models.ResourceType;
import io.jikkou.core.selector.SelectorMatchingStrategy;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;

class GetResourceCommandDeprecationTest {

    private PrintStream originalErr;
    private ByteArrayOutputStream capturedErr;

    @AfterEach
    void restoreStderr() {
        if (originalErr != null) {
            System.setErr(originalErr);
        }
    }

    private void captureStderr() {
        originalErr = System.err;
        capturedErr = new ByteArrayOutputStream();
        System.setErr(new PrintStream(capturedErr));
    }

    @Test
    void shouldPrintDeprecationLine_whenDeprecatedFormIsSet() throws Exception {
        // Given
        GetResourceCommand command = newCommandWithMocks();
        command.setType(ResourceType.of("KafkaTopic", "kafka/v1"));
        command.setDeprecated("topics", "kafka topics");

        // When
        captureStderr();
        int exitCode = command.call();

        // Then
        assertEquals(CommandLine.ExitCode.OK, exitCode);
        String stderr = capturedErr.toString();
        assertTrue(stderr.contains("[DEPRECATED]"), "stderr: " + stderr);
        assertTrue(stderr.contains("'jikkou get topics'"), "stderr: " + stderr);
        assertTrue(stderr.contains("'jikkou get kafka topics'"), "stderr: " + stderr);
    }

    @Test
    void shouldNotPrintDeprecationLine_whenDeprecatedFormIsNotSet() throws Exception {
        // Given
        GetResourceCommand command = newCommandWithMocks();
        command.setType(ResourceType.of("KafkaTopic", "kafka/v1"));

        // When
        captureStderr();
        int exitCode = command.call();

        // Then
        assertEquals(CommandLine.ExitCode.OK, exitCode);
        assertEquals("", capturedErr.toString());
    }

    private GetResourceCommand newCommandWithMocks() throws Exception {
        GetResourceCommand command = new GetResourceCommand();
        JikkouApi api = mock(JikkouApi.class);
        when(api.listResources(any(ResourceType.class), any(ListContext.class)))
            .thenReturn(ResourceList.<HasMetadata>empty());
        ResourceWriter writer = mock(ResourceWriter.class);
        injectField(command, "api", api);
        injectField(command, "writer", writer);
        SelectorOptionsMixin selectorOptions = new SelectorOptionsMixin();
        selectorOptions.selectorMatchingStrategy = SelectorMatchingStrategy.ALL;
        injectField(command, "selectorOptions", selectorOptions);
        injectField(command, "formatOptions", new FormatOptionsMixin());
        injectField(command, "providerOptions", new ProviderOptionMixin());
        return command;
    }

    private static void injectField(Object target, String fieldName, Object value) throws Exception {
        Field f = GetResourceCommand.class.getDeclaredField(fieldName);
        f.setAccessible(true);
        f.set(target, value);
    }
}
