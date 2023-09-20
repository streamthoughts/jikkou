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
package io.streamthoughts.jikkou.schema.registry;

import io.streamthoughts.jikkou.api.config.Configuration;
import io.streamthoughts.jikkou.api.extensions.ExtensionFactory;
import io.streamthoughts.jikkou.schema.registry.control.SchemaRegistrySubjectCollector;
import io.streamthoughts.jikkou.schema.registry.control.SchemaRegistrySubjectController;
import io.streamthoughts.jikkou.schema.registry.health.SchemaRegistryHealthIndicator;
import io.streamthoughts.jikkou.schema.registry.transform.NormalizeSubjectSchemaTransformation;
import io.streamthoughts.jikkou.schema.registry.validation.AvroSchemaValidation;
import io.streamthoughts.jikkou.schema.registry.validation.CompatibilityLevelValidation;
import io.streamthoughts.jikkou.schema.registry.validation.SchemaCompatibilityValidation;
import io.streamthoughts.jikkou.spi.ExtensionProvider;
import org.jetbrains.annotations.NotNull;

public class SchemaRegistryExtensionProvider implements ExtensionProvider {

    /** {@inheritDoc} **/
    @Override
    public void registerExtensions(@NotNull ExtensionFactory factory,
                                   @NotNull Configuration configuration) {
        // Collectors
        factory.register(SchemaRegistrySubjectCollector.class, SchemaRegistrySubjectCollector::new);

        // Controllers
        factory.register(SchemaRegistrySubjectController.class, SchemaRegistrySubjectController::new);

        // Validations
        factory.register(AvroSchemaValidation.class, AvroSchemaValidation::new);
        factory.register(SchemaCompatibilityValidation.class, SchemaCompatibilityValidation::new);
        factory.register(CompatibilityLevelValidation.class, CompatibilityLevelValidation::new);

        // Transformations
        factory.register(NormalizeSubjectSchemaTransformation.class, NormalizeSubjectSchemaTransformation::new);

        // Health indicators
        factory.register(SchemaRegistryHealthIndicator.class, SchemaRegistryHealthIndicator::new);

    }
}