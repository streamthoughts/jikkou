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

import io.streamthoughts.jikkou.core.config.Configuration;
import io.streamthoughts.jikkou.core.extension.ExtensionRegistry;
import io.streamthoughts.jikkou.extension.aiven.control.AivenKafkaQuotaCollector;
import io.streamthoughts.jikkou.extension.aiven.control.AivenKafkaQuotaController;
import io.streamthoughts.jikkou.extension.aiven.control.AivenKafkaTopicAclEntryCollector;
import io.streamthoughts.jikkou.extension.aiven.control.AivenKafkaTopicAclEntryController;
import io.streamthoughts.jikkou.extension.aiven.control.AivenSchemaRegistryAclEntryCollector;
import io.streamthoughts.jikkou.extension.aiven.control.AivenSchemaRegistryAclEntryController;
import io.streamthoughts.jikkou.extension.aiven.control.AivenSchemaRegistrySubjectCollector;
import io.streamthoughts.jikkou.extension.aiven.control.AivenSchemaRegistrySubjectController;
import io.streamthoughts.jikkou.extension.aiven.health.AivenServiceHealthIndicator;
import io.streamthoughts.jikkou.extension.aiven.validation.AivenSchemaCompatibilityValidation;
import io.streamthoughts.jikkou.extension.aiven.validation.SchemaRegistryAclEntryValidation;
import io.streamthoughts.jikkou.spi.ExtensionProvider;
import org.jetbrains.annotations.NotNull;

public class AivenExtensionProvider implements ExtensionProvider {

    /**
     * {@inheritDoc}
     **/
    @Override
    public void registerExtensions(@NotNull ExtensionRegistry registry,
                                   @NotNull Configuration configuration) {
        registry.register(AivenServiceHealthIndicator.class, AivenServiceHealthIndicator::new);
        registry.register(AivenKafkaTopicAclEntryCollector.class, AivenKafkaTopicAclEntryCollector::new);
        registry.register(AivenKafkaTopicAclEntryController.class, AivenKafkaTopicAclEntryController::new);
        registry.register(AivenSchemaRegistryAclEntryCollector.class, AivenSchemaRegistryAclEntryCollector::new);
        registry.register(AivenSchemaRegistryAclEntryController.class, AivenSchemaRegistryAclEntryController::new);
        registry.register(AivenSchemaRegistrySubjectCollector.class, AivenSchemaRegistrySubjectCollector::new);
        registry.register(AivenSchemaRegistrySubjectController.class, AivenSchemaRegistrySubjectController::new);
        registry.register(SchemaRegistryAclEntryValidation.class, SchemaRegistryAclEntryValidation::new);
        registry.register(AivenKafkaQuotaCollector.class, AivenKafkaQuotaCollector::new);
        registry.register(AivenKafkaQuotaController.class, AivenKafkaQuotaController::new);
        registry.register(AivenSchemaCompatibilityValidation.class, AivenSchemaCompatibilityValidation::new);
    }
}
