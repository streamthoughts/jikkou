/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.kafka.reconciler.service;

import static io.streamthoughts.jikkou.kafka.KafkaLabelAndAnnotations.JIKKOU_IO_KAFKA_IS_SIMPLE_CONSUMER;

import io.streamthoughts.jikkou.common.utils.AsyncUtils;
import io.streamthoughts.jikkou.common.utils.Strings;
import io.streamthoughts.jikkou.core.exceptions.JikkouRuntimeException;
import io.streamthoughts.jikkou.core.models.ObjectMeta;
import io.streamthoughts.jikkou.kafka.collections.V1KafkaConsumerGroupList;
import io.streamthoughts.jikkou.kafka.internals.Futures;
import io.streamthoughts.jikkou.kafka.models.*;
import io.streamthoughts.jikkou.kafka.reconciler.service.KafkaOffsetSpec.ToEarliest;
import io.streamthoughts.jikkou.kafka.reconciler.service.KafkaOffsetSpec.ToLatest;
import io.streamthoughts.jikkou.kafka.reconciler.service.KafkaOffsetSpec.ToOffset;
import io.streamthoughts.jikkou.kafka.reconciler.service.KafkaOffsetSpec.ToTimestamp;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.kafka.clients.admin.*;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.ConsumerGroupState;
import org.apache.kafka.common.KafkaFuture;
import org.apache.kafka.common.TopicPartition;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * Service to manage Kafka resources
 */
public final class KafkaAdminService {

    private static final Logger LOG = LoggerFactory.getLogger(KafkaAdminService.class);

    private final AdminClient client;

    /**
     * Creates a new {@link KafkaAdminService} instance.
     *
     * @param client The AdminClient.
     */
    public KafkaAdminService(final @NotNull AdminClient client) {
        this.client = Objects.requireNonNull(client, "client cannot be null");
    }

    /**
     * Resets the Consumer Group offsets for the specified groupID and topics.
     *
     * @param groupId    The group ID - cannot be {@code null}.
     * @param topics     The list of topics - cannot be {@code null}.
     * @param offsetSpec The offset specification.
     * @param dryRun     Specify whether to run this method in dry-run.
     * @return The V1KafkaConsumerGroup.
     */
    public V1KafkaConsumerGroup resetConsumerGroupOffsets(final @NotNull String groupId,
                                                          final @NotNull List<String> topics,
                                                          final @NotNull KafkaOffsetSpec offsetSpec,
                                                          boolean dryRun) {
        return switch (offsetSpec) {
            // TO_EARLIEST
            case ToEarliest ignored ->
                resetConsumerGroupOffsets(groupId, topics, OffsetSpec.earliest(), dryRun);
            // TO_LATEST
            case ToLatest ignored ->
                resetConsumerGroupOffsets(groupId, topics, OffsetSpec.latest(), dryRun);
            // TO_TIMESTAMP
            case ToTimestamp spec ->
                resetConsumerGroupOffsets(groupId, topics, OffsetSpec.forTimestamp(spec.timestamp()), dryRun);
            // TO_OFFSETS
            case ToOffset spec -> {
                // Get the partitions for the given topics.
                CompletableFuture<List<TopicPartition>> future = listTopicPartitions(topics);
                Map<TopicPartition, OffsetAndMetadata> offsets = AsyncUtils.getValueOrThrowException(future, JikkouRuntimeException::new)
                    .stream()
                    .collect(Collectors.toMap(Function.identity(), unused -> new OffsetAndMetadata(spec.offset())));
                // Alter the consumer group offsets.
                yield alterConsumerGroupOffsets(groupId, offsets, dryRun);
            }
            case null -> throw new IllegalArgumentException("offsetSpec cannot be null");
        };
    }

    /**
     * Resets the Consumer Group offsets for the specified groupID and topics.
     *
     * @param groupId    The group ID - cannot be {@code null}.
     * @param topics     The list of topics - cannot be {@code null}.
     * @param offsetSpec The offset to reset to.
     * @param dryRun     Specify whether to run this method in dry-run.
     * @return The V1KafkaConsumerGroup.
     */
    public V1KafkaConsumerGroup resetConsumerGroupOffsets(@NotNull String groupId,
                                                          @NotNull List<String> topics,
                                                          @NotNull OffsetSpec offsetSpec,
                                                          boolean dryRun) {
        if (Strings.isNullOrEmpty(groupId)) {
            throw new IllegalArgumentException("groupId cannot be null");
        }
        if (topics == null) {
            throw new IllegalArgumentException("topics cannot be null");
        }

        // List offsets and Map to OffsetAndMetadata
        CompletableFuture<Map<TopicPartition, ListOffsetsResult.ListOffsetsResultInfo>> future = listOffsets(topics, offsetSpec);
        Map<TopicPartition, OffsetAndMetadata> offsets = AsyncUtils.getValueOrThrowException(future, JikkouRuntimeException::new)
            .entrySet()
            .stream()
            .collect(Collectors.toMap(Map.Entry::getKey, entry -> new OffsetAndMetadata(entry.getValue().offset())));

        return alterConsumerGroupOffsets(groupId, offsets, dryRun);
    }

    private V1KafkaConsumerGroup alterConsumerGroupOffsets(@NotNull String groupId,
                                                           @NotNull Map<TopicPartition, OffsetAndMetadata> offsets,
                                                           boolean dryRun) {

        if (LOG.isInfoEnabled()) {
            LOG.info("Altering offsets for consumer group '{}': {} (DRY_RUN: {}).", groupId, offsets, dryRun);
        }

        // DRY-RUN = FALSE
        if (!dryRun) {
            // Alter Consumer Group Offsets
            KafkaFuture<Void> future = client.alterConsumerGroupOffsets(groupId, offsets).all();
            AsyncUtils.getValueOrThrowException(future.toCompletionStage().toCompletableFuture(), JikkouRuntimeException::new);
        }

        V1KafkaConsumerGroupList groups = listConsumerGroups(List.of(groupId), true);
        V1KafkaConsumerGroup group = groups.first();

        // DRY-RUN = TRUE
        if (dryRun) {
            V1KafkaConsumerGroupStatus status = group.getStatus();
            Map<TopicPartition, V1KafkaConsumerOffset> offsetsByTopicPartitions = status.getOffsets()
                .stream()
                .collect(Collectors.toMap(it -> new TopicPartition(it.getTopic(), it.getPartition()), it -> it));

            Map<TopicPartition, V1KafkaConsumerOffset> newOffsetsByTopicPartitions = new HashMap<>(offsets.
                entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, it -> V1KafkaConsumerOffset
                    .builder()
                    .withTopic(it.getKey().topic())
                    .withPartition(it.getKey().partition())
                    .withOffset(it.getValue().offset())
                    .build()
                )));
            offsetsByTopicPartitions.forEach((tp, offset) -> {
                if (!newOffsetsByTopicPartitions.containsKey(tp)) {
                    newOffsetsByTopicPartitions.put(tp, offset);
                }
            });
            group = group.withStatus(status.withOffsets(new ArrayList<>(newOffsetsByTopicPartitions.values())));
        }
        return group;
    }

    public CompletableFuture<Map<TopicPartition, ListOffsetsResult.ListOffsetsResultInfo>> listOffsets(@NotNull final List<String> topics,
                                                                                                       @NotNull final OffsetSpec offsetSpec) {
        // Get the partitions for the given topics.
        CompletableFuture<List<TopicPartition>> future = listTopicPartitions(topics);

        // Gets EARLIEST offsets for each topic partitions.
        return future.thenCompose(partitions -> {
            var partitionOffsets = partitions.stream()
                .collect(Collectors.toMap(Function.identity(), it -> offsetSpec));
            return client.listOffsets(partitionOffsets).all().toCompletionStage();
        });

    }

    public CompletableFuture<List<TopicPartition>> listTopicPartitions(@NotNull final List<String> topics) {
        return client.describeTopics(topics)
            .allTopicNames()
            .toCompletionStage()
            .thenApply(topicByName -> topicByName.values()
                .stream()
                .flatMap(topic -> topic.partitions()
                    .stream().
                    map(partitionInfo -> new TopicPartition(topic.name(), partitionInfo.partition()))
                ).toList()
            )
            .toCompletableFuture();
    }

    /**
     * Lists all consumer groups for the specified states.
     *
     * @param inStates        Set of ConsumerGroupState to listing group.
     * @param describeOffsets Specify whether offsets should be described.
     * @return The {@link V1KafkaConsumerGroupList}.
     */
    @NotNull
    public V1KafkaConsumerGroupList listConsumerGroups(@NotNull Set<ConsumerGroupState> inStates,
                                                       boolean describeOffsets) {
        final List<String> groupIds = getConsumerGroupIds(inStates);
        return listConsumerGroups(groupIds, describeOffsets);
    }

    /**
     * Lists all consumer groups for the specified states.
     *
     * @param groups          The consumer groups.
     * @param describeOffsets Specify whether offsets should be described.
     * @return The {@link V1KafkaConsumerGroupList}.
     */
    @NotNull
    public V1KafkaConsumerGroupList listConsumerGroups(@NotNull List<String> groups,
                                                       boolean describeOffsets) {

        // List Consumer Group Offsets
        final ListConsumerGroupOffsetsResult groupOffsetsResult;
        if (describeOffsets) {
            groupOffsetsResult = client.listConsumerGroupOffsets(groups.stream().collect(Collectors.toMap(
                Function.identity(),
                it -> new ListConsumerGroupOffsetsSpec()
            )));
        } else {
            groupOffsetsResult = null;
        }

        // Describe Consumer Groups
        try {
            List<V1KafkaConsumerGroup> items = Flux
                .fromStream(client
                    .describeConsumerGroups(groups)
                    .describedGroups()
                    .values()
                    .stream()
                    .map(consumerGroupDescriptionKafkaFuture -> Futures
                        .toCompletableFuture(consumerGroupDescriptionKafkaFuture)
                        .thenApply(this::mapToResource)
                    )
                )
                .publishOn(Schedulers.boundedElastic())
                .flatMap(Mono::fromFuture)
                .flatMap(group -> {
                    String groupName = group.getMetadata().getName();
                    if (groupOffsetsResult != null) {
                        return Mono
                            .fromFuture(Futures.toCompletableFuture(groupOffsetsResult.partitionsToOffsetAndMetadata(groupName)))
                            .flatMap(partitions -> mapToResources(partitions).collectList()
                                .map(offsets -> group.withStatus(group.getStatus().withOffsets(offsets))));
                    }
                    return Mono.just(group);
                })
                .collectList()
                .block();

            return new V1KafkaConsumerGroupList.Builder().withItems(items).build();
        } catch (Exception e) {
            LOG.error("Failed to describe consumer groups.", e);
            throw new JikkouRuntimeException(String.format(
                "Failed to describe consumer groups. Cause %s: %s.", e.getClass().getSimpleName(), e.getLocalizedMessage())
            );
        }
    }

    @NotNull
    private List<String> getConsumerGroupIds(@NotNull Set<ConsumerGroupState> inStates) {
        var options = new ListConsumerGroupsOptions().inStates(inStates);
        ListConsumerGroupsResult groups = client.listConsumerGroups(options);

        CompletableFuture<Collection<ConsumerGroupListing>> groupListingFuture = Futures.toCompletableFuture(groups.all());
        Collection<ConsumerGroupListing> listings = AsyncUtils.getValueOrThrowException(groupListingFuture, e -> {
            LOG.error("Failed to list consumer groups.", e);
            return new JikkouRuntimeException(String.format(
                "Failed to list consumer groups. Cause %s: %s.",
                e.getClass().getSimpleName(),
                e.getLocalizedMessage()
            ));
        });

        return listings.stream().map(ConsumerGroupListing::groupId).toList();
    }

    public V1KafkaConsumerGroup mapToResource(@NotNull ConsumerGroupDescription description) {

        List<V1KafkaConsumerGroupMember> members = description.members().stream()
            .map(member -> {
                    V1KafkaConsumerGroupMember.V1KafkaConsumerGroupMemberBuilder builder = V1KafkaConsumerGroupMember
                        .builder()
                        .withHost(member.host())
                        .withClientId(member.clientId())
                        .withMemberId(member.consumerId());
                    // groupInstanceId
                    builder = member.groupInstanceId()
                        .map(builder::withGroupInstanceId)
                        .orElse(builder);
                    // assignments
                    List<String> assignments = member.assignment().topicPartitions().stream().map(TopicPartition::toString).toList();
                    builder = builder.withAssignments(assignments);
                    return builder.build();
                }
            )
            .toList();

        V1KafkaConsumerGroupStatus.V1KafkaConsumerGroupStatusBuilder groupStatusBuilder = V1KafkaConsumerGroupStatus
            .builder()
            .withState(description.state().name())
            .withCoordinator(V1KafkaNode
                .builder()
                .withId(description.coordinator().idString())
                .withHost(description.coordinator().host())
                .withPort(description.coordinator().port())
                .withRack(description.coordinator().rack())
                .build()
            )
            .withMembers(members);

        V1KafkaConsumerGroupStatus status = groupStatusBuilder.build();

        return V1KafkaConsumerGroup.builder()
            .withMetadata(ObjectMeta
                .builder()
                .withName(description.groupId())
                .withLabel(JIKKOU_IO_KAFKA_IS_SIMPLE_CONSUMER, description.isSimpleConsumerGroup())
                .build()
            )
            .withStatus(status)
            .build();
    }

    private Flux<V1KafkaConsumerOffset> mapToResources(final Map<TopicPartition, OffsetAndMetadata> offsetsByTopicPartition) {
        return new KafkaTopicService(client).getLogEndOffsetForTopicPartition(offsetsByTopicPartition.keySet())
            .map(logEndOffsetForTopicPartition -> offsetsByTopicPartition.entrySet()
                .stream()
                .map(entry -> {
                        TopicPartition tp = entry.getKey();
                        long offset = entry.getValue().offset();
                        long offsetLag = Optional
                            .ofNullable(logEndOffsetForTopicPartition.get(tp))
                            .map(endOffset -> endOffset - offset).orElse(-1L);
                        return new V1KafkaConsumerOffset(
                            tp.topic(),
                            tp.partition(),
                            offset,
                            offsetLag
                        );
                    }
                ).toList())
            .flatMapMany(Flux::fromIterable);
    }
}
