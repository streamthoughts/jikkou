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
package com.zenika.kafka.specs.command;

import com.zenika.kafka.specs.ClusterSpec;
import com.zenika.kafka.specs.ClusterSpecReader;
import com.zenika.kafka.specs.KafkaSpecsRunnerOptions;
import com.zenika.kafka.specs.OperationResult;
import com.zenika.kafka.specs.YAMLClusterSpecReader;
import com.zenika.kafka.specs.operation.AlterTopicOperation;
import com.zenika.kafka.specs.operation.CreateTopicOperation;
import com.zenika.kafka.specs.operation.CreateTopicOperationOptions;
import com.zenika.kafka.specs.operation.DeleteTopicOperation;
import com.zenika.kafka.specs.operation.DescribeOperationOptions;
import com.zenika.kafka.specs.operation.DescribeTopicOperation;
import com.zenika.kafka.specs.operation.ResourceOperationOptions;
import com.zenika.kafka.specs.resources.Named;
import com.zenika.kafka.specs.resources.ResourcesIterable;
import com.zenika.kafka.specs.resources.TopicResource;
import org.apache.kafka.clients.admin.AdminClient;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class ExecuteTopicCommand implements ClusterCommand<Collection<OperationResult<TopicResource>>> {

    private static final ClusterSpecReader READER = new YAMLClusterSpecReader();

    private AdminClient client;

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<OperationResult<TopicResource>> execute(final KafkaSpecsRunnerOptions options, final AdminClient client) {
        this.client = client;
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

            TopicsCommands lastCommand = null;
            if (options.isDeleteTopicsCommand()) {
                lastCommand = TopicsCommands.DELETE;
                results.addAll(
                        new DeleteTopicOperation()
                                .execute(client, new ResourcesIterable<>(unknown), new ResourceOperationOptions(){})
                );
            }

            if (options.isCreateTopicsCommand()) {
                lastCommand = TopicsCommands.CREATE;
                results.addAll(new CreateTopicOperation()
                        .execute(client, new ResourcesIterable<>(created), new CreateTopicOperationOptions()));
            }

            if (options.isAlterTopicsCommand()) {
                lastCommand = TopicsCommands.ALTER;
                results.addAll(new AlterTopicOperation()
                        .execute(client, new ResourcesIterable<>(altered), new ResourceOperationOptions(){}));
            }

            // We should keep trace of unchanged topic for tool output - in theory the last command should never be null.
            final TopicsCommands command = lastCommand == null ? TopicsCommands.UNKNOWN : lastCommand;
            unchanged.forEach(topic -> results.add(OperationResult.unchanged(topic, command)));


            return results;

        } catch (FileNotFoundException | InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
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