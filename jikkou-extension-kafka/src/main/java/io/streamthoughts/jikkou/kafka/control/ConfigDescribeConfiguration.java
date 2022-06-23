/*
 * Copyright 2022 StreamThoughts.
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
package io.streamthoughts.jikkou.kafka.control;

import static org.apache.kafka.clients.admin.ConfigEntry.ConfigSource.DYNAMIC_BROKER_CONFIG;
import static org.apache.kafka.clients.admin.ConfigEntry.ConfigSource.DYNAMIC_DEFAULT_BROKER_CONFIG;
import static org.apache.kafka.clients.admin.ConfigEntry.ConfigSource.STATIC_BROKER_CONFIG;

import io.streamthoughts.jikkou.api.config.ConfigProperty;
import io.streamthoughts.jikkou.api.config.Configuration;
import io.streamthoughts.jikkou.api.config.ConfigurationSupport;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import org.apache.kafka.clients.admin.ConfigEntry;
import org.jetbrains.annotations.NotNull;

public class ConfigDescribeConfiguration extends ConfigurationSupport<ConfigDescribeConfiguration> {

    public static final ConfigProperty<Boolean> DESCRIBE_DEFAULT_CONFIGS_PROPERTY =
            ConfigProperty.ofBoolean("describe-default-configs").orElse(true);

    public static final ConfigProperty<Boolean> DESCRIBE_DYNAMIC_BROKER_CONFIGS_PROPERTY =
            ConfigProperty.ofBoolean("describe-dynamic-broker-configs").orElse(true);

    public static final ConfigProperty<Boolean> DESCRIBE_STATIC_BROKER_CONFIGS_PROPERTY =
            ConfigProperty.ofBoolean("describe-static-broker-configs").orElse(true);

    /**
     * Creates a new {@link ConfigDescribeConfiguration} instance.
     */
    public ConfigDescribeConfiguration() {
        this(Configuration.empty());
    }

    /**
     * Creates a new {@link ConfigDescribeConfiguration} instance.
     *
     * @param configuration the configuration properties to use for configuring this class.
     */
    public ConfigDescribeConfiguration(final @NotNull Configuration configuration) {
        configure(configuration);
    }

    /** {@inheritDoc} **/
    @Override
    protected ConfigDescribeConfiguration newInstance(final @NotNull Configuration configuration) {
        return new ConfigDescribeConfiguration(configuration);
    }

    /** {@inheritDoc} **/
    @Override
    protected Set<ConfigProperty<?>> defaultConfigProperties() {
        return Set.of(
                DESCRIBE_DEFAULT_CONFIGS_PROPERTY,
                DESCRIBE_DYNAMIC_BROKER_CONFIGS_PROPERTY,
                DESCRIBE_STATIC_BROKER_CONFIGS_PROPERTY
        );
    }

    public ConfigDescribeConfiguration withDescribeDefaultConfigs(boolean value) {
        return with(DESCRIBE_DEFAULT_CONFIGS_PROPERTY, value);
    }

    public ConfigDescribeConfiguration withDescribeDynamicBrokerConfigs(boolean value) {
        return with(DESCRIBE_DYNAMIC_BROKER_CONFIGS_PROPERTY, value);
    }

    public ConfigDescribeConfiguration withDescribeStaticBrokerConfigs(boolean value) {
        return with(DESCRIBE_STATIC_BROKER_CONFIGS_PROPERTY, value);
    }

    public boolean isDescribeDefaultConfigs() {
        return get(DESCRIBE_DEFAULT_CONFIGS_PROPERTY);
    }

    public boolean isDescribeDynamicBrokerConfigs() {
        return get(DESCRIBE_DYNAMIC_BROKER_CONFIGS_PROPERTY);
    }

    public boolean isDescribeStaticBrokerConfigs() {
        return get(DESCRIBE_STATIC_BROKER_CONFIGS_PROPERTY);
    }

    public Predicate<ConfigEntry> configEntryPredicate() {
        List<Predicate<ConfigEntry>> predicates = new ArrayList<>();

        predicates.add(entry -> !entry.isDefault() || isDescribeDefaultConfigs());

        if (!isDescribeStaticBrokerConfigs()) {
            predicates.add(config -> config.source() != STATIC_BROKER_CONFIG);
        }

        if (!isDescribeDynamicBrokerConfigs()) {
            predicates.add(config -> config.source() != DYNAMIC_BROKER_CONFIG);
            predicates.add(config -> config.source() != DYNAMIC_DEFAULT_BROKER_CONFIG);
        }

        return predicates.stream().reduce(t -> DESCRIBE_DEFAULT_CONFIGS_PROPERTY.defaultValue(), Predicate::and);
    }

}
