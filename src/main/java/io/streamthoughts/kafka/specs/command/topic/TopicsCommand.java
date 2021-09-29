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
import io.streamthoughts.kafka.specs.command.topic.subcommands.Alter;
import io.streamthoughts.kafka.specs.command.topic.subcommands.Create;
import io.streamthoughts.kafka.specs.command.topic.subcommands.Delete;
import io.streamthoughts.kafka.specs.command.topic.subcommands.Describe;
import io.streamthoughts.kafka.specs.operation.DescribeOperationOptions;
import io.streamthoughts.kafka.specs.operation.DescribeTopicOperation;
import io.streamthoughts.kafka.specs.operation.TopicOperation;
import io.streamthoughts.kafka.specs.resources.ResourcesIterable;
import io.streamthoughts.kafka.specs.resources.TopicResource;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.common.KafkaFuture;
import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
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
public class TopicsCommand extends WithAdminClientCommand {

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

    public static abstract class Base extends WithSpecificationCommand<TopicChange> {

        /**
         * {@inheritDoc}
         */
        @Override
        public Collection<OperationResult<TopicChange>> executeCommand(final AdminClient client) {

            final TopicChanges topicChanges = TopicChanges.computeChanges(
                    listClusterTopics(client, this::isResourceCandidate),
                    clusterSpec().getTopics(it -> isResourceCandidate(it.name()))
            );

            final TopicOperation operation = createTopicOperation(client);

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
                results.addAll(applyChanges(topicChanges, operation));
                topicChanges.all()
                        .stream()
                        .filter(it -> it.getOperation() == Change.OperationType.NONE)
                        .map(change -> OperationResult.ok(change, operation.getDescriptionFor(change)))
                        .forEach(results::add);
            }

            return results;
        }

        private List<OperationResult<TopicChange>> applyChanges(final TopicChanges topicChanges,
                                                                final TopicOperation topicOperation) {

            final Map<String, KafkaFuture<Void>> resultMap = topicChanges.apply(topicOperation);

            List<CompletableFuture<OperationResult<TopicChange>>> completableFutures = resultMap.entrySet()
                    .stream()
                    .map(entry -> {
                        final Future<Void> future = entry.getValue();
                        return makeCompletableFuture(future, topicChanges.get(entry.getKey()), topicOperation);
                    }).collect(Collectors.toList());

            return completableFutures
                    .stream()
                    .map(CompletableFuture::join)
                    .collect(Collectors.toList());
        }

        CompletableFuture<OperationResult<TopicChange>> makeCompletableFuture(final Future<Void> future,
                                                                              final TopicChange change,
                                                                              final TopicOperation operation) {
            return CompletableFuture.supplyAsync(() -> {
                try {
                    future.get();
                    return OperationResult.changed(change, operation.getDescriptionFor(change));
                } catch (InterruptedException | ExecutionException e) {
                    return OperationResult.failed(change, operation.getDescriptionFor(change), e);
                }
            });
        }

        public abstract TopicOperation createTopicOperation(final AdminClient client);
    }
}
