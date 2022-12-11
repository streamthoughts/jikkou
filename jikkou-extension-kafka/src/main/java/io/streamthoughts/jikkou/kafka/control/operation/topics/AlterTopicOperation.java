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
package io.streamthoughts.jikkou.kafka.control.operation.topics;

import io.streamthoughts.jikkou.api.control.ChangeDescription;
import io.streamthoughts.jikkou.api.control.ChangeType;
import io.streamthoughts.jikkou.api.control.ConfigEntryChange;
import io.streamthoughts.jikkou.api.control.ValueChange;
import io.streamthoughts.jikkou.kafka.control.change.TopicChange;
import io.vavr.concurrent.Future;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AlterConfigOp;
import org.apache.kafka.clients.admin.ConfigEntry;
import org.apache.kafka.clients.admin.NewPartitions;
import org.apache.kafka.common.config.ConfigResource;
import org.jetbrains.annotations.NotNull;

/**
 * Default command to alter multiple topics.
 */
public final class AlterTopicOperation implements TopicOperation {

    private final AdminClient client;

    /**
     * Creates a new {@link AlterTopicOperation} instance.
     *
     * @param client    the {@link AdminClient} to be used.
     */
    public AlterTopicOperation(final @NotNull AdminClient client) {
        this.client = Objects.requireNonNull(client,"'client' cannot be null");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ChangeDescription getDescriptionFor(@NotNull final TopicChange change) {
        return new TopicChangeDescription(change);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean test(final TopicChange change) {
        return change.getChange() == ChangeType.UPDATE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull Map<String, List<Future<Void>>> doApply(@NotNull final Collection<TopicChange> changes) {

        final Map<ConfigResource, Collection<AlterConfigOp>> alterConfigs = new HashMap<>();
        final Map<String, NewPartitions> newPartitions = new HashMap<>();

        final Map<String, List<Future<Void>>> results = new HashMap<>();
        for (TopicChange change : changes) {

            verify(change);
            results.put(change.getName(), new ArrayList<>());

            if (change.hasConfigEntryChanges()) {
                final List<AlterConfigOp> alters = new ArrayList<>(change.getConfigEntryChanges().size());
                for (ConfigEntryChange configEntryChange : change.getConfigEntryChanges()) {
                    var operationType = configEntryChange.getChange();

                    if (operationType == ChangeType.DELETE) {
                        alters.add(newAlterConfigOp(configEntryChange, null, AlterConfigOp.OpType.DELETE));
                    }

                    if (operationType == ChangeType.UPDATE) {
                        final String configValue = String.valueOf(configEntryChange.getValueChange().getAfter());
                        alters.add(newAlterConfigOp(configEntryChange, configValue, AlterConfigOp.OpType.SET));
                    }
                }
                alterConfigs.put(new ConfigResource(ConfigResource.Type.TOPIC, change.getName()), alters);
            }

            change.getPartitions()
                  .flatMap(ValueChange::tOption)
                  .forEach(newValue -> newPartitions.put(change.getName(), NewPartitions.increaseTo(newValue)));

        }

        if (!alterConfigs.isEmpty()) {
            client.incrementalAlterConfigs(alterConfigs).values()
                    .forEach((k, v) -> results.get(k.name()).add(Future.fromJavaFuture(v)));
        }

        if (!newPartitions.isEmpty()) {
            client.createPartitions(newPartitions).values()
                    .forEach((k, v) -> results.get(k).add(Future.fromJavaFuture(v)));
        }

        return results;
    }

    @NotNull
    private AlterConfigOp newAlterConfigOp(final ConfigEntryChange configEntryChange,
                                           final String value,
                                           final AlterConfigOp.OpType op) {
        return new AlterConfigOp(new ConfigEntry(configEntryChange.getName(), value), op);
    }

    private void verify(final @NotNull TopicChange change) {
        if (!test(change)) {
            throw new IllegalArgumentException("This operation does not support the passed change: " + change);
        }
    }
}
