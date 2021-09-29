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
import io.streamthoughts.kafka.specs.internal.DescriptionProvider;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AlterConfigOp;
import org.apache.kafka.clients.admin.ConfigEntry;
import org.apache.kafka.common.KafkaFuture;
import org.apache.kafka.common.config.ConfigResource;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Default command to alter multiple topics.
 */
public class AlterTopicOperation implements TopicOperation {

    private static final Logger LOG = LoggerFactory.getLogger(AlterTopicOperation.class);

    public static DescriptionProvider<TopicChange> DESCRIPTION = (resource -> {
        return (Description.Alter) () -> String.format("Alter topic %s", resource.name());
    });

    private final AdminClient client;

    private final boolean deleteOrphans;

    /**
     * Creates a new {@link AlterTopicOperation} instance.
     *
     * @param deleteOrphans is
     */
    public AlterTopicOperation(final AdminClient client, final boolean deleteOrphans) {
        this.client = client;
        this.deleteOrphans = deleteOrphans;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Description getDescriptionFor(@NotNull final TopicChange change) {
        return DESCRIPTION.getForResource(change);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean test(final TopicChange change) {
        return change.hasConfigEntryChanges();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, KafkaFuture<Void>> apply(@NotNull final TopicChanges topicChanges) {
        final Map<ConfigResource, Collection<AlterConfigOp>> configs = new HashMap<>();
        for (TopicChange change : topicChanges) {
            final List<AlterConfigOp> alters = new ArrayList<>(change.getConfigEntryChanges().size());
            for (ConfigEntryChange configEntryChange : change.getConfigEntryChanges()) {
                var operationType = configEntryChange.getOperation();

                if (operationType == Change.OperationType.DELETE && deleteOrphans) {
                    alters.add(new AlterConfigOp(
                            new ConfigEntry(configEntryChange.name(), null),
                            AlterConfigOp.OpType.DELETE)
                    );
                }

                if (operationType == Change.OperationType.UPDATE) {
                    alters.add(new AlterConfigOp(
                            new ConfigEntry(configEntryChange.name(), configEntryChange.getAfter()),
                            AlterConfigOp.OpType.SET)
                    );
                }
            }
            configs.put(new ConfigResource(ConfigResource.Type.TOPIC, change.name()), alters);
        }
        return client.incrementalAlterConfigs(configs)
                .values()
                .entrySet()
                .stream()
                .collect(Collectors.toMap(it -> it.getKey().name(), Map.Entry::getValue));
    }
}
