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
import io.streamthoughts.jikkou.kafka.converters.V1KafkaBrokerListConverter;
import io.streamthoughts.jikkou.kafka.internals.ConfigsBuilder;
import io.streamthoughts.jikkou.kafka.internals.KafkaConfigPredicate;
import io.streamthoughts.jikkou.kafka.internals.KafkaUtils;
import io.streamthoughts.jikkou.kafka.models.V1KafkaBroker;
import io.streamthoughts.jikkou.kafka.models.V1KafkaBrokerList;
import io.streamthoughts.jikkou.kafka.models.V1KafkaBrokersSpec;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.Config;
import org.apache.kafka.clients.admin.ConfigEntry;
import org.apache.kafka.clients.admin.DescribeClusterResult;
import org.apache.kafka.clients.admin.DescribeConfigsResult;
import org.apache.kafka.common.Node;
import org.apache.kafka.common.config.ConfigResource;
import org.jetbrains.annotations.NotNull;

@AcceptsResource(type = V1KafkaBroker.class)
@AcceptsResource(type = V1KafkaBrokerList.class, converter = V1KafkaBrokerListConverter.class)
public final class AdminClientKafkaBrokerCollector extends AbstractAdminClientKafkaController
        implements ResourceCollector<V1KafkaBroker> {

    /**
     * Creates a new {@link AdminClientKafkaBrokerCollector} instance.
     */
    public AdminClientKafkaBrokerCollector() {
        super();
    }

    /**
     * Creates a new {@link AdminClientKafkaBrokerCollector} instance.
     *
     * @param config the application's configuration.
     */
    public AdminClientKafkaBrokerCollector(final @NotNull Configuration config) {
        super(config);
    }

    /**
     * Creates a new {@link AdminClientKafkaBrokerCollector} instance.
     *
     * @param adminClientContext the {@link AdminClientContext} to use for acquiring a new {@link AdminClient}.
     */
    public AdminClientKafkaBrokerCollector(final @NotNull AdminClientContext adminClientContext) {
        super(adminClientContext);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<V1KafkaBroker> listAll(@NotNull final Configuration configuration,
                                       @NotNull final List<ResourceSelector> selectors) {

        var options = new ConfigDescribeConfiguration(configuration);

        var predicate = new KafkaConfigPredicate()
                .withDefaultConfig(options.isDescribeDefaultConfigs())
                .withDynamicBrokerConfig(options.isDescribeDynamicBrokerConfigs())
                .withStaticBrokerConfig(options.isDescribeStaticBrokerConfigs());

        var resources = adminClientContext.invoke(client -> new KafkaBrokerClient(client).listAll(predicate));

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

    private Collection<String> loadClusterBrokerIds(final AdminClient client) {
        CompletableFuture<Collection<Node>> topics = KafkaUtils.listBrokers(client);
        return topics
                .thenApply(t -> t.stream().map(Node::idString).collect(Collectors.toList()))
                .join();
    }

    /**
     * Class that can be used to describe topic resources.
     */
    public final class KafkaBrokerClient {

        private final AdminClient client;

        public KafkaBrokerClient(final AdminClient client) {
            this.client = client;
        }

        /**
         * List all kafka brokers with only config-entries matching the given predicate.
         *
         * @param configEntryPredicate predicate to be used for matching config entries.
         * @return the list of kafka brokers.
         */
        public List<V1KafkaBroker> listAll(final Predicate<ConfigEntry> configEntryPredicate) {

            var brokerIds = loadClusterBrokerIds(client);

            final CompletableFuture<Map<String, Node>> futureTopicDesc = describeCluster();
            final CompletableFuture<Map<String, Config>> futureTopicConfig = describeConfigs(brokerIds);

            try {
                return futureTopicDesc.thenCombine(futureTopicConfig, (descriptions, configs) -> {
                    return descriptions.values().stream().map(desc -> V1KafkaBroker
                            .builder()
                            .withMetadata(ObjectMeta
                                .builder()
                                .withName(desc.idString())
                                .build()
                            )
                            .withSpec(V1KafkaBrokersSpec
                                    .builder()
                                    .withId(desc.idString())
                                    .withRack(desc.rack())
                                    .withHost(desc.host())
                                    .withPort(desc.port())
                                    .withConfigs(KafkaConfigsAdapter.of(configs.get(desc.idString()), configEntryPredicate))
                                    .build()
                            )
                            .build()
                    ).collect(Collectors.toList());
                }).get();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new JikkouRuntimeException(e);
            } catch (ExecutionException e) {
                throw new JikkouRuntimeException(e);
            }
        }

        private CompletableFuture<Map<String, Config>> describeConfigs(final Collection<String> brokerIds) {
            return CompletableFuture.supplyAsync(() -> {
                try {
                    final ConfigsBuilder builder = new ConfigsBuilder();
                    brokerIds.forEach(idString ->
                            builder.newResourceConfig()
                                    .setType(ConfigResource.Type.BROKER)
                                    .setName(idString));
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

        private CompletableFuture<Map<String, Node>> describeCluster() {
            return CompletableFuture.supplyAsync(() -> {
                try {
                    DescribeClusterResult describeClusterResult = client.describeCluster();
                    final Collection<Node> nodes = describeClusterResult.nodes().get();
                    return nodes.stream().collect(Collectors.toMap(Node::idString, n -> n));
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
