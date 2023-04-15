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
package io.streamthoughts.jikkou.api.control;

import io.streamthoughts.jikkou.api.model.ConfigValue;
import io.streamthoughts.jikkou.api.model.Nameable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.NotNull;

public class ConfigEntryChangeComputer implements ChangeComputer<ConfigValue, ConfigEntryChange> {

    private boolean isConfigDeletionEnabled;

    /**
     * Creates a new {@link ConfigEntryChangeComputer} instance.
     */
    public ConfigEntryChangeComputer() {
        this(true);
    }

    /**
     * Creates a new {@link ConfigEntryChangeComputer} instance.
     *
     * @param isConfigDeletionEnabled {@code true} to delete orphaned config entries.
     */
    public ConfigEntryChangeComputer(boolean isConfigDeletionEnabled) {
        this.isConfigDeletionEnabled = isConfigDeletionEnabled;
    }

    /**
     * Sets whether orphaned config entries should be deleted or ignored.
     *
     * @param isConfigDeletionEnabled {@code true} to enable orphans deletion.
     */
    public void isConfigDeletionEnabled(boolean isConfigDeletionEnabled) {
        this.isConfigDeletionEnabled = isConfigDeletionEnabled;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ConfigEntryChange> computeChanges(@NotNull final Iterable<ConfigValue> actualStates,
                                                  @NotNull final Iterable<ConfigValue> expectedStates) {

        final Map<String, ConfigValue> actualConfigsByName = Nameable.keyByName(actualStates);
        final Map<String, ConfigEntryChange> expectedConfigsByName = new HashMap<>();

        for (ConfigValue expectedConfigValue : expectedStates) {
            final String configEntryName = expectedConfigValue.getName();

            final ConfigValue actualConfigValue = actualConfigsByName.getOrDefault(
                    configEntryName,
                    new ConfigValue(configEntryName, null)
            );

            final ValueChange<Object> change = ValueChange.with(
                    expectedConfigValue.value(),
                    actualConfigValue.value()
            );

            expectedConfigsByName.put(configEntryName, new ConfigEntryChange(configEntryName, change));
        }

        if (isConfigDeletionEnabled) {
            List<ConfigEntryChange> orphanChanges = actualConfigsByName.values()
                    .stream()
                    .filter(ConfigValue::isDeletable)
                    .filter(it -> !expectedConfigsByName.containsKey(it.getName()))
                    .map(it -> new ConfigEntryChange(it.getName(), ValueChange.withBeforeValue(it.value())))
                    .toList();

            orphanChanges.forEach(it -> expectedConfigsByName.put(it.getName(), it));
        }

        return new ArrayList<>(expectedConfigsByName.values());
    }
}
