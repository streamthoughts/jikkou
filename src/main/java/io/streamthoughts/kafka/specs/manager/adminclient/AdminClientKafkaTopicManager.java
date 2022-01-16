/*
 * Copyright 2022 StreamThoughts.
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
package io.streamthoughts.kafka.specs.manager.adminclient;

import io.streamthoughts.kafka.specs.change.ChangeResult;
import io.streamthoughts.kafka.specs.change.TopicChange;
import io.streamthoughts.kafka.specs.change.TopicChangeOptions;
import io.streamthoughts.kafka.specs.config.JikkouConfig;
import io.streamthoughts.kafka.specs.error.ConfigException;
import io.streamthoughts.kafka.specs.internal.ConfigsBuilder;
import io.streamthoughts.kafka.specs.manager.AbstractKafkaTopicManager;
import io.streamthoughts.kafka.specs.manager.KafkaResourceOperationContext;
import io.streamthoughts.kafka.specs.manager.TopicDescribeOptions;
import io.streamthoughts.kafka.specs.model.V1SpecsObject;
import io.streamthoughts.kafka.specs.model.V1TopicObject;
import io.streamthoughts.kafka.specs.operation.topics.AlterTopicOperation;
import io.streamthoughts.kafka.specs.operation.topics.ApplyTopicOperation;
import io.streamthoughts.kafka.specs.operation.topics.CreateTopicOperation;
import io.streamthoughts.kafka.specs.operation.topics.DeleteTopicOperation;
import io.streamthoughts.kafka.specs.operation.topics.TopicOperation;
import io.streamthoughts.kafka.specs.resources.Configs;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.Config;
import org.apache.kafka.clients.admin.ConfigEntry;
import org.apache.kafka.clients.admin.DescribeConfigsResult;
import org.apache.kafka.clients.admin.DescribeTopicsResult;
import org.apache.kafka.clients.admin.TopicDescription;
import org.apache.kafka.common.TopicPartitionInfo;
import org.apache.kafka.common.config.ConfigResource;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public final class AdminClientKafkaTopicManager extends AbstractKafkaTopicManager {

    private AdminClientContext adminClientContext;

    /**
     * {@inheritDoc}
     */
    @Override
    public void configure(@NotNull JikkouConfig config) throws ConfigException {
        adminClientContext = new AdminClientContext(config);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<ChangeResult<TopicChange>> update(UpdateMode mode,
                                                        List<V1SpecsObject> specObjects,
                                                        KafkaResourceOperationContext<TopicChangeOptions> context) {
        return adminClientContext.invokeAndClose(adminClient -> super.update(mode, specObjects, context));
    }

    @Override
    public List<V1TopicObject> describe(@NotNull final TopicDescribeOptions options) {
        if (adminClientContext.isInitialized())
            return new DescribeTopics(adminClientContext.current(), options).describe();

        return adminClientContext.invokeAndClose(adminClient ->
                new DescribeTopics(adminClient, options).describe()
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected TopicOperation getOperationFor(@NotNull final UpdateMode mode) {
        return switch (mode) {
            case CREATE_ONLY -> new CreateTopicOperation(adminClientContext.current());
            case ALTER_ONLY -> new AlterTopicOperation(adminClientContext.current());
            case DELETE_ONLY -> new DeleteTopicOperation(adminClientContext.current());
            case APPLY -> new ApplyTopicOperation(adminClientContext.current());
        };
    }

    /**
     * Function to list all topics on Kafka Cluster matching a given predicate.
     */
    public final class DescribeTopics {

        private final AdminClient client;

        private final Predicate<ConfigEntry> configEntryPredicate;

        private final Predicate<String> topicPredicate;


        /**
         * Creates a new {@link DescribeTopics} instance.
         *
         * @param client       the {@link AdminClient}.
         * @param options      the {@link TopicDescribeOptions}.
         */
        public DescribeTopics(final AdminClient client,
                              final TopicDescribeOptions options) {
            this.client = client;
            this.topicPredicate = options.topicPredicate();
            this.configEntryPredicate = options.configEntryPredicate();
        }

        public List<V1TopicObject> describe() {

            final Collection<String> topicNames;
            try {
                topicNames = client.listTopics().names().get()
                        .stream()
                        .filter(topicPredicate)
                        .collect(Collectors.toList());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            final CompletableFuture<Map<String, TopicDescription>> futureTopicDesc = describeTopics(topicNames);
            final CompletableFuture<Map<String, Config>> futureTopicConfig = describeConfigs(topicNames);

            try {
                return futureTopicDesc.thenCombine(futureTopicConfig, (descriptions, configs) -> {
                    return descriptions.values()
                            .stream()
                            .map(desc -> newTopicResources(desc, configs.get(desc.name())))
                            .collect(Collectors.toList());
                }).get();
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        }
        private V1TopicObject newTopicResources(final TopicDescription desc, final Config config) {
            int rf = computeReplicationFactor(desc);
            return new V1TopicObject(
                    desc.name(),
                    desc.partitions().size(),
                    (short) rf,
                    Configs.of(config, configEntryPredicate)
            );
        }

        /**
         * Determines the replication factor for the specified topic based on its partitions.
         *
         * @param desc  the topic description
         * @return      return {@literal -1} if all partitions do not have a same number of replicas (this may happen during replicas reassignment).
         */
        private int computeReplicationFactor(final TopicDescription desc) {
            Iterator<TopicPartitionInfo> it = desc.partitions().iterator();
            int rf = it.next().replicas().size();
            while (it.hasNext() && rf != -1) {
                int replica = it.next().replicas().size();
                if (rf != replica) {
                    rf = -1;
                }
            }
            return rf;
        }

        private CompletableFuture<Map<String, Config>> describeConfigs(final Collection<String> topicNames) {
            return CompletableFuture.supplyAsync(() -> {
                try {
                    final ConfigsBuilder builder = new ConfigsBuilder();
                    topicNames.forEach(topicName ->
                            builder.newResourceConfig()
                                    .setType(ConfigResource.Type.TOPIC)
                                    .setName(topicName));
                    DescribeConfigsResult rs = client.describeConfigs(builder.build().keySet());
                    Map<ConfigResource, Config> configs = rs.all().get();
                    return configs.entrySet()
                            .stream()
                            .collect(Collectors.toMap(entry -> entry.getKey().name(), Map.Entry::getValue));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        }

        private CompletableFuture<Map<String, TopicDescription>> describeTopics(final Collection<String> topicNames) {
            return CompletableFuture.supplyAsync(() -> {
                try {
                    DescribeTopicsResult describeTopicsResult = client.describeTopics(topicNames);
                    return describeTopicsResult.all().get();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }
}
