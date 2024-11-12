/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.kafka.connect;

import io.streamthoughts.jikkou.core.annotation.Provider;
import io.streamthoughts.jikkou.core.config.ConfigProperty;
import io.streamthoughts.jikkou.core.config.Configuration;
import io.streamthoughts.jikkou.core.exceptions.ConfigException;
import io.streamthoughts.jikkou.core.extension.ExtensionRegistry;
import io.streamthoughts.jikkou.core.models.change.GenericResourceChange;
import io.streamthoughts.jikkou.core.models.change.ResourceChange;
import io.streamthoughts.jikkou.core.resource.ResourceRegistry;
import io.streamthoughts.jikkou.kafka.connect.action.KafkaConnectRestartConnectorsAction;
import io.streamthoughts.jikkou.kafka.connect.api.KafkaConnectClientConfig;
import io.streamthoughts.jikkou.kafka.connect.collections.V1KafkaConnectorList;
import io.streamthoughts.jikkou.kafka.connect.health.KafkaConnectHealthIndicator;
import io.streamthoughts.jikkou.kafka.connect.models.V1KafkaConnector;
import io.streamthoughts.jikkou.kafka.connect.reconciler.KafkaConnectorCollector;
import io.streamthoughts.jikkou.kafka.connect.reconciler.KafkaConnectorController;
import io.streamthoughts.jikkou.kafka.connect.transform.KafkaConnectorResourceTransformation;
import io.streamthoughts.jikkou.kafka.connect.validation.KafkaConnectorResourceValidation;
import io.streamthoughts.jikkou.spi.BaseExtensionProvider;
import java.util.Collections;
import java.util.List;
import org.jetbrains.annotations.NotNull;

/**
 * Extension provider for Kafka Connect.
 */
@Provider(
    name = "KafkaConnect",
    description = "Extension provider for Kafka Connect",
    tags = {"Apache Kafka", "Kafka Connect"}
)
public final class KafkaConnectExtensionProvider extends BaseExtensionProvider {

    private final ConfigProperty<List<KafkaConnectClientConfig>> clusters = ConfigProperty
        .ofConfigList("clusters")
        .map(list -> list.stream().map(KafkaConnectClientConfig::from).toList())
        .defaultValue(Collections.emptyList())
        .description("List of Kafka Connect Cluster configuration.");

    private KafkaConnectClusterConfigs clusterConfigs;

    /**
     * {@inheritDoc}
     **/
    @Override
    public void configure(@NotNull Configuration configuration) throws ConfigException {
        super.configure(configuration);
        this.clusterConfigs = new KafkaConnectClusterConfigs(clusters.get(configuration));
    }

    public KafkaConnectClusterConfigs clusterConfigs() {
        return clusterConfigs;
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
        registry.register(V1KafkaConnector.class);
        registry.register(GenericResourceChange.class, ResourceChange.fromResource(V1KafkaConnector.class));
        registry.register(V1KafkaConnectorList.class);
    }
}
