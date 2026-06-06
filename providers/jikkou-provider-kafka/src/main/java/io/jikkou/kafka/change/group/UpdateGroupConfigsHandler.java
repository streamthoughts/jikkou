/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.jikkou.kafka.change.group;

import io.jikkou.common.utils.CollectionUtils;
import io.jikkou.core.models.change.ResourceChange;
import io.jikkou.core.models.change.StateChange;
import io.jikkou.core.models.change.StateChangeList;
import io.jikkou.core.reconciler.ChangeMetadata;
import io.jikkou.core.reconciler.ChangeResponse;
import io.jikkou.core.reconciler.Operation;
import io.jikkou.core.reconciler.TextDescription;
import io.jikkou.core.reconciler.change.BaseChangeHandler;
import io.jikkou.kafka.internals.Futures;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AlterConfigOp;
import org.apache.kafka.clients.admin.ConfigEntry;
import org.apache.kafka.common.config.ConfigResource;
import org.jetbrains.annotations.NotNull;

/**
 * Shared change handler that applies dynamic configuration changes to Kafka GROUP
 * resources (consumer groups and share groups) via {@code incrementalAlterConfigs}.
 * Handles both CREATE (set configs for a not-yet-existing group id) and UPDATE.
 */
public final class UpdateGroupConfigsHandler extends BaseChangeHandler {

    private final AdminClient client;

    /**
     * Creates a new {@link UpdateGroupConfigsHandler} instance.
     *
     * @param client the {@link AdminClient} to be used.
     */
    public UpdateGroupConfigsHandler(final @NotNull AdminClient client) {
        super(Set.of(Operation.CREATE, Operation.UPDATE));
        this.client = Objects.requireNonNull(client, "client cannot be null");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull List<ChangeResponse> handleChanges(final @NotNull List<ResourceChange> items) {
        final Map<ConfigResource, Collection<AlterConfigOp>> alterConfigs = new HashMap<>();

        for (ResourceChange item : items) {
            final String groupId = item.getMetadata().getName();
            StateChangeList<? extends StateChange> data = item.getSpec().getChanges();
            StateChangeList<StateChange> configs = data.allWithPrefix(GroupConfigs.CONFIG_PREFIX);
            final List<AlterConfigOp> alters = new ArrayList<>(configs.size());
            for (StateChange configEntryChange : configs) {
                Operation op = configEntryChange.getOp();
                if (op == Operation.DELETE) {
                    alters.add(new AlterConfigOp(
                        new ConfigEntry(configEntryChange.getName(), null), AlterConfigOp.OpType.DELETE));
                } else if (op == Operation.UPDATE || op == Operation.CREATE) {
                    alters.add(new AlterConfigOp(
                        new ConfigEntry(configEntryChange.getName(), String.valueOf(configEntryChange.getAfter())),
                        AlterConfigOp.OpType.SET));
                }
            }
            if (!alters.isEmpty()) {
                alterConfigs.put(new ConfigResource(ConfigResource.Type.GROUP, groupId), alters);
            }
        }

        final Map<String, List<CompletableFuture<Void>>> results = new HashMap<>();
        items.forEach(it -> results.put(it.getMetadata().getName(), new ArrayList<>()));

        if (!alterConfigs.isEmpty()) {
            client.incrementalAlterConfigs(alterConfigs).values()
                .forEach((k, v) -> results.get(k.name()).add(Futures.toCompletableFuture(v)));
        }

        Map<String, ResourceChange> changesByName = CollectionUtils.keyBy(items, it -> it.getMetadata().getName());
        return results.entrySet().stream()
            .map(e -> {
                ResourceChange item = changesByName.get(e.getKey());
                List<CompletableFuture<ChangeMetadata>> futures = e.getValue().stream()
                    .map(f -> f.thenApply(unused -> ChangeMetadata.empty()))
                    .toList();
                return new ChangeResponse(item, futures);
            })
            .toList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TextDescription describe(@NotNull ResourceChange change) {
        return () -> String.format("%s configs for group '%s'",
            change.getSpec().getOp().humanize(), change.getMetadata().getName());
    }
}
