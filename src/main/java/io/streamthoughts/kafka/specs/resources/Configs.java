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
package io.streamthoughts.kafka.specs.resources;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.apache.kafka.clients.admin.Config;
import org.apache.kafka.clients.admin.ConfigEntry;

import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Class which is used to represent a set of resource configuration.
 **/
@JsonSerialize(using = Configs.Serializer.class)
public class Configs implements Iterable<ConfigValue> {

    public static Configs empty() {
        return new Configs();
    }

    private final TreeMap<String, ConfigValue> configValues;

    /**
     * Static helper method to create a new {@link Configs} object from a given {@link Map}.
     *
     * @param configs   the {@link Map}.
     * @return          new {@link Configs}.
     */
    public static Configs of(final Map<String, Object> configs) {
        if (configs == null || configs.isEmpty())  return new Configs();

        final Set<ConfigValue> values = configs.entrySet()
                .stream()
                .map(e -> new ConfigValue(e.getKey(), e.getValue()))
                .collect(Collectors.toSet());

        return new Configs(values);
    }

    /**
     * Static helper method to create a new {@link Configs} object from a given Kafka {@link Config}.
     * A {@link Predicate} can be passed to filter config entries.
     *
     * @param config        the {@link Config}.
     * @param predicate     the {@link Predicate}.
     * @return              a new {@link Configs}.
     */
    public static Configs of(final Config config,
                             final Predicate<ConfigEntry> predicate) {
        Set<ConfigValue> configs = config.entries().stream()
                .filter(predicate)
                .map(ConfigValue::new)
                .collect(Collectors.toSet());
        return new Configs(configs);
    }

    /**
     * Creates a new {@link Configs} instances.
     */
    public Configs() {
        this(new HashSet<>());
    }

    /**
     * Creates a new {@link Configs} instances.
     *
     * @param values the config values.
     */
    public Configs(final Set<ConfigValue> values) {
        this.configValues = new TreeMap<>();
        values.forEach(this::add);
    }

    /**
     * Add a new {@link ConfigValue} to this configs.
     *
     * @param value a new config entry.
     * @return the previous config containing into this configs.
     */
    public ConfigValue add(final ConfigValue value) {
        return this.configValues.put(value.name(), value);
    }

    /**
     * Returns all config values.
     *
     * @return a new {@link Set} of {@link ConfigValue}.
     */
    public Set<ConfigValue> values() {
        return new LinkedHashSet<>(this.configValues.values());
    }

    public Configs filters(final Configs configs) {
        Set<ConfigValue> filteredConfigs = this.configValues.values()
                .stream()
                .filter(v -> !configs.values().contains(v))
                .collect(Collectors.toSet());
        return new Configs(filteredConfigs);
    }


    public int size() {
        return this.configValues.size();
    }

    public ConfigValue get(final String name) {
        return this.configValues.get(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterator<ConfigValue> iterator() {
        return configValues.values().iterator();
    }

    public Map<String, Object> toMap() {
        return configValues.values().stream().collect(Collectors.toMap(ConfigValue::name, ConfigValue::value));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Configs)) return false;
        Configs that = (Configs) o;
        return Objects.equals(configValues, that.configValues);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Objects.hash(configValues);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "Configs{" +
                "values=" + configValues +
                '}';
    }

    public static class Serializer extends JsonSerializer<Configs> {

        /**
         * {@inheritDoc}
         */
        @Override
        public void serialize(final Configs configs,
                              final JsonGenerator gen,
                              final SerializerProvider serializers) throws IOException {

            gen.writeObject(new TreeMap<>(StreamSupport.
                    stream(configs.spliterator(), false)
                    .collect(Collectors.toMap(ConfigValue::name, ConfigValue::value))
                    )
            );
        }
    }
}
