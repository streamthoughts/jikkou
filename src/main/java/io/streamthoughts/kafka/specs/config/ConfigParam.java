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
package io.streamthoughts.kafka.specs.config;

import io.vavr.Lazy;
import io.vavr.control.Option;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Represent a configuration param.
 *
 * @param <T> param type.
 * @see JikkouConfig
 * @see JikkouParams
 */
public class ConfigParam<T> {

    /**
     * Static helper method to create a new {@link ConfigParam} with an expected {@link Integer} value.
     *
     * @param path the option string path.
     * @return a new {@link ConfigParam}.
     */
    public static ConfigParam<Integer> ofInt(final @NotNull String path) {
        return new ConfigParam<>(path, (p, config) -> config.findInt(p));
    }

    /**
     * Static helper method to create a new {@link ConfigParam} with an expected {@link Long} value.
     *
     * @param path the option string path.
     * @return a new {@link ConfigParam}.
     */
    public static ConfigParam<Long> ofLong(final @NotNull String path) {
        return new ConfigParam<>(path, (p, config) -> config.findLong(p));
    }

    /**
     * Static helper method to create a new {@link ConfigParam} with an expected {@link String} value.
     *
     * @param path the option string path.
     * @return a new {@link ConfigParam}.
     */
    public static ConfigParam<String> ofString(final @NotNull String path) {
        return new ConfigParam<>(path, (p, config) -> config.findString(p));
    }

    /**
     * Static helper method to create a new {@link ConfigParam} with an expected {@link Boolean} value.
     *
     * @param path the option string path.
     * @return a new {@link ConfigParam}.
     */
    public static ConfigParam<Boolean> ofBoolean(final @NotNull String path) {
        return new ConfigParam<>(path, (p, config) -> config.findBoolean(p));
    }

    /**
     * Static helper method to create a new {@link ConfigParam} with an expected {@link Map} value.
     *
     * @param path the option string path.
     * @return a new {@link ConfigParam}.
     */
    public static ConfigParam<Map<String, Object>> ofMap(final @NotNull String path) {
        return new ConfigParam<>(path, (p, config) -> config.findConfigAsMap(p));
    }

    /**
     * Static helper method to create a new {@link ConfigParam} with an expected {@link List} of string values.
     *
     * @param path the option string path.
     * @return a new {@link ConfigParam}.
     */
    public static ConfigParam<List<String>> ofList(final @NotNull String path) {
        return new ConfigParam<>(path, (p, config) -> config.findStringList(p));
    }

    /**
     * Static helper method to create a new {@link ConfigParam} with an expected {@link List} of classes.
     *
     * @param path the option string path.
     * @return a new {@link ConfigParam}.
     */
    public static <T> ConfigParam<List<Class<T>>> ofClasses(final @NotNull String path) {
        return new ConfigParam<>(path, (p, config) -> config.findClassList(p));
    }

    private final String path;
    private final Option<Lazy<T>> defaultValue;

    public final BiFunction<String, JikkouConfig, Option<T>> supplier;

    /**
     * Creates a new {@link ConfigParam} instance.
     *
     * @param path the option string path.
     */
    public ConfigParam(final @NotNull String path,
                       final @NotNull BiFunction<String, JikkouConfig, Option<T>> supplier) {
        this(path, Option.none(), supplier);
    }

    /**
     * Creates a new {@link ConfigParam} instance.
     *
     * @param path         the option string path.
     * @param defaultValue the option default value.
     */
    public ConfigParam(final @NotNull String path,
                       final @NotNull Lazy<T> defaultValue,
                       final @NotNull BiFunction<String, JikkouConfig, Option<T>> supplier) {
        this(path, Option.of(defaultValue), supplier);
    }

    /**
     * Creates a new {@link ConfigParam} instance.
     *
     * @param path         the option string path.
     * @param defaultValue the option default value.
     */
    private ConfigParam(final @NotNull String path,
                       final @NotNull Option<Lazy<T>> defaultValue,
                       final @NotNull BiFunction<String, JikkouConfig, Option<T>> supplier) {
        this.path = Objects.requireNonNull(path, "'path cannot be null'");
        this.defaultValue = defaultValue;
        this.supplier = supplier;
    }

    public ConfigParam<T> orElse(final @NotNull T other) {
        return orElse(() -> other);
    }


    public ConfigParam<T> orElse(final @NotNull Supplier<? extends T> other) {
        return new ConfigParam<>(path, Lazy.of(other), supplier);
    }

    /**
     * Maps the underlying value to a different component type.
     *
     * @param mapper A mapper
     * @param <U>    The new component type
     * @return A new value
     */
    public <U> ConfigParam<U> map(Function<? super T, ? extends U> mapper) {
        return new ConfigParam<>(
            path, defaultValue.map(it -> it.map(mapper)),
            (p, config) -> supplier.apply(p, config).map(mapper)
        );
    }

    public T orElseGet(final @NotNull JikkouConfig config,
                       final @NotNull Supplier<? extends T> other) {
        return orElse(other).get(config);
    }

    /**
     * @return the value for this param from the given {@link JikkouConfig}.
     */
    public T get(final @NotNull JikkouConfig config) {
        return getOption(config).get();
    }

    /**
     * @return the value for this param from the given {@link JikkouConfig}.
     */
    public Option<T> getOption(final @NotNull JikkouConfig config) {
        return supplier.apply(path, config).orElse(defaultValue.map(Lazy::get));
    }

    public String path() {
        return path;
    }

    public Lazy<T> defaultValue() {
        return defaultValue.getOrElse(Lazy.of( () -> (T) null));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ConfigParam)) return false;
        ConfigParam<?> that = (ConfigParam<?>) o;
        return Objects.equals(path, that.path) &&
                Objects.equals(defaultValue, that.defaultValue);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Objects.hash(path, defaultValue);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "[" + "path=" + path + ", defaultValue=" + defaultValue + ']';
    }
}
