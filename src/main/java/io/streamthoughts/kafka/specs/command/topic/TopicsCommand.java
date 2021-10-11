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

import io.streamthoughts.kafka.specs.Description;
import io.streamthoughts.kafka.specs.OperationResult;
import io.streamthoughts.kafka.specs.change.Change;
import io.streamthoughts.kafka.specs.change.TopicChange;
import io.streamthoughts.kafka.specs.change.TopicChanges;
import io.streamthoughts.kafka.specs.command.WithAdminClientCommand;
import io.streamthoughts.kafka.specs.command.WithSpecificationCommand;
import io.streamthoughts.kafka.specs.command.topic.subcommands.*;
import io.streamthoughts.kafka.specs.command.topic.subcommands.internal.DescribeTopics;
import io.streamthoughts.kafka.specs.operation.AclOperation;
import io.streamthoughts.kafka.specs.operation.DescribeOperationOptions;
import io.streamthoughts.kafka.specs.operation.TopicOperation;
import org.apache.kafka.clients.admin.AdminClient;
import org.jetbrains.annotations.NotNull;
import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.util.Collection;
import java.util.LinkedList;

@Command(name = "topics",
        headerHeading = "Usage:%n%n",
        descriptionHeading = "%nDescription:%n%n",
        parameterListHeading = "%nParameters:%n%n",
        optionListHeading = "%nOptions:%n%n",
        commandListHeading = "%nCommands:%n%n",
        synopsisHeading = "%n",
        header = "Execute changes to the Kafka cluster Topics.",
        description = "This command can be used to create, alter, delete or describe Topics on a remote Kafka cluster",
        subcommands = {
                Alter.class,
                Create.class,
                Delete.class,
                Apply.class,
                Describe.class,
                CommandLine.HelpCommand.class
        },
        mixinStandardHelpOptions = true)
public class TopicsCommand extends WithAdminClientCommand {

    public static abstract class Base extends WithSpecificationCommand<TopicChange> {

        /**
         * Gets the operation to execute.
         *
         * @param client    the {@link AdminClient}.
         * @return          a new {@link AclOperation}.
         */
        public abstract TopicOperation getOperation(@NotNull final AdminClient client);

        /**
         * {@inheritDoc}
         */
        @Override
        public Collection<OperationResult<TopicChange>> executeCommand(final AdminClient client) {

            var topics = new DescribeTopics(
                    client,
                    DescribeOperationOptions.withDescribeDefaultConfigs(true)
            ).describe(this::isResourceCandidate);

            final TopicChanges topicChanges = TopicChanges.computeChanges(
                    topics,
                    specFile().specs().topics(it -> isResourceCandidate(it.name()))
            );

            final TopicOperation operation = getOperation(client);

            final LinkedList<OperationResult<TopicChange>> results = new LinkedList<>();

            if (isDryRun()) {
                topicChanges.all()
                    .stream()
                    .filter(it -> operation.test(it) || it.getOperation() == Change.OperationType.NONE)
                    .map(change -> {
                        Description description = operation.getDescriptionFor(change);
                        return change.getOperation() == Change.OperationType.NONE ?
                                OperationResult.ok(change, description) :
                                OperationResult.changed(change, description);
                    })
                   .forEach(results::add);
            } else {
                results.addAll(topicChanges.apply(operation));
                topicChanges.all()
                        .stream()
                        .filter(it -> it.getOperation() == Change.OperationType.NONE)
                        .map(change -> OperationResult.ok(change, operation.getDescriptionFor(change)))
                        .forEach(results::add);
            }

            return results;
        }
    }
}
