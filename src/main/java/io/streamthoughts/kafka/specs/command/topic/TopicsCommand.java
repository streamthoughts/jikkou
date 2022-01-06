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
package io.streamthoughts.kafka.specs.command.topic;

import io.streamthoughts.kafka.specs.change.ChangeExecutor;
import io.streamthoughts.kafka.specs.change.ChangeResult;
import io.streamthoughts.kafka.specs.change.TopicChange;
import io.streamthoughts.kafka.specs.change.TopicChangeComputer;
import io.streamthoughts.kafka.specs.change.TopicChangeOptions;
import io.streamthoughts.kafka.specs.command.WithAdminClientCommand;
import io.streamthoughts.kafka.specs.command.WithSpecificationCommand;
import io.streamthoughts.kafka.specs.command.topic.subcommands.Alter;
import io.streamthoughts.kafka.specs.command.topic.subcommands.Apply;
import io.streamthoughts.kafka.specs.command.topic.subcommands.Create;
import io.streamthoughts.kafka.specs.command.topic.subcommands.Delete;
import io.streamthoughts.kafka.specs.command.topic.subcommands.Describe;
import io.streamthoughts.kafka.specs.command.topic.subcommands.internal.DescribeTopics;
import io.streamthoughts.kafka.specs.model.V1TopicObject;
import io.streamthoughts.kafka.specs.operation.DescribeOperationOptions;
import io.streamthoughts.kafka.specs.operation.acls.AclOperation;
import io.streamthoughts.kafka.specs.operation.topics.TopicOperation;
import org.apache.kafka.clients.admin.AdminClient;
import org.jetbrains.annotations.NotNull;
import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Command(name = "topics",
        headerHeading = "Usage:%n%n",
        descriptionHeading = "%nDescription:%n%n",
        parameterListHeading = "%nParameters:%n%n",
        optionListHeading = "%nOptions:%n%n",
        commandListHeading = "%nCommands:%n%n",
        synopsisHeading = "%n",
        header = "Apply the Topic changes described by your specs-file against the Kafka cluster you are currently pointing at.",
        description = "This command can be used to create, alter, delete or describe Topics on a remote Kafka cluster",
        subcommands = {
                Alter.class,
                Apply.class,
                Create.class,
                Delete.class,
                Describe.class,
                CommandLine.HelpCommand.class
        },
        mixinStandardHelpOptions = true)
public class TopicsCommand extends WithAdminClientCommand {

    public static abstract class Base extends WithSpecificationCommand<TopicChange> {

        /**
         * Gets the operation to execute.
         *
         * @param client the {@link AdminClient}.
         * @return a new {@link AclOperation}.
         */
        public abstract TopicOperation getOperation(@NotNull final AdminClient client);

        public abstract TopicChangeOptions getOptions();

        /**
         * {@inheritDoc}
         */
        @Override
        public Collection<ChangeResult<TopicChange>> executeCommand(final AdminClient client) {

            return loadSpecObjects()
                    .stream()
                    .flatMap(spec -> {
                        // Get the list of topics, that are candidates for this execution, from the SpecsFile.
                        final Collection<V1TopicObject> expectedStates = spec.topics().stream()
                                .filter(it -> isResourceCandidate(it.name()))
                                .collect(Collectors.toList());

                        // Compute state changes
                        Supplier<List<TopicChange>> supplier = () -> {
                            // Get the list of topics, that are candidates for this execution, from the remote Kafka cluster
                            final Collection<V1TopicObject> actualStates = new DescribeTopics(
                                    client,
                                    DescribeOperationOptions.withDescribeDefaultConfigs(true)
                            ).describe(this::isResourceCandidate);

                            return new TopicChangeComputer().
                                    computeChanges(actualStates, expectedStates, getOptions());
                        };
                        return ChangeExecutor
                                .ofSupplier(supplier)
                                .execute(getOperation(client), isDryRun())
                                .stream();
                    })
                    .collect(Collectors.toList());
        }
    }
}
