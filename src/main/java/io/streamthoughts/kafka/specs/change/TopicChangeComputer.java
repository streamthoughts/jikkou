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
package io.streamthoughts.kafka.specs.change;

import io.streamthoughts.kafka.specs.internal.KafkaTopics;
import io.streamthoughts.kafka.specs.model.V1TopicObject;
import io.streamthoughts.kafka.specs.resources.Named;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class TopicChangeComputer implements ChangeComputer<V1TopicObject, String, TopicChange, TopicChangeOptions> {

    /**
     * {@inheritDoc}
     */
    @Override
    public List<TopicChange> computeChanges(@NotNull final Iterable<V1TopicObject> actualStates,
                                            @NotNull final Iterable<V1TopicObject> expectedStates,
                                            @NotNull final TopicChangeOptions options) {

        final Map<String, V1TopicObject> actualTopicResourceMapByName = Named.keyByName(actualStates);

        final Map<String, TopicChange> changes = new HashMap<>();

        for (final V1TopicObject expectedTopic : expectedStates) {

            final V1TopicObject actualTopic = actualTopicResourceMapByName.get(expectedTopic.name());
            final TopicChange change = actualTopic == null ?
                    buildChangeForNewTopic(expectedTopic) :
                    buildChangeForExistingTopic(actualTopic, expectedTopic, options);

            changes.put(change.name(), change);
        }

        if (options.isDeleteTopicOrphans()) {
            changes.putAll(buildChangesForOrphanTopics(actualTopicResourceMapByName.values(), changes.keySet(), options));
        }

        return new ArrayList<>(changes.values());
    }

    private static @NotNull Map<String, TopicChange> buildChangesForOrphanTopics(
            @NotNull final Collection<V1TopicObject> topics,
            @NotNull final Set<String> changes,
            @NotNull final TopicChangeOptions options) {
        return topics
                .stream()
                .filter(it -> !changes.contains(it.name()))
                .filter(it -> !(KafkaTopics.isInternalTopics(it.name()) && options.isExcludeInternalTopics()))
                .map(topic -> {
                    TopicChange.Builder change = new TopicChange.Builder()
                            .setName(topic.name())
                            .setOperation(Change.OperationType.DELETE);
                    return change.build();
                })
                .collect(Collectors.toMap(TopicChange::name, it -> it));
    }

    private static @NotNull TopicChange buildChangeForExistingTopic(@NotNull final V1TopicObject actualState,
                                                                    @NotNull final V1TopicObject expectedState,
                                                                    @NotNull final TopicChangeOptions options) {

        var partitions = ValueChange.with(
                expectedState.partitionsOrDefault(),
                actualState.partitionsOrDefault()
        );

        var replication = ValueChange.with(
                expectedState.replicationFactorOrDefault(),
                actualState.replicationFactorOrDefault()
        );

        final ConfigEntryOptions configEntryOptions = new ConfigEntryOptions()
                .withDeleteConfigOrphans(options.isDeleteConfigOrphans());

        var configEntryChanges = new ConfigEntryChangeComputer()
                .computeChanges(actualState.configs(), expectedState.configs(), configEntryOptions);

        boolean hasChanged = configEntryChanges.stream()
                .anyMatch(configEntryChange -> configEntryChange.getOperation() != Change.OperationType.NONE);

        var configOpType = hasChanged ? Change.OperationType.UPDATE : Change.OperationType.NONE;
        var partitionOpType = partitions.getOperation();
        Change.OperationType op = List.of(partitionOpType, configOpType).contains(Change.OperationType.UPDATE) ?
                Change.OperationType.UPDATE :
                Change.OperationType.NONE;

        return new TopicChange.Builder()
                .setName(expectedState.name())
                .setPartitionsChange(partitions)
                .setReplicationFactorChange(replication)
                .setOperation(op)
                .setConfigs(configEntryChanges)
                .build();
    }

    private static @NotNull TopicChange buildChangeForNewTopic(@NotNull final V1TopicObject afterTopic) {

        final TopicChange.Builder builder = new TopicChange.Builder()
                .setName(afterTopic.name())
                .setPartitionsChange(ValueChange.withAfterValue(afterTopic.partitionsOrDefault()))
                .setReplicationFactorChange(ValueChange.withAfterValue(afterTopic.replicationFactorOrDefault()))
                .setOperation(Change.OperationType.ADD);

        afterTopic.configs().forEach(it -> builder.addConfigChange(
                new ConfigEntryChange(
                        it.name(),
                        ValueChange.withAfterValue(String.valueOf(it.value()))
                )
        ));
        return builder.build();
    }
}
