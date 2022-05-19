/*
 * Copyright 2020 StreamThoughts.
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
package io.streamthoughts.jikkou.api.operation.topics;

import io.streamthoughts.jikkou.api.operation.Description;
import io.streamthoughts.jikkou.api.change.Change;
import io.streamthoughts.jikkou.api.change.ConfigEntryChange;
import io.streamthoughts.jikkou.api.change.TopicChange;
import io.streamthoughts.jikkou.internal.DescriptionProvider;
import io.vavr.Tuple2;
import io.vavr.concurrent.Future;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.CreateTopicsOptions;
import org.apache.kafka.clients.admin.CreateTopicsResult;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.KafkaFuture;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Default command to create multiple topics.
 */
public final class CreateTopicOperation implements TopicOperation {

    private static final Logger LOG = LoggerFactory.getLogger(CreateTopicOperation.class);

    public static DescriptionProvider<TopicChange> DESCRIPTION = (resource -> {
        return (Description.Create) () -> String.format("Create a new topic %s (partitions=%d, replicas=%d)",
                resource.name(),
                resource.getPartitions().get().getAfter(),
                resource.getReplicationFactor().get().getAfter()
        );
    });

    private final AdminClient client;

    /**
     * Creates a new {@link CreateTopicOperation} instance.
     *
     * @param client    the {@link AdminClient} to be used.
     */
    public CreateTopicOperation(final AdminClient client) {
        this.client = client;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Description getDescriptionFor(@NotNull final TopicChange topicChange) {
        return DESCRIPTION.getForResource(topicChange);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean test(final TopicChange change) {
        return change.getOperation() == Change.OperationType.ADD;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull Map<String, List<Future<Void>>> doApply(final @NotNull Collection<TopicChange> changes) {
        List<NewTopic> topics = changes
                .stream()
                .peek(this::verify)
                .map(this::toNewTopic)
                .collect(Collectors.toList());
        LOG.info("Creating new topics : {}", topics);
        CreateTopicsResult result = client.createTopics(topics, new CreateTopicsOptions());

        final Map<String, KafkaFuture<Void>> kafkaResults = result.values();
        return kafkaResults.entrySet()
                .stream()
                .map(e -> new Tuple2<>(e.getKey(), List.of(Future.fromJavaFuture(e.getValue()))))
                .collect(Collectors.toMap(Tuple2::_1, Tuple2::_2));
    }

    private NewTopic toNewTopic(final TopicChange t) {
        Map<String, String> configs = t.getConfigEntryChanges()
                .stream()
                .collect(Collectors.toMap(ConfigEntryChange::name, v -> String.valueOf(v.getAfter())));

        return new NewTopic(
                t.name(),
                t.getPartitions().get().getAfter(),
                t.getReplicationFactor().get().getAfter())
                .configs(configs);
    }

    private void verify(final @NotNull TopicChange change) {
        if (!test(change)) {
            throw new IllegalArgumentException("This operation does not support the passed change: " + change);
        }
    }

}
