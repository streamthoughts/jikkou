/*
 * Copyright 2023 StreamThoughts.
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

import io.streamthoughts.jikkou.api.annotations.AcceptsConfigProperty;
import io.streamthoughts.jikkou.api.annotations.AcceptsResource;
import io.streamthoughts.jikkou.api.config.Configuration;
import io.streamthoughts.jikkou.api.control.ResourceCollector;
import io.streamthoughts.jikkou.api.error.JikkouRuntimeException;
import io.streamthoughts.jikkou.api.model.ObjectMeta;
import io.streamthoughts.jikkou.api.selector.AggregateSelector;
import io.streamthoughts.jikkou.api.selector.ResourceSelector;
import io.streamthoughts.jikkou.kafka.AdminClientContext;
import io.streamthoughts.jikkou.kafka.MetadataAnnotations;
import io.streamthoughts.jikkou.kafka.adapters.KafkaConfigsAdapter;
import io.streamthoughts.jikkou.kafka.adapters.V1KafkaTopicSupport;
import io.streamthoughts.jikkou.kafka.converters.V1KafkaTopicListConverter;
import io.streamthoughts.jikkou.kafka.internals.ConfigsBuilder;
import io.streamthoughts.jikkou.kafka.internals.KafkaConfigPredicate;
import io.streamthoughts.jikkou.kafka.models.V1KafkaTopic;
import io.streamthoughts.jikkou.kafka.models.V1KafkaTopicList;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@AcceptsResource(type = V1KafkaTopic.class)
@AcceptsResource(type = V1KafkaTopicList.class, converter = V1KafkaTopicListConverter.class)
@AcceptsConfigProperty(
        name = ConfigDescribeConfiguration.DESCRIBE_DEFAULT_CONFIGS_PROPERTY_NAME,
        description = ConfigDescribeConfiguration.DESCRIBE_DEFAULT_CONFIGS_PROPERTY_DESC,
        defaultValue = "false",
        type = Boolean.class
)
@AcceptsConfigProperty(
        name = ConfigDescribeConfiguration.DESCRIBE_DYNAMIC_BROKER_CONFIGS_PROPERTY_NAME,
        description = ConfigDescribeConfiguration.DESCRIBE_DYNAMIC_BROKER_CONFIGS_PROPERTY_DESC,
        defaultValue = "false",
        type = Boolean.class
)
@AcceptsConfigProperty(
        name = ConfigDescribeConfiguration.DESCRIBE_STATIC_BROKER_CONFIGS_PROPERTY_CONFIG,
        description = ConfigDescribeConfiguration.DESCRIBE_STATIC_BROKER_CONFIGS_PROPERTY_DESC,
        defaultValue = "false",
        type = Boolean.class
)
public final class AdminClientKafkaTopicCollector extends AbstractAdminClientKafkaController
        implements ResourceCollector<V1KafkaTopic> {

    private static final Logger LOG = LoggerFactory.getLogger(AdminClientKafkaTopicCollector.class);

    /**
     * Creates a new {@link AdminClientKafkaTopicCollector} instance.
     */
    public AdminClientKafkaTopicCollector() {
        super();
    }

    /**
     * Creates a new {@link AdminClientKafkaTopicCollector} instance with the specified
     * application's configuration.
     *
     * @param config the application's configuration.
     */
    public AdminClientKafkaTopicCollector(final @NotNull Configuration config) {
        configure(config);
    }

    /**
     * Creates a new {@link AdminClientKafkaTopicCollector} instance with the specified {@link AdminClientContext}.
     *
     * @param adminClientContext the {@link AdminClientContext} to use for acquiring a new {@link AdminClient}.
     */
    public AdminClientKafkaTopicCollector(final @NotNull AdminClientContext adminClientContext) {
        super(adminClientContext);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<V1KafkaTopic> listAll(@NotNull final Configuration configuration,
                                      @NotNull final List<ResourceSelector> selectors) {

        var options = new ConfigDescribeConfiguration(configuration);

        if (LOG.isInfoEnabled()) {
            LOG.info("Listing all kafka topics using following options: {}", options.asConfiguration().asMap());
        }

        var predicate = new KafkaConfigPredicate()
                .withDynamicTopicConfig(true)
                .withDefaultConfig(options.isDescribeDefaultConfigs())
                .withDynamicBrokerConfig(options.isDescribeDynamicBrokerConfigs())
                .withStaticBrokerConfig(options.isDescribeStaticBrokerConfigs());

        KafkaFunction<List<V1KafkaTopic>> function = client -> new KafkaTopicsClient(client).listAll(predicate);

        List<V1KafkaTopic> resources = adminClientContext.invoke(function);

        if (LOG.isInfoEnabled()) {
            LOG.info("Found '{}' kafka topics matching the given selector(s).", resources.size());
        }

        String clusterId = adminClientContext.getClusterId();

        return resources
                .stream()
                .filter(new AggregateSelector(selectors)::apply)
                .map(resource -> resource
                        .toBuilder()
                        .withMetadata(resource.getMetadata()
                                .toBuilder()
                                .withAnnotation(MetadataAnnotations.JIKKOU_IO_KAFKA_CLUSTER_ID, clusterId)
                                .build()
                        )
                        .build()
                )
                .toList();
    }

    /**
     * Function to list all topics on Kafka Cluster matching a given predicate.
     */
    public static final class KafkaTopicsClient {

        public static final Set<String> NO_CONFIG_MAP_REFS = Collections.emptySet();
        private final AdminClient client;

        /**
         * Creates a new {@link KafkaTopicsClient} instance.
         *
         * @param client the {@link AdminClient} instance.
         */
        public KafkaTopicsClient(final AdminClient client) {
            this.client = client;
        }

        /**
         * List all kafka topics with only config-entries matching the given predicate.
         *
         * @param configEntryPredicate predicate to be used for matching config entries.
         * @return the list of kafka topics.
         */
        public List<V1KafkaTopic> listAll(@NotNull final Predicate<ConfigEntry> configEntryPredicate) {

            final Collection<String> topicNames;
            try {
                Set<String> topics = client.listTopics().names().get();
                topicNames = V1KafkaTopicSupport.stream(topics)
                        .map(topic -> topic.getMetadata().getName())
                        .toList();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new JikkouRuntimeException(e);
            } catch (ExecutionException e) {
                throw new JikkouRuntimeException(e);
            }

            final CompletableFuture<Map<String, TopicDescription>> futureTopicDesc = describeTopics(topicNames);
            final CompletableFuture<Map<String, Config>> futureTopicConfig = describeConfigs(topicNames);

            try {
                return futureTopicDesc.thenCombine(futureTopicConfig, (descriptions, configs) -> {
                    return descriptions.values()
                            .stream()
                            .map(desc -> newTopicResources(desc, configs.get(desc.name()), configEntryPredicate))
                            .toList();
                }).get();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new JikkouRuntimeException(e);
            } catch (ExecutionException e) {
                throw new JikkouRuntimeException(e);
            }
        }

        private V1KafkaTopic newTopicResources(final TopicDescription desc,
                                               final Config config,
                                               final Predicate<ConfigEntry> configEntryPredicate) {
            int rf = computeReplicationFactor(desc);
            return V1KafkaTopic.builder()
                    .withMetadata(ObjectMeta.builder().withName(desc.name()).build())
                    .withSpec(V1KafkaTopicSpec.builder()
                            .withPartitions(desc.partitions().size())
                            .withReplicas((short) rf)
                            .withConfigs(KafkaConfigsAdapter.of(config, configEntryPredicate))
                            .withConfigMapRefs(NO_CONFIG_MAP_REFS)
                            .build()
                    )
                    .build();
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
                    throw new JikkouRuntimeException(e);
                } catch (ExecutionException e) {
                    throw new JikkouRuntimeException(e);
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
                    throw new JikkouRuntimeException(e);
                } catch (ExecutionException e) {
                    throw new JikkouRuntimeException(e);
                }
            });
        }
    }
}
