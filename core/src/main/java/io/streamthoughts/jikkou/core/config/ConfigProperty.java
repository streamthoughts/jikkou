/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.config;

import io.streamthoughts.jikkou.common.utils.Enums;
import io.streamthoughts.jikkou.core.config.internals.Type;
import io.streamthoughts.jikkou.core.data.TypeConverter;
import io.streamthoughts.jikkou.core.models.NamedValue;
import java.util.*;
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
     * Static helper method to create a new {@link ConfigProperty} for the specified type and path.
     *
     * @param path the option string path.
     * @return a new {@link ConfigProperty}.
     * @throws IllegalArgumentException if the given type is unsupported by this method.
     */
    @SuppressWarnings("rawtypes")
    public static ConfigProperty of(final @NotNull Type type,
                                    final @NotNull String path) {
        return switch (type) {
            case SHORT -> ConfigProperty.ofShort(path);
            case INTEGER -> ConfigProperty.ofInt(path);
            case LONG -> ConfigProperty.ofLong(path);
            case FLOAT -> ConfigProperty.ofFloat(path);
            case DOUBLE -> ConfigProperty.ofDouble(path);
            case BOOLEAN -> ConfigProperty.ofBoolean(path);
            case STRING -> ConfigProperty.ofString(path);
            case LIST -> ConfigProperty.ofList(path);
            case MAP -> ConfigProperty.ofMap(path);
            case BYTES -> throw new IllegalArgumentException("Unsupported type: " + type);
        };
    }

    /**
     * Static helper method to create a new {@link ConfigProperty} with a convertible value.
     *
     * @param path      the option string path.
     * @param converter the type converter.
     * @return a new {@link ConfigProperty}.
     */
    public static <T> ConfigProperty<T> of(final @NotNull String path,
                                           final @NotNull TypeConverter<T> converter) {
        return new ConfigProperty<>(path, null, (p, configuration) -> configuration.findAny(p).map(converter::convertValue));
    }

    /**
     * Static helper method to create a new {@link ConfigProperty} with an expected {@link Enum} value.
     *
     * @param path     the option string path.
     * @param enumType the enum type.
     * @return a new {@link ConfigProperty}.
     */
    public static <T extends Enum<T>> ConfigProperty<T> ofEnum(final @NotNull String path,
                                                               final @NotNull Class<T> enumType) {
        return new ConfigProperty<>(path, enumType, (p, config) -> config.findString(p).map(val -> Enums.getForNameIgnoreCase(val, enumType)));
    }

    /**
     * Static helper method to create a new {@link ConfigProperty} with an expected {@link Short} value.
     *
     * @param path the option string path.
     * @return a new {@link ConfigProperty}.
     */
    public static ConfigProperty<Short> ofShort(final @NotNull String path) {
        return new ConfigProperty<>(path, Short.class, (p, config) -> config.findShort(p));
    }

    /**
     * Static helper method to create a new {@link ConfigProperty} with an expected {@link Integer} value.
     *
     * @param path the option string path.
     * @return a new {@link ConfigProperty}.
     */
    public static ConfigProperty<Integer> ofInt(final @NotNull String path) {
        return new ConfigProperty<>(path, Integer.class, (p, config) -> config.findInteger(p));
    }

    /**
     * Static helper method to create a new {@link ConfigProperty} with an expected {@link Float} value.
     *
     * @param path the option string path.
     * @return a new {@link ConfigProperty}.
     */
    public static ConfigProperty<Float> ofFloat(final @NotNull String path) {
        return new ConfigProperty<>(path, Float.class, (p, config) -> config.findFloat(p));
    }

    /**
     * Static helper method to create a new {@link ConfigProperty} with an expected {@link Long} value.
     *
     * @param key the option string key.
     * @return a new {@link ConfigProperty}.
     */
    public static ConfigProperty<Long> ofLong(final @NotNull String key) {
        return new ConfigProperty<>(key, Long.class, (p, config) -> config.findLong(p));
    }

    /**
     * Static helper method to create a new {@link ConfigProperty} with an expected {@link Double} value.
     *
     * @param key the option string key.
     * @return a new {@link ConfigProperty}.
     */
    public static ConfigProperty<Double> ofDouble(final @NotNull String key) {
        return new ConfigProperty<>(key, Double.class, (p, config) -> config.findDouble(p));
    }

    /**
     * Static helper method to create a new {@link ConfigProperty} with an expected {@link String} value.
     *
     * @param key the option string key.
     * @return a new {@link ConfigProperty}.
     */
    public static ConfigProperty<String> ofString(final @NotNull String key) {
        return new ConfigProperty<>(key, String.class, (p, config) -> config.findString(p));
    }

    /**
     * Static helper method to create a new {@link ConfigProperty} with an expected {@link Boolean} value.
     *
     * @param key the option string key.
     * @return a new {@link ConfigProperty}.
     */
    public static ConfigProperty<Boolean> ofBoolean(final @NotNull String key) {
        return new ConfigProperty<>(key, Boolean.class, (p, config) -> config.findBoolean(p));
    }

    /**
     * Static helper method to create a new {@link ConfigProperty} with an expected {@link Map} value.
     *
     * @param key the option string key.
     * @return a new {@link ConfigProperty}.
     */
    public static ConfigProperty<Map<String, Object>> ofMap(final @NotNull String key) {
        return new ConfigProperty<>(key, Map.class, (p, config) -> config.findConfig(p).map(Configuration::asMap));
    }

    /**
     * Static helper method to create a new {@link ConfigProperty} with an expected {@link List} of string values.
     *
     * @param key the option string key.
     * @return a new {@link ConfigProperty}.
     */
    public static ConfigProperty<List<String>> ofList(final @NotNull String key) {
        return new ConfigProperty<>(key, List.class, (p, config) -> config.findStringList(p));
    }

    /**
     * Static helper method to create a new {@link ConfigProperty} with an expected {@link Set} value.
     *
     * @param key the option string key.
     * @return a new {@link ConfigProperty}.
     */
    public static ConfigProperty<Object> ofAny(final @NotNull String key) {
        return new ConfigProperty<>(key, Object.class, (p, config) -> config.findAny(p));
    }

    /**
     * Static helper method to create a new {@link ConfigProperty} with an expected {@link List} of classes.
     *
     * @param key the option string key.
     * @return a new {@link ConfigProperty}.
     */
    public static <T> ConfigProperty<List<Class<T>>> ofClasses(final @NotNull String key) {
        return new ConfigProperty<>(key, List.class, (p, config) -> config.findClassList(p));
    }

    /**
     * Static helper method to create a new {@link ConfigProperty} with an expected {@link Configuration} value
     *
     * @param path the option string path.
     * @return a new {@link ConfigProperty}.
     */
    public static ConfigProperty<Configuration> ofConfig(final @NotNull String path) {
        return new ConfigProperty<>(path, Configuration.class, (p, config) -> config.findConfig(p));
    }

    /**
     * Static helper method to create a new {@link ConfigProperty} with an expected {@link List} of {@link Configuration}.
     *
     * @param path the option string path.
     * @return a new {@link ConfigProperty}.
     */
    public static ConfigProperty<List<Configuration>> ofConfigList(final @NotNull String path) {
        return new ConfigProperty<>(path, List.class, (p, config) -> config.findConfigList(p));
    }

    private final String key;

    private final String description;

    private final Supplier<? extends T> defaultValueSupplier;

    private final boolean isRequired;

    private final Class<?> rawType;

    public final BiFunction<String, Configuration, Optional<T>> accessor;

    /**
     * Creates a new {@link ConfigProperty} instance.
     *
     * @param key the key of the configuration property.
     */
    public ConfigProperty(final @NotNull String key,
                          final Class<?> rawType,
                          final @NotNull BiFunction<String, Configuration, Optional<T>> valueSupplier) {
        this(key, rawType, valueSupplier, null, null, false);
    }

    public Configuration asConfiguration(final Object value) {
        return Configuration.of(key, value);
    }

    public NamedValue asValue(final Object value) {
        return new NamedValue(key, value);
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
                          final Class<?> rawType,
                          final @NotNull BiFunction<String, Configuration, Optional<T>> accessor,
                          final @Nullable Supplier<? extends T> defaultValueSupplier,
                          final @Nullable String description,
                          final boolean isRequired) {
        this.key = Objects.requireNonNull(key, "key cannot be null");
        this.rawType = Objects.requireNonNull(rawType, "rawType cannot be null");
        this.accessor = accessor;
        this.defaultValueSupplier = defaultValueSupplier;
        this.description = description;
        this.isRequired = isRequired;
    }

    public ConfigProperty<T> description(final String description) {
        return new ConfigProperty<>(key, rawType, accessor, defaultValueSupplier, description, isRequired);
    }

    public ConfigProperty<T> required(boolean isRequired) {
        return new ConfigProperty<>(key, rawType, accessor, defaultValueSupplier, description, isRequired);
    }

    public ConfigProperty<T> defaultValue(final T other) {
        return defaultValue(() -> other);
    }

    public ConfigProperty<T> defaultValue(final @NotNull Supplier<? extends T> other) {
        return new ConfigProperty<>(key, rawType, accessor, other, description, isRequired);
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
                rawType,
                (p, config) -> accessor.apply(p, config).map(mapper),
                defaultValueSupplierWithMapper,
                description,
                isRequired
        );
    }

    public <U> ConfigProperty<U> convert(final TypeConverter<U> converter) {
        return map(converter::convertValue);
    }

    public T orElseGet(final @NotNull Configuration config,
                       final @NotNull Supplier<? extends T> other) {
        return defaultValue(other).get(config);
    }

    /**
     * Evaluate this configuration property against the given configuration.
     *
     * @return the value for this param from the given {@link Configuration}.
     * @throws NoSuchElementException if this property does not exist in the given configuration.
     */
    public T get(final @NotNull Configuration config) {
        Optional<T> option = getOptional(config);
        return isRequired ? option.orElseThrow(() -> new ConfigException.Missing(this)) : option.orElse(null);
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
     * Gets the key of this property.
     *
     * @return the key for this property ; never null.
     */
    public String key() {
        return key;
    }

    /**
     * Gets the description of this property.
     *
     * @return the description string, or {@code null} if no description was set.
     */
    public String description() {
        return description;
    }

    /**
     * Gets the description of this property.
     *
     * @return the description string, or {@code null} if no description was set.
     */
    public boolean required() {
        return isRequired;
    }

    /**
     * Gets the raw-type of this property.
     *
     * @return the raw-type class.
     */
    public Class<?> rawType() {
        return rawType;
    }

    /**
     * Gets the default value for this config property.
     *
     * @return the default value, or {@code null} if no supplier was provided for default value.
     */
    public T defaultValue() {
        return Optional.ofNullable(defaultValueSupplier).map(Supplier::get).orElse(null);
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConfigProperty<?> that = (ConfigProperty<?>) o;
        return key.equals(that.key) && Objects.equals(description, that.description);
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public int hashCode() {
        return Objects.hash(key, description);
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public String toString() {
        return "ConfigProperty[" +
                "key=" + key +
                ", description=" + description +
                ']';
    }
}
