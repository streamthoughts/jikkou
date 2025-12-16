/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.kafka.change.topics;

import static io.streamthoughts.jikkou.kafka.change.topics.TopicChange.CONFIG_PREFIX;
import static io.streamthoughts.jikkou.kafka.change.topics.TopicChange.PARTITIONS;

import io.streamthoughts.jikkou.common.utils.CollectionUtils;
import io.streamthoughts.jikkou.core.data.TypeConverter;
import io.streamthoughts.jikkou.core.models.change.ResourceChange;
import io.streamthoughts.jikkou.core.models.change.SpecificStateChange;
import io.streamthoughts.jikkou.core.models.change.StateChange;
import io.streamthoughts.jikkou.core.models.change.StateChangeList;
import io.streamthoughts.jikkou.core.reconciler.ChangeMetadata;
import io.streamthoughts.jikkou.core.reconciler.ChangeResponse;
import io.streamthoughts.jikkou.core.reconciler.Operation;
import io.streamthoughts.jikkou.core.reconciler.TextDescription;
import io.streamthoughts.jikkou.core.reconciler.change.BaseChangeHandler;
import io.streamthoughts.jikkou.kafka.internals.Futures;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AlterConfigOp;
import org.apache.kafka.clients.admin.ConfigEntry;
import org.apache.kafka.clients.admin.NewPartitions;
import org.apache.kafka.common.config.ConfigResource;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default command to alter multiple topics.
 */
public final class UpdateTopicChangeHandler extends BaseChangeHandler {

    private static final Logger LOG = LoggerFactory.getLogger(UpdateTopicChangeHandler.class);

    private final AdminClient client;

    /**
     * Creates a new {@link UpdateTopicChangeHandler} instance.
     *
     * @param client the {@link AdminClient} to be used.
     */
    public UpdateTopicChangeHandler(final @NotNull AdminClient client) {
        super(Operation.UPDATE);
        this.client = Objects.requireNonNull(client, "'client' cannot be null");
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
    public @NotNull List<ChangeResponse> handleChanges(final @NotNull List<ResourceChange> items) {

        final Map<ConfigResource, Collection<AlterConfigOp>> alterConfigs = new HashMap<>();
        final Map<String, NewPartitions> newPartitions = new HashMap<>();

        final Map<String, List<CompletableFuture<Void>>> results = new HashMap<>();

        for (ResourceChange item : items) {

            final String topicName = item.getMetadata().getName();

            results.put(topicName, new ArrayList<>());
            StateChangeList<? extends StateChange> data = item.getSpec().getChanges();
            StateChangeList<StateChange> configs = data.allWithPrefix(CONFIG_PREFIX);
            if (!configs.isEmpty()) {
                final List<AlterConfigOp> alters = new ArrayList<>(configs.size());
                for (StateChange configEntryChange : configs) {
                    var operationType = configEntryChange.getOp();

                    if (operationType == Operation.DELETE) {
                        alters.add(newAlterConfigOp(configEntryChange, null, AlterConfigOp.OpType.DELETE));
                    }

                    if (operationType == Operation.UPDATE || operationType == Operation.CREATE) {
                        final String configValue = String.valueOf(configEntryChange.getAfter());
                        alters.add(newAlterConfigOp(configEntryChange, configValue, AlterConfigOp.OpType.SET));
                    }
                }
                alterConfigs.put(new ConfigResource(ConfigResource.Type.TOPIC, topicName), alters);
            }

            data.findLast(PARTITIONS, TypeConverter.Integer())
                    .stream()
                    .filter(it -> it.getOp() != Operation.NONE)
                    .map(SpecificStateChange::getAfter)
                    .findFirst()
                    .ifPresent(newValue -> newPartitions.put(topicName, NewPartitions.increaseTo(newValue)));

        }

        // Update topic's configs
        if (!alterConfigs.isEmpty()) {
            client.incrementalAlterConfigs(alterConfigs).values()
                    .forEach((k, v) -> {
                        CompletableFuture<Void> future = Futures.toCompletableFuture(v);
                        if (LOG.isDebugEnabled()) {
                            future = future.thenAccept(unused -> {
                                LOG.debug("Completed config changes for topic: {}", k.name());
                            });
                        }
                        results.get(k.name()).add(future);
                    });
        }
        // Update topic's partitions
        if (!newPartitions.isEmpty()) {
            client.createPartitions(newPartitions).values()
                    .forEach((k, v) -> {
                        CompletableFuture<Void> future = Futures.toCompletableFuture(v);
                        if (LOG.isDebugEnabled()) {
                            future = future.thenAccept(unused -> {
                                LOG.debug("Completed partitions creation for topic: {}", k);
                            });
                        }
                        results.get(k).add(future);
                    });
        }

        Map<String, ResourceChange> changesByTopicName = CollectionUtils
                .keyBy(items, it -> it.getMetadata().getName());

        return results.entrySet()
                .stream()
                .map(e -> {
                    ResourceChange item = changesByTopicName.get(e.getKey());
                    List<CompletableFuture<ChangeMetadata>> futures = e.getValue().stream()
                            .map(f -> f.thenApply(unused -> ChangeMetadata.empty()))
                            .toList();
                    return new ChangeResponse(item, futures);
                })
                .toList();
    }

    @NotNull
    private AlterConfigOp newAlterConfigOp(final StateChange configEntryChange,
                                           final String value,
                                           final AlterConfigOp.OpType op) {
        return new AlterConfigOp(new ConfigEntry(configEntryChange.getName(), value), op);
    }
}
