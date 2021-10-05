/*
 * Copyright 2020 StreamThoughts.
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
package io.streamthoughts.kafka.specs.command.broker.subcommands.internal;

import io.streamthoughts.kafka.specs.internal.ConfigsBuilder;
import io.streamthoughts.kafka.specs.operation.DescribeOperationOptions;
import io.streamthoughts.kafka.specs.model.V1BrokerObject;
import io.streamthoughts.kafka.specs.resources.Configs;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.Config;
import org.apache.kafka.clients.admin.ConfigEntry;
import org.apache.kafka.clients.admin.DescribeClusterResult;
import org.apache.kafka.clients.admin.DescribeConfigsResult;
import org.apache.kafka.common.Node;
import org.apache.kafka.common.config.ConfigResource;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Class that can be used to describe topic resources.
 */
public class DescribeBrokersFunction implements Function<Collection<String>, Collection<V1BrokerObject>> {

    private final AdminClient client;

    private Predicate<ConfigEntry> configEntryPredicate;

    public DescribeBrokersFunction(final AdminClient client,
                                   final DescribeOperationOptions options) {
        this.client = client;
        this.configEntryPredicate = entry -> !entry.isDefault() || options.describeDefaultConfigs();
    }

    public DescribeBrokersFunction addConfigEntryPredicate(final Predicate<ConfigEntry> configEntryPredicate) {
        this.configEntryPredicate = this.configEntryPredicate.and(configEntryPredicate) ;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<V1BrokerObject> apply(final Collection<String> brokerIds) {

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
