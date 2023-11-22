/*
 * Copyright 2023 The original authors
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
package io.streamthoughts.jikkou.core.config;

import io.streamthoughts.jikkou.common.annotation.InterfaceStability;
import io.streamthoughts.jikkou.common.utils.Classes;
import io.streamthoughts.jikkou.common.utils.PropertiesUtils;
import io.streamthoughts.jikkou.core.config.internals.TypeConverter;
import io.streamthoughts.jikkou.runtime.JikkouConfig;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.BooleanSupplier;
import java.util.function.DoubleSupplier;
import java.util.function.IntSupplier;
import java.util.function.LongSupplier;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * An immutable configuration.
 */
@InterfaceStability.Evolving
public interface Configuration {

    /**
     * Creates a new configuration {@link Builder} instance.
     *
     * @return a new {@link Builder} instance.
     */
    static Builder builder() {
        return new Builder();
    }

    /**
     * The basic interface for configuration builders.
     *
     * @param <C> the type of configuration
     * @param <B> the type of builder
     */
    interface ConfigBuilder<C extends Configuration, B extends ConfigBuilder<C, B>> {

        /**
         * Associate the given value with the specified key.
         *
         * @param key   the key
         * @param value the value
         * @return this builder object so methods can be chained together; never null
         */
        B with(String key, String value);

        /**
         * Associate the given value with the specified key.
         *
         * @param key   the key
         * @param value the value
         * @return this builder object so methods can be chained together; never null
         */
        default B with(String key, Object value) {
            return with(key, value != null ? value.toString() : null);
        }

        /**
         * Associate the given value with the specified key.
         *
         * @param key   the key
         * @param value the value
         * @return this builder object so methods can be chained together; never null
         */
        default B with(String key, int value) {
            return with(key, Integer.toString(value));
        }

        /**
         * Associate the given value with the specified key.
         *
         * @param key   the key
         * @param value the value
         * @return this builder object so methods can be chained together; never null
         */
        default B with(String key, float value) {
            return with(key, Float.toString(value));
        }

        /**
         * Associate the given value with the specified key.
         *
         * @param key   the key
         * @param value the value
         * @return this builder object so methods can be chained together; never null
         */
        default B with(String key, double value) {
            return with(key, Double.toString(value));
        }

        /**
         * Associate the given value with the specified key.
         *
         * @param key   the key
         * @param value the value
         * @return this builder object so methods can be chained together; never null
         */
        default B with(String key, long value) {
            return with(key, Long.toString(value));
        }

        /**
         * Associate the given value with the specified key.
         *
         * @param key   the key
         * @param value the value
         * @return this builder object so methods can be chained together; never null
         */
        default B with(String key, boolean value) {
            return with(key, Boolean.toString(value));
        }

        /**
         * Associate the given class name value with the specified key.
         *
         * @param key   the key
         * @param value the Class value
         * @return this builder object so methods can be chained together; never null
         */
        default B with(String key, Class<?> value) {
            return with(key, value != null ? value.getName() : null);
        }

        /**
         * If there is no field with the specified key, then associate the given value with the specified key.
         *
         * @param key   the key
         * @param value the value
         * @return this builder object so methods can be chained together; never null
         */
        B withDefault(String key, String value);

        /**
         * If there is no field with the specified key, then associate the given value with the specified key.
         *
         * @param key   the key
         * @param value the value
         * @return this builder object so methods can be chained together; never null
         */
        default B withDefault(String key, int value) {
            return withDefault(key, Integer.toString(value));
        }

        /**
         * If there is no field with the specified key, then associate the given value with the specified key.
         *
         * @param key   the key
         * @param value the value
         * @return this builder object so methods can be chained together; never null
         */
        default B withDefault(String key, float value) {
            return withDefault(key, Float.toString(value));
        }

        /**
         * If there is no field with the specified key, then associate the given value with the specified key.
         *
         * @param key   the key
         * @param value the value
         * @return this builder object so methods can be chained together; never null
         */
        default B withDefault(String key, double value) {
            return withDefault(key, Double.toString(value));
        }

        /**
         * If there is no field with the specified key, then associate the given value with the specified key.
         *
         * @param key   the key
         * @param value the value
         * @return this builder object so methods can be chained together; never null
         */
        default B withDefault(String key, long value) {
            return withDefault(key, Long.toString(value));
        }

        /**
         * If there is no field with the specified key, then associate the given value with the specified key.
         *
         * @param key   the key
         * @param value the value
         * @return this builder object so methods can be chained together; never null
         */
        default B withDefault(String key, boolean value) {
            return withDefault(key, Boolean.toString(value));
        }

        /**
         * If there is no field with the specified key, then associate the given class name value with the specified key.
         *
         * @param key   the key
         * @param value the Class value
         * @return this builder object so methods can be chained together; never null
         */
        default B withDefault(String key, Class<?> value) {
            return withDefault(key, value != null ? value.getName() : null);
        }

        /**
         * Build and return the immutable configuration.
         *
         * @return the immutable configuration; never null
         */
        C build();
    }

    /**
     * A builder of Configuration objects.
     */
    class Builder implements ConfigBuilder<Configuration, Builder> {
        private final Map<String, Object> props = new HashMap<>();

        public Builder() {
        }

        public Builder(final Map<String, Object> props) {
            this.props.putAll(props);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Builder with(String key, String value) {
            props.put(key, value);
            return this;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Builder withDefault(String key, String value) {
            if (!props.containsKey(key)) {
                props.put(key, value);
            }
            return this;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Configuration build() {
            return Configuration.from(props);
        }
    }

    default Builder edit() {
        return new Builder(asMap());
    }

    /**
     * Get the set of keys in this configuration.
     *
     * @return the set of keys; never null but possibly empty
     */
    Set<String> keys();

    boolean hasKey(@NotNull final String key);

    /**
     * Get the string value associated with the given key.
     *
     * @param key the key of the configuration property
     * @return the value, or null if the key is null, or no such property exist in the configuration.
     */
    Object getAny(@NotNull final String key);

    /**
     * Get the string value associated with the given key.
     *
     * @param key the key of the configuration property
     * @return the value, or null if the key is null, or no such property exist in the configuration.
     */
    default String getString(@NotNull final String key) {
        return Optional.ofNullable(getAny(key)).map(Object::toString).orElse(null);
    }

    /**
     * Get the boolean value associated with the given key.
     *
     * @param key the key of the configuration property
     * @return the value, or null if the key is null, or no such property exist in the configuration.
     */
    default Boolean getBoolean(@NotNull final String key) {
        return getBoolean(key, null);
    }

    /**
     * Get the long value associated with the given key.
     *
     * @param key the key of the configuration property
     * @return the value, or null if the key is null, or no such property exist in the configuration.
     */
    default Long getLong(@NotNull final String key) {
        return getLong(key, null);
    }

    /**
     * Get the integer value associated with the given key.
     *
     * @param key the key of the configuration property
     * @return the value, or null if the key is null, or no such property exist in the configuration.
     */
    default Integer getInteger(@NotNull final String key) {
        return getInteger(key, null);
    }

    /**
     * Get the float value associated with the given key.
     *
     * @param key the key of the configuration property
     * @return the value, or null if the key is null, or no such property exist in the configuration.
     */
    default Short getShort(@NotNull final String key) {
        return getShort(key, null);
    }

    /**
     * Get the double value associated with the given key.
     *
     * @param key the key of the configuration property
     * @return the value, or null if the key is null, or no such property exist in the configuration.
     */
    default Double getDouble(@NotNull final String key) {
        return getDouble(key, null);
    }

    /**
     * Get the float value associated with the given key.
     *
     * @param key the key of the configuration property
     * @return the value, or null if the key is null, or no such property exist in the configuration.
     */
    default Float getFloat(@NotNull final String key) {
        return getFloat(key, null);
    }

    /**
     * Get the string list value associated with the given key.
     *
     * @param key the key for the configuration property.
     * @return the configuration value.
     */
    default List<String> getStringList(@NotNull final String key) {
        return getStringList(key, null);
    }

    /**
     * Get the configuration associated with the given key.
     *
     * @param key the key for the configuration property.
     * @return the configuration value.
     */
    default Configuration getConfig(@NotNull final String key) {
        return getConfig(key, null);
    }

    /**
     * Get the configuration associated with the given key.
     *
     * @param key the key for the configuration property.
     * @return the configuration value.
     */
    default List<Configuration> getConfigList(@NotNull final String key) {
        return getConfigList(key, null);
    }


    /**
     * Get the list of classes associated with the given key.
     *
     * @param key the key for the configuration property.
     * @return the configuration value.
     */
    default <T> List<Class<T>> getClassList(@NotNull final String key) {
        return getClassList(key, null);
    }

    /**
     * Get the string value associated with the given key.
     *
     * @param key          the key for the configuration property.
     * @param defaultValue the default value; may be null
     * @return the configuration value.
     */
    default String getString(@NotNull final String key,
                             @Nullable String defaultValue) {
        return getString(key, () -> defaultValue);
    }

    /**
     * Get the boolean value associated with the given key.
     *
     * @param key          the key for the configuration property.
     * @param defaultValue the default value; may be null
     * @return the configuration value.
     */
    default boolean getBoolean(@NotNull final String key,
                               boolean defaultValue) {
        return getBoolean(key, () -> defaultValue);
    }

    /**
     * Get the long value associated with the given key.
     *
     * @param key          the key for the configuration property.
     * @param defaultValue the default value; may be null
     * @return the configuration value.
     */
    default long getLong(@NotNull final String key, long defaultValue) {
        return getLong(key, () -> defaultValue);
    }

    /**
     * Get the integer value associated with the given key.
     *
     * @param key          the key for the configuration property.
     * @param defaultValue the default value; may be null
     * @return the configuration value.
     */
    default int getInteger(@NotNull final String key, int defaultValue) {
        return getInteger(key, () -> defaultValue);
    }

    /**
     * Get the float value associated with the given key.
     *
     * @param key          the key for the configuration property.
     * @param defaultValue the default value; may be null
     * @return the configuration value.
     */
    default short getShort(@NotNull final String key, short defaultValue) {
        return getShort(key, () -> defaultValue);
    }

    /**
     * Get the double value associated with the given key.
     *
     * @param key          the key for the configuration property.
     * @param defaultValue the default value; may be null
     * @return the configuration value.
     */
    default double getDouble(@NotNull final String key, double defaultValue) {
        return getDouble(key, () -> defaultValue);
    }

    /**
     * Get the float value associated with the given key.
     *
     * @param key          the key for the configuration property.
     * @param defaultValue the default value; may be null
     * @return the configuration value.
     */
    default float getFloat(@NotNull final String key, float defaultValue) {
        return getFloat(key, () -> defaultValue);
    }

    /**
     * Find the string value associated with the given key.
     *
     * @param key                  the key for the configuration property.
     * @param defaultValueSupplier the supplier for the default value; may be null
     * @return the configuration value.
     */
    default String getString(@NotNull final String key,
                             @Nullable final Supplier<String> defaultValueSupplier) {
        String value = getString(key);
        return value != null ? value : (defaultValueSupplier != null ? defaultValueSupplier.get() : null);
    }

    /**
     * Find the boolean value associated with the given key.
     *
     * @param key                  the key for the configuration property.
     * @param defaultValueSupplier the supplier for the default value; may be null
     * @return the configuration value.
     */
    default Boolean getBoolean(@NotNull final String key,
                               @Nullable final BooleanSupplier defaultValueSupplier) {
        return findString(key)
                .map(TypeConverter::getBool)
                .orElseGet(() -> defaultValueSupplier != null ? defaultValueSupplier.getAsBoolean() : null);
    }

    /**
     * Find the long value associated with the given key.
     *
     * @param key                  the key for the configuration property.
     * @param defaultValueSupplier the supplier for the default value; may be null
     * @return the configuration value.
     */
    default Long getLong(@NotNull final String key,
                         @Nullable final LongSupplier defaultValueSupplier) {
        return findString(key)
                .map(TypeConverter::getLong)
                .orElseGet(() -> defaultValueSupplier != null ? defaultValueSupplier.getAsLong() : null);
    }

    /**
     * Find the integer value associated with the given key.
     *
     * @param key                  the key for the configuration property.
     * @param defaultValueSupplier the supplier for the default value; may be null
     * @return the configuration value.
     */
    default Integer getInteger(@NotNull final String key,
                               @Nullable final IntSupplier defaultValueSupplier) {
        return findString(key)
                .map(TypeConverter::getInt)
                .orElseGet(() -> defaultValueSupplier != null ? defaultValueSupplier.getAsInt() : null);
    }

    /**
     * Get the short value associated with the given key.
     *
     * @param key                  the key for the configuration property.
     * @param defaultValueSupplier the supplier for the default value; may be null
     * @return the configuration value.
     */
    default Short getShort(@NotNull final String key,
                           @Nullable final Supplier<Short> defaultValueSupplier) {
        return findString(key)
                .map(TypeConverter::getShort)
                .orElseGet(() -> defaultValueSupplier != null ? defaultValueSupplier.get() : null);
    }

    /**
     * Get the string list value associated with the given key.
     *
     * @param key                  the key for the configuration property.
     * @param defaultValueSupplier the supplier for the default value; may be null
     * @return the configuration value.
     */
    default List<String> getStringList(@NotNull final String key,
                                       @Nullable final Supplier<List<String>> defaultValueSupplier) {
        Object object = getAny(key);
        if (object != null) {
            return (List<String>) TypeConverter.getList(object, true);
        }
        return defaultValueSupplier != null ? defaultValueSupplier.get() : null;
    }

    /**
     * Get the list of classes associated with the given key.
     *
     * @param key                  the key for the configuration property.
     * @param defaultValueSupplier the supplier for the default value; may be null
     * @return the configuration value.
     */
    default <T> List<Class<T>> getClassList(@NotNull final String key,
                                            @Nullable final Supplier<List<Class<T>>> defaultValueSupplier) {
        Optional<List<String>> stringList = findStringList(key);
        return stringList.map(strings -> strings.stream()
                        .map(it -> (Class<T>) Classes.forName(it))
                        .toList())
                .orElse(Optional.ofNullable(defaultValueSupplier).map(Supplier::get).orElse(null));
    }

    /**
     * Find the integer value associated with the given key.
     *
     * @param key                  the key for the configuration property.
     * @param defaultValueSupplier the supplier for the default value; may be null
     * @return the configuration value.
     */
    default Float getFloat(@NotNull final String key,
                           @Nullable final Supplier<Float> defaultValueSupplier) {
        Optional<String> value = findString(key);
        if (value.isPresent()) {
            return TypeConverter.getFloat(value);
        }
        return defaultValueSupplier != null ? defaultValueSupplier.get() : null;
    }

    /**
     * Find the double value associated with the given key.
     *
     * @param key                  the key for the configuration property.
     * @param defaultValueSupplier the supplier for the default value; may be null
     * @return the configuration value.
     */
    default Double getDouble(@NotNull final String key,
                             @Nullable final DoubleSupplier defaultValueSupplier) {
        Optional<String> value = findString(key);
        if (value.isPresent()) {
            return TypeConverter.getDouble(value);
        }
        return defaultValueSupplier != null ? defaultValueSupplier.getAsDouble() : null;
    }

    /**
     * Get the configuration associated with the given key.
     *
     * @param key                  the key for the configuration property.
     * @param defaultValueSupplier the supplier for the default value; may be null
     * @return the configuration value.
     */
    default Configuration getConfig(@NotNull final String key,
                                    @Nullable final Supplier<Configuration> defaultValueSupplier) {
        Object value = getAny(key);
        if (value != null) {
            if (value instanceof Configuration config) {
                return config;
            }
            if (value instanceof Map map) {
                return Configuration.from((Map<String, Object>) map);
            }
        }

        return defaultValueSupplier != null ? defaultValueSupplier.get() : null;
    }

    /**
     * Get the configuration associated with the given key.
     *
     * @param key                  the key for the configuration property.
     * @param defaultValueSupplier the supplier for the default value; may be null
     * @return the configuration value.
     */
    List<Configuration> getConfigList(@NotNull final String key,
                                      @Nullable final Supplier<List<Configuration>> defaultValueSupplier);

    /**
     * Finds the boolean configuration value for the specified config key.
     *
     * @param key the key for the configuration property.
     * @return the {@link java.util.Optional} configuration value.
     */
    default Optional<Boolean> findBoolean(@NotNull final String key) {
        return Optional.ofNullable(getBoolean(key));
    }

    /**
     * Finds the boolean configuration value for the specified config key.
     *
     * @param key the key for the configuration property.
     * @return the {@link java.util.Optional} configuration value.
     */
    default Optional<String> findString(@NotNull final String key) {
        return Optional.ofNullable(getString(key));
    }

    /**
     * Find the boolean value associated with the given key.
     *
     * @param key the key for the configuration property.
     * @return the {@link java.util.Optional} configuration value.
     */
    default Optional<Long> findLong(@NotNull final String key) {
        return Optional.ofNullable(getLong(key));
    }

    /**
     * Find the integer value associated with the given key.
     *
     * @param key the key for the configuration property.
     * @return the {@link java.util.Optional} configuration value.
     */
    default Optional<Integer> findInteger(@NotNull final String key) {
        return Optional.ofNullable(getInteger(key));
    }

    /**
     * Find the short value associated with the given key.
     *
     * @param key the key for the configuration property.
     * @return the {@link java.util.Optional} configuration value.
     */
    default Optional<Short> findShort(@NotNull final String key) {
        return Optional.ofNullable(getShort(key));
    }

    /**
     * Find the float value associated with the given key.
     *
     * @param key the key for the configuration property.
     * @return the {@link java.util.Optional} configuration value.
     */
    default Optional<Float> findFloat(@NotNull final String key) {
        return Optional.ofNullable(getFloat(key));
    }

    /**
     * Find the double value associated with the given key.
     *
     * @param key the key for the configuration property.
     * @return the {@link java.util.Optional} configuration value.
     */
    default Optional<Double> findDouble(@NotNull final String key) {
        return Optional.ofNullable(getDouble(key));
    }

    /**
     * Finds the list configuration value for the specified config key.
     *
     * @param key the key for the configuration property.
     * @return the {@link java.util.Optional} configuration value.
     */
    default Optional<List<String>> findStringList(@NotNull final String key) {
        return Optional.ofNullable(getStringList(key));
    }

    /**
     * Finds the map configuration value for the specified config key.
     *
     * @param key the key for the configuration property.
     * @return the {@link java.util.Optional} configuration value.
     */
    default Optional<Configuration> findConfig(@NotNull final String key) {
        return Optional.ofNullable(getConfig(key));
    }

    /**
     * Finds the map configuration value for the specified config key.
     *
     * @param key the key for the configuration property.
     * @return the {@link java.util.Optional} configuration value.
     */
    default Optional<List<Configuration>> findConfigList(@NotNull final String key) {
        return Optional.ofNullable(getConfigList(key));
    }

    /**
     * Finds the list configuration value for the specified config key.
     *
     * @param key the key for the configuration property.
     * @return the {@link java.util.Optional} configuration value.
     */
    default <T> Optional<List<Class<T>>> findClassList(@NotNull final String key) {
        return Optional.ofNullable(getClassList(key));
    }

    default String toPrettyString() {
        return toPrettyString("\n");
    }

    default String toPrettyString(String delimiter) {
        Map<String, Object> confAsMap = new TreeMap<>(asMap());
        return confAsMap.entrySet()
                .stream()
                .map(e -> e.getKey() + " = " + e.getValue())
                .collect(Collectors.joining(delimiter));
    }

    /**
     * @param other – a configuration whose keys should be used as fallbacks, if the keys are not present in this one
     * @return a new configuration, or this original one, if the fallback does not get used.
     */
    Configuration withFallback(@NotNull final Configuration other);

    default Map<String, Object> asMap() {
        return asMap(true);
    }

    Map<String, Object> asMap(boolean flatten);


    /**
     * Static helper method to create a new empty configuration.
     * 
     * @return  a new {@link Configuration}.
     */
    static Configuration empty() {
        return from(Collections.emptyMap());
    }

    static Configuration of(final String k1, final Object v1) {
        return from(Map.of(k1, v1));
    }

    static Configuration of(final String k1, final Object v1,
                            final String k2, final Object v2) {
        return from(Map.of(k1, v1, k2, v2));
    }

    static Configuration of(final String k1, final Object v1,
                            final String k2, final Object v2,
                            final String k3, final Object v3) {
        return from(Map.of(k1, v1, k2, v2, k3, v3));
    }

    /**
     * Static helper method to create a new a configuration from the specified properties.
     *
     * @param properties the map configuration.
     * @return a new {@link Configuration}.
     */
    static Configuration from(Properties properties) {
        return from(PropertiesUtils.toMap(properties));
    }

    /**
     * Static helper method to create a new a configuration from the specified map.
     *
     * @param configMap the map configuration.
     * @return a new {@link Configuration}.
     */
    static Configuration from(final Map<String, ?> configMap) {
        return JikkouConfig.create(configMap, false);
    }
}
