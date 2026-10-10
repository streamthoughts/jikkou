/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.jikkou.kafka.connect;

import io.jikkou.core.annotation.Provider;
import io.jikkou.core.config.ConfigProperty;
import io.jikkou.core.config.Configuration;
import io.jikkou.core.exceptions.ConfigException;
import io.jikkou.core.extension.ExtensionRegistry;
import io.jikkou.core.models.change.GenericResourceChange;
import io.jikkou.core.models.change.ResourceChange;
import io.jikkou.core.resource.ResourceRegistry;
import io.jikkou.kafka.connect.action.KafkaConnectRestartConnectorsAction;
import io.jikkou.kafka.connect.api.KafkaConnectClientConfig;
import io.jikkou.kafka.connect.collections.V1KafkaConnectorList;
import io.jikkou.kafka.connect.health.KafkaConnectHealthIndicator;
import io.jikkou.kafka.connect.models.V1KafkaConnector;
import io.jikkou.kafka.connect.reconciler.KafkaConnectorCollector;
import io.jikkou.kafka.connect.reconciler.KafkaConnectorController;
import io.jikkou.kafka.connect.transform.KafkaConnectorResourceTransformation;
import io.jikkou.kafka.connect.validation.KafkaConnectorResourceValidation;
import io.jikkou.spi.BaseExtensionProvider;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Extension provider for Kafka Connect.
 */
@Provider(
    name = "kafkaconnect",
    description = "Extension provider for Kafka Connect",
    tags = {"Apache Kafka", "Kafka Connect"}
)
public final class KafkaConnectExtensionProvider extends BaseExtensionProvider {

    /**
     * Default pattern for masked config values: a non-empty sequence of bullet characters (U+2022).
     */
    public static final String DEFAULT_MASKED_CONFIG_VALUE_PATTERN = "^\\u2022+$";

    private final ConfigProperty<List<KafkaConnectClientConfig>> clusters = ConfigProperty
        .ofConfigList("clusters")
        .displayName("Kafka Connect Clusters")
        .map(list -> list.stream().map(KafkaConnectClientConfig::from).toList())
        .defaultValue(Collections.emptyList())
        .description("List of Kafka Connect Cluster configuration.");

    private final ConfigProperty<String> maskedConfigValuePattern = ConfigProperty
        .ofString("maskedConfigValuePattern")
        .displayName("Masked Config Value Pattern")
        .defaultValue(DEFAULT_MASKED_CONFIG_VALUE_PATTERN)
        .description("Regular expression matching connector config values masked by the Kafka Connect REST API "
            + "(e.g. passwords). A masked value is considered equal to the desired value when computing changes. "
            + "Set to an empty string to disable.");

    private KafkaConnectClusterConfigs clusterConfigs;

    private Pattern maskedConfigValueRegex;

    /** {@inheritDoc} **/
    @Override
    public List<ConfigProperty<?>> configProperties() {
        return List.of(clusters, maskedConfigValuePattern);
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public void configure(@NotNull Configuration configuration) throws ConfigException {
        super.configure(configuration);
        this.clusterConfigs = new KafkaConnectClusterConfigs(clusters.get(configuration));
        this.maskedConfigValueRegex = compileMaskedConfigValuePattern(maskedConfigValuePattern.get(configuration));
    }

    public KafkaConnectClusterConfigs clusterConfigs() {
        return clusterConfigs;
    }

    /**
     * Gets the pattern matching connector config values masked by the Kafka Connect REST API.
     *
     * @return the pattern, or {@code null} if masked values detection is disabled.
     */
    public @Nullable Pattern maskedConfigValuePattern() {
        return maskedConfigValueRegex;
    }

    private Pattern compileMaskedConfigValuePattern(String regex) {
        if (regex == null || regex.isBlank()) {
            return null;
        }
        try {
            return Pattern.compile(regex);
        } catch (PatternSyntaxException e) {
            throw new ConfigException(String.format(
                "Invalid value for '%s': %s", maskedConfigValuePattern.key(), e.getMessage()
            ));
        }
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public void registerExtensions(@NotNull ExtensionRegistry registry) {
        registry.register(KafkaConnectorCollector.class, KafkaConnectorCollector::new);
        registry.register(KafkaConnectorController.class, KafkaConnectorController::new);
        registry.register(KafkaConnectorResourceValidation.class, KafkaConnectorResourceValidation::new);
        registry.register(KafkaConnectorResourceTransformation.class, KafkaConnectorResourceTransformation::new);
        registry.register(KafkaConnectHealthIndicator.class, KafkaConnectHealthIndicator::new);
        registry.register(KafkaConnectRestartConnectorsAction.class, KafkaConnectRestartConnectorsAction::new);

    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public void registerResources(@NotNull ResourceRegistry registry) {
        registry.register(V1KafkaConnector.class).setReconciliationOrder(100);
        registry.register(GenericResourceChange.class, ResourceChange.getResourceTypeOf(V1KafkaConnector.class));
        registry.register(V1KafkaConnectorList.class);
    }
}
