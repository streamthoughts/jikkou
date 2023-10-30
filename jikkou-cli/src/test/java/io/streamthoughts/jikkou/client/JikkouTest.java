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
    void shouldPrintApiResources() {
        int execute = Jikkou.execute(new String[]{"api-resources"});
        Assertions.assertEquals(CommandLine.ExitCode.OK, execute);
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
        int execute = Jikkou.execute(new String[]{"extensions", "list"});
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