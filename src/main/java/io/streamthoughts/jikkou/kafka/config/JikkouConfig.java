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
package io.streamthoughts.jikkou.kafka.config;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigMergeable;
import com.typesafe.config.ConfigParseOptions;
import io.streamthoughts.jikkou.kafka.internal.ClassUtils;
import io.vavr.control.Option;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * The default configuration class backed by the Typesafe {@link Config}.
 */
public final class JikkouConfig {

    private static final Logger LOG = LoggerFactory.getLogger(JikkouConfig.class);

    private static final String DEFAULT_CONFIG = "application.conf";
    private static final String ROOT_CONFIG_KEY = "jikkou";

    private static JikkouConfig CACHED;

    private final Config config;

    /**
     * Static helper that can be used to create a new empty {@link JikkouConfig} instance.
     *
     * @return a new {@link JikkouConfig} instance.
     */
    public static JikkouConfig empty() {
        return new JikkouConfig(ConfigFactory.empty(), false);
    }

    /**
     * Static helper that can be used to create a new {@link JikkouConfig} instance
     * from the specified properties.
     *
     * @return a new {@link JikkouConfig} instance.
     */
    public static JikkouConfig create(final Properties config) {
        return new JikkouConfig(ConfigFactory.parseProperties(config, ConfigParseOptions.defaults()));
    }

    /**
     * Static helper that can be used to create a new {@link JikkouConfig} instance
     * from the specified map.
     *
     * @return a new {@link JikkouConfig} instance.
     */
    public static JikkouConfig create(final Map<String, Object> config) {
        return new JikkouConfig(ConfigFactory.parseMap(config));
    }

    /**
     * Retrieves the global static configuration.
     *
     * @return the {@link JikkouConfig} configuration object.
     * @throws IllegalStateException if no configuration was initialized.
     */
    public static @NotNull JikkouConfig get() {
        if (CACHED != null) return CACHED;
        throw new IllegalStateException("No configuration was initialized");
    }

    /**
     * Static helper method to load the default application's configuration.
     *
     * <p>
     * This method loads the following (first-listed are higher priority):
     * <ul>
     * <li>system properties
     * <li>./application.config
     * <li>$USER_HOME/./jikkou/application.config
     * <li>application.conf (all resources on classpath with this name)
     * <li>application.json (all resources on classpath with this name)
     * <li>application.properties (all resources on classpath with this name)
     * <li>reference.conf (all resources on classpath with this name)
     * </ul>
     *
     * @return a new {@link JikkouConfig}
     */
    public static JikkouConfig load() {
        return load(null, null);
    }

    /**
     * Like {@link #load()} but allows overriding some config properties.
     *
     * @param configOverrides the application's configuration properties to override.
     *
     * @return a new {@link JikkouConfig}
     */
    public static JikkouConfig load(final @Nullable Map<String, Object> configOverrides) {

        return load(configOverrides, null);
    }

    /**
     * Like {@link #load()} but allows specifying the application's configuration file to load,
     * and overriding some config properties
     *
     * @param configOverrides   the application's configuration properties to override.
     * @param configFilePath    the application's configuration file to load
     *
     * @return a new {@link JikkouConfig}
     */
    public static JikkouConfig load(final @Nullable Map<String, Object> configOverrides,
                                    final @Nullable String configFilePath) {

        getConfigFile(configFilePath).ifPresentOrElse(configFile -> {
            LOG.info("Loading configuration from: '{}'", configFile);
            System.setProperty("config.file", configFile);
        }, () -> LOG.info("No configuration file was found"));

        ConfigFactory.invalidateCaches();
        Config config = ConfigFactory.load().getConfig(ROOT_CONFIG_KEY);

        if (configOverrides != null && !configOverrides.isEmpty()) {
            final Config overridingConfig = ConfigFactory.parseMap(configOverrides);
            config = overridingConfig.withFallback(config);
        }
        return new JikkouConfig(config);
    }

    private static Optional<String> getConfigFile(@Nullable final String configFilePath) {
        if (configFilePath != null && !configFilePath.isEmpty())
            return Optional.of(configFilePath);

        Path configFromCurrentRelative = Paths.get(DEFAULT_CONFIG);
        if (Files.exists(configFromCurrentRelative)) {
            return Optional.of(configFromCurrentRelative.toAbsolutePath().toString());
        }

        Path configFromUserHome = Paths.get(String.format(
                "%s/.%s/%s",
                System.getProperty("user.home"),
                ROOT_CONFIG_KEY,
                DEFAULT_CONFIG)
        );
        if (Files.exists(configFromUserHome)) {
            return Optional.of(configFromUserHome.toAbsolutePath().toString());
        }

        return Optional.empty();
    }

    /**
     * Creates a new {@link JikkouConfig} instance.
     *
     * @param config the {@link Config}.
     */
    public JikkouConfig(@NotNull final Config config) {
        this(config, true);
    }

    /**
     * Creates a new {@link JikkouConfig} instance.
     *
     * @param config the {@link Config}.
     * @param doLog  flag indicating if config should be logged.
     */
    public JikkouConfig(@NotNull final Config config, final boolean doLog) {
        this.config = Objects.requireNonNull(config, "'config' cannot be null");
        if (doLog) {
            LOG.info("Created new {}:\n\t{}", this.getClass().getName(), toPrettyString());
        }
    }

    /**
     * @return the underlying {@link Config} object.
     */
    public Config unwrap() {
        return config;
    }

    /**
     * @see Config#getString(String)
     */
    public String getString(@NotNull final String path) {
        return config.getString(path);
    }

    /**
     * @see Config#getString(String)
     */
    public Option<String> findString(@NotNull final String path) {
        return config.hasPath(path) ? Option.of(config.getString(path)) : Option.none();
    }

    /**
     * @see Config#getBoolean(String)
     */
    public boolean getBoolean(@NotNull final String path) {
        return config.getBoolean(path);
    }

    /**
     * @see Config#getBoolean(String)
     */
    public Option<Boolean> findBoolean(@NotNull final String path) {
        return config.hasPath(path) ? Option.of(config.getBoolean(path)) : Option.none();
    }

    /**
     * @see Config#getLong(String)
     */
    public long getLong(@NotNull final String path) {
        return config.getLong(path);
    }

    /**
     * @see Config#getLong(String)
     */
    public Option<Long> findLong(@NotNull final String path) {
        return config.hasPath(path) ? Option.of(config.getLong(path)) : Option.none();
    }

    /**
     * @see Config#getInt(String)
     */
    public int getInt(@NotNull final String path) {
        return config.getInt(path);
    }

    /**
     * @see Config#getInt(String)
     */
    public Option<Integer> findInt(@NotNull final String path) {
        return config.hasPath(path) ? Option.of(config.getInt(path)) : Option.none();
    }

    /**
     * @see Config#getConfig(String)
     */
    public Option<JikkouConfig> findConfig(@NotNull final String path) {
        return config.hasPath(path) ? Option.some(new JikkouConfig(config.getConfig(path), false)) : Option.none();
    }

    /**
     * @see Config#getStringList(String)
     */
    public Option<List<String>> findStringList(@NotNull final String path) {
        return config.hasPath(path) ? Option.some(config.getStringList(path)) : Option.none();
    }

    @SuppressWarnings("unchecked")
    public <T> Option<List<Class<T>>> findClassList(@NotNull final String path) {
        return findStringList(path).flatMap(classes -> {
            var l = classes.stream()
                    .map(it -> (Class<T>) ClassUtils.forName(it))
                    .collect(Collectors.toList());
            return Option.of(l);
        });
    }

    public Option<Map<String, Object>> findConfigAsMap(@NotNull final String path) {
        return findConfig(path).map(JikkouConfig::unwrap).map(JikkouConfig::getConfAsMap);
    }

    /**
     * @see Config#withFallback(ConfigMergeable)
     */
    public JikkouConfig withFallback(final JikkouConfig config) {
        return new JikkouConfig(this.config.withFallback(config.config), false);
    }

    private static Map<String, Object> getConfAsMap(@NotNull final Config config) {
        Map<String, Object> props = new HashMap<>();
        config.entrySet().forEach(e -> props.put(e.getKey(), config.getAnyRef(e.getKey())));
        return new TreeMap<>(props);
    }

    private static Properties getConfAsProperties(@NotNull final Config config) {
        Properties properties = new Properties();
        config.entrySet().forEach(e -> properties.setProperty(e.getKey(), config.getString(e.getKey())));
        return properties;
    }

    public String toPrettyString() {
        Map<String, Object> confAsMap = new TreeMap<>(getConfAsMap(config));
        return confAsMap.entrySet()
                .stream()
                .map(e -> e.getKey() + " = " + e.getValue())
                .collect(Collectors.joining("\n\t"));
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private String configFile;
        private final Map<String, Object> cliConfigParams = new HashMap<>();

        public Builder withConfigFile(final @NotNull String configFile) {
            this.configFile = configFile;
            return this;
        }

        public Builder withConfigOverrides(final @NotNull Map<String, Object> configOverrides) {
            this.cliConfigParams.putAll(configOverrides);
            return this;
        }

        public Builder withConfigOverrides(final @NotNull String configKey, final @NotNull Object configValue) {
            this.cliConfigParams.put(configKey, configValue);
            return this;
        }

        public JikkouConfig getOrCreate() {
            Option.of(CACHED).onEmpty(() -> CACHED = load(cliConfigParams, configFile));
            return CACHED;
        }
    }

}
