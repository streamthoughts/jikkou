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
package io.streamthoughts.jikkou.kafka.change;

import io.streamthoughts.jikkou.kafka.resources.ConfigValue;
import io.streamthoughts.jikkou.kafka.resources.Named;
import org.apache.kafka.clients.admin.ConfigEntry;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ConfigEntryChangeComputer implements ChangeComputer<ConfigValue, String, ConfigEntryChange, ConfigEntryOptions> {

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ConfigEntryChange> computeChanges(@NotNull final Iterable<ConfigValue> actualStates,
                                                  @NotNull final Iterable<ConfigValue> expectedStates,
                                                  @NotNull final ConfigEntryOptions options) {

        final Map<String, ConfigValue> actualConfigsByName = Named.keyByName(actualStates);
        final Map<String, ConfigEntryChange> expectedConfigsByName = new HashMap<>();

        for (ConfigValue expected : expectedStates) {
            final String configEntryName = expected.name();

            final ConfigValue beforeConfigValue = actualConfigsByName.getOrDefault(
                    configEntryName,
                    new ConfigValue(configEntryName, null)
            );

            final ValueChange<Object> change = ValueChange.with(
                    expected.value(),
                    beforeConfigValue.value()
            );

            expectedConfigsByName.put(configEntryName, new ConfigEntryChange(configEntryName, change));
        }

        if (options.isDeleteConfigOrphans()) {
            // Iterate on all configs apply on the topic for
            // looking for DYNAMIC_TOPIC_CONFIGS that may be orphan.
            List<ConfigEntryChange> orphanChanges = actualConfigsByName.values()
                    .stream()
                    .filter(it -> it.unwrap() == null || it.unwrap().source() == ConfigEntry.ConfigSource.DYNAMIC_TOPIC_CONFIG)
                    .filter(it -> !expectedConfigsByName.containsKey(it.name()))
                    .map(it -> new ConfigEntryChange(it.name(), ValueChange.withBeforeValue(it.value())))
                    .collect(Collectors.toList());

            orphanChanges.forEach(it -> expectedConfigsByName.put(it.name(), it));
        }

        return new ArrayList<>(expectedConfigsByName.values());
    }
}
