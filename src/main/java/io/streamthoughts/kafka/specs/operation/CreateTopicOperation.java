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
package io.streamthoughts.kafka.specs.operation;

import io.streamthoughts.kafka.specs.Description;
import io.streamthoughts.kafka.specs.change.Change;
import io.streamthoughts.kafka.specs.change.ConfigEntryChange;
import io.streamthoughts.kafka.specs.change.TopicChange;
import io.streamthoughts.kafka.specs.change.TopicChanges;
import io.streamthoughts.kafka.specs.change.ValueChange;
import io.streamthoughts.kafka.specs.internal.DescriptionProvider;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.CreateTopicsOptions;
import org.apache.kafka.clients.admin.CreateTopicsResult;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.KafkaFuture;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Default command to create multiple topics.
 */
public class CreateTopicOperation implements TopicOperation {

    private static final Logger LOG = LoggerFactory.getLogger(CreateTopicOperation.class);

    public static DescriptionProvider<TopicChange> DESCRIPTION = (resource -> {
        return (Description.Create) () -> String.format("Create a new topic %s (partitions=%d, replicas=%d)",
                resource.name(),
                resource.getPartitions().get().getAfter(),
                resource.getReplicationFactor().get().getAfter()
        );
    });

    private final CreateTopicOperationOptions options;

    private final AdminClient client;

    public CreateTopicOperation(final AdminClient client,
                                final CreateTopicOperationOptions options) {
        this.options = options;
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
    public Map<String, KafkaFuture<Void>> apply(@NotNull final TopicChanges topicChanges) {
        List<NewTopic> topics = topicChanges.all()
                .stream()
                .map(this::toNewTopic)
                .collect(Collectors.toList());
        LOG.info("Creating new topics : {}", topics);
        CreateTopicsResult result = client.createTopics(topics, new CreateTopicsOptions());

        return result.values();
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

}
