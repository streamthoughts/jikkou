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

import io.streamthoughts.jikkou.api.config.Configuration;
import io.streamthoughts.jikkou.api.extensions.ExtensionFactory;
import io.streamthoughts.jikkou.kafka.connect.control.KafkaConnectorCollector;
import io.streamthoughts.jikkou.kafka.connect.control.KafkaConnectorController;
import io.streamthoughts.jikkou.kafka.connect.health.KafkaConnectHealthIndicator;
import io.streamthoughts.jikkou.kafka.connect.transform.KafkaConnectorResourceTransformation;
import io.streamthoughts.jikkou.kafka.connect.validation.KafkaConnectorResourceValidation;
import io.streamthoughts.jikkou.spi.ExtensionProvider;
import org.jetbrains.annotations.NotNull;

public class KafkaConnectExtensionProvider implements ExtensionProvider {

    /** {@inheritDoc} **/
    @Override
    public void registerExtensions(@NotNull ExtensionFactory factory,
                                   @NotNull Configuration configuration) {
        // Collectors
        factory.register(KafkaConnectorCollector.class, KafkaConnectorCollector::new);

        // Controllers
        factory.register(KafkaConnectorController.class, KafkaConnectorController::new);

        // Validations
        factory.register(KafkaConnectorResourceValidation.class, KafkaConnectorResourceValidation::new);

        // Transformations
        factory.register(KafkaConnectorResourceTransformation.class, KafkaConnectorResourceTransformation::new);

        // Health indicators
        factory.register(KafkaConnectHealthIndicator.class, KafkaConnectHealthIndicator::new);

    }
}
