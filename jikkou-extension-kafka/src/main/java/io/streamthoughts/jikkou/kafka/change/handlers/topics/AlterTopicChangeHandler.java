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
package io.streamthoughts.jikkou.kafka.change.handlers.topics;

import io.streamthoughts.jikkou.api.change.ChangeHandler;
import io.streamthoughts.jikkou.api.change.ChangeMetadata;
import io.streamthoughts.jikkou.api.change.ChangeResponse;
import io.streamthoughts.jikkou.api.change.ChangeType;
import io.streamthoughts.jikkou.api.change.ConfigEntryChange;
import io.streamthoughts.jikkou.api.change.ValueChange;
import io.streamthoughts.jikkou.api.model.HasMetadataChange;
import io.streamthoughts.jikkou.common.utils.CollectionUtils;
import io.streamthoughts.jikkou.kafka.change.TopicChange;
import io.streamthoughts.jikkou.kafka.internals.Futures;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AlterConfigOp;
import org.apache.kafka.clients.admin.ConfigEntry;
import org.apache.kafka.clients.admin.NewPartitions;
import org.apache.kafka.common.config.ConfigResource;
import org.jetbrains.annotations.NotNull;

/**
 * Default command to alter multiple topics.
 */
public final class AlterTopicChangeHandler implements KafkaTopicChangeHandler {

    private final AdminClient client;

    /**
     * Creates a new {@link AlterTopicChangeHandler} instance.
     *
     * @param client the {@link AdminClient} to be used.
     */
    public AlterTopicChangeHandler(final @NotNull AdminClient client) {
        this.client = Objects.requireNonNull(client, "'client' cannot be null");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<ChangeType> supportedChangeTypes() {
        return Set.of(ChangeType.UPDATE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull List<ChangeResponse<TopicChange>> apply(final @NotNull List<HasMetadataChange<TopicChange>> items) {

        final Map<ConfigResource, Collection<AlterConfigOp>> alterConfigs = new HashMap<>();
        final Map<String, NewPartitions> newPartitions = new HashMap<>();

        final Map<String, List<CompletableFuture<Void>>> results = new HashMap<>();
        for (HasMetadataChange<TopicChange> item : items) {
            ChangeHandler.verify(this, item);

            TopicChange change = item.getChange();
            results.put(change.getName(), new ArrayList<>());
            if (change.hasConfigEntryChanges()) {
                final List<AlterConfigOp> alters = new ArrayList<>(change.getConfigEntryChanges().size());
                for (ConfigEntryChange configEntryChange : change.getConfigEntryChanges()) {
                    var operationType = configEntryChange.getChangeType();

                    if (operationType == ChangeType.DELETE) {
                        alters.add(newAlterConfigOp(configEntryChange, null, AlterConfigOp.OpType.DELETE));
                    }

                    if (operationType == ChangeType.UPDATE || operationType == ChangeType.ADD) {
                        final String configValue = String.valueOf(configEntryChange.getValueChange().getAfter());
                        alters.add(newAlterConfigOp(configEntryChange, configValue, AlterConfigOp.OpType.SET));
                    }
                }
                alterConfigs.put(new ConfigResource(ConfigResource.Type.TOPIC, change.getName()), alters);
            }

            Optional.ofNullable(change.getPartitions())
                    .flatMap(ValueChange::toOptional)
                    .ifPresent(newValue -> newPartitions.put(change.getName(), NewPartitions.increaseTo(newValue)));

        }

        if (!alterConfigs.isEmpty()) {
            client.incrementalAlterConfigs(alterConfigs).values()
                    .forEach((k, v) -> results.get(k.name()).add(Futures.toCompletableFuture(v)));
        }

        if (!newPartitions.isEmpty()) {
            client.createPartitions(newPartitions).values()
                    .forEach((k, v) -> results.get(k).add(Futures.toCompletableFuture(v)));
        }

        Map<String, HasMetadataChange<TopicChange>> changesKeyedByTopicName = CollectionUtils
                .keyBy(items, it -> it.getChange().getName());

        return results.entrySet()
                .stream()
                .map(e -> new ChangeResponse<>(
                                changesKeyedByTopicName.get(e.getKey()),
                                e.getValue().stream().map(f -> f.thenApply(unused -> ChangeMetadata.empty())).collect(Collectors.toList())
                        )
                )
                .toList();
    }

    @NotNull
    private AlterConfigOp newAlterConfigOp(final ConfigEntryChange configEntryChange,
                                           final String value,
                                           final AlterConfigOp.OpType op) {
        return new AlterConfigOp(new ConfigEntry(configEntryChange.getName(), value), op);
    }
}
