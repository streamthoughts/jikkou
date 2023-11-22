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
package io.streamthoughts.jikkou.kafka.reconcilier;

import static io.streamthoughts.jikkou.kafka.reconcilier.AdminClientKafkaConfigs.DEFAULT_CONFIGS_CONFIG;
import static io.streamthoughts.jikkou.kafka.reconcilier.AdminClientKafkaConfigs.DYNAMIC_BROKER_CONFIGS_CONFIG;
import static io.streamthoughts.jikkou.kafka.reconcilier.AdminClientKafkaConfigs.STATIC_BROKER_CONFIGS_CONFIG;

import io.streamthoughts.jikkou.core.config.Configuration;
import io.streamthoughts.jikkou.core.extension.ContextualExtension;
import io.streamthoughts.jikkou.core.extension.annotations.ExtensionOptionSpec;
import io.streamthoughts.jikkou.core.extension.annotations.ExtensionSpec;
import io.streamthoughts.jikkou.kafka.internals.KafkaConfigPredicate;

@ExtensionSpec(
        options = {
                @ExtensionOptionSpec(
                        name = DEFAULT_CONFIGS_CONFIG,
                        description = "Describe built-in default configuration for configs that have a default value.",
                        type = Boolean.class,
                        defaultValue = "false"
                ),
                @ExtensionOptionSpec(
                        name = DYNAMIC_BROKER_CONFIGS_CONFIG,
                        description = "Describe dynamic configs that are configured as default for all brokers or for specific broker in the cluster.",
                        type = Boolean.class,
                        defaultValue = "false"
                ),
                @ExtensionOptionSpec(
                        name = STATIC_BROKER_CONFIGS_CONFIG,
                        description = "Describe static configs provided as broker properties at start up (e.g. server.properties file).",
                        type = Boolean.class,
                        defaultValue = "false"
                )
        }
)
public class AdminClientKafkaConfigs extends ContextualExtension {

    public static final String DEFAULT_CONFIGS_CONFIG = "default-configs";

    public static final String DYNAMIC_BROKER_CONFIGS_CONFIG = "dynamic-broker-configs";

    public static final String STATIC_BROKER_CONFIGS_CONFIG = "static-broker-configs";

    KafkaConfigPredicate kafkaConfigPredicate(Configuration configuration) {
        return new KafkaConfigPredicate()
                .dynamicTopicConfig(true)
                .defaultConfig(extensionContext().<Boolean>configProperty(DEFAULT_CONFIGS_CONFIG).get(configuration))
                .dynamicBrokerConfig(extensionContext().<Boolean>configProperty(DYNAMIC_BROKER_CONFIGS_CONFIG).get(configuration))
                .staticBrokerConfig(extensionContext().<Boolean>configProperty(STATIC_BROKER_CONFIGS_CONFIG).get(configuration));
    }

}
