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

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Class which is used to represent a set of resource configuration.
 **/
@JsonSerialize(using = Configs.Serializer.class)
public class Configs implements Iterable<ConfigValue> {

    private static final Configs EMPTY_CONFIGS = new Configs() {
        @Override
        public ConfigValue add(final ConfigValue value) {
            throw new UnsupportedOperationException();
        }
    };

    public static Configs empty() {
        return EMPTY_CONFIGS;
    }

    private final TreeMap<String, ConfigValue> values;

    public static Configs of(final Config config, final boolean describeDefault) {
        Set<ConfigValue> configs = config.entries().stream()
                .filter(entry -> !entry.isDefault() || describeDefault)
                .map(ConfigValue::new)
                .collect(Collectors.toSet());
        return new Configs(configs);
    }

    public static Map<String, String> asStringValueMap(final Configs configs) {
        if (configs == null || configs.isEmpty()) return Collections.emptyMap();
        return new TreeMap<>(configs.values()
                .stream()
                .filter(it -> it.value() != null)
                .collect(Collectors.toMap(ConfigValue::name, v -> v.value().toString())));
    }

    /**
     * Creates a new {@link Configs} instances.
     */
    Configs() {
        this(new HashSet<>());
    }

    /**
     * Creates a new {@link Configs} instances.
     *
     * @param values the config values.
     */
    public Configs(final Set<ConfigValue> values) {
        this.values = new TreeMap<>();
        values.forEach(this::add);
    }

    /**
     * Add a new {@link ConfigValue} to this configs.
     *
     * @param value a new config entry.
     * @return the previous config containing into this configs.
     */
    public ConfigValue add(final ConfigValue value) {
        return this.values.put(value.name(), value);
    }

    /**
     * Returns all config values.
     *
     * @return a new {@link Set} of {@link ConfigValue}.
     */
    public Set<ConfigValue> values() {
        return new LinkedHashSet<>(this.values.values());
    }

    private Map<String, ConfigValue> asOrderedMap() {
        return new TreeMap<>(values);
    }

    public Configs defaultConfigs() {
        Set<ConfigValue> values = this.values.values()
                .stream()
                .filter(ConfigValue::isDefault)
                .collect(Collectors.toSet());
        return new Configs(values);
    }

    public Configs filters(final Configs configs) {
        Set<ConfigValue> filteredConfigs = this.values.values()
                .stream()
                .filter(v -> !configs.values().contains(v))
                .collect(Collectors.toSet());
        return new Configs(filteredConfigs);
    }


    public int size() {
        return this.values.size();
    }

    public boolean isEmpty() {
        return this.values.isEmpty();
    }

    public ConfigValue get(final String name) {
        return this.values.get(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterator<ConfigValue> iterator() {
        return values.values().iterator();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Configs)) return false;
        Configs that = (Configs) o;
        return Objects.equals(values, that.values);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Objects.hash(values);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "Configs{" +
                "values=" + values +
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
