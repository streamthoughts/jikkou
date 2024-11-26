/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.kafka.connect;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.streamthoughts.jikkou.core.config.ConfigProperty;
import io.streamthoughts.jikkou.core.config.Configuration;
import io.streamthoughts.jikkou.core.exceptions.JikkouRuntimeException;
import io.streamthoughts.jikkou.core.io.Jackson;
import io.streamthoughts.jikkou.core.models.CoreAnnotations;
import io.streamthoughts.jikkou.core.models.HasMetadata;
import io.streamthoughts.jikkou.kafka.connect.api.KafkaConnectClientConfig;
import io.streamthoughts.jikkou.kafka.connect.exception.KafkaConnectClusterNotFoundException;
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
     * @param name the connect cluster name.
     * @return an optional {@link KafkaConnectClientConfig}.
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

    public KafkaConnectClientConfig resolveClientConfigForCluster(final String connectClusterName,
                                                                  final List<? extends HasMetadata> connectors) {
        List<KafkaConnectClientConfig> clientConfigs = connectors
            .stream()
            .map(connector -> connector.getMetadata().findAnnotationByKey(CoreAnnotations.JIKKOU_IO_CONFIG_OVERRIDE))
            .flatMap(Optional::stream)
            .map(config -> {
                try {
                    return Jackson.json().readValue(config.toString(), Map.class);
                } catch (JsonProcessingException e) {
                    throw new JikkouRuntimeException(String.format(
                        "Failed to parse JSON from metadata.annotation '%s': %s.", CoreAnnotations.JIKKOU_IO_CONFIG_OVERRIDE, e.getMessage()
                    ), e);
                }
            })
            .map(Configuration::from)
            .map(KafkaConnectClientConfig::new)
            .toList();

        if (clientConfigs.isEmpty()) {
            return getConfigForCluster(connectClusterName)
                .orElseThrow(() -> new KafkaConnectClusterNotFoundException(String.format(
                    "Cannot resolve configuration for connect cluster '%s'.", connectClusterName)
                ));
        } else {
            if (clientConfigs.size() != connectors.size()) {
                throw new JikkouRuntimeException(String.format(
                    "Not all connector resources for cluster %s define the metadata.annotation '%s'.",
                    connectClusterName, CoreAnnotations.JIKKOU_IO_CONFIG_OVERRIDE
                ));
            }

            if (new HashSet<>(clientConfigs).size() > 1) {
                throw new JikkouRuntimeException(String.format(
                    "Multiple config was defined for Kafka Connect cluster '%s' through the metadata.annotation '%s'.",
                    connectClusterName, CoreAnnotations.JIKKOU_IO_CONFIG_OVERRIDE
                ));
            }
            return clientConfigs.getFirst();
        }
    }
}
