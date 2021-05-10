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
package io.streamthoughts.kafka.specs;

import io.streamthoughts.kafka.specs.command.AdminClientMixin;
import io.streamthoughts.kafka.specs.command.acls.AclsCommand;
import io.streamthoughts.kafka.specs.command.broker.BrokerCommand;
import io.streamthoughts.kafka.specs.command.topic.TopicsCommand;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;

import java.io.PrintWriter;

import static picocli.CommandLine.Model.CommandSpec;

@Command(name = "kafka-specs",
        descriptionHeading   = "%nDescription:%n%n",
        parameterListHeading = "%nParameters:%n%n",
        optionListHeading    = "%nOptions:%n%n",
        commandListHeading   = "%nCommands:%n%n",
        headerHeading = "Usage: ",
        synopsisHeading = "%n",
        description = "CLI to ease and automate Apache Kafka cluster configuration management.",
        mixinStandardHelpOptions = true,
        subcommands = { TopicsCommand.class, AclsCommand.class, BrokerCommand.class, CommandLine.HelpCommand.class})
public class KafkaSpecs {

    @Mixin
    AdminClientMixin adminClientOptions;

    public static void main(final String... args) {
        final CommandLine commandLine = new CommandLine(new KafkaSpecs())
                .setExecutionStrategy(new CommandLine.RunLast())
                .setParameterExceptionHandler(new ShortErrorMessageHandler());

        if (args.length > 0) {
            final int exitCode = commandLine.execute(args);
            System.exit(exitCode);
        } else {
            commandLine.usage(System.out);
        }
    }

    public static class ShortErrorMessageHandler implements CommandLine.IParameterExceptionHandler {

        /**
         * {@inheritDoc}
         */
        @Override
        public int handleParseException(final CommandLine.ParameterException ex,
                                        final String[] args) {
            CommandLine cmd = ex.getCommandLine();
            PrintWriter err = cmd.getErr();

            // if tracing at DEBUG level, show the location of the issue
            if ("DEBUG".equalsIgnoreCase(System.getProperty("picocli.trace"))) {
                err.println(cmd.getColorScheme().stackTraceText(ex));
            }

            err.println(ex.getMessage());
            CommandLine.UnmatchedArgumentException.printSuggestions(ex, err);
            CommandSpec spec = cmd.getCommandSpec();
            cmd.usage(err);
            err.printf("%nSee '%s --help' for more information about a command.%n", spec.qualifiedName());
            return cmd.getExitCodeExceptionMapper() != null
                    ? cmd.getExitCodeExceptionMapper().getExitCode(ex)
                    : spec.exitCodeOnInvalidInput();
        }
    }
}
