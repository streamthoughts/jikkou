/*
 * Copyright 2022 The original authors
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
package io.streamthoughts.jikkou.kafka.control;


import io.streamthoughts.jikkou.core.config.ConfigProperty;
import io.streamthoughts.jikkou.core.config.Configuration;
import io.streamthoughts.jikkou.core.config.ConfigurationSupport;
import java.util.Set;
import org.jetbrains.annotations.NotNull;

public class ConfigDescribeConfiguration extends ConfigurationSupport<ConfigDescribeConfiguration> {

    public static final String DESCRIBE_DEFAULT_CONFIGS_PROPERTY_NAME = "describe-default-configs";
    public static final String DESCRIBE_DEFAULT_CONFIGS_PROPERTY_DESC = "Describe built-in default configuration for configs that have a default value.";
    public static final ConfigProperty<Boolean> DESCRIBE_DEFAULT_CONFIGS_PROPERTY = ConfigProperty
            .ofBoolean(DESCRIBE_DEFAULT_CONFIGS_PROPERTY_NAME)
            .orElse(false);

    public static final String DESCRIBE_DYNAMIC_BROKER_CONFIGS_PROPERTY_NAME = "describe-dynamic-broker-configs";
    public static final String DESCRIBE_DYNAMIC_BROKER_CONFIGS_PROPERTY_DESC = "Describe dynamic configs that is configured as default for all brokers or for specific broker in the cluster.";
    public static final ConfigProperty<Boolean> DESCRIBE_DYNAMIC_BROKER_CONFIGS_PROPERTY = ConfigProperty
            .ofBoolean(DESCRIBE_DYNAMIC_BROKER_CONFIGS_PROPERTY_NAME)
            .orElse(false);

    public static final String DESCRIBE_STATIC_BROKER_CONFIGS_PROPERTY_CONFIG = "describe-static-broker-configs";
    public static final String DESCRIBE_STATIC_BROKER_CONFIGS_PROPERTY_DESC = "Describe static configs provided as broker properties at start up (e.g. server.properties file).";
    public static final ConfigProperty<Boolean> DESCRIBE_STATIC_BROKER_CONFIGS_PROPERTY = ConfigProperty
            .ofBoolean(DESCRIBE_STATIC_BROKER_CONFIGS_PROPERTY_CONFIG)
            .orElse(false);

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

}
