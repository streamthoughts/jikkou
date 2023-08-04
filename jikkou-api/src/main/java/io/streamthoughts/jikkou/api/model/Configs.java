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
package io.streamthoughts.jikkou.api.model;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;


/**
 * Class which is used to represent a set of resource configuration.
 **/
@JsonSerialize(using = Configs.Serializer.class)
@JsonDeserialize(using = Configs.Deserializer.class)
public class Configs implements Iterable<ConfigValue> {

    public static Configs empty() {
        return new Configs();
    }

    private final TreeMap<String, ConfigValue> configValues;

    /**
     * Static helper method to create a new {@link Configs} object containing a single config property.
     *
     * @param   k1 – the config property 's key
     * @param   v1 – the config property 's value
     * @return       new {@link Configs}.
     */
    public static Configs of(@NotNull String k1,
                             @NotNull Object v1) {
        return of(Map.of(k1, v1));
    }

    /**
     * Static helper method to create a new {@link Configs} object containing a single config property.
     *
     * @param   k1 – the config property 's key
     * @param   v1 – the config property 's value
     * @param   k2 – the config property 's key
     * @param   v2 – the config property 's value
     * @return       new {@link Configs}.
     */
    public static Configs of(@NotNull String k1,
                             @NotNull Object v1,
                             @NotNull String k2,
                             @NotNull Object v2) {
        return of(Map.of(k1, v1, k2, v2));
    }

    /**
     * Static helper method to create a new {@link Configs} object from a given {@link Map}.
     *
     * @param configs   the {@link Map}.
     * @return          new {@link Configs}.
     */
    public static Configs of(final Map<String, ?> configs) {
        if (configs == null || configs.isEmpty())  return new Configs();

        final Set<ConfigValue> values = configs.entrySet()
                .stream()
                .map(e -> new ConfigValue(e.getKey(), e.getValue()))
                .collect(Collectors.toSet());

        return new Configs(values);
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
     * Adds a new {@link ConfigValue} to {@code this} configs.
     *
     * @param value a new config entry.
     * @return the previous config containing into {@code this} configs.
     */
    public ConfigValue add(final ConfigValue value) {
        return this.configValues.put(value.getName(), value);
    }

    /**
     * Adds all config values from the given configs.
     *
     * @param configs the config values to be added.
     */
    public void addAll(final Map<String, Object> configs) {
       addAll(Configs.of(configs));
    }

    /**
     * Adds all config values from the given configs.
     *
     * @param configs the config values to be added.
     */
    public void addAll(final Configs configs) {
        this.configValues.putAll(configs.configValues);
    }

    /**
     * Returns all config values.
     *
     * @return a new {@link Set} of {@link ConfigValue}.
     */
    public Set<ConfigValue> values() {
        return new LinkedHashSet<>(this.configValues.values());
    }

    public Configs filterAllNotContainedIn(final Configs configs) {
        Set<ConfigValue> filteredConfigs = this.configValues.values()
                .stream()
                .filter(v -> !configs.values().contains(v))
                .collect(Collectors.toSet());
        return new Configs(filteredConfigs);
    }


    public int size() {
        return this.configValues.size();
    }

    public boolean isEmpty() {
        return size() == 0;
    }

    public ConfigValue get(final String name) {
        return this.configValues.get(name);
    }

    /** {@inheritDoc} */
    @Override
    public Iterator<ConfigValue> iterator() {
        return configValues.values().iterator();
    }

    public Map<String, Object> toMap() {
        return new TreeMap<>(configValues.values()
                .stream()
                .filter(it -> it.value() != null)
                .collect(Collectors.toMap(
                        ConfigValue::getName,
                        ConfigValue::value)
                ));
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Configs)) return false;
        Configs that = (Configs) o;
        return Objects.equals(configValues, that.configValues);
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return Objects.hash(configValues);
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "Configs{" +
                "values=" + configValues +
                '}';
    }

    public static class Deserializer extends JsonDeserializer<Configs> {

        /** {@inheritDoc} */
        @Override
        public Configs deserialize(final JsonParser jsonParser,
                                   final DeserializationContext deserializationContext) {

            try {
                Map<String, Object> map = jsonParser.readValueAs(Map.class);
                return Configs.of(map);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static class Serializer extends JsonSerializer<Configs> {

        /** {@inheritDoc} */
        @Override
        public void serialize(final Configs configs,
                              final JsonGenerator gen,
                              final SerializerProvider serializers) throws IOException {
            gen.writeObject(configs.toMap());
        }
    }
}
