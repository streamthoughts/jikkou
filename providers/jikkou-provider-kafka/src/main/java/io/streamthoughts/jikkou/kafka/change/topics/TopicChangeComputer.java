/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.kafka.change.topics;

import static io.streamthoughts.jikkou.core.reconciler.Operation.DELETE;
import static io.streamthoughts.jikkou.kafka.change.topics.TopicChange.CONFIG_PREFIX;

import io.streamthoughts.jikkou.core.models.ConfigValue;
import io.streamthoughts.jikkou.core.models.change.GenericResourceChange;
import io.streamthoughts.jikkou.core.models.change.ResourceChange;
import io.streamthoughts.jikkou.core.models.change.ResourceChangeSpec;
import io.streamthoughts.jikkou.core.models.change.StateChange;
import io.streamthoughts.jikkou.core.reconciler.Operation;
import io.streamthoughts.jikkou.core.reconciler.change.ChangeComputer;
import io.streamthoughts.jikkou.core.reconciler.change.ResourceChangeComputer;
import io.streamthoughts.jikkou.core.reconciler.change.ResourceChangeFactory;
import io.streamthoughts.jikkou.kafka.internals.KafkaTopics;
import io.streamthoughts.jikkou.kafka.models.V1KafkaTopic;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class TopicChangeComputer extends ResourceChangeComputer<String, V1KafkaTopic, ResourceChange> {

    /**
     * Creates a new {@link TopicChangeComputer} instance.
     */
    public TopicChangeComputer() {
        this(true);
    }

    /**
     * Creates a new {@link TopicChangeComputer} instance.
     *
     * @param isConfigDeletionEnabled {@code true} to delete orphaned config entries.
     */
    public TopicChangeComputer(boolean isConfigDeletionEnabled) {
        super(object -> object.getMetadata().getName(), new TopicChangeFactory(isConfigDeletionEnabled));
    }

    public static final class TopicChangeFactory extends ResourceChangeFactory<String, V1KafkaTopic, ResourceChange> {

        private final ChangeComputer<ConfigValue, StateChange> configEntryChangeComputer;

        public TopicChangeFactory(boolean isConfigDeletionEnabled) {
            this.configEntryChangeComputer = getChangeComputerForConfig(isConfigDeletionEnabled);
        }

        @Override
        public ResourceChange createChangeForCreate(String key, V1KafkaTopic after) {
            List<StateChange> changes = new ArrayList<>();
            changes.add(StateChange.create(TopicChange.PARTITIONS, getPartitionsOrDefault(after)));
            changes.add(StateChange.create(TopicChange.REPLICAS, getReplicationFactorOrDefault(after)));
            changes.addAll(getConfigChanges(null, after));
            return buildResourceChange(after, Operation.CREATE, changes);
        }

        @Override
        public ResourceChange createChangeForDelete(String key, V1KafkaTopic before) {
            List<StateChange> changes = new ArrayList<>();
            changes.add(StateChange.delete(TopicChange.PARTITIONS, getPartitionsOrDefault(before)));
            changes.add(StateChange.delete(TopicChange.REPLICAS, getReplicationFactorOrDefault(before)));
            changes.addAll(getConfigChanges(before, null));
            return buildResourceChange(before, DELETE, changes);
        }

        @Override
        public ResourceChange createChangeForUpdate(String key, V1KafkaTopic before, V1KafkaTopic after) {
            StateChange partitions;
            // Do not compute change when partition is equals to default.
            if (getPartitionsOrDefault(after) == KafkaTopics.NO_NUM_PARTITIONS) {
                partitions = StateChange.none(
                        TopicChange.PARTITIONS,
                        KafkaTopics.NO_NUM_PARTITIONS);
            } else {
                partitions = StateChange.with(
                        TopicChange.PARTITIONS,
                        getPartitionsOrDefault(before),
                        getPartitionsOrDefault(after)
                );
            }

            StateChange replicas;
            // Do not compute change when replication-factor is equals to default.
            if (getReplicationFactorOrDefault(after) == KafkaTopics.NO_REPLICATION_FACTOR) {
                replicas = StateChange.none(
                        TopicChange.REPLICAS,
                        KafkaTopics.NO_REPLICATION_FACTOR);
            } else {
                replicas = StateChange.with(
                        TopicChange.REPLICAS,
                        getReplicationFactorOrDefault(before),
                        getReplicationFactorOrDefault(after)
                );
            }

            List<StateChange> configChanges = getConfigChanges(before, after);

            boolean hasChanged = configChanges.stream()
                    .anyMatch(change -> change.getOp() != Operation.NONE);

            var configOpType = hasChanged ? Operation.UPDATE : Operation.NONE;
            var partitionOpType = partitions.getOp();

            Operation op = List.of(partitionOpType, configOpType).contains(Operation.UPDATE) ?
                    Operation.UPDATE :
                    Operation.NONE;

            List<StateChange> valueChanges = new ArrayList<>();
            valueChanges.add(partitions);
            valueChanges.add(replicas);
            valueChanges.addAll(configChanges);

            return buildResourceChange(before, op, valueChanges);
        }

        @NotNull
        private List<StateChange> getConfigChanges(@Nullable V1KafkaTopic before,
                                                   @Nullable V1KafkaTopic after) {
            List<StateChange> changes = configEntryChangeComputer.computeChanges(
                    Optional.ofNullable(before).map(o -> o.getSpec().getConfigs()).orElse(null),
                    Optional.ofNullable(after).map(o -> o.getSpec().getConfigs()).orElse(null)
            );
            return changes
                    .stream()
                    .map(change -> StateChange.builder()
                            .withName(CONFIG_PREFIX + change.getName())
                            .withOp(change.getOp())
                            .withBefore(change.getBefore())
                            .withAfter(change.getAfter())
                            .build()

                    )
                    .collect(Collectors.toList());
        }

        private static ResourceChange buildResourceChange(V1KafkaTopic resource,
                                                          Operation type,
                                                          List<StateChange> changes) {
            return GenericResourceChange.builder(V1KafkaTopic.class)
                    .withMetadata(resource.getMetadata())
                    .withSpec(ResourceChangeSpec
                            .builder()
                            .withOperation(type)
                            .withChanges(changes)
                            .build()
                    )
                    .build();
        }

        /**
         * @return the partition of the topic or default to ({@link KafkaTopics#NO_NUM_PARTITIONS})
         */
        private int getPartitionsOrDefault(V1KafkaTopic resource) {
            return Optional
                    .ofNullable(resource.getSpec())
                    .flatMap(spec -> Optional.ofNullable(spec.getPartitions()))
                    .orElse(KafkaTopics.NO_NUM_PARTITIONS);
        }

        /**
         * @return the replication of the topic or default to ({@link KafkaTopics#NO_REPLICATION_FACTOR})
         */
        private short getReplicationFactorOrDefault(V1KafkaTopic resource) {
            return Optional
                    .ofNullable(resource.getSpec())
                    .flatMap(spec -> Optional.ofNullable(spec.getReplicas()))
                    .orElse(KafkaTopics.NO_REPLICATION_FACTOR);
        }

        private static ChangeComputer<ConfigValue, StateChange> getChangeComputerForConfig(boolean deleteOrphans) {
            return ChangeComputer
                    .<String, ConfigValue, StateChange>builder()
                    .withDeleteOrphans(deleteOrphans)
                    .withKeyMapper(ConfigValue::getName)
                    .withChangeFactory((key, before, after) -> {
                        Object beforeValue = Optional.ofNullable(before)
                                .map(ConfigValue::value)
                                .orElse(null);

                        Object afterValue = Optional.ofNullable(after)
                                .map(ConfigValue::value)
                                .orElse(null);
                        StateChange change = StateChange.with(key, beforeValue, afterValue);
                        return change.getOp() == DELETE && !before.isDeletable() ? Optional.empty() : Optional.of(change);
                    })
                    .build();
        }
    }
}
