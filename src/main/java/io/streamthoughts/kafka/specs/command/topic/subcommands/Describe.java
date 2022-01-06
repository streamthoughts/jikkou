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
package io.streamthoughts.kafka.specs.command.topic.subcommands;

import io.streamthoughts.kafka.specs.io.YAMLSpecWriter;
import io.streamthoughts.kafka.specs.command.BaseCommand;
import io.streamthoughts.kafka.specs.command.topic.subcommands.internal.DescribeTopics;
import io.streamthoughts.kafka.specs.model.MetaObject;
import io.streamthoughts.kafka.specs.model.V1SpecFile;
import io.streamthoughts.kafka.specs.model.V1SpecsObject;
import io.streamthoughts.kafka.specs.operation.DescribeOperationOptions;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.ConfigEntry;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

import static org.apache.kafka.clients.admin.ConfigEntry.ConfigSource.DYNAMIC_BROKER_CONFIG;
import static org.apache.kafka.clients.admin.ConfigEntry.ConfigSource.DYNAMIC_DEFAULT_BROKER_CONFIG;
import static org.apache.kafka.clients.admin.ConfigEntry.ConfigSource.STATIC_BROKER_CONFIG;

@Command(name = "describe",
        description = "Describe all the topics that currently exist on the remote Kafka cluster."
)
public class Describe extends BaseCommand {

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

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer call(final AdminClient client) {

        DescribeTopics describeTopics = new DescribeTopics(
                client,
                DescribeOperationOptions.withDescribeDefaultConfigs(describeDefaultConfigs)
        );

        if (!describeStaticBrokerConfigs) {
            describeTopics.addConfigEntryPredicate(config -> config.source() != STATIC_BROKER_CONFIG);
        }

        if (!describeDynamicBrokerConfigs) {
            List<ConfigEntry.ConfigSource> excludeSources = Arrays.asList(
                    DYNAMIC_BROKER_CONFIG,
                    DYNAMIC_DEFAULT_BROKER_CONFIG
            );
            describeTopics.addConfigEntryPredicate(Predicate.not(config -> excludeSources.contains(config.source())));
        }

        var topics = describeTopics.describe(this::isResourceCandidate);

        try {
            OutputStream os = (outputFile != null) ? new FileOutputStream(outputFile) : System.out;

            final V1SpecsObject specsObject = V1SpecsObject.withTopics(topics);
            YAMLSpecWriter.instance().write(new V1SpecFile(MetaObject.defaults(), specsObject), os);
            return CommandLine.ExitCode.OK;
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
