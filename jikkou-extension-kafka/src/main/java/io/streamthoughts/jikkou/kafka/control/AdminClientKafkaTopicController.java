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
package io.streamthoughts.jikkou.kafka.control;

import io.streamthoughts.jikkou.api.AcceptResource;
import io.streamthoughts.jikkou.api.ReconciliationContext;
import io.streamthoughts.jikkou.api.ResourceFilter;
import io.streamthoughts.jikkou.api.config.Configuration;
import io.streamthoughts.jikkou.api.control.ChangeExecutor;
import io.streamthoughts.jikkou.api.control.ChangeResult;
import io.streamthoughts.jikkou.api.control.ResourceController;
import io.streamthoughts.jikkou.api.error.JikkouException;
import io.streamthoughts.jikkou.api.model.ObjectMeta;
import io.streamthoughts.jikkou.kafka.AdminClientContext;
import io.streamthoughts.jikkou.kafka.adapters.KafkaConfigsAdapter;
import io.streamthoughts.jikkou.kafka.control.change.KafkaTopicReconciliationConfig;
import io.streamthoughts.jikkou.kafka.control.change.TopicChange;
import io.streamthoughts.jikkou.kafka.control.change.TopicChangeComputer;
import io.streamthoughts.jikkou.kafka.control.operation.topics.AlterTopicOperation;
import io.streamthoughts.jikkou.kafka.control.operation.topics.ApplyTopicOperation;
import io.streamthoughts.jikkou.kafka.control.operation.topics.CreateTopicOperation;
import io.streamthoughts.jikkou.kafka.control.operation.topics.DeleteTopicOperation;
import io.streamthoughts.jikkou.kafka.internals.ConfigsBuilder;
import io.streamthoughts.jikkou.kafka.models.V1KafkaTopicList;
import io.streamthoughts.jikkou.kafka.models.V1KafkaTopicObject;
import io.streamthoughts.jikkou.kafka.models.V1KafkaTopicSpec;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.Config;
import org.apache.kafka.clients.admin.ConfigEntry;
import org.apache.kafka.clients.admin.DescribeConfigsResult;
import org.apache.kafka.clients.admin.DescribeTopicsResult;
import org.apache.kafka.clients.admin.TopicDescription;
import org.apache.kafka.common.TopicPartitionInfo;
import org.apache.kafka.common.config.ConfigResource;
import org.jetbrains.annotations.NotNull;

@AcceptResource(type = V1KafkaTopicList.class)
public final class AdminClientKafkaTopicController extends AdminClientKafkaController
        implements ResourceController<V1KafkaTopicList, TopicChange> {

    private static final TopicChangeComputer COMPUTER = new TopicChangeComputer();

    /**
     * Creates a new {@link AdminClientKafkaTopicController} instance.
     */
    public AdminClientKafkaTopicController() {
        super();
    }

    /**
     * Creates a new {@link AdminClientKafkaTopicController} instance with the specified
     * application's configuration.
     *
     * @param config the application's configuration.
     */
    public AdminClientKafkaTopicController(final @NotNull Configuration config) {
        configure(config);
    }

    /**
     * Creates a new {@link AdminClientKafkaTopicController} instance with the specified {@link AdminClientContext}.
     *
     * @param adminClientContext the {@link AdminClientContext} to use for acquiring a new {@link AdminClient}.
     */
    public AdminClientKafkaTopicController(final @NotNull AdminClientContext adminClientContext) {
        super(adminClientContext);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public KafkaTopicReconciliationConfig defaultConciliationConfig() {
        return new KafkaTopicReconciliationConfig();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<TopicChange> computeReconciliationChanges(@NotNull V1KafkaTopicList topicList,
                                                          @NotNull ReconciliationContext context) {


        List<V1KafkaTopicObject> topicObjects = topicList
                .optionalSpec()
                .map(V1KafkaTopicSpec::getTopics)
                .orElse(null);

        if (topicObjects == null) {
            // return; no spec exist for topics
            return Collections.emptyList();
        }

        // Build the configuration for describing actual resources
        Configuration describeConfiguration = new ConfigDescribeConfiguration(context.configuration())
                .withDescribeDefaultConfigs(true)
                .asConfiguration();

        // Get the list of remote resources that are candidates for this reconciliation
        List<V1KafkaTopicObject> actualResources =
                describe(describeConfiguration, context.filter())
                .getSpec().getTopics();

        // Get the list of described resource that are candidates for this reconciliation
        List<V1KafkaTopicObject> expectedResources = topicObjects
                .stream()
                .filter(context.filter()::apply)
                .toList();

        return COMPUTER.computeChanges(
                actualResources,
                expectedResources,
                new KafkaTopicReconciliationConfig(context.configuration())
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public V1KafkaTopicList describe(@NotNull final Configuration configuration,
                                     @NotNull final ResourceFilter resourceFilter) {

        ConfigDescribeConfiguration describeConfiguration = new ConfigDescribeConfiguration(configuration);
        KafkaFunction<List<V1KafkaTopicObject>> function = client -> new DescribeTopics(
                client,
                describeConfiguration.configEntryPredicate(),
                resourceFilter.getPredicateByName()
        ).describe();
        List<V1KafkaTopicObject> topicObjects = adminClientContext.invoke(function);

        return new V1KafkaTopicList()
                .toBuilder()
                .withMetadata(ObjectMeta
                        .builder()
                        .withAnnotation("jikkou.io/kafka-cluster-id", adminClientContext.getClusterId())
                        .build()
                )
                .withSpec(V1KafkaTopicSpec.builder()
                        .withTopics(topicObjects)
                        .build()
                )
                .build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<ChangeResult<TopicChange>> create(@NotNull List<TopicChange> changes, boolean dryRun) {
        var operation = new CreateTopicOperation(adminClientContext.client());
        return ChangeExecutor.ofSupplier(() -> changes).execute(operation, dryRun);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<ChangeResult<TopicChange>> update(@NotNull List<TopicChange> changes, boolean dryRun) {
        var operation = new AlterTopicOperation(adminClientContext.client());
        return ChangeExecutor.ofSupplier(() -> changes).execute(operation, dryRun);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<ChangeResult<TopicChange>> delete(@NotNull List<TopicChange> changes, boolean dryRun) {
        var operation = new DeleteTopicOperation(adminClientContext.client());
        return ChangeExecutor.ofSupplier(() -> changes).execute(operation, dryRun);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<ChangeResult<TopicChange>> apply(@NotNull List<TopicChange> changes, boolean dryRun) {
        var operation = new ApplyTopicOperation(adminClientContext.client());
        return ChangeExecutor.ofSupplier(() -> changes).execute(operation, dryRun);
    }

    /**
     * Function to list all topics on Kafka Cluster matching a given predicate.
     */
    public static final class DescribeTopics {

        public static final Set<String> NO_CONFIG_MAP_REFS = null;
        private final AdminClient client;

        private final Predicate<ConfigEntry> configEntryPredicate;

        private final Predicate<String> topicPredicate;

        public DescribeTopics(final AdminClient client,
                              final Predicate<ConfigEntry> configEntryPredicate,
                              final Predicate<String> topicPredicate) {
            this.client = client;
            this.configEntryPredicate = configEntryPredicate;
            this.topicPredicate = topicPredicate;
        }

        public List<V1KafkaTopicObject> describe() {

            final Collection<String> topicNames;
            try {
                topicNames = client.listTopics().names().get()
                        .stream()
                        .filter(topicPredicate)
                        .collect(Collectors.toList());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new JikkouException(e);
            } catch (ExecutionException e) {
                throw new JikkouException(e);
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
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new JikkouException(e);
            } catch (ExecutionException e) {
                throw new JikkouException(e);
            }
        }

        private V1KafkaTopicObject newTopicResources(final TopicDescription desc, final Config config) {
            int rf = computeReplicationFactor(desc);
            return new V1KafkaTopicObject(
                    desc.name(),
                    desc.partitions().size(),
                    (short) rf,
                    KafkaConfigsAdapter.of(config, configEntryPredicate),
                    NO_CONFIG_MAP_REFS
            );
        }

        /**
         * Determines the replication factor for the specified topic based on its partitions.
         *
         * @param desc the topic description
         * @return return {@literal -1} if all partitions do not have a same number of replicas (this may happen during replicas reassignment).
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
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new JikkouException(e);
                } catch (ExecutionException e) {
                    throw new JikkouException(e);
                }
            });
        }

        private CompletableFuture<Map<String, TopicDescription>> describeTopics(final Collection<String> topicNames) {
            return CompletableFuture.supplyAsync(() -> {
                try {
                    DescribeTopicsResult describeTopicsResult = client.describeTopics(topicNames);
                    return describeTopicsResult.allTopicNames().get();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new JikkouException(e);
                } catch (ExecutionException e) {
                    throw new JikkouException(e);
                }
            });
        }
    }
}
