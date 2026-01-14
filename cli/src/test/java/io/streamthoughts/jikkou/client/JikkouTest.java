/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.streamthoughts.jikkou.client.context.ConfigurationContext;
import java.io.File;
import java.net.URL;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import picocli.CommandLine;

class JikkouTest {

    static {
        URL resource = JikkouTest.class.getResource("/test-jikkou-config.json");
        String path = resource.getPath();
        GlobalConfigurationContext.setConfigurationContext(new ConfigurationContext(
            new File(path),
            new ObjectMapper()
        ));
    }

    @Test
    void shouldReturnUsageCodeForNoArgs() {
        int exitCode = Jikkou.execute(new String[]{});
        Assertions.assertEquals(CommandLine.ExitCode.USAGE, exitCode);
    }

    @Test
    void shouldPrintApiResources() {
        int execute = Jikkou.execute(new String[]{"api-resources"});
        Assertions.assertEquals(CommandLine.ExitCode.OK, execute);
    }

    @Test
    void shouldPrintUsageForActionCommand() {
        int execute = Jikkou.execute(new String[]{"action"});
        Assertions.assertEquals(CommandLine.ExitCode.USAGE, execute);
    }

    @Test
    void shouldPrintUsageForGetCommand() {
        int execute = Jikkou.execute(new String[]{"get"});
        Assertions.assertEquals(CommandLine.ExitCode.USAGE, execute);
    }

    @Test
    void testCommandHealthGetIndicators() {
        int execute = Jikkou.execute(new String[]{"health", "get-indicators"});
        Assertions.assertEquals(CommandLine.ExitCode.OK, execute);
    }

    @Test
    void testCommandExtensionsList() {
        int execute = Jikkou.execute(new String[]{"api-extensions", "list"});
        Assertions.assertEquals(CommandLine.ExitCode.OK, execute);
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "delete",
        "apply",
        "create",
        "update",
        "prepare",
        "validate",
    })
    void testCommandGivenNoArg(String command) {
        int execute = Jikkou.execute(new String[]{command});
        Assertions.assertEquals(CommandLine.ExitCode.OK, execute);
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "delete",
        "apply",
        "create",
        "update",
        "prepare",
        "validate",
    })
    void testCommandHelp(String command) {
        int execute = Jikkou.execute(new String[]{command, "--help"});
        Assertions.assertEquals(CommandLine.ExitCode.OK, execute);
    }
}