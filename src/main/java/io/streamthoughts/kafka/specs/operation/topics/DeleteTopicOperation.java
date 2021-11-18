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
package io.streamthoughts.kafka.specs.operation.topics;

import io.streamthoughts.kafka.specs.Description;
import io.streamthoughts.kafka.specs.change.Change;
import io.streamthoughts.kafka.specs.change.TopicChange;
import io.streamthoughts.kafka.specs.internal.DescriptionProvider;
import io.vavr.Tuple2;
import io.vavr.concurrent.Future;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.common.KafkaFuture;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Default command to delete multiple topics.
 */
public class DeleteTopicOperation implements TopicOperation {

    private static final Logger LOG = LoggerFactory.getLogger(DeleteTopicOperation.class);

    private static final Set<String> INTERNAL_TOPICS = Set.of(
            "__consumer_offsets",
            "_schemas",
            "__transaction_state",
            "connect-offsets",
            "connect-status",
            "connect-configs"
    );

    public static DescriptionProvider<TopicChange> DESCRIPTION = (resource -> {
        return (Description.Delete) () -> String.format("Delete topic %s ", resource.name());
    });

    private final AdminClient client;
    private final boolean excludeInternalTopics;

    public DeleteTopicOperation(final AdminClient client,
                                final boolean excludeInternalTopics) {
        this.client = client;
        this.excludeInternalTopics = excludeInternalTopics;
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
        return change.getOperation() == Change.OperationType.DELETE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull Map<String, List<Future<Void>>> doApply(final @NotNull Collection<TopicChange> changes) {
        List<String> topics = changes
                .stream()
                .map(TopicChange::name)
                .filter(name -> !excludeInternalTopics || isNotInternalTopics(name))
                .collect(Collectors.toList());
        LOG.info("Deleting topics: {}", topics);

        final Map<String, KafkaFuture<Void>> kafkaResults = client.deleteTopics(topics).topicNameValues();
        return kafkaResults.entrySet()
                .stream()
                .map(e -> new Tuple2<>(e.getKey(), List.of(Future.fromJavaFuture(e.getValue()))))
                .collect(Collectors.toMap(Tuple2::_1, Tuple2::_2));
    }

    private boolean isNotInternalTopics(final String topic) {
        return !INTERNAL_TOPICS.contains(topic) && !topic.startsWith("__");
    }
}
