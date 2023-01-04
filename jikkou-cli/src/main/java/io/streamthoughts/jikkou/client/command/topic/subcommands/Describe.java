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
package io.streamthoughts.jikkou.client.command.topic.subcommands;

import static io.streamthoughts.jikkou.client.JikkouConfigProperty.EXCLUDE_RESOURCES;
import static io.streamthoughts.jikkou.client.JikkouConfigProperty.INCLUDE_RESOURCES;

import io.streamthoughts.jikkou.api.ResourceByNameFilter;
import io.streamthoughts.jikkou.api.ResourceFilter;
import io.streamthoughts.jikkou.api.SimpleJikkouApi;
import io.streamthoughts.jikkou.api.io.YAMLResourceWriter;
import io.streamthoughts.jikkou.api.model.HasMetadata;
import io.streamthoughts.jikkou.client.JikkouConfig;
import io.streamthoughts.jikkou.client.JikkouContext;
import io.streamthoughts.jikkou.client.command.ExecOptionsMixin;
import io.streamthoughts.jikkou.kafka.control.ConfigDescribeConfiguration;
import io.streamthoughts.jikkou.kafka.models.V1KafkaTopicList;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Optional;
import java.util.concurrent.Callable;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;

@Command(
        name = "describe",
        description = "Describe all the topics that currently exist on the remote Kafka cluster.",
        synopsisHeading      = "%nUsage:%n%n",
        descriptionHeading   = "%nDescription:%n%n",
        parameterListHeading = "%nParameters:%n%n",
        optionListHeading    = "%nOptions:%n%n",
        commandListHeading   = "%nCommands:%n%n",
        mixinStandardHelpOptions = true
)
public class Describe implements Callable<Integer> {

    @Option(names = {"--default-configs"},
            description = "Export built-in default configuration for configs that have a default value."
    )
    boolean describeDefaultConfigs;

    @Option(names = {"--static-broker-configs"},
            defaultValue = "false",
            description = "Export static configs provided as broker properties at start up (e.g. server.properties file)."
    )
    boolean describeStaticBrokerConfigs;

    @Option(names = {"--dynamic-broker-configs"},
            defaultValue = "false",
            description = "Export dynamic configs that is configured as default for all brokers or for specific broker in the cluster."
    )
    boolean describeDynamicBrokerConfigs;

    @Option(names = "--output-file",
            description = "Writes the result of the command to this file instead of stdout."
    )
    File outputFile;


    @Mixin
    ExecOptionsMixin execOptions;

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer call() {

        try {
            var configDescribeConfig = new ConfigDescribeConfiguration()
                    .withDescribeDefaultConfigs(describeDefaultConfigs)
                    .withDescribeDynamicBrokerConfigs(describeDynamicBrokerConfigs)
                    .withDescribeStaticBrokerConfigs(describeStaticBrokerConfigs);

            try(SimpleJikkouApi api = JikkouContext.jikkouApi()) {
                HasMetadata resource = api.getResource(
                        V1KafkaTopicList.class,
                        getResourceByName(),
                        configDescribeConfig.asConfiguration()
                );

                OutputStream os = (outputFile != null) ? new FileOutputStream(outputFile) : System.out;

                YAMLResourceWriter.instance().write(resource, os);
                return CommandLine.ExitCode.OK;
            }

        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public final ResourceFilter getResourceByName() {
        JikkouConfig config = JikkouContext.jikkouConfig();
        return new ResourceByNameFilter()
                .withExcludes(Optional.ofNullable(execOptions.exclude)
                        .or(() -> EXCLUDE_RESOURCES.getOptional(config)).orElse(null)
                )
                .withIncludes(Optional.ofNullable(execOptions.include)
                        .or(() -> INCLUDE_RESOURCES.getOptional(config)).orElse(null)
                );
    }
}
