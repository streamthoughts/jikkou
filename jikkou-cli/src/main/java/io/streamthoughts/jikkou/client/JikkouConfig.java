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
package io.streamthoughts.jikkou.client;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigMergeable;
import com.typesafe.config.ConfigParseOptions;
import io.streamthoughts.jikkou.api.config.Configuration;
import io.streamthoughts.jikkou.common.utils.Classes;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Singular;
import lombok.With;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The default configuration class backed by the Typesafe {@link Config}.
 */
public final class JikkouConfig implements Configuration {

    private static final Logger LOG = LoggerFactory.getLogger(JikkouConfig.class);

    private static final String DEFAULT_CONFIG = "application.conf";
    private static final String ROOT_CONFIG_KEY = "jikkou";

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
     * from the specified properties.
     *
     * @param config the config Properties.
     * @param doLog  should configuration be logged.
     *
     * @return a new {@link JikkouConfig} instance.
     */
    public static JikkouConfig create(final Properties config, final boolean doLog) {
        return new JikkouConfig(ConfigFactory.parseProperties(config, ConfigParseOptions.defaults()), doLog);
    }

    /**
     * Static helper that can be used to create a new {@link JikkouConfig} instance
     * from the specified map.
     *
     * @param config the config Map.
     * @return a new {@link JikkouConfig} instance.
     */
    public static JikkouConfig create(final Map<String, Object> config) {
        return new JikkouConfig(ConfigFactory.parseMap(config));
    }

    /**
     * Static helper that can be used to create a new {@link JikkouConfig} instance
     * from the specified map.
     *
     * @param config the config Map.
     * @param doLog  should configuration be logged.
     * @return a new {@link JikkouConfig} instance.
     */
    public static JikkouConfig create(final Map<String, Object> config, final boolean doLog) {
        return new JikkouConfig(ConfigFactory.parseMap(config), doLog);
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
     * {@inheritDoc}
     *
     * @see Config#entrySet()
     */
    @Override
    public Set<String> keys() {
        return config.entrySet().stream().map(Map.Entry::getKey).collect(Collectors.toSet());
    }

    /**
     * {@inheritDoc}
     *
     * @see Config#hasPath(String)
     */
    @Override
    public boolean hasKey(@NotNull final String key) {
        return config.hasPath(key);
    }

    /**
     * {@inheritDoc}
     *
     * @see Config#getAnyRef(String)
     */
    @Override
    public Object getAny(@NotNull String key) {
        return config.getAnyRef(key);
    }

    /**
     * {@inheritDoc}
     *
     * @see Config#getConfig(String)
     */
    @Override
    public Configuration getConfig(@NotNull String path,
                                   @Nullable Supplier<Configuration> defaultValueSupplier) {
        if (config.hasPath(path)) {
            return new JikkouConfig(config.getConfig(path));
        }
        return defaultValueSupplier != null ? defaultValueSupplier.get() : null;
    }

    /**
     * {@inheritDoc}
     *
     * @see Config#getConfig(String)
     */
    @Override
    public JikkouConfig getConfig(@NotNull final String path) {
        return new JikkouConfig(config.getConfig(path), false);
    }

    /**
     * {@inheritDoc}
     *
     * @see Config#getConfig(String)
     */
    @Override
    public Optional<Configuration> findConfig(@NotNull final String path) {
        return config.hasPath(path) ? Optional.of(getConfig(path)) : Optional.empty();
    }

    @SuppressWarnings("unchecked")
    public <T> Class<T> getClass(@NotNull final String path) {
        return (Class<T>) Classes.forName(config.getString(path));
    }


    /** {@inheritDoc} */
    @Override
    public Map<String, Object> asMap() {
        return getConfAsMap(config);
    }

    /**
     * @see Config#withFallback(ConfigMergeable)
     */
    public Configuration withFallback(final Configuration config) {
        JikkouConfig casted;
        if (config instanceof JikkouConfig) {
            casted = ((JikkouConfig) config);
        } else {
            casted = JikkouConfig.create(config.asMap());
        }
        return new JikkouConfig(this.config.withFallback(casted.unwrap()), false);

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

    public static Builder builder() {
        return new Builder(null, null);
    }

    @With
    @AllArgsConstructor
    @NoArgsConstructor
    public static final class Builder {

        private String configFile;

        @Singular
        private Map<String, Object> configOverrides = new HashMap<>();


        public JikkouConfig build() {
            return load(configOverrides, configFile);
        }
    }

}
