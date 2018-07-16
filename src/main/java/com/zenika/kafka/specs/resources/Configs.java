/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.zenika.kafka.specs.resources;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Class which is used to represent a set of resource configuration.
 **/
public class Configs implements Iterable<ConfigValue> {

    private static final Configs EMPTY_CONFIGS = new Configs() {
        @Override
        public ConfigValue add(ConfigValue value) {
            throw new UnsupportedOperationException();
        }
    };

    public static Configs emptyConfigs() {
        return EMPTY_CONFIGS;
    }

    private TreeMap<String, ConfigValue> entries;

    public static Map<String, String> asStringValueMap(final Configs configs) {
        return new TreeMap<>(configs.values()
                .stream()
                .collect(Collectors.toMap(ConfigValue::name, ConfigValue::getValue)));
    }

    /**
     * Creates a new {@link Configs} instances.
     */
    public Configs() {
        this(new HashSet<>());
    }

    /**
     * Creates a new {@link Configs} instances.
     * @param values    the config values.
     */
    public Configs(final Set<ConfigValue> values) {
        this.entries = new TreeMap<>();
        values.forEach(this::add);
    }

    /**
     * Add a new {@link ConfigValue} to this configs.
     *
     * @param   value a new config entry.
     * @return  the previous config containing into this configs.
     */
    public ConfigValue add(final ConfigValue value) {
        return this.entries.put(value.name(), value);
    }

    /**
     * Returns all config values.
     *
     * @return  a new {@link Set} of {@link ConfigValue}.
     */
    public Set<ConfigValue> values() {
        return new LinkedHashSet<>(this.entries.values());
    }

    private Map<String, ConfigValue> asOrderedMap() {
        return new TreeMap<>(entries);
    }

    public Configs defaultConfigs() {
        Set<ConfigValue> values = this.entries.values()
                .stream()
                .filter(ConfigValue::isDefault)
                .collect(Collectors.toSet());
        return new Configs(values);
    }

    public Configs filters(final Configs configs) {
        Set<ConfigValue> filteredConfigs = this.entries.values()
                .stream()
                .filter(v -> !configs.values().contains(v))
                .collect(Collectors.toSet());
        return new Configs(filteredConfigs);
    }


    public int size() {
        return this.entries.size();
    }

    public ConfigValue get(final String name) {
        return this.entries.get(name);
    }

    /**
     * Checks whether this configuration contains some changes.
     *
     * @param configs
     * @return
     */
    public boolean containsChanges(final Configs configs) {

        Map<String, ConfigValue> thatConfigs = configs.asOrderedMap();

        // Check if all same configs are the same value.
        for (ConfigValue config : this.entries.values()) {
            ConfigValue currentValue = thatConfigs.remove(config.name());
            if (!currentValue.getValue().equals(config.getValue())) {
                return true;
            }
        }

        // Checks if all remaining configuration are defaults.
        for (ConfigValue config : thatConfigs.values()) {
            if (!config.isDefault()) {
                return true;
            }
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterator<ConfigValue> iterator() {
        return entries.values().iterator();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Configs that = (Configs) o;
        return Objects.equals(entries, that.entries);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {

        return Objects.hash(entries);
    }
}
