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
import io.streamthoughts.jikkou.api.ResourceFilter;
import io.streamthoughts.jikkou.api.config.Configuration;
import io.streamthoughts.jikkou.api.control.ResourceDescriptor;
import io.streamthoughts.jikkou.api.model.ObjectMeta;
import io.streamthoughts.jikkou.kafka.AdminClientContext;
import io.streamthoughts.jikkou.kafka.adapters.KafkaConfigsAdapter;
import io.streamthoughts.jikkou.kafka.internals.ConfigsBuilder;
import io.streamthoughts.jikkou.kafka.internals.KafkaUtils;
import io.streamthoughts.jikkou.kafka.models.V1KafkaBrokerList;
import io.streamthoughts.jikkou.kafka.models.V1KafkaBrokerObject;
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

@AcceptResource(type = V1KafkaBrokerList.class)
public final class AdminClientKafkaBrokerDescriptor extends AdminClientKafkaController
        implements ResourceDescriptor<V1KafkaBrokerList> {

    /**
     * Creates a new {@link AdminClientKafkaBrokerDescriptor} instance.
     */
    public AdminClientKafkaBrokerDescriptor() {
        super();
    }

    /**
     * Creates a new {@link AdminClientKafkaBrokerDescriptor} instance.
     *
     * @param config the application's configuration.
     */
    public AdminClientKafkaBrokerDescriptor(final @NotNull Configuration config) {
        super(config);
    }

    /**
     * Creates a new {@link AdminClientKafkaBrokerDescriptor} instance.
     *
     * @param adminClientContext the {@link AdminClientContext} to use for acquiring a new {@link AdminClient}.
     */
    public AdminClientKafkaBrokerDescriptor(final @NotNull AdminClientContext adminClientContext) {
        super(adminClientContext);
    }

    /** {@inheritDoc} */
    @Override
    public V1KafkaBrokerList describe(@NotNull final Configuration configuration,
                                      @NotNull final ResourceFilter filter) {

        var configDescribeConfiguration = new ConfigDescribeConfiguration(configuration);
        var brokerObjectList = adminClientContext.invoke(
                client -> {
                    return new DescribeBrokers(
                            client,
                            configDescribeConfiguration
                                    .configEntryPredicate()
                    ).describe();
                }
        );

        return new V1KafkaBrokerList().toBuilder()
                .withMetadata(ObjectMeta
                        .builder()
                        .withAnnotation("jikkou.io/kafka-cluster-id", adminClientContext.getClusterId() )
                        .build()
                )
                .withSpec(V1KafkaBrokersSpec
                        .builder()
                        .withBrokers(brokerObjectList)
                        .build()
                )
                .build();
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
    public final class DescribeBrokers {

        private final AdminClient client;

        private final Predicate<ConfigEntry> configEntryPredicate;

        public DescribeBrokers(final AdminClient client,
                               final Predicate<ConfigEntry> configEntryPredicate) {
            this.client = client;
            this.configEntryPredicate = configEntryPredicate;
        }

        public List<V1KafkaBrokerObject> describe() {

            var brokerIds = loadClusterBrokerIds(client);

            final CompletableFuture<Map<String, Node>> futureTopicDesc = describeCluster();
            final CompletableFuture<Map<String, Config>> futureTopicConfig = describeConfigs(brokerIds);

            try {
                return futureTopicDesc.thenCombine(futureTopicConfig, (descriptions, configs) -> {
                    return descriptions.values().stream().map(desc -> {
                        return new V1KafkaBrokerObject(
                                desc.idString(),
                                desc.host(),
                                desc.port(),
                                desc.rack(),
                                KafkaConfigsAdapter.of(configs.get(desc.idString()), configEntryPredicate)
                        );
                    }).collect(Collectors.toList());
                }).get();
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
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
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        }

        private CompletableFuture<Map<String, Node>> describeCluster() {
            return CompletableFuture.supplyAsync(() -> {
                try {
                    DescribeClusterResult describeClusterResult = client.describeCluster();
                    final Collection<Node> nodes = describeClusterResult.nodes().get();
                    return nodes.stream().collect(Collectors.toMap(Node::idString, n -> n));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }
}
