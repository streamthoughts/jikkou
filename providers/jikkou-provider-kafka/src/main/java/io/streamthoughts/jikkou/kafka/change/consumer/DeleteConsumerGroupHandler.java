/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.kafka.change.consumer;

import io.streamthoughts.jikkou.core.models.change.ResourceChange;
import io.streamthoughts.jikkou.core.reconciler.ChangeMetadata;
import io.streamthoughts.jikkou.core.reconciler.ChangeResponse;
import io.streamthoughts.jikkou.core.reconciler.Operation;
import io.streamthoughts.jikkou.core.reconciler.TextDescription;
import io.streamthoughts.jikkou.core.reconciler.change.BaseChangeHandler;
import io.streamthoughts.jikkou.kafka.internals.Futures;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.DeleteConsumerGroupsResult;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeleteConsumerGroupHandler extends BaseChangeHandler<ResourceChange> {

    private static final Logger LOG = LoggerFactory.getLogger(DeleteConsumerGroupHandler.class);

    private final AdminClient client;

    /**
     * Creates a new {@link DeleteConsumerGroupHandler} instance.
     *
     * @param client The AdminClient.
     */
    public DeleteConsumerGroupHandler(@NotNull AdminClient client) {
        super(Operation.DELETE);
        this.client = Objects.requireNonNull(client, "client cannot be null");
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public List<ChangeResponse<ResourceChange>> handleChanges(@NotNull List<ResourceChange> changes) {

        Map<String, ResourceChange> consumerGroupsByName = changes
                .stream()
                .collect(Collectors.toMap(
                        resource -> resource.getMetadata().getName(),
                        Function.identity()
                ));

        DeleteConsumerGroupsResult result = client.deleteConsumerGroups(consumerGroupsByName.keySet());
        Map<String, CompletableFuture<Void>> futuresByConsumerGroup = result.deletedGroups()
                .entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> Futures.toCompletableFuture(e.getValue())));

        return futuresByConsumerGroup
                .entrySet()
                .stream()
                .map(e -> {
                    CompletableFuture<ChangeMetadata> future = e.getValue().thenApply(unused -> {
                        if (LOG.isInfoEnabled()) {
                            LOG.info("Completed deletion of Kafka Consumer Group {}", e.getKey());
                        }
                        return ChangeMetadata.empty();
                    });
                    ResourceChange item = consumerGroupsByName.get(e.getKey());
                    return new ChangeResponse<>(item, future);
                })
                .toList();
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public TextDescription describe(@NotNull ResourceChange change) {
        return new ConsumerGroupChangeDescription(change);
    }
}
