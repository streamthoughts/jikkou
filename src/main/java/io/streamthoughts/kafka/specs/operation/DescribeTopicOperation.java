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
import io.streamthoughts.kafka.specs.resources.Configs;
import io.streamthoughts.kafka.specs.resources.Named;
import io.streamthoughts.kafka.specs.resources.ResourcesIterable;
import io.streamthoughts.kafka.specs.resources.TopicResource;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.Config;
import org.apache.kafka.clients.admin.DescribeConfigsResult;
import org.apache.kafka.clients.admin.DescribeTopicsResult;
import org.apache.kafka.clients.admin.TopicDescription;
import org.apache.kafka.common.TopicPartitionInfo;
import org.apache.kafka.common.config.ConfigResource;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Class that can be used to describe topic resources.
 */
public class DescribeTopicOperation implements ClusterOperation<ResourcesIterable<TopicResource>, DescribeOperationOptions, Collection<TopicResource>> {

    private DescribeOperationOptions options;

    private AdminClient client;

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<TopicResource> execute(final AdminClient client,
                                             final ResourcesIterable<TopicResource> resources,
                                             final DescribeOperationOptions options) {

        this.options = options;
        this.client = client;

        Collection<String> topicNames = StreamSupport.stream(resources.spliterator(), false)
                .map(Named::name)
                .collect(Collectors.toList());

        final CompletableFuture<Map<String, TopicDescription>> futureTopicDesc = describe(topicNames);
        final CompletableFuture<Map<String, Config>> futureTopicConfig = getTopicConfigs(topicNames);

        try {
            return futureTopicDesc.thenCombine(futureTopicConfig, (descriptions, configs) -> {
                return descriptions.values().stream().map(desc -> {
                    return newTopicResources(desc, configs.get(desc.name()));
                }).collect(Collectors.toList());
            }).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }
    private TopicResource newTopicResources(final TopicDescription desc, final Config config) {
        int rf = computeReplicationFactor(desc);
        return new TopicResource(
            desc.name(),
            desc.partitions().size(),
            (short) rf,
            Configs.of(config, options.describeDefaultConfigs())
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
            } else {
                rf = replica;
            }
        }
        return rf;
    }

    private CompletableFuture<Map<String, Config>> getTopicConfigs(final Collection<String> topicNames) {
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

    private CompletableFuture<Map<String, TopicDescription>> describe(final Collection<String> topicNames) {
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
