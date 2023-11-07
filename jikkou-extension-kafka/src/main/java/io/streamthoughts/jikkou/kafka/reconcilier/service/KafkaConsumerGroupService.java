/*
 * Copyright 2023 The original authors
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
package io.streamthoughts.jikkou.kafka.reconcilier.service;

import static io.streamthoughts.jikkou.kafka.MetadataAnnotations.JIKKOU_IO_KAFKA_IS_SIMPLE_CONSUMER;

import io.streamthoughts.jikkou.common.utils.AsyncUtils;
import io.streamthoughts.jikkou.common.utils.Pair;
import io.streamthoughts.jikkou.core.exceptions.JikkouRuntimeException;
import io.streamthoughts.jikkou.core.models.ObjectMeta;
import io.streamthoughts.jikkou.kafka.collections.V1KafkaConsumerGroupList;
import io.streamthoughts.jikkou.kafka.internals.Futures;
import io.streamthoughts.jikkou.kafka.models.V1KafkaConsumerGroup;
import io.streamthoughts.jikkou.kafka.models.V1KafkaConsumerGroupMember;
import io.streamthoughts.jikkou.kafka.models.V1KafkaConsumerGroupSpec;
import io.streamthoughts.jikkou.kafka.models.V1KafkaConsumerOffset;
import io.streamthoughts.jikkou.kafka.models.V1KafkaNode;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.ConsumerGroupDescription;
import org.apache.kafka.clients.admin.ConsumerGroupListing;
import org.apache.kafka.clients.admin.DescribeConsumerGroupsResult;
import org.apache.kafka.clients.admin.ListConsumerGroupOffsetsResult;
import org.apache.kafka.clients.admin.ListConsumerGroupOffsetsSpec;
import org.apache.kafka.clients.admin.ListConsumerGroupsOptions;
import org.apache.kafka.clients.admin.ListConsumerGroupsResult;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.ConsumerGroupState;
import org.apache.kafka.common.KafkaFuture;
import org.apache.kafka.common.TopicPartition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service to manage Kafka Consumer Groups.
 */
public final class KafkaConsumerGroupService {

    private static final Logger LOG = LoggerFactory.getLogger(KafkaConsumerGroupService.class);

    private final AdminClient client;

    /**
     * Creates a new {@link KafkaConsumerGroupService} instance.
     *
     * @param client The AdminClient.
     */
    public KafkaConsumerGroupService(@NotNull AdminClient client) {
        this.client = Objects.requireNonNull(client, "client cannot be null");
    }

    /**
     * Lists all consumer groups for the specified states.
     *
     * @param inStates        Set of ConsumerGroupState to listing group.
     * @param describeOffsets Specify whether offsets should be described.
     * @return The {@link V1KafkaConsumerGroupList}.
     */
    public V1KafkaConsumerGroupList listConsumerGroups(@NotNull Set<ConsumerGroupState> inStates,
                                                       boolean describeOffsets) {
        final List<String> groupIds = getConsumerGroupIds(inStates);
        return getV1KafkaConsumerGroups(describeOffsets, groupIds);
    }

    @NotNull
    private V1KafkaConsumerGroupList getV1KafkaConsumerGroups(boolean describeOffsets,
                                                              @NotNull List<String> groupIds) {
        // Describe Consumer Groups
        DescribeConsumerGroupsResult consumerGroupResult = client.describeConsumerGroups(groupIds);
        List<Pair<String, CompletableFuture<ConsumerGroupDescription>>> descriptionsByGroup = consumerGroupResult
                .describedGroups()
                .entrySet()
                .stream()
                .map(Pair::of)
                .map(p -> p.mapRight(Futures::toCompletableFuture))
                .toList();

        // List Consumer Group Offsets
        final ListConsumerGroupOffsetsResult groupOffsetsResult;
        if (describeOffsets) {
            Map<String, ListConsumerGroupOffsetsSpec> groupSpecs = groupIds
                    .stream()
                    .collect(Collectors.toMap(Function.identity(), it -> new ListConsumerGroupOffsetsSpec()));
            groupOffsetsResult = client.listConsumerGroupOffsets(groupSpecs);
        } else {
            groupOffsetsResult = null;
        }

        // Build Resources
        List<CompletableFuture<V1KafkaConsumerGroup>> futures = descriptionsByGroup
                .stream()
                .map(future -> mergeResults(future._1(), future._2(), groupOffsetsResult))
                .toList();

        CompletableFuture<List<V1KafkaConsumerGroup>> all = AsyncUtils.waitForAll(futures);
        List<V1KafkaConsumerGroup> items = AsyncUtils.getValueOrThrowException(all, e -> {
            LOG.error("Failed to describe consumer groups.", e);
            return new JikkouRuntimeException(String.format(
                    "Failed to describe consumer groups. Cause %s: %s.", e.getClass().getSimpleName(), e.getLocalizedMessage())
            );
        });
        return new V1KafkaConsumerGroupList(items);
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

    private CompletableFuture<V1KafkaConsumerGroup> mergeResults(@NotNull String groupId,
                                                                 @NotNull CompletableFuture<ConsumerGroupDescription> future,
                                                                 @Nullable ListConsumerGroupOffsetsResult groupOffsetsResult) {
        if (groupOffsetsResult == null) {
            return future.thenApply(this::mapToResource);
        }

        KafkaFuture<Map<TopicPartition, OffsetAndMetadata>> offsetsFuture = groupOffsetsResult
                .partitionsToOffsetAndMetadata(groupId);

        return future.thenCombine(Futures.toCompletableFuture(offsetsFuture), this::mapToResource);
    }

    public V1KafkaConsumerGroup mapToResource(@NotNull ConsumerGroupDescription description) {
        return mapToResource(description, null);
    }

    public V1KafkaConsumerGroup mapToResource(@NotNull ConsumerGroupDescription description,
                                              @Nullable Map<TopicPartition, OffsetAndMetadata> offsetsByTopicPartition) {

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

                            // offsets
                            if (offsetsByTopicPartition != null) {
                                List<V1KafkaConsumerOffset> offsets = offsetsByTopicPartition.entrySet()
                                        .stream()
                                        .map(entry -> new V1KafkaConsumerOffset(
                                                        entry.getKey().topic(),
                                                        entry.getKey().partition(),
                                                        entry.getValue().offset()
                                                )
                                        ).toList();
                                builder = builder.withOffsets(offsets);
                            }
                            return builder.build();
                        }
                )
                .toList();

        return V1KafkaConsumerGroup.builder()
                .withMetadata(ObjectMeta
                        .builder()
                        .withName(description.groupId())
                        .withLabel(JIKKOU_IO_KAFKA_IS_SIMPLE_CONSUMER, description.isSimpleConsumerGroup())
                        .build()
                )
                .withSpec(V1KafkaConsumerGroupSpec
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
                        .withMembers(members)
                        .build()
                )
                .build();
    }
}
