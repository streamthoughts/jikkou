/*
 * Copyright 2021 StreamThoughts.
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
package io.streamthoughts.jikkou.kafka.control.change;

import static io.streamthoughts.jikkou.api.control.ValueChange.withAfterValue;

import io.streamthoughts.jikkou.api.control.ChangeComputer;
import io.streamthoughts.jikkou.api.control.ChangeType;
import io.streamthoughts.jikkou.api.control.ConfigEntryChange;
import io.streamthoughts.jikkou.api.control.ConfigEntryChangeComputer;
import io.streamthoughts.jikkou.api.control.ValueChange;
import io.streamthoughts.jikkou.api.model.Nameable;
import io.streamthoughts.jikkou.kafka.adapters.KafkaTopicAdapter;
import io.streamthoughts.jikkou.kafka.internals.KafkaTopics;
import io.streamthoughts.jikkou.kafka.models.V1KafkaTopic;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.StreamSupport;
import org.jetbrains.annotations.NotNull;

public class TopicChangeComputer implements ChangeComputer<V1KafkaTopic, TopicChange> {

    private boolean isConfigDeletionEnabled;

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
        this.isConfigDeletionEnabled = isConfigDeletionEnabled;
    }

    /**
     * Sets whether orphaned config entries should be deleted or ignored.
     *
     * @param isConfigDeletionEnabled {@code true} to enable orphans deletion.
     */
    public void isConfigDeletionEnabled(boolean isConfigDeletionEnabled) {
        this.isConfigDeletionEnabled = isConfigDeletionEnabled;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<TopicChange> computeChanges(@NotNull final Iterable<V1KafkaTopic> actualStates,
                                            @NotNull final Iterable<V1KafkaTopic> expectedStates) {

        final Map<String, KafkaTopicAdapter> actualTopicResourceMapByName = Nameable.keyByName(
                StreamSupport.stream(actualStates.spliterator(), false)
                        .map(KafkaTopicAdapter::new)
                        .toList());

        final Map<String, TopicChange> changes = new HashMap<>();

        for (final V1KafkaTopic it : expectedStates) {

            final KafkaTopicAdapter expect = new KafkaTopicAdapter(it);
            final KafkaTopicAdapter actual = actualTopicResourceMapByName.get(expect.getName());

            TopicChange change;
            if (expect.isDelete()) {
                change = actual != null ? buildChangeForTopicToDelete(actual) : null;
            } else if (actual != null) {
                change = buildChangeForExistingTopic(actual, expect);
            } else {
                change = buildChangeForTopicToCreate(expect);
            }

            if (change != null) {
                changes.put(change.getName(), change);
            }
        }
        return new ArrayList<>(changes.values());
    }

    private @NotNull TopicChange buildChangeForTopicToDelete(@NotNull final KafkaTopicAdapter topic) {
        return TopicChange.builder()
                .withName(topic.getName())
                .withOperation(ChangeType.DELETE)
                .build();
    }

    private @NotNull TopicChange buildChangeForExistingTopic(@NotNull final KafkaTopicAdapter actual,
                                                             @NotNull final KafkaTopicAdapter expect) {

        ValueChange<Integer> partitions;
        // Do not compute change when described partition is equals to default.
        if (expect.getPartitionsOrDefault() == KafkaTopics.NO_NUM_PARTITIONS) {
            partitions = ValueChange.none(actual.getPartitionsOrDefault());
        } else {
            partitions = ValueChange.with(
                    expect.getPartitionsOrDefault(),
                    actual.getPartitionsOrDefault()
            );
        }

        ValueChange<Short> replication;
        // Do not compute change when describe replication-factor is equals to default.
        if (expect.getReplicationFactorOrDefault() == KafkaTopics.NO_REPLICATION_FACTOR) {
            replication = ValueChange.none(actual.getReplicationFactorOrDefault());
        } else {
            replication = ValueChange.with(
                    expect.getReplicationFactorOrDefault(),
                    actual.getReplicationFactorOrDefault()
            );
        }

        var configEntryChanges = new ConfigEntryChangeComputer(isConfigDeletionEnabled)
                .computeChanges(actual.getConfigs(), expect.getConfigs());

        boolean hasChanged = configEntryChanges.stream()
                .anyMatch(configEntryChange -> configEntryChange.getChangeType() != ChangeType.NONE);

        var configOpType = hasChanged ? ChangeType.UPDATE : ChangeType.NONE;
        var partitionOpType = partitions.type();
        ChangeType op = List.of(partitionOpType, configOpType).contains(ChangeType.UPDATE) ?
                ChangeType.UPDATE :
                ChangeType.NONE;

        return TopicChange.builder()
                .withName(expect.getName())
                .withPartitions(partitions)
                .withReplicas(replication)
                .withOperation(op)
                .withConfigs(configEntryChanges)
                .build();
    }

    private @NotNull TopicChange buildChangeForTopicToCreate(@NotNull final KafkaTopicAdapter topic) {

        var configEntryChanges = StreamSupport
                .stream(topic.getConfigs().spliterator(), false)
                .map(it -> new ConfigEntryChange(it.getName(), withAfterValue(String.valueOf(it.value()))))
                .toList();

        return TopicChange.builder()
                .withName(topic.getName())
                .withPartitions(withAfterValue(topic.getPartitionsOrDefault()))
                .withReplicas(withAfterValue(topic.getReplicationFactorOrDefault()))
                .withOperation(ChangeType.ADD)
                .withConfigs(configEntryChanges)
                .build();
    }
}
