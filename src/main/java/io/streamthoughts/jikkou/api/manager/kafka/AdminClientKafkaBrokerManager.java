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
package io.streamthoughts.jikkou.api.manager.kafka;

import io.streamthoughts.jikkou.api.config.JikkouConfig;
import io.streamthoughts.jikkou.api.error.ConfigException;
import io.streamthoughts.jikkou.internal.ConfigsBuilder;
import io.streamthoughts.jikkou.internal.KafkaUtils;
import io.streamthoughts.jikkou.api.manager.BrokerDescribeOptions;
import io.streamthoughts.jikkou.api.manager.KafkaBrokerManager;
import io.streamthoughts.jikkou.api.model.V1BrokerObject;
import io.streamthoughts.jikkou.api.resources.Configs;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.Config;
import org.apache.kafka.clients.admin.ConfigEntry;
import org.apache.kafka.clients.admin.DescribeClusterResult;
import org.apache.kafka.clients.admin.DescribeConfigsResult;
import org.apache.kafka.common.Node;
import org.apache.kafka.common.config.ConfigResource;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public final class AdminClientKafkaBrokerManager implements KafkaBrokerManager {

    private AdminClientContext adminClientContext;

    /**
     * Creates a new {@link AdminClientKafkaBrokerManager} instance.
     */
    public AdminClientKafkaBrokerManager() {
    }

    /**
     * Creates a new {@link AdminClientKafkaBrokerManager} instance.
     *
     * @param config the application's configuration.
     */
    public AdminClientKafkaBrokerManager(final @NotNull JikkouConfig config) {
        configure(config);
    }

    /**
     * Creates a new {@link AdminClientKafkaBrokerManager} instance.
     *
     * @param adminClientContext the {@link AdminClientContext} to use for acquiring a new {@link AdminClient}.
     */
    public AdminClientKafkaBrokerManager(final @NotNull AdminClientContext adminClientContext) {
        this.adminClientContext = adminClientContext;
    }

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
    public List<V1BrokerObject> describe(@NotNull final BrokerDescribeOptions options) {
        if (adminClientContext.isInitialized())
            return new DescribeBrokers(adminClientContext.current(), options).describe();

        return adminClientContext.invokeAndClose(adminClient -> new DescribeBrokers(adminClient, options).describe());
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
                               final BrokerDescribeOptions options) {
            this.client = client;
            this.configEntryPredicate = options.configEntryPredicate();
        }

        public List<V1BrokerObject> describe() {

            var brokerIds = loadClusterBrokerIds(client);

            final CompletableFuture<Map<String, Node>> futureTopicDesc = describeCluster();
            final CompletableFuture<Map<String, Config>> futureTopicConfig = describeConfigs(brokerIds);

            try {
                return futureTopicDesc.thenCombine(futureTopicConfig, (descriptions, configs) -> {
                    return descriptions.values().stream().map(desc -> {
                        return new V1BrokerObject(
                                desc.idString(),
                                desc.host(),
                                desc.port(),
                                desc.rack(),
                                Configs.of(configs.get(desc.idString()), configEntryPredicate)
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
