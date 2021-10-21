/*
 * Copyright 2021 StreamThoughts.
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
package io.streamthoughts.kafka.specs.change;

import io.streamthoughts.kafka.specs.resources.ConfigValue;
import io.streamthoughts.kafka.specs.resources.Configs;
import io.streamthoughts.kafka.specs.resources.Named;
import io.vavr.Tuple2;
import org.apache.kafka.clients.admin.ConfigEntry;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ConfigEntryChanges {

    public static Tuple2<Change.OperationType, List<ConfigEntryChange>> computeChange(@NotNull final Configs beforeConfigs,
                                                                                      @NotNull final Configs afterConfigs) {

        final Map<String, ConfigValue> beforeConfigsByName = Named.keyByName(beforeConfigs);
        final Map<String, ConfigEntryChange> afterConfigsByName = new HashMap<>();

        Change.OperationType op = Change.OperationType.NONE;

        for (ConfigValue afterConfigValue : afterConfigs) {
            final String configEntryName = afterConfigValue.name();

            final ConfigValue beforeConfigValue = beforeConfigsByName.getOrDefault(
                    configEntryName,
                    new ConfigValue(configEntryName, null)
            );

            final ValueChange<Object> change = ValueChange.with(
                   afterConfigValue.value(),
                   beforeConfigValue.value()
            );

            if (change.getOperation() != Change.OperationType.NONE) {
                op = Change.OperationType.UPDATE;
            }

            afterConfigsByName.put(configEntryName, new ConfigEntryChange(configEntryName, change));
        }

        // Iterate on all configs apply on the topic for
        // looking for DYNAMIC_TOPIC_CONFIGS that may be orphan.
        List<ConfigEntryChange> orphanChanges = beforeConfigsByName.values()
                .stream()
                .filter(it -> it.unwrap() == null || it.unwrap().source() == ConfigEntry.ConfigSource.DYNAMIC_TOPIC_CONFIG)
                .filter(it -> !afterConfigsByName.containsKey(it.name()))
                .map(it -> new ConfigEntryChange(it.name(), ValueChange.withBeforeValue(it.value())))
                .collect(Collectors.toList());

        if (!orphanChanges.isEmpty()) {
            op = Change.OperationType.UPDATE;
        }

        orphanChanges.forEach(it -> afterConfigsByName.put(it.name(), it));

        return new Tuple2<>(op, new ArrayList<>(afterConfigsByName.values()));
    }
}
