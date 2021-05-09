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
import io.streamthoughts.kafka.specs.command.WithSpecificationCommand;
import io.streamthoughts.kafka.specs.command.topic.subcommands.Alter;
import io.streamthoughts.kafka.specs.command.topic.subcommands.Create;
import io.streamthoughts.kafka.specs.command.topic.subcommands.Delete;
import io.streamthoughts.kafka.specs.command.topic.subcommands.Describe;
import io.streamthoughts.kafka.specs.command.topic.subcommands.internal.TopicCandidates;
import io.streamthoughts.kafka.specs.operation.OperationType;
import io.streamthoughts.kafka.specs.internal.DescriptionProvider;
import io.streamthoughts.kafka.specs.operation.AlterTopicOperation;
import io.streamthoughts.kafka.specs.operation.CreateTopicOperation;
import io.streamthoughts.kafka.specs.operation.DeleteTopicOperation;
import io.streamthoughts.kafka.specs.operation.DescribeOperationOptions;
import io.streamthoughts.kafka.specs.operation.DescribeTopicOperation;
import io.streamthoughts.kafka.specs.resources.Named;
import io.streamthoughts.kafka.specs.resources.ResourcesIterable;
import io.streamthoughts.kafka.specs.resources.TopicResource;
import org.apache.kafka.clients.admin.AdminClient;
import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

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
                Describe.class,
                CommandLine.HelpCommand.class
        },
        mixinStandardHelpOptions = true)
public class TopicsCommand {

    /**
     * Helper methods to list all topics on Kafka Cluster matching the given predicate.
     *
     * @param client      the {@link AdminClient} to be used.
     * @param isCandidate the {@link Predicate} to filter topics.
     * @return A Collection of {@link TopicResource}.
     */
    public static Collection<TopicResource> listClusterTopics(final AdminClient client,
                                                              final Predicate<String> isCandidate) {
        final Collection<String> topicNames;
        try {
            topicNames = client.listTopics().names().get()
                    .stream()
                    .filter(isCandidate)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        final List<TopicResource> topics = topicNames
                .stream()
                .map(TopicResource::new)
                .collect(Collectors.toList());

        return new DescribeTopicOperation()
                .execute(
                        client,
                        new ResourcesIterable<>(topics),
                        DescribeOperationOptions.withDescribeDefaultConfigs(true)
                );
    }

    public static abstract class Base extends WithSpecificationCommand<TopicResource> {

        private static final Map<OperationType, DescriptionProvider<TopicResource>> DESCRIPTIONS_BY_TYPE = new HashMap<>();

        static {
            DESCRIPTIONS_BY_TYPE.put(OperationType.CREATE, CreateTopicOperation.DESCRIPTION);
            DESCRIPTIONS_BY_TYPE.put(OperationType.DELETE, DeleteTopicOperation.DESCRIPTION);
            DESCRIPTIONS_BY_TYPE.put(OperationType.ALTER, AlterTopicOperation.DESCRIPTION);
            DESCRIPTIONS_BY_TYPE.put(OperationType.UNKNOWN, resource -> (Description.Unknown) () -> "Executing operation on topic " + resource.name());
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Collection<OperationResult<TopicResource>> executeCommand(final AdminClient client) {
            final Collection<TopicResource> declaredTopics = clusterSpec()
                    .getTopics(it -> isResourceCandidate(it.name()));

            final TopicCandidates candidates = new TopicCandidates(
                    declaredTopics,
                    Named.keyByName(listClusterTopics(client, this::isResourceCandidate)));

            Collection<TopicResource> topics = getTopics(candidates);
            final LinkedList<OperationResult<TopicResource>> results = new LinkedList<>();
            if (isDryRun()) {
                results.addAll(buildDryRunResults(topics, true, CreateTopicOperation.DESCRIPTION));
            } else {
                results.addAll(execute(topics, client));
            }
            results.addAll(addSynchronized(candidates, getOperationType(), isDryRun()));
            return results;
        }

        public abstract Collection<OperationResult<TopicResource>> execute(final Collection<TopicResource> topics,
                                                                           final AdminClient client);

        public abstract Collection<TopicResource> getTopics(final TopicCandidates candidates);

        public abstract OperationType getOperationType();

        public static List<OperationResult<TopicResource>> addSynchronized(final TopicCandidates candidates,
                                                                           final OperationType operationType,
                                                                           final boolean isDryRun) {
            // We should keep trace of unchanged topic for tool output - in theory the last command should never be null.
            final OperationType command = operationType == null ? OperationType.UNKNOWN : operationType;
            if (isDryRun) {
                return buildDryRunResults(candidates.topicsSynchronized(), false, DESCRIPTIONS_BY_TYPE.get(command));
            } else {
                return candidates.topicsSynchronized()
                        .stream()
                        .map(r -> OperationResult.unchanged(r, DESCRIPTIONS_BY_TYPE.get(command).getForResource(r)))
                        .collect(Collectors.toList());
            }
        }

        private static List<OperationResult<TopicResource>> buildDryRunResults(final Collection<TopicResource> resources,
                                                                               final boolean changed,
                                                                               final DescriptionProvider<TopicResource> provider) {
            return resources.stream()
                    .map(r -> OperationResult.dryRun(r, changed, provider.getForResource(r)))
                    .collect(Collectors.toList());
        }
    }
}
