/*
 * Copyright 2020 The original authors
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
package io.streamthoughts.jikkou.kafka.control.handlers.topics;

import io.streamthoughts.jikkou.api.change.ChangeHandler;
import io.streamthoughts.jikkou.api.change.ChangeMetadata;
import io.streamthoughts.jikkou.api.change.ChangeResponse;
import io.streamthoughts.jikkou.api.change.ChangeType;
import io.streamthoughts.jikkou.api.change.ConfigEntryChange;
import io.streamthoughts.jikkou.api.model.HasMetadataChange;
import io.streamthoughts.jikkou.common.utils.CollectionUtils;
import io.streamthoughts.jikkou.kafka.change.TopicChange;
import io.streamthoughts.jikkou.kafka.internals.Futures;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.CreateTopicsOptions;
import org.apache.kafka.clients.admin.CreateTopicsResult;
import org.apache.kafka.clients.admin.NewTopic;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default command to create multiple topics.
 */
public final class CreateTopicChangeHandler implements KafkaTopicChangeHandler {

    private static final Logger LOG = LoggerFactory.getLogger(CreateTopicChangeHandler.class);

    private final AdminClient client;

    /**
     * Creates a new {@link CreateTopicChangeHandler} instance.
     *
     * @param client the {@link AdminClient} to be used.
     */
    public CreateTopicChangeHandler(final AdminClient client) {
        this.client = client;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<ChangeType> supportedChangeTypes() {
        return Set.of(ChangeType.ADD);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull List<ChangeResponse<TopicChange>> apply(final @NotNull List<HasMetadataChange<TopicChange>> items) {
        List<NewTopic> topics = items
                .stream()
                .peek(it -> ChangeHandler.verify(this, it))
                .map(this::toNewTopic)
                .collect(Collectors.toList());

        LOG.info("Creating new topics : {}", topics);
        CreateTopicsResult result = client.createTopics(topics, new CreateTopicsOptions());

        Map<String, HasMetadataChange<TopicChange>> changesKeyedByTopicName = CollectionUtils
                .keyBy(items, it -> it.getChange().getName());

        final Map<String, CompletableFuture<Void>> results = new HashMap<>(result.values())
                .entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> Futures.toCompletableFuture(e.getValue())));

        return results.entrySet()
                .stream()
                .map(e -> new ChangeResponse<>(
                                changesKeyedByTopicName.get(e.getKey()),
                                e.getValue().thenApply(unused -> ChangeMetadata.empty())
                        )
                )
                .toList();
    }

    private NewTopic toNewTopic(final HasMetadataChange<TopicChange> t) {
        TopicChange change = t.getChange();
        Map<String, String> configs = change.getConfigEntryChanges()
                .stream()
                .collect(Collectors.toMap(ConfigEntryChange::getName, v -> String.valueOf(v.getValueChange().getAfter())));

        return new NewTopic(
                change.getName(),
                change.getPartitions().getAfter(),
                change.getReplicas().getAfter())
                .configs(configs);
    }
}
