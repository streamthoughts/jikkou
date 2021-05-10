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

import io.streamthoughts.kafka.specs.ClusterSpec;
import io.streamthoughts.kafka.specs.YAMLClusterSpecWriter;
import io.streamthoughts.kafka.specs.command.BaseCommand;
import io.streamthoughts.kafka.specs.operation.DescribeOperationOptions;
import io.streamthoughts.kafka.specs.operation.DescribeTopicOperation;
import io.streamthoughts.kafka.specs.resources.ResourcesIterable;
import io.streamthoughts.kafka.specs.resources.TopicResource;
import org.apache.kafka.clients.admin.AdminClient;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Collection;

import static io.streamthoughts.kafka.specs.command.topic.TopicsCommand.listClusterTopics;

@Command(name = "describe",
        description = "Describe all the topics that currently exist on the remote Kafka cluster."
)
public class Describe extends BaseCommand {

    @Option(names = {"--default-configs"},
            description = "Export built-in default configuration for configs that have a default value"
    )
    boolean describeDefaultConfigs;

    @Option(names = "--file-path",
            description = "The file path to write the description of Topics."
    )
    File filePath;

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer call(final AdminClient client) {
        final Collection<TopicResource> topics = listClusterTopics(client, this::isResourceCandidate);

        ResourcesIterable<TopicResource> it = new ResourcesIterable<>(topics);
        final Collection<TopicResource> resources = new DescribeTopicOperation().execute(
                client,
                it,
                DescribeOperationOptions.withDescribeDefaultConfigs(describeDefaultConfigs)
        );

        try {
            OutputStream os = (filePath != null) ? new FileOutputStream(filePath) : System.out;
            YAMLClusterSpecWriter.instance().write(ClusterSpec.withTopics(resources), os);
            return CommandLine.ExitCode.OK;
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
