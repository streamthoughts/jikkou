/*
 * Copyright 2020 The original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.streamthoughts.jikkou.kafka.internals;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.TopicListing;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.Node;

/**
 * Reusable methods for Kafka.
 */
public final class KafkaUtils {

    public static CompletableFuture<Collection<Node>> listBrokers(final AdminClient client) {
        Objects.requireNonNull(client, "client cannot be null");
        return Futures.toCompletableFuture(client.describeCluster().nodes());
    }

    public static CompletableFuture<Collection<TopicListing>> listTopics(final AdminClient client) {
        Objects.requireNonNull(client, "client cannot be null");
        return Futures.toCompletableFuture(client.listTopics().listings());
    }

    public static Map<String, Object> getAdminClientConfigs(final Map<String, Object> configs) {
        return getConfigsForKeys(configs, AdminClientConfig.configNames());
    }

    public static Map<String, Object> getProducerClientConfigs(final Map<String, Object> configs) {
        return getConfigsForKeys(configs, ProducerConfig.configNames());
    }

    public static Map<String, Object> getConsumerClientConfigs(final Map<String, Object> configs) {
        return getConfigsForKeys(configs, ConsumerConfig.configNames());
    }

    private static Map<String, Object> getConfigsForKeys(final Map<String, Object> configs,
                                                         final Set<String> keys) {
        final Map<String, Object> parsed = new HashMap<>();
        for (final String configName : keys) {
            if (configs.containsKey(configName)) {
                parsed.put(configName, configs.get(configName));
            }
        }
        return parsed;
    }
}
