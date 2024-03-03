/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.kafka.reconciler.service;

import static io.streamthoughts.jikkou.common.utils.AsyncUtils.getValueOrThrowException;

import io.streamthoughts.jikkou.core.exceptions.JikkouRuntimeException;
import io.streamthoughts.jikkou.core.models.ObjectMeta;
import io.streamthoughts.jikkou.kafka.KafkaLabelAndAnnotations;
import io.streamthoughts.jikkou.kafka.adapters.KafkaConfigsAdapter;
import io.streamthoughts.jikkou.kafka.internals.ConfigsBuilder;
import io.streamthoughts.jikkou.kafka.internals.Futures;
import io.streamthoughts.jikkou.kafka.models.KafkaTopicPartitionInfo;
import io.streamthoughts.jikkou.kafka.models.V1KafkaTopic;
import io.streamthoughts.jikkou.kafka.models.V1KafkaTopicSpec;
import io.streamthoughts.jikkou.kafka.models.V1KafkaTopicStatus;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.Config;
import org.apache.kafka.clients.admin.ConfigEntry;
import org.apache.kafka.clients.admin.DescribeConfigsResult;
import org.apache.kafka.clients.admin.DescribeTopicsResult;
import org.apache.kafka.clients.admin.OffsetSpec;
import org.apache.kafka.clients.admin.TopicDescription;
import org.apache.kafka.common.Node;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.TopicPartitionInfo;
import org.apache.kafka.common.Uuid;
import org.apache.kafka.common.config.ConfigResource;
import org.jetbrains.annotations.NotNull;

/**
 * Service for manipulating Kafka topics.
 */
public final class KafkaTopicService {

    public static final Set<String> NO_CONFIG_MAP_REFS = Collections.emptySet();
    private final AdminClient client;

    /**
     * Creates a new {@link KafkaTopicService} instance.
     *
     * @param client the {@link AdminClient} instance.
     */
    public KafkaTopicService(final AdminClient client) {
        this.client = Objects.requireNonNull(client, "client cannot be null");
    }

    /**
     * List all kafka topics with only config-entries matching the given predicate.
     *
     * @param configEntryPredicate The predicate for matching config entries.
     * @return the list of V1KafkaTopic.
     */
    public List<V1KafkaTopic> listAll(@NotNull final Predicate<ConfigEntry> configEntryPredicate, boolean status) {

        // Gather all topic names
        Set<String> topics = getValueOrThrowException(
            Futures.toCompletableFuture(client.listTopics().names()),
            e -> new JikkouRuntimeException("Failed to list kafka topics", e)
        );
        return listAll(topics, configEntryPredicate, status);
    }

    /**
     * List all kafka topics with only config-entries matching the given predicate.
     *
     * @param topics               The set of topic names.
     * @param configEntryPredicate The predicate for matching config entries.
     * @return The V1KafkaTopic.
     */
    public List<V1KafkaTopic> listAll(@NotNull final Set<String> topics,
                                      @NotNull final Predicate<ConfigEntry> configEntryPredicate,
                                      boolean status) {

        // Gather description and configuration for all topics
        CompletableFuture<List<V1KafkaTopic>> results = getDescriptionForTopics(topics)
            .thenCombine(getConfigForTopics(topics), (descriptions, configs) -> descriptions.values()
                .stream()
                .map(desc -> newTopicResources(desc, configs.get(desc.name()), configEntryPredicate, status))
                .toList());

        return getValueOrThrowException(
            results,
            e -> new JikkouRuntimeException("Failed to retrieve kafka topic descriptions/or configurations.", e)
        );
    }

    private V1KafkaTopic newTopicResources(final TopicDescription description,
                                           final Config config,
                                           final Predicate<ConfigEntry> configEntryPredicate,
                                           boolean status) {
        int rf = computeReplicationFactor(description);
        V1KafkaTopic.V1KafkaTopicBuilder builder = V1KafkaTopic.builder()
            .withMetadata(ObjectMeta
                .builder()
                .withName(description.name())
                .withLabel(KafkaLabelAndAnnotations.JIKKOU_IO_KAFKA_TOPIC_ID, description.topicId().toString())
                .build()
            )
            .withSpec(V1KafkaTopicSpec.builder()
                .withPartitions(description.partitions().size())
                .withReplicas((short) rf)
                .withConfigs(KafkaConfigsAdapter.of(config, configEntryPredicate))
                .withConfigMapRefs(NO_CONFIG_MAP_REFS)
                .build()
            );
        if (status) {
            List<KafkaTopicPartitionInfo> partitions = description.partitions()
                .stream()
                .map(info -> new KafkaTopicPartitionInfo(
                        info.partition(),
                        info.leader().id(),
                        info.replicas().stream().map(Node::id).toList(),
                        info.isr().stream().map(Node::id).toList()
                    )
                )
                .toList();
            builder = builder.withStatus(V1KafkaTopicStatus
                .builder()
                .withPartitions(partitions)
                .build()
            );
        }
        return builder.build();
    }

    public Map<TopicPartition, Long> getLogEndOffsetForTopicPartition(final Set<TopicPartition> topicPartitions) {
        try {
            Map<TopicPartition, OffsetSpec> offsetSpecByPartition = topicPartitions
                .stream()
                .collect(Collectors.toMap(Function.identity(), it -> OffsetSpec.latest()));
            return client.listOffsets(offsetSpecByPartition)
                .all()
                .get()
                .entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, it -> it.getValue().offset()));
        } catch (InterruptedException | ExecutionException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            throw new JikkouRuntimeException(
                "Failed to get log end-offset for topic partitions: " + topicPartitions, e);
        }
    }

    /**
     * Determines the replication factor for the specified topic based on its partitions.
     *
     * @param desc The TopicDescription.
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

    private CompletableFuture<Map<String, Config>> getConfigForTopics(final Collection<String> topicNames) {
        final ConfigsBuilder builder = new ConfigsBuilder();
        topicNames.forEach(topicName ->
            builder.newResourceConfig()
                .setType(ConfigResource.Type.TOPIC)
                .setName(topicName)
        );

        Set<ConfigResource> resources = builder.build().keySet();
        DescribeConfigsResult result = client.describeConfigs(resources);
        return Futures.toCompletableFuture(result.all())
            .thenApply(configs -> configs.entrySet()
                .stream()
                .collect(Collectors.toMap(entry -> entry.getKey().name(), Map.Entry::getValue)));
    }

    private CompletableFuture<Map<String, TopicDescription>> getDescriptionForTopics(final Collection<String> topicNames) {
        DescribeTopicsResult result = client.describeTopics(topicNames);
        return Futures.toCompletableFuture(result.allTopicNames());
    }

    public static void main(String[] args) {
        System.out.println(new Uuid(0L, 0L));
    }
}
