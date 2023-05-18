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
package io.streamthoughts.jikkou.api.config;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represent a configuration key/value property.
 *
 * @param <T> type of the property.
 */
public final class ConfigProperty<T> {

    /**
     * Static helper method to create a new {@link ConfigProperty} with an expected {@link Integer} value.
     *
     * @param path the option string path.
     * @return a new {@link ConfigProperty}.
     */
    public static ConfigProperty<Integer> ofInt(final @NotNull String path) {
        return new ConfigProperty<>(path, (p, config) -> config.findInteger(p));
    }

    /**
     * Static helper method to create a new {@link ConfigProperty} with an expected {@link Long} value.
     *
     * @param key the option string key.
     * @return a new {@link ConfigProperty}.
     */
    public static ConfigProperty<Long> ofLong(final @NotNull String key) {
        return new ConfigProperty<>(key, (p, config) -> config.findLong(p));
    }

    /**
     * Static helper method to create a new {@link ConfigProperty} with an expected {@link String} value.
     *
     * @param key the option string key.
     * @return a new {@link ConfigProperty}.
     */
    public static ConfigProperty<String> ofString(final @NotNull String key) {
        return new ConfigProperty<>(key, (p, config) -> config.findString(p));
    }

    /**
     * Static helper method to create a new {@link ConfigProperty} with an expected {@link Boolean} value.
     *
     * @param key the option string key.
     * @return a new {@link ConfigProperty}.
     */
    public static ConfigProperty<Boolean> ofBoolean(final @NotNull String key) {
        return new ConfigProperty<>(key, (p, config) -> config.findBoolean(p));
    }

    /**
     * Static helper method to create a new {@link ConfigProperty} with an expected {@link Map} value.
     *
     * @param key the option string key.
     * @return a new {@link ConfigProperty}.
     */
    public static ConfigProperty<Map<String, Object>> ofMap(final @NotNull String key) {
        return new ConfigProperty<>(key, (p, config) -> config.findConfig(p).map(Configuration::asMap));
    }

    /**
     * Static helper method to create a new {@link ConfigProperty} with an expected {@link List} of string values.
     *
     * @param key the option string key.
     * @return a new {@link ConfigProperty}.
     */
    public static ConfigProperty<List<String>> ofList(final @NotNull String key) {
        return new ConfigProperty<>(key, (p, config) -> config.findStringList(p));
    }

    /**
     * Static helper method to create a new {@link ConfigProperty} with an expected {@link List} of classes.
     *
     * @param key the option string key.
     * @return a new {@link ConfigProperty}.
     */
    public static <T> ConfigProperty<List<Class<T>>> ofClasses(final @NotNull String key) {
        return new ConfigProperty<>(key, (p, config) -> config.findClassList(p));
    }

    /**
     * Static helper method to create a new {@link ConfigProperty} with an expected {@link List} of {@link Configuration}.
     *
     * @param path the option string path.
     * @return a new {@link ConfigProperty}.
     */
    public static ConfigProperty<List<Configuration>> ofConfigList(final @NotNull String path) {
        BiFunction<String, Configuration, Optional<List<Configuration>>> supplier = (p, config) -> config.findConfigList(p);
        return new ConfigProperty<>(path, supplier);
    }

    private final String key;

    private final String description;

    private final Supplier<? extends T> defaultValueSupplier;

    public final BiFunction<String, Configuration, Optional<T>> accessor;

    /**
     * Creates a new {@link ConfigProperty} instance.
     *
     * @param key the key of the configuration property.
     */
    public ConfigProperty(final @NotNull String key,
                          final @NotNull BiFunction<String, Configuration, Optional<T>> valueSupplier) {
        this(key, valueSupplier, null, null);
    }

    public Configuration asConfiguration(final Object value) {
        return Configuration.of(key, value);
    }

    /**
     * Creates a new {@link ConfigProperty} instance.
     *
     * @param key                  the key of the configuration property.
     * @param accessor             the function for accessing the value from a configuration.
     * @param defaultValueSupplier the supplier for the default value; may be null
     * @param description          the option default value.
     */
    public ConfigProperty(final @NotNull String key,
                          final @NotNull BiFunction<String, Configuration, Optional<T>> accessor,
                          final @Nullable Supplier<? extends T> defaultValueSupplier,
                          final @Nullable String description) {
        this.key = Objects.requireNonNull(key, "key cannot be null");
        this.accessor = accessor;
        this.defaultValueSupplier = defaultValueSupplier;
        this.description = description;
    }

    public ConfigProperty<T> orElse(final @NotNull T other) {
        return orElse(() -> other);
    }

    public ConfigProperty<T> orElse(final @NotNull Supplier<? extends T> other) {
        return new ConfigProperty<>(key, accessor, other, description);
    }

    /**
     * Maps the underlying value to a different component type.
     *
     * @param mapper A mapper
     * @param <U>    The new component type
     * @return A new value
     */
    public <U> ConfigProperty<U> map(Function<? super T, ? extends U> mapper) {

        Supplier<? extends U> defaultValueSupplierWithMapper = () -> Optional
                .ofNullable(defaultValueSupplier)
                .flatMap(supplier -> Optional.ofNullable(supplier.get()).map(mapper))
                .orElse(null);

        return new ConfigProperty<>(
                key,
                (p, config) -> accessor.apply(p, config).map(mapper),
                defaultValueSupplierWithMapper,
                description
        );
    }

    public T orElseGet(final @NotNull Configuration config,
                       final @NotNull Supplier<? extends T> other) {
        return orElse(other).evaluate(config);
    }

    /**
     * Evaluate this configuration property against the given configuration.
     *
     * @return the value for this param from the given {@link Configuration}.
     * @throws NoSuchElementException if this property does not exist in the given configuration.
     */
    public T evaluate(final @NotNull Configuration config) {
        Optional<T> option = getOptional(config);
        return option.orElseThrow(() -> new NoSuchElementException("No value present for param '" + key + "'"));
    }

    /**
     * @return the value for this param from the given {@link Configuration}.
     */
    public Optional<T> getOptional(final @NotNull Configuration config) {
        return accessor
                .apply(key, config)
                .or(() -> Optional.ofNullable(defaultValueSupplier).map(Supplier::get));
    }

    /**
     * Get the key of this property.
     *
     * @return the key for this property ; never null.
     */
    public String key() {
        return key;
    }

    public Supplier<? extends T> defaultValueSupplier() {
        return Optional.ofNullable(defaultValueSupplier).orElse(() -> null);
    }

    /**
     * Get the default value for this config property.
     *
     * @return the default value, or {@code null} if no supplier was provided for default value.
     */
    public T defaultValue() {
        return Optional.ofNullable(defaultValueSupplier).map(Supplier::get).orElse(null);
    }

    /** {@inheritDoc} **/
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConfigProperty<?> that = (ConfigProperty<?>) o;
        return key.equals(that.key) && Objects.equals(description, that.description);
    }

    /** {@inheritDoc} **/
    @Override
    public int hashCode() {
        return Objects.hash(key, description);
    }
}
