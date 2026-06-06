/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.jikkou.kafka.reconciler.service;

import static io.jikkou.kafka.KafkaLabelAndAnnotations.JIKKOU_IO_KAFKA_IS_SIMPLE_CONSUMER;

import io.jikkou.common.utils.AsyncUtils;
import io.jikkou.common.utils.Strings;
import io.jikkou.core.exceptions.JikkouRuntimeException;
import io.jikkou.core.models.ConfigValue;
import io.jikkou.core.models.Configs;
import io.jikkou.core.models.ObjectMeta;
import io.jikkou.kafka.collections.V1KafkaConsumerGroupList;
import io.jikkou.kafka.collections.V1KafkaShareGroupList;
import io.jikkou.kafka.internals.Futures;
import io.jikkou.kafka.models.*;
import io.jikkou.kafka.reconciler.service.KafkaOffsetSpec.ToEarliest;
import io.jikkou.kafka.reconciler.service.KafkaOffsetSpec.ToLatest;
import io.jikkou.kafka.reconciler.service.KafkaOffsetSpec.ToOffset;
import io.jikkou.kafka.reconciler.service.KafkaOffsetSpec.ToTimestamp;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.kafka.clients.admin.*;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.ConsumerGroupState;
import org.apache.kafka.common.GroupState;
import org.apache.kafka.common.KafkaFuture;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.config.ConfigResource;
import org.apache.kafka.common.errors.GroupIdNotFoundException;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

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
                // Resolve the topic selectors to the target partitions.
                CompletableFuture<List<TopicPartition>> future =
                    resolveTopicPartitions(parseSelectors(topics));
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
        // Resolve the topic selectors to the target partitions.
        CompletableFuture<List<TopicPartition>> future = resolveTopicPartitions(parseSelectors(topics));

        // Gets offsets for each resolved topic-partition.
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
     * Resolves a list of {@link TopicPartitionSelector} to concrete {@link TopicPartition}s.
     * Bare selectors (no partition) are expanded via a single {@code describeTopics} call;
     * fully-qualified selectors are used directly without an admin round-trip. Duplicates
     * are removed, preserving first-seen order for deterministic logging.
     */
    CompletableFuture<List<TopicPartition>> resolveTopicPartitions(@NotNull List<TopicPartitionSelector> selectors) {
        List<String> bareTopics = selectors.stream()
            .filter(TopicPartitionSelector::isAllPartitions)
            .map(TopicPartitionSelector::topic)
            .distinct()
            .toList();

        List<TopicPartition> explicit = selectors.stream()
            .filter(s -> !s.isAllPartitions())
            .map(s -> new TopicPartition(s.topic(), s.partition().getAsInt()))
            .toList();

        CompletableFuture<List<TopicPartition>> expanded = bareTopics.isEmpty()
            ? CompletableFuture.completedFuture(List.of())
            : listTopicPartitions(bareTopics);

        return expanded.thenApply(expandedPartitions -> {
            // LinkedHashSet preserves insertion order and removes duplicates.
            LinkedHashSet<TopicPartition> union = new LinkedHashSet<>(expandedPartitions);
            union.addAll(explicit);
            return List.copyOf(union);
        });
    }

    private static List<TopicPartitionSelector> parseSelectors(@NotNull List<String> rawTopics) {
        return rawTopics.stream().map(TopicPartitionSelector::parse).toList();
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
     * <p>
     * This method blocks the calling thread until the Kafka AdminClient completes.
     * Callers running on a non-blocking thread (e.g. a Netty event loop or a Reactor
     * scheduler) must offload, or use {@link #listConsumerGroupsAsync(List, boolean)}.
     *
     * @param groups          The consumer groups.
     * @param describeOffsets Specify whether offsets should be described.
     * @return The {@link V1KafkaConsumerGroupList}.
     */
    @NotNull
    public V1KafkaConsumerGroupList listConsumerGroups(@NotNull List<String> groups,
                                                       boolean describeOffsets) {
        return AsyncUtils.getValueOrThrowException(
            listConsumerGroupsAsync(groups, describeOffsets).toFuture(),
            cause -> cause instanceof JikkouRuntimeException jre
                ? jre
                : new JikkouRuntimeException(String.format(
                    "Failed to describe consumer groups. Cause %s: %s.",
                    cause.getClass().getSimpleName(), cause.getLocalizedMessage()), cause));
    }

    /**
     * Non-blocking variant of {@link #listConsumerGroups(List, boolean)}.
     * <p>
     * The returned {@link Mono} does not block any thread internally; the entire
     * pipeline is driven by the Kafka AdminClient's I/O callbacks. Safe to consume
     * from reactive contexts.
     *
     * @param groups          The consumer groups.
     * @param describeOffsets Specify whether offsets should be described.
     * @return A {@link Mono} that completes with the {@link V1KafkaConsumerGroupList}.
     */
    @NotNull
    public Mono<V1KafkaConsumerGroupList> listConsumerGroupsAsync(@NotNull List<String> groups,
                                                                  boolean describeOffsets) {
        return Mono.defer(() -> {
            final ListConsumerGroupOffsetsResult groupOffsetsResult = describeOffsets
                ? client.listConsumerGroupOffsets(groups.stream().collect(Collectors.toMap(
                    Function.identity(),
                    it -> new ListConsumerGroupOffsetsSpec())))
                : null;

            return Flux.fromStream(client.describeConsumerGroups(groups)
                    .describedGroups()
                    .values()
                    .stream()
                    .map(future -> Futures.toCompletableFuture(future).thenApply(this::mapToResource)))
                .flatMap(Mono::fromFuture)
                .flatMap(group -> {
                    if (groupOffsetsResult == null) {
                        return Mono.just(group);
                    }
                    String groupName = group.getMetadata().getName();
                    return Mono.fromFuture(Futures.toCompletableFuture(
                            groupOffsetsResult.partitionsToOffsetAndMetadata(groupName)))
                        .flatMap(partitions -> mapToResources(partitions).collectList()
                            .map(offsets -> group.withStatus(group.getStatus().withOffsets(offsets))));
                })
                .collectList()
                .map(items -> new V1KafkaConsumerGroupList.Builder().withItems(items).build());
        })
            .onErrorMap(e -> {
                LOG.error("Failed to describe consumer groups.", e);
                if (e instanceof JikkouRuntimeException) {
                    return e;
                }
                return new JikkouRuntimeException(String.format(
                    "Failed to describe consumer groups. Cause %s: %s.",
                    e.getClass().getSimpleName(), e.getLocalizedMessage()), e);
            });
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

    /**
     * Lists share groups for the specified states.
     *
     * @param inStates        Set of GroupState to filter; empty means all.
     * @param describeOffsets Specify whether SPSO offsets should be described.
     * @return The {@link V1KafkaShareGroupList}.
     */
    @NotNull
    public V1KafkaShareGroupList listShareGroups(@NotNull Set<GroupState> inStates,
                                                 boolean describeOffsets) {
        ListGroupsOptions options = ListGroupsOptions.forShareGroups();
        if (!inStates.isEmpty()) {
            options = options.inGroupStates(inStates);
        }
        final List<String> groupIds = AsyncUtils.getValueOrThrowException(
                Futures.toCompletableFuture(client.listGroups(options).all()),
                e -> new JikkouRuntimeException(String.format(
                    "Failed to list share groups. Cause %s: %s.",
                    e.getClass().getSimpleName(), e.getLocalizedMessage())))
            .stream()
            .map(GroupListing::groupId)
            .toList();
        return listShareGroups(groupIds, describeOffsets);
    }

    /**
     * Lists the specified share groups.
     *
     * @param groups          The share group ids.
     * @param describeOffsets Specify whether SPSO offsets should be described.
     * @return The {@link V1KafkaShareGroupList}.
     */
    @NotNull
    public V1KafkaShareGroupList listShareGroups(@NotNull List<String> groups,
                                                 boolean describeOffsets) {
        if (groups.isEmpty()) {
            return new V1KafkaShareGroupList.Builder().withItems(List.of()).build();
        }
        Map<String, ShareGroupDescription> described = AsyncUtils.getValueOrThrowException(
                Futures.toCompletableFuture(client.describeShareGroups(groups).all()),
                e -> new JikkouRuntimeException(String.format(
                    "Failed to describe share groups. Cause %s: %s.",
                    e.getClass().getSimpleName(), e.getLocalizedMessage())));

        Map<String, Configs> configsByGroup = describeGroupConfigs(groups);

        List<V1KafkaShareGroup> items = described.values().stream()
            .map(this::mapToResource)
            .map(group -> {
                Configs configs = configsByGroup.getOrDefault(group.getMetadata().getName(), Configs.empty());
                return group.withSpec(V1KafkaShareGroupSpec.builder().withConfigs(configs).build());
            })
            .map(group -> describeOffsets ? withShareGroupOffsets(group) : group)
            .toList();
        return new V1KafkaShareGroupList.Builder().withItems(items).build();
    }

    private V1KafkaShareGroup withShareGroupOffsets(@NotNull V1KafkaShareGroup group) {
        String groupId = group.getMetadata().getName();
        ListShareGroupOffsetsSpec spec = new ListShareGroupOffsetsSpec();
        Map<TopicPartition, SharePartitionOffsetInfo> offsets = AsyncUtils.getValueOrThrowException(
            Futures.toCompletableFuture(
                client.listShareGroupOffsets(Map.of(groupId, spec)).partitionsToOffsetInfo(groupId)),
            e -> new JikkouRuntimeException(String.format(
                "Failed to list share group offsets for '%s'. Cause %s: %s.",
                groupId, e.getClass().getSimpleName(), e.getLocalizedMessage())));
        List<V1KafkaConsumerOffset> mapped = offsets.entrySet().stream()
            .map(e -> new V1KafkaConsumerOffset(
                e.getKey().topic(), e.getKey().partition(),
                e.getValue().startOffset(), e.getValue().lag().orElse(-1L)))
            .toList();
        return group.withStatus(group.getStatus().withOffsets(mapped));
    }

    /**
     * Maps a {@link ShareGroupDescription} to a {@link V1KafkaShareGroup} (status only).
     *
     * @param description the share group description.
     * @return the {@link V1KafkaShareGroup}.
     */
    public V1KafkaShareGroup mapToResource(@NotNull ShareGroupDescription description) {
        List<V1KafkaShareGroupMember> members = description.members().stream()
            .map(this::mapToShareMember)
            .toList();

        V1KafkaShareGroupStatus status = V1KafkaShareGroupStatus.builder()
            .withState(description.groupState().name())
            .withCoordinator(V1KafkaNode.builder()
                .withId(description.coordinator().idString())
                .withHost(description.coordinator().host())
                .withPort(description.coordinator().port())
                .withRack(description.coordinator().rack())
                .build())
            .withMembers(members)
            .build();

        return V1KafkaShareGroup.builder()
            .withMetadata(ObjectMeta.builder().withName(description.groupId()).build())
            .withStatus(status)
            .build();
    }

    private V1KafkaShareGroupMember mapToShareMember(@NotNull ShareMemberDescription member) {
        List<String> assignments = member.assignment().topicPartitions().stream()
            .map(TopicPartition::toString)
            .toList();
        return V1KafkaShareGroupMember.builder()
            .withMemberId(member.consumerId())
            .withClientId(member.clientId())
            .withHost(member.host())
            .withRackId(member.rackId().orElse(null))
            .withAssignments(assignments)
            .build();
    }

    /**
     * Reads the user-set dynamic configs for the given group ids (GROUP resource type),
     * filtered to {@code DYNAMIC_GROUP_CONFIG} so broker defaults are not treated as drift.
     *
     * @param groupIds the group ids.
     * @return a map of group id to its dynamic {@link Configs}.
     */
    @NotNull
    public Map<String, Configs> describeGroupConfigs(@NotNull List<String> groupIds) {
        if (groupIds.isEmpty()) {
            return Map.of();
        }
        List<ConfigResource> resources = groupIds.stream()
            .map(id -> new ConfigResource(ConfigResource.Type.GROUP, id))
            .toList();

        // Resolve each resource individually so that a group with no dynamic config (which some
        // brokers surface as GroupIdNotFoundException) is treated as having empty configs rather
        // than failing the whole describe.
        Map<ConfigResource, KafkaFuture<Config>> futures = client.describeConfigs(resources).values();
        Map<String, Configs> result = new HashMap<>();
        for (Map.Entry<ConfigResource, KafkaFuture<Config>> entry : futures.entrySet()) {
            String groupId = entry.getKey().name();
            try {
                Config config = entry.getValue().get();
                Set<ConfigValue> values = config.entries().stream()
                    .filter(e -> e.source() == ConfigEntry.ConfigSource.DYNAMIC_GROUP_CONFIG)
                    .map(e -> new ConfigValue(e.name(), e.value()))
                    .collect(Collectors.toSet());
                result.put(groupId, new Configs(values));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new JikkouRuntimeException(String.format(
                    "Interrupted while describing configs for group '%s'.", groupId), e);
            } catch (ExecutionException e) {
                if (e.getCause() instanceof GroupIdNotFoundException) {
                    result.put(groupId, Configs.empty());
                } else {
                    throw new JikkouRuntimeException(String.format(
                        "Failed to describe configs for group '%s'. Cause %s: %s.",
                        groupId, e.getCause().getClass().getSimpleName(), e.getCause().getLocalizedMessage()),
                        e.getCause());
                }
            }
        }
        return result;
    }

    /**
     * Resets share-group offsets (SPSO) for the specified group and topics.
     *
     * @param groupId    The share group id.
     * @param topics     The topics (each "topic" or "topic:partition").
     * @param offsetSpec The offset specification (ToEarliest, ToLatest, ToOffset, ToTimestamp), or {@code null} to delete offsets.
     * @param dryRun     Whether to run in dry-run.
     * @return The refreshed {@link V1KafkaShareGroup}.
     */
    public V1KafkaShareGroup resetShareGroupOffsets(@NotNull String groupId,
                                                    @NotNull List<String> topics,
                                                    KafkaOffsetSpec offsetSpec,
                                                    boolean dryRun) {
        if (Strings.isNullOrEmpty(groupId)) {
            throw new IllegalArgumentException("groupId cannot be null");
        }

        // delete offsets when no spec provided
        if (offsetSpec == null) {
            if (!dryRun) {
                Set<String> topicNames = topics.stream()
                    .map(TopicPartitionSelector::parse)
                    .map(TopicPartitionSelector::topic)
                    .collect(Collectors.toSet());
                AsyncUtils.getValueOrThrowException(
                    Futures.toCompletableFuture(client.deleteShareGroupOffsets(groupId, topicNames).all()),
                    JikkouRuntimeException::new);
            }
            return listShareGroups(List.of(groupId), true).first();
        }

        Map<TopicPartition, Long> offsets;
        if (offsetSpec instanceof ToOffset to) {
            offsets = AsyncUtils.getValueOrThrowException(
                    resolveTopicPartitions(parseSelectors(topics)), JikkouRuntimeException::new)
                .stream()
                .collect(Collectors.toMap(Function.identity(), tp -> to.offset()));
        } else {
            OffsetSpec spec = switch (offsetSpec) {
                case ToEarliest ignored -> OffsetSpec.earliest();
                case ToLatest ignored -> OffsetSpec.latest();
                case ToTimestamp t -> OffsetSpec.forTimestamp(t.timestamp());
                default -> throw new IllegalArgumentException("Unsupported offset specification: " + offsetSpec);
            };
            offsets = AsyncUtils.getValueOrThrowException(listOffsets(topics, spec), JikkouRuntimeException::new)
                .entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().offset()));
        }

        if (!dryRun) {
            AsyncUtils.getValueOrThrowException(
                Futures.toCompletableFuture(client.alterShareGroupOffsets(groupId, offsets).all()),
                JikkouRuntimeException::new);
        }
        return listShareGroups(List.of(groupId), true).first();
    }
}
