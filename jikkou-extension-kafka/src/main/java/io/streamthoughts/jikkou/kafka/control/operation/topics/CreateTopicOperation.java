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
package io.streamthoughts.jikkou.kafka.control.operation.topics;

import io.streamthoughts.jikkou.api.control.ChangeDescription;
import io.streamthoughts.jikkou.api.control.ChangeType;
import io.streamthoughts.jikkou.api.control.ConfigEntryChange;
import io.streamthoughts.jikkou.kafka.control.change.TopicChange;
import io.vavr.Tuple2;
import io.vavr.concurrent.Future;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.CreateTopicsOptions;
import org.apache.kafka.clients.admin.CreateTopicsResult;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.KafkaFuture;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default command to create multiple topics.
 */
public final class CreateTopicOperation implements TopicOperation {

    private static final Logger LOG = LoggerFactory.getLogger(CreateTopicOperation.class);

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
    public ChangeDescription getDescriptionFor(@NotNull final TopicChange topicChange) {
        return new TopicChangeDescription(topicChange);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean test(final TopicChange change) {
        return change.getChange() == ChangeType.ADD;
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
                .collect(Collectors.toMap(ConfigEntryChange::getName, v -> String.valueOf(v.getValueChange().getAfter())));

        return new NewTopic(
                t.getName(),
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
