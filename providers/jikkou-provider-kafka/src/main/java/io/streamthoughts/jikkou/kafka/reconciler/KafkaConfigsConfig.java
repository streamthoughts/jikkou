/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.kafka.reconciler;

import io.streamthoughts.jikkou.core.config.ConfigProperty;
import io.streamthoughts.jikkou.core.config.Configuration;
import io.streamthoughts.jikkou.kafka.internals.KafkaConfigPredicate;

public interface KafkaConfigsConfig {

    ConfigProperty<Boolean> DEFAULT_CONFIGS = ConfigProperty
        .ofBoolean("default-configs")
        .description("Describe built-in default configuration for configs that have a default value.")
        .defaultValue(false);

    ConfigProperty<Boolean> DYNAMIC_BROKER_CONFIGS = ConfigProperty
        .ofBoolean("dynamic-broker-configs")
        .description("Describe dynamic configs that are configured as default for all brokers or for specific broker in the cluster.")
        .defaultValue(false);

    ConfigProperty<Boolean> STATIC_BROKER_CONFIGS = ConfigProperty
        .ofBoolean("static-broker-configs")
        .description("Describe static configs provided as broker properties at start up (e.g. server.properties file).")
        .defaultValue(false);

    static KafkaConfigPredicate newConfigPredicate(final Configuration configuration) {
        return new KafkaConfigPredicate()
            .dynamicTopicConfig(true)
            .defaultConfig(DEFAULT_CONFIGS.get(configuration))
            .dynamicBrokerConfig(DYNAMIC_BROKER_CONFIGS.get(configuration))
            .staticBrokerConfig(STATIC_BROKER_CONFIGS.get(configuration));
    }

}
