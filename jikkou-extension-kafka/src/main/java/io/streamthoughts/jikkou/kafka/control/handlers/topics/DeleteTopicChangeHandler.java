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
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default command to delete multiple topics.
 */
public final class DeleteTopicChangeHandler implements KafkaTopicChangeHandler {

    private static final Logger LOG = LoggerFactory.getLogger(DeleteTopicChangeHandler.class);

    private final AdminClient client;

    /**
     * Creates a new {@link DeleteTopicChangeHandler} instance.
     *
     * @param client the {@link AdminClient} to be used.
     */
    public DeleteTopicChangeHandler(final AdminClient client) {
        this.client = client;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<ChangeType> supportedChangeTypes() {
        return Set.of(ChangeType.DELETE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull List<ChangeResponse<TopicChange>> apply(final @NotNull List<HasMetadataChange<TopicChange>> items) {
        List<String> topics = items
                .stream()
                .peek(it -> ChangeHandler.verify(this, it))
                .map(it -> it.getChange().getName())
                .collect(Collectors.toList());

        LOG.info("Deleting topics: {}", topics);
        final Map<String, CompletableFuture<Void>> results = new HashMap<>(client.deleteTopics(topics).topicNameValues())
                .entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> Futures.toCompletableFuture(e.getValue())));

        Map<String, HasMetadataChange<TopicChange>> changesKeyedByTopicName = CollectionUtils
                .keyBy(items, it -> it.getChange().getName());

        return results.entrySet()
                .stream()
                .map(e -> new ChangeResponse<>(
                                changesKeyedByTopicName.get(e.getKey()),
                                e.getValue().thenApply(unused -> ChangeMetadata.empty())
                        )
                )
                .toList();
    }
}
