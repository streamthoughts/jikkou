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

import io.streamthoughts.kafka.specs.operation.TopicOperation;
import io.streamthoughts.kafka.specs.resources.ConfigValue;
import io.streamthoughts.kafka.specs.resources.Named;
import io.streamthoughts.kafka.specs.resources.TopicResource;
import org.apache.kafka.clients.admin.ConfigEntry;
import org.apache.kafka.common.KafkaFuture;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class TopicChanges implements Iterable<TopicChange> {

    private final Map<String, TopicChange> changes;

    public static TopicChanges computeChanges(@NotNull final Iterable<TopicResource> beforeTopicStates,
                                              @NotNull final Iterable<TopicResource> afterTopicStates) {

        final Map<String, TopicResource> beforeTopicResourceMapByName = Named.keyByName(beforeTopicStates);

        final Map<String, TopicChange> changes = new HashMap<>();

        for (final TopicResource afterTopic : afterTopicStates) {

            final TopicResource beforeTopic = beforeTopicResourceMapByName.get(afterTopic.name());
            final TopicChange change = beforeTopic == null ?
                    buildChangeForNewTopic(afterTopic) :
                    buildChangeForExistingTopic(afterTopic, beforeTopic);

            changes.put(change.name(), change);
        }

        Map<String, TopicChange> changeForDeletedTopics = buildChangesForOrphanTopics(
                beforeTopicResourceMapByName.values(),
                changes.keySet()
        );

        changes.putAll(changeForDeletedTopics);

        return new TopicChanges(changes);
    }

    private static @NotNull Map<String, TopicChange> buildChangesForOrphanTopics(
            @NotNull final Collection<TopicResource> topics,
            @NotNull final Set<String> changes)
    {
        return topics
            .stream()
            .filter(it ->!changes.contains(it.name()))
            .map(topic -> {
                TopicChange.Builder change = new TopicChange.Builder()
                    .setName(topic.name())
                    .setOperation(Change.OperationType.DELETE);
                return change.build();
            })
            .collect(Collectors.toMap(TopicChange::name, it -> it));
    }

    private static @NotNull TopicChange buildChangeForExistingTopic(@NotNull final TopicResource afterTopic,
                                                                    @NotNull final TopicResource beforeTopic) {

        final Map<String, ConfigValue> beforeTopicConfigsByName = Named.keyByName(beforeTopic.configs());
        final Map<String, ConfigEntryChange> afterTopicConfigsByName = new HashMap<>();

        Change.OperationType topicOp = Change.OperationType.NONE;

        for (ConfigValue afterConfigValue : afterTopic.configs()) {
            final String configEntryName = afterConfigValue.name();

            final ConfigValue beforeConfigValue = beforeTopicConfigsByName.getOrDefault(
                    configEntryName,
                    new ConfigValue(configEntryName, null)
            );

            var change = ValueChange.with(
                    String.valueOf(afterConfigValue.value()),
                    String.valueOf(beforeConfigValue.value())
            );

            if (change.getOperation() != Change.OperationType.NONE) {
                topicOp = Change.OperationType.UPDATE;
            }

            afterTopicConfigsByName.put(configEntryName, new ConfigEntryChange(configEntryName, change));
        }

        // Iterate on all configs apply on the topic for
        // looking for DYNAMIC_TOPIC_CONFIGS that may be orphan.
        List<ConfigEntryChange> orphanChanges = beforeTopicConfigsByName.values()
                .stream()
                .filter(it -> it.unwrap().source() == ConfigEntry.ConfigSource.DYNAMIC_TOPIC_CONFIG)
                .filter(it -> !afterTopicConfigsByName.containsKey(it.name()))
                .map(it -> new ConfigEntryChange(it.name(), ValueChange.withBeforeValue(String.valueOf(it.value()))))
                .collect(Collectors.toList());

        if (!orphanChanges.isEmpty()) {
            topicOp = Change.OperationType.UPDATE;
        }

        orphanChanges.forEach(it -> afterTopicConfigsByName.put(it.name(), it));

        var partitions = ValueChange.with(afterTopic.partitions(), beforeTopic.partitions());
        var replication = ValueChange.with(afterTopic.replicationFactor(), beforeTopic.replicationFactor());

        return new TopicChange.Builder()
                .setName(afterTopic.name())
                .setPartitionsChange(partitions)
                .setReplicationFactorChange(replication)
                .setOperation(topicOp)
                .setConfigs(new ArrayList<>(afterTopicConfigsByName.values()))
                .build();
    }

    private static @NotNull TopicChange buildChangeForNewTopic(@NotNull final TopicResource afterTopic) {

        final TopicChange.Builder builder = new TopicChange.Builder()
                .setName(afterTopic.name())
                .setPartitionsChange(ValueChange.withAfterValue(afterTopic.partitions()))
                .setReplicationFactorChange(ValueChange.withAfterValue(afterTopic.replicationFactor()))
                .setOperation(Change.OperationType.ADD);

        afterTopic.configs().forEach(it -> builder.addConfigChange(
                        new ConfigEntryChange(
                                it.name(),
                                ValueChange.withAfterValue(String.valueOf(it.value()))
                        )
                )
        );
        return builder.build();
    }

    /**
     * Creates a new {@link TopicChanges} instance.
     *
     * @param changes the changes by topic name.
     */
    TopicChanges(@NotNull final Map<String, TopicChange> changes) {
        this.changes = Objects.requireNonNull(changes, "'changes cannot be null'");
    }

    /**
     * @return the list of {@link TopicChange}.
     */
    public List<TopicChange> all() {
        return new ArrayList<>(changes.values());
    }

    public Map<String, KafkaFuture<Void>> apply(final TopicOperation operation) {

        Map<String, TopicChange> filtered = all()
                .stream()
                .filter(operation)
                .collect(Collectors.toMap(TopicChange::name, it -> it));

        return operation.apply(new TopicChanges(filtered));
    }

    public TopicChange get(@NotNull final String topic) {
        return changes.get(topic);
    }

    @NotNull
    @Override
    public Iterator<TopicChange> iterator() {
        return all().iterator();
    }
}
