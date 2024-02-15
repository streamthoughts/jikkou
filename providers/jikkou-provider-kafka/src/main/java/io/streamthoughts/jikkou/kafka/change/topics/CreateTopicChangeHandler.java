/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.kafka.change.topics;

import static io.streamthoughts.jikkou.kafka.change.topics.TopicChange.CONFIG_PREFIX;
import static io.streamthoughts.jikkou.kafka.change.topics.TopicChange.PARTITIONS;
import static io.streamthoughts.jikkou.kafka.change.topics.TopicChange.REPLICAS;

import io.streamthoughts.jikkou.common.utils.CollectionUtils;
import io.streamthoughts.jikkou.common.utils.Pair;
import io.streamthoughts.jikkou.core.data.TypeConverter;
import io.streamthoughts.jikkou.core.models.change.ResourceChange;
import io.streamthoughts.jikkou.core.models.change.StateChange;
import io.streamthoughts.jikkou.core.models.change.StateChangeList;
import io.streamthoughts.jikkou.core.reconciler.ChangeMetadata;
import io.streamthoughts.jikkou.core.reconciler.ChangeResponse;
import io.streamthoughts.jikkou.core.reconciler.Operation;
import io.streamthoughts.jikkou.core.reconciler.TextDescription;
import io.streamthoughts.jikkou.core.reconciler.change.BaseChangeHandler;
import io.streamthoughts.jikkou.kafka.internals.Futures;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
public final class CreateTopicChangeHandler extends BaseChangeHandler<ResourceChange> {

    private static final Logger LOG = LoggerFactory.getLogger(CreateTopicChangeHandler.class);

    private final AdminClient client;

    /**
     * Creates a new {@link CreateTopicChangeHandler} instance.
     *
     * @param client the {@link AdminClient} to be used.
     */
    public CreateTopicChangeHandler(final AdminClient client) {
        super(Operation.CREATE);
        this.client = client;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TextDescription describe(@NotNull final ResourceChange change) {
        return TopicChange.getDescription(change);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull List<ChangeResponse<ResourceChange>> handleChanges(
            final @NotNull List<ResourceChange> changes) {
        List<NewTopic> topics = changes
                .stream()
                .map(this::toNewTopic)
                .collect(Collectors.toList());

        CreateTopicsResult result = client.createTopics(topics, new CreateTopicsOptions());

        Map<String, ResourceChange> changesByTopicName = CollectionUtils
                .keyBy(changes, it -> it.getMetadata().getName());

        final Map<String, CompletableFuture<Void>> results = new HashMap<>(result.values())
                .entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> Futures.toCompletableFuture(e.getValue())));

        return results.entrySet()
                .stream()
                .map(e -> {
                    ResourceChange resource = changesByTopicName.get(e.getKey());
                    CompletableFuture<ChangeMetadata> future = e.getValue().thenApply(
                            unused -> {
                                if (LOG.isInfoEnabled()) {
                                    StateChangeList<? extends StateChange> data = resource.getSpec().getChanges();
                                    LOG.info("Completed topic creation with: name={}, partitions={}, replicas={}",
                                            e.getKey(),
                                            data.getLast(PARTITIONS, TypeConverter.Integer()).getAfter(),
                                            data.getLast(REPLICAS, TypeConverter.Short()).getAfter()
                                    );
                                }
                                return ChangeMetadata.empty();
                            });
                    return new ChangeResponse<>(resource, future);
                })
                .toList();
    }

    private NewTopic toNewTopic(final ResourceChange change) {
        StateChangeList<? extends StateChange> data = change.getSpec().getChanges();
        Map<String, String> configs = data.allWithPrefix(CONFIG_PREFIX)
                .stream()
                .map(it -> Pair.of(it.getName(), it))
                .map(it -> it.mapRight(StateChange::getAfter))
                .map(it -> it.mapRight(Object::toString))
                .collect(Collectors.toMap(Pair::_1, Pair::_2));

        return new NewTopic(
                change.getMetadata().getName(),
                data.getLast(PARTITIONS, TypeConverter.Integer()).getAfter(),
                data.getLast(REPLICAS, TypeConverter.Short()).getAfter()
        ).configs(configs);
    }
}
