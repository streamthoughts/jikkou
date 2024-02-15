/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.kafka.connect;

import io.streamthoughts.jikkou.core.annotation.Named;
import io.streamthoughts.jikkou.core.extension.ExtensionRegistry;
import io.streamthoughts.jikkou.core.resource.ResourceRegistry;
import io.streamthoughts.jikkou.kafka.connect.action.KafkaConnectRestartConnectorsAction;
import io.streamthoughts.jikkou.kafka.connect.collections.V1KafkaConnectorList;
import io.streamthoughts.jikkou.kafka.connect.health.KafkaConnectHealthIndicator;
import io.streamthoughts.jikkou.kafka.connect.models.V1KafkaConnector;
import io.streamthoughts.jikkou.kafka.connect.reconciler.KafkaConnectorCollector;
import io.streamthoughts.jikkou.kafka.connect.reconciler.KafkaConnectorController;
import io.streamthoughts.jikkou.kafka.connect.transform.KafkaConnectorResourceTransformation;
import io.streamthoughts.jikkou.kafka.connect.validation.KafkaConnectorResourceValidation;
import io.streamthoughts.jikkou.spi.AbstractExtensionProvider;
import org.jetbrains.annotations.NotNull;

/**
 * Extension provider for Kafka Connect.
 */
@Named("KafkaConnect")
public final class KafkaConnectExtensionProvider extends AbstractExtensionProvider {

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
        registerResource(registry, V1KafkaConnector.class);
        registerResource(registry, V1KafkaConnectorList.class);
    }
}
