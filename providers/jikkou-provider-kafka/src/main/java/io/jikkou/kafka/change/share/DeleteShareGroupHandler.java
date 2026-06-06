/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.jikkou.kafka.change.share;

import io.jikkou.core.models.change.ResourceChange;
import io.jikkou.core.reconciler.ChangeMetadata;
import io.jikkou.core.reconciler.ChangeResponse;
import io.jikkou.core.reconciler.Operation;
import io.jikkou.core.reconciler.TextDescription;
import io.jikkou.core.reconciler.change.BaseChangeHandler;
import io.jikkou.kafka.internals.Futures;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.DeleteShareGroupsResult;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class DeleteShareGroupHandler extends BaseChangeHandler {

    private static final Logger LOG = LoggerFactory.getLogger(DeleteShareGroupHandler.class);

    private final AdminClient client;

    public DeleteShareGroupHandler(@NotNull AdminClient client) {
        super(Operation.DELETE);
        this.client = Objects.requireNonNull(client, "client cannot be null");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ChangeResponse> handleChanges(@NotNull List<ResourceChange> changes) {
        Map<String, ResourceChange> byName = changes.stream()
            .collect(Collectors.toMap(r -> r.getMetadata().getName(), Function.identity()));

        DeleteShareGroupsResult result = client.deleteShareGroups(byName.keySet());
        return result.deletedGroups().entrySet().stream()
            .map(e -> {
                CompletableFuture<ChangeMetadata> future = Futures.toCompletableFuture(e.getValue())
                    .thenApply(unused -> {
                        LOG.info("Completed deletion of Kafka Share Group {}", e.getKey());
                        return ChangeMetadata.empty();
                    });
                return new ChangeResponse(byName.get(e.getKey()), future);
            })
            .toList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TextDescription describe(@NotNull ResourceChange change) {
        return new ShareGroupChangeDescription(change);
    }
}
