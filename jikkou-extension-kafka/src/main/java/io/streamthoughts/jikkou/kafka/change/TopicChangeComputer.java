/*
 * Copyright 2021 The original authors
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
package io.streamthoughts.jikkou.kafka.change;

import static io.streamthoughts.jikkou.core.change.ValueChange.withAfterValue;

import io.streamthoughts.jikkou.core.change.ChangeType;
import io.streamthoughts.jikkou.core.change.ConfigEntryChange;
import io.streamthoughts.jikkou.core.change.ConfigEntryChangeComputer;
import io.streamthoughts.jikkou.core.change.ResourceChangeComputer;
import io.streamthoughts.jikkou.core.change.ValueChange;
import io.streamthoughts.jikkou.core.models.Configs;
import io.streamthoughts.jikkou.core.models.HasMetadataChange;
import io.streamthoughts.jikkou.kafka.internals.KafkaTopics;
import io.streamthoughts.jikkou.kafka.models.V1KafkaTopic;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class TopicChangeComputer extends ResourceChangeComputer<V1KafkaTopic, V1KafkaTopic, TopicChange> {
    private final boolean isConfigDeletionEnabled;

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
        super(metadataNameKeyMapper(), identityChangeValueMapper(), false);
        this.isConfigDeletionEnabled = isConfigDeletionEnabled;
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public List<TopicChange> buildChangeForDeleting(V1KafkaTopic before) {
        return List.of(TopicChange.builder()
                .withName(before.getMetadata().getName())
                .withOperation(ChangeType.DELETE)
                .build());
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public List<TopicChange> buildChangeForNone(V1KafkaTopic before, V1KafkaTopic after) {
        return buildChangeForUpdating(before, after);
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public List<TopicChange> buildChangeForUpdating(V1KafkaTopic before, V1KafkaTopic after) {
        ValueChange<Integer> partitions;
        // Do not compute change when described partition is equals to default.
        if (getPartitionsOrDefault(after) == KafkaTopics.NO_NUM_PARTITIONS) {
            partitions = ValueChange.none(getPartitionsOrDefault(before));
        } else {
            partitions = ValueChange.with(
                    getPartitionsOrDefault(before),
                    getPartitionsOrDefault(after)
            );
        }

        ValueChange<Short> replication;
        // Do not compute change when describe replication-factor is equals to default.
        if (getReplicationFactorOrDefault(after) == KafkaTopics.NO_REPLICATION_FACTOR) {
            replication = ValueChange.none(getReplicationFactorOrDefault(before));
        } else {
            replication = ValueChange.with(
                    getReplicationFactorOrDefault(before),
                    getReplicationFactorOrDefault(after)
            );
        }

        var configEntryChanges = new ConfigEntryChangeComputer(isConfigDeletionEnabled)
                .computeChanges(getConfigs(before), getConfigs(after));

        boolean hasChanged = configEntryChanges.stream()
                .anyMatch(configEntryChange -> configEntryChange.getChange().operation() != ChangeType.NONE);

        var configOpType = hasChanged ? ChangeType.UPDATE : ChangeType.NONE;
        var partitionOpType = partitions.operation();
        ChangeType op = List.of(partitionOpType, configOpType).contains(ChangeType.UPDATE) ?
                ChangeType.UPDATE :
                ChangeType.NONE;

        return List.of(
                TopicChange.builder()
                        .withName(after.getMetadata().getName())
                        .withPartitions(partitions)
                        .withReplicas(replication)
                        .withOperation(op)
                        .withConfigs(configEntryChanges.stream().map(HasMetadataChange::getChange).collect(Collectors.toList()))
                        .build()
        );
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public List<TopicChange> buildChangeForCreating(V1KafkaTopic after) {

        Configs configs = getConfigs(after);
        var configEntryChanges = StreamSupport
                .stream(configs.spliterator(), false)
                .map(it -> new ConfigEntryChange(it.getName(), withAfterValue(String.valueOf(it.value()))))
                .toList();

        return List.of(
                TopicChange.builder()
                        .withName(after.getMetadata().getName())
                        .withPartitions(withAfterValue(getPartitionsOrDefault(after)))
                        .withReplicas(withAfterValue(getReplicationFactorOrDefault(after)))
                        .withOperation(ChangeType.ADD)
                        .withConfigs(configEntryChanges)
                        .build()
        );
    }

    /**
     * @return the {@link Configs}.
     */
    private Configs getConfigs(V1KafkaTopic resource) {
        return Optional
                .ofNullable(resource.getSpec())
                .flatMap(spec -> Optional.ofNullable(spec.getConfigs()))
                .orElse(Configs.empty());
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
}
