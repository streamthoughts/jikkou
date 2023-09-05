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
package io.streamthoughts.jikkou.extension.aiven;

import io.streamthoughts.jikkou.api.config.Configuration;
import io.streamthoughts.jikkou.api.extensions.ExtensionFactory;
import io.streamthoughts.jikkou.extension.aiven.control.KafkaQuotaCollector;
import io.streamthoughts.jikkou.extension.aiven.control.KafkaQuotaController;
import io.streamthoughts.jikkou.extension.aiven.control.KafkaTopicAclEntryCollector;
import io.streamthoughts.jikkou.extension.aiven.control.KafkaTopicAclEntryController;
import io.streamthoughts.jikkou.extension.aiven.control.SchemaRegistryAclEntryCollector;
import io.streamthoughts.jikkou.extension.aiven.control.SchemaRegistryAclEntryController;
import io.streamthoughts.jikkou.extension.aiven.health.AivenServiceHealthIndicator;
import io.streamthoughts.jikkou.extension.aiven.validation.SchemaRegistryAclEntryValidation;
import io.streamthoughts.jikkou.spi.ExtensionProvider;
import org.jetbrains.annotations.NotNull;

public class AivenExtensionsProvider implements ExtensionProvider {

    /** {@inheritDoc} **/
    @Override
    public void registerExtensions(@NotNull ExtensionFactory factory,
                                   @NotNull Configuration configuration) {
        factory.register(AivenServiceHealthIndicator.class, AivenServiceHealthIndicator::new);
        factory.register(KafkaTopicAclEntryCollector.class, KafkaTopicAclEntryCollector::new);
        factory.register(KafkaTopicAclEntryController.class, KafkaTopicAclEntryController::new);
        factory.register(SchemaRegistryAclEntryCollector.class, SchemaRegistryAclEntryCollector::new);
        factory.register(SchemaRegistryAclEntryController.class, SchemaRegistryAclEntryController::new);
        factory.register(SchemaRegistryAclEntryValidation.class, SchemaRegistryAclEntryValidation::new);
        factory.register(KafkaQuotaCollector.class, KafkaQuotaCollector::new);
        factory.register(KafkaQuotaController.class, KafkaQuotaController::new);
    }
}
