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
package io.streamthoughts.jikkou.kafka.connect;

import io.streamthoughts.jikkou.core.config.ConfigProperty;
import io.streamthoughts.jikkou.core.config.Configuration;
import io.streamthoughts.jikkou.kafka.connect.api.KafkaConnectClientConfig;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;

public final class KafkaConnectExtensionConfig {

    public static final String KAFKA_CONNECT_EXTENSION_CONFIG = "kafkaConnect";
    public static final ConfigProperty<List<KafkaConnectClientConfig>> KAFKA_CONNECT_CLUSTERS_CONFIG = ConfigProperty
            .ofConfigList(KAFKA_CONNECT_EXTENSION_CONFIG + ".clusters")
            .map(list -> list.stream().map(KafkaConnectClientConfig::new).toList())
            .orElse(Collections.emptyList())
            .description("List of Kafka Connect Cluster configuration.");

    private final Configuration configuration;

    private Map<String, KafkaConnectClientConfig> configurationByClusterName;

    /**
     * Creates a new {@link KafkaConnectExtensionConfig} instance.
     *
     * @param configuration the configuration.
     */
    public KafkaConnectExtensionConfig(Configuration configuration) {
        this.configuration = configuration;
    }

    /**
     * Creates a new {@link KafkaConnectExtensionConfig} instance.
     *
     * @param configurations the configuration.
     */
    public KafkaConnectExtensionConfig(@NotNull List<KafkaConnectClientConfig> configurations) {
        Objects.requireNonNull(configurations, "configurations must not be null");
        this.configuration = null;
        setKafkaConnectClientConfiguration(configurations);
    }

    /**
     * Gets the configuration for the specified connect cluster name.
     *
     * @param name  the connect cluster name.
     * @return      an optional {@link KafkaConnectClientConfig}.
     */
    public Optional<KafkaConnectClientConfig> getConfigForCluster(@NotNull final String name) {
        return Optional.ofNullable(getConfigurationsByClusterName().get(name));
    }

    /**
     * Gets all connect client configuration by connect cluster name.
     *
     * @return a list of cluster name and {@link KafkaConnectClientConfig}.
     */
    public List<KafkaConnectClientConfig> getConfigurations() {
        return new ArrayList<>(getConfigurationsByClusterName().values());
    }

    /**
     * Gets the list of connect cluster names.
     *
     * @return the set of kafka connect cluster names.
     */
    public Set<String> getClusters() {
        return new HashSet<>(getConfigurationsByClusterName().keySet());
    }

    /**
     * Gets all connect client configuration by connect cluster name.
     *
     * @return a map of cluster name and {@link KafkaConnectClientConfig}.
     */
    public Map<String, KafkaConnectClientConfig> getConfigurationsByClusterName() {
        if (configurationByClusterName == null) {
            setKafkaConnectClientConfiguration(KAFKA_CONNECT_CLUSTERS_CONFIG.get(configuration));
        }
        return configurationByClusterName;
    }

    private void setKafkaConnectClientConfiguration(List<KafkaConnectClientConfig> configurations) {
        configurationByClusterName = configurations
                .stream()
                .collect(Collectors.toMap(KafkaConnectClientConfig::getConnectClusterName, Function.identity()));
    }
}
