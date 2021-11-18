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

import io.streamthoughts.kafka.specs.model.V1TopicObject;
import io.streamthoughts.kafka.specs.operation.topics.TopicOperation;
import io.streamthoughts.kafka.specs.resources.Named;
import io.vavr.Tuple2;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class TopicChanges extends AbstractChanges<TopicChange, String, Void, TopicOperation> {

    public static TopicChanges computeChanges(@NotNull final Iterable<V1TopicObject> beforeTopicObjects,
                                              @NotNull final Iterable<V1TopicObject> afterTopicObjects) {

        final Map<String, V1TopicObject> beforeTopicResourceMapByName = Named.keyByName(beforeTopicObjects);

        final Map<String, TopicChange> changes = new HashMap<>();

        for (final V1TopicObject afterTopic : afterTopicObjects) {

            final V1TopicObject beforeTopic = beforeTopicResourceMapByName.get(afterTopic.name());
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
            @NotNull final Collection<V1TopicObject> topics,
            @NotNull final Set<String> changes) {
        return topics
                .stream()
                .filter(it -> !changes.contains(it.name()))
                .map(topic -> {
                    TopicChange.Builder change = new TopicChange.Builder()
                            .setName(topic.name())
                            .setOperation(Change.OperationType.DELETE);
                    return change.build();
                })
                .collect(Collectors.toMap(TopicChange::name, it -> it));
    }

    private static @NotNull TopicChange buildChangeForExistingTopic(@NotNull final V1TopicObject afterTopic,
                                                                    @NotNull final V1TopicObject beforeTopic) {

        var partitions = ValueChange.with(
                afterTopic.partitionsOrDefault(),
                beforeTopic.partitionsOrDefault()
        );
        var replication = ValueChange.with(
                afterTopic.replicationFactorOrDefault(),
                beforeTopic.replicationFactorOrDefault()
        );

        final Tuple2<Change.OperationType, List<ConfigEntryChange>> t = ConfigEntryChanges.computeChange(
                beforeTopic.configs(),
                afterTopic.configs()
        );

        return new TopicChange.Builder()
                .setName(afterTopic.name())
                .setPartitionsChange(partitions)
                .setReplicationFactorChange(replication)
                .setOperation(t._1)
                .setConfigs(t._2)
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
        super(changes);
    }
}
