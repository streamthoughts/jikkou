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

import io.streamthoughts.jikkou.core.annotation.Named;
import io.streamthoughts.jikkou.core.extension.ExtensionRegistry;
import io.streamthoughts.jikkou.core.resource.ResourceRegistry;
import io.streamthoughts.jikkou.kafka.connect.collections.V1KafkaConnectorList;
import io.streamthoughts.jikkou.kafka.connect.health.KafkaConnectHealthIndicator;
import io.streamthoughts.jikkou.kafka.connect.models.V1KafkaConnector;
import io.streamthoughts.jikkou.kafka.connect.reconcilier.KafkaConnectorCollector;
import io.streamthoughts.jikkou.kafka.connect.reconcilier.KafkaConnectorController;
import io.streamthoughts.jikkou.kafka.connect.transform.KafkaConnectorResourceTransformation;
import io.streamthoughts.jikkou.kafka.connect.validation.KafkaConnectorResourceValidation;
import io.streamthoughts.jikkou.spi.ExtensionProvider;
import org.jetbrains.annotations.NotNull;

/**
 * Extension provider for Kafka Connect.
 */
@Named("KafkaConnect")
public final class KafkaConnectExtensionProvider implements ExtensionProvider {

    /**
     * {@inheritDoc}
     **/
    @Override
    public void registerExtensions(@NotNull ExtensionRegistry registry) {
        // Collectors
        registry.register(KafkaConnectorCollector.class, KafkaConnectorCollector::new);

        // Controllers
        registry.register(KafkaConnectorController.class, KafkaConnectorController::new);

        // Validations
        registry.register(KafkaConnectorResourceValidation.class, KafkaConnectorResourceValidation::new);

        // Transformations
        registry.register(KafkaConnectorResourceTransformation.class, KafkaConnectorResourceTransformation::new);

        // Health indicators
        registry.register(KafkaConnectHealthIndicator.class, KafkaConnectHealthIndicator::new);

    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public void registerResources(@NotNull ResourceRegistry registry) {
        registry.register(V1KafkaConnector.class);
        registry.register(V1KafkaConnectorList.class);
    }
}
