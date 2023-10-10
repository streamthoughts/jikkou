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
    public void registerExtensions(@NotNull ExtensionFactory factory,
                                   @NotNull Configuration configuration) {
        factory.register(AivenServiceHealthIndicator.class, AivenServiceHealthIndicator::new);
        factory.register(AivenKafkaTopicAclEntryCollector.class, AivenKafkaTopicAclEntryCollector::new);
        factory.register(AivenKafkaTopicAclEntryController.class, AivenKafkaTopicAclEntryController::new);
        factory.register(AivenSchemaRegistryAclEntryCollector.class, AivenSchemaRegistryAclEntryCollector::new);
        factory.register(AivenSchemaRegistryAclEntryController.class, AivenSchemaRegistryAclEntryController::new);
        factory.register(AivenSchemaRegistrySubjectCollector.class, AivenSchemaRegistrySubjectCollector::new);
        factory.register(AivenSchemaRegistrySubjectController.class, AivenSchemaRegistrySubjectController::new);
        factory.register(SchemaRegistryAclEntryValidation.class, SchemaRegistryAclEntryValidation::new);
        factory.register(AivenKafkaQuotaCollector.class, AivenKafkaQuotaCollector::new);
        factory.register(AivenKafkaQuotaController.class, AivenKafkaQuotaController::new);
        factory.register(AivenSchemaCompatibilityValidation.class, AivenSchemaCompatibilityValidation::new);
    }
}
