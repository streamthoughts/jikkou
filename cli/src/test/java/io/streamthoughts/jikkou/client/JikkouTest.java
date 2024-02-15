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
    void shouldPrintPrepareHelp() {
        int execute = Jikkou.execute(new String[]{"prepare"});
        Assertions.assertEquals(CommandLine.ExitCode.USAGE, execute);
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

    // TEST SUBCOMMANDS --HELP
    @Test
    void testCommandCreateHelp() {
        int execute = Jikkou.execute(new String[]{"create", "--help"});
        Assertions.assertEquals(CommandLine.ExitCode.OK, execute);
    }

    @Test
    void testCommandUpdateHelp() {
        int execute = Jikkou.execute(new String[]{"update", "--help"});
        Assertions.assertEquals(CommandLine.ExitCode.OK, execute);
    }

    @Test
    void testCommandDeleteHelp() {
        int execute = Jikkou.execute(new String[]{"delete", "--help"});
        Assertions.assertEquals(CommandLine.ExitCode.OK, execute);
    }

    @Test
    void testCommandApplyHelp() {
        int execute = Jikkou.execute(new String[]{"apply", "--help"});
        Assertions.assertEquals(CommandLine.ExitCode.OK, execute);
    }
}