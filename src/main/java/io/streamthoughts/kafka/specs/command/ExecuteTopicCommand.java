/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.streamthoughts.kafka.specs.command;

import io.streamthoughts.kafka.specs.ClusterSpec;
import io.streamthoughts.kafka.specs.ClusterSpecReader;
import io.streamthoughts.kafka.specs.Description;
import io.streamthoughts.kafka.specs.KafkaSpecsRunnerOptions;
import io.streamthoughts.kafka.specs.OperationResult;
import io.streamthoughts.kafka.specs.YAMLClusterSpecReader;
import io.streamthoughts.kafka.specs.internal.DescriptionProvider;
import io.streamthoughts.kafka.specs.operation.AlterTopicOperation;
import io.streamthoughts.kafka.specs.operation.CreateTopicOperation;
import io.streamthoughts.kafka.specs.operation.CreateTopicOperationOptions;
import io.streamthoughts.kafka.specs.operation.DeleteTopicOperation;
import io.streamthoughts.kafka.specs.operation.DescribeOperationOptions;
import io.streamthoughts.kafka.specs.operation.DescribeTopicOperation;
import io.streamthoughts.kafka.specs.operation.ResourceOperationOptions;
import io.streamthoughts.kafka.specs.resources.Named;
import io.streamthoughts.kafka.specs.resources.ResourcesIterable;
import io.streamthoughts.kafka.specs.resources.TopicResource;
import org.apache.kafka.clients.admin.AdminClient;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class ExecuteTopicCommand implements ClusterCommand<Collection<OperationResult<TopicResource>>> {

    private static final ClusterSpecReader READER = new YAMLClusterSpecReader();

    private static final Map<OperationType, DescriptionProvider<TopicResource>> DESCRIPTIONS_BY_TYPE = new HashMap<>();

    static {
        DESCRIPTIONS_BY_TYPE.put(OperationType.CREATE, CreateTopicOperation.DESCRIPTION);
        DESCRIPTIONS_BY_TYPE.put(OperationType.DELETE, DeleteTopicOperation.DESCRIPTION);
        DESCRIPTIONS_BY_TYPE.put(OperationType.ALTER, AlterTopicOperation.DESCRIPTION);
        DESCRIPTIONS_BY_TYPE.put(OperationType.UNKNOWN, resource -> (Description.Unknown) () -> "Executing operation on topic " + resource.name());
    }

    private final AdminClient client;

    private KafkaSpecsRunnerOptions options;

    /**
     * Creates a new {@link ExecuteTopicCommand} instance.
     * @param client
     */
    public ExecuteTopicCommand(final AdminClient client) {
        this.client = client;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<OperationResult<TopicResource>> execute(final KafkaSpecsRunnerOptions options) {
        this.options = options;
        File specification = options.clusterSpecificationOpt();
        try {
            // Read input specification
            final ClusterSpec specs = READER.read(new FileInputStream(specification));

            // Retrieve topics defined on the remote cluster.
            final Map<String, TopicResource> clusterTopics = Named.keyByName(getClusterTopics(options));

            final Collection<TopicResource> specifiedTopics = specs.getTopics(options.topics());
            Collection<TopicResource> altered = new LinkedList<>();
            Collection<TopicResource> created = new LinkedList<>();
            Collection<TopicResource> unchanged = new LinkedList<>();

            specifiedTopics.forEach(topic -> {
                TopicResource clusterTopic = clusterTopics.remove(topic.name());
                if (clusterTopic == null) {
                    created.add(topic);
                } else if (topic.containsConfigsChanges(clusterTopic)) {

                    altered.add(topic.dropDefaultConfigs(clusterTopic));
                } else {
                    unchanged.add(topic);
                }

            });
            // remaining remote topics are unknown and may be deleted
            Collection<TopicResource> unknown = clusterTopics.values();

            final List<OperationResult<TopicResource>> results = new LinkedList<>();

            OperationType lastOperation = null;
            if (options.isDeleteTopicsCommand()) {
                lastOperation = OperationType.DELETE;
                results.addAll(executeDeleteTopics(unknown));
            }

            if (options.isCreateTopicsCommand()) {
                lastOperation = OperationType.CREATE;
                results.addAll(executeCreateTopics(created));
            }

            if (options.isAlterTopicsCommand()) {
                lastOperation = OperationType.ALTER;
                results.addAll(executeAlterTopics(altered));
            }

            // We should keep trace of unchanged topic for tool output - in theory the last command should never be null.
            final OperationType command = lastOperation == null ? OperationType.UNKNOWN : lastOperation;

            if (options.isDryRun()) {
                results.addAll(buildDryRunResult(unchanged, false, DESCRIPTIONS_BY_TYPE.get(command)));
            } else {
                results.addAll(unchanged.stream()
                        .map(r -> OperationResult.unchanged(r, DESCRIPTIONS_BY_TYPE.get(command).getForResource(r)))
                        .collect(Collectors.toList()));
            }
            return results;

        } catch (FileNotFoundException | InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    private Collection<OperationResult<TopicResource>> executeAlterTopics(final Collection<TopicResource> topics) {
        if (options.isDryRun()) {
            return buildDryRunResult(topics, true, AlterTopicOperation.DESCRIPTION);
        }
        return new AlterTopicOperation()
                .execute(client, new ResourcesIterable<>(topics), new ResourceOperationOptions() {
                });
    }

    private Collection<OperationResult<TopicResource>> executeCreateTopics(final Collection<TopicResource> topics) {
        if (options.isDryRun()) {
            return buildDryRunResult(topics, true, CreateTopicOperation.DESCRIPTION);
        }

        return new CreateTopicOperation()
                .execute(client, new ResourcesIterable<>(topics), new CreateTopicOperationOptions());
    }

    private Collection<OperationResult<TopicResource>> executeDeleteTopics(final Collection<TopicResource> topics) {
        if (options.isDryRun()) {
            return buildDryRunResult(topics, true, DeleteTopicOperation.DESCRIPTION);
        }

        return new DeleteTopicOperation()
                .execute(client, new ResourcesIterable<>(topics), new ResourceOperationOptions() {});
    }

    private List<OperationResult<TopicResource>> buildDryRunResult(final Collection<TopicResource> resources,
                                                                   final boolean changed,
                                                                   final DescriptionProvider<TopicResource> provider) {
        return resources.stream()
                .map(r -> OperationResult.dryRun(r, changed, provider.getForResource(r)))
                .collect(Collectors.toList());
    }

    private Collection<TopicResource> getClusterTopics(final KafkaSpecsRunnerOptions options) throws ExecutionException, InterruptedException {
        // lookup for existing topics
        Collection<String> topicNames = (options.topics().isEmpty()) ?
                client.listTopics().names().get() :
                options.topics();
        List<TopicResource> topics = topicNames.stream().map(TopicResource::new).collect(Collectors.toList());

        return new DescribeTopicOperation()
                .execute(client,  new ResourcesIterable<>(topics), DescribeOperationOptions.withDescribeDefaultConfigs(true));
    }
}