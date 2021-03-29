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
package io.streamthoughts.kafka.specs.operation;

import io.streamthoughts.kafka.specs.internal.ConfigsBuilder;
import io.streamthoughts.kafka.specs.resources.BrokerResource;
import io.streamthoughts.kafka.specs.resources.Configs;
import io.streamthoughts.kafka.specs.resources.ResourcesIterable;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.Config;
import org.apache.kafka.clients.admin.DescribeClusterResult;
import org.apache.kafka.clients.admin.DescribeConfigsResult;
import org.apache.kafka.common.Node;
import org.apache.kafka.common.config.ConfigResource;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Class that can be used to describe topic resources.
 */
public class DescribeBrokerOperation implements ClusterOperation<ResourcesIterable<BrokerResource>, DescribeOperationOptions, Collection<BrokerResource>> {

    private DescribeOperationOptions options;

    private AdminClient client;

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<BrokerResource> execute(final AdminClient client,
                                              final ResourcesIterable<BrokerResource> resources,
                                              final DescribeOperationOptions options) {

        this.options = options;
        this.client = client;

        Collection<String> brokerIds = StreamSupport.stream(resources.spliterator(), false)
                .map(BrokerResource::id)
                .collect(Collectors.toList());

        final CompletableFuture<Map<String, Node>> futureTopicDesc = describe();
        final CompletableFuture<Map<String, Config>> futureTopicConfig = getBrokerConfigs(brokerIds);

        try {
            return futureTopicDesc.thenCombine(futureTopicConfig, (descriptions, configs) -> {
                return descriptions.values().stream().map(desc -> {
                    return new BrokerResource(
                            desc.idString(),
                            desc.host(),
                            desc.port(),
                            desc.rack(),
                            Configs.of(configs.get(desc.idString()), options.describeDefaultConfigs())
                    );
                }).collect(Collectors.toList());
            }).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    private CompletableFuture<Map<String, Config>> getBrokerConfigs(final Collection<String> brokerIds) {
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

    private CompletableFuture<Map<String, Node>> describe() {
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
