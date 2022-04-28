/*
 * Copyright 2021 StreamThoughts.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.streamthoughts.jikkou.kafka.command.config;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigRenderOptions;
import io.streamthoughts.jikkou.kafka.config.JikkouConfig;
import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Callable;

@Command(name = "config",
        headerHeading = "Usage:%n%n",
        descriptionHeading = "%nDescription:%n%n",
        parameterListHeading = "%nParameters:%n%n",
        optionListHeading = "%nOptions:%n%n",
        commandListHeading = "%nCommands:%n%n",
        synopsisHeading = "%n",
        header = "Sets or retrieves the configuration of this client.",
        description = "This command can be used to set or retrieve the configuration of this client.",
        mixinStandardHelpOptions = true,
        subcommands = {
            ConfigCommand.Get.class,
        })
public class ConfigCommand {

    @Command(synopsisHeading      = "%nUsage:%n%n",
            descriptionHeading   = "%nDescription:%n%n",
            parameterListHeading = "%nParameters:%n%n",
            optionListHeading    = "%nOptions:%n%n",
            commandListHeading   = "%nCommands:%n%n",
            mixinStandardHelpOptions = true,
            name = "get",
            description = "Retrieve the configuration of this client."
    )
    public static class Get implements Callable<Integer> {

        @CommandLine.Option(names = "--debug",
                defaultValue = "false",
                description = "Print CLI's configuration with the origin of setting as comments.")
        public boolean debug;

        @CommandLine.Option(names = "--comments",
                defaultValue = "false",
                description = "Print CLI's configuration with human-written comments.")
        public boolean comments;

        @Override
        public Integer call() {
            JikkouConfig config = JikkouConfig.get();
            try {
                ConfigRenderOptions options = ConfigRenderOptions
                        .defaults()
                        .setOriginComments(debug)
                        .setComments(comments);

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                Config unwrapped = config.unwrap();
                baos.write(unwrapped.root().render(options).getBytes(StandardCharsets.UTF_8));
                System.out.println(baos);
                return CommandLine.ExitCode.OK;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
