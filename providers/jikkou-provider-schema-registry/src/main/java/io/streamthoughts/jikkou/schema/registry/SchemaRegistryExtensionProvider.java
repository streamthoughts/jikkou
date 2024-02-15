/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.schema.registry;

import io.streamthoughts.jikkou.core.annotation.Named;
import io.streamthoughts.jikkou.core.extension.ExtensionRegistry;
import io.streamthoughts.jikkou.core.resource.ResourceRegistry;
import io.streamthoughts.jikkou.schema.registry.collections.V1SchemaRegistrySubjectList;
import io.streamthoughts.jikkou.schema.registry.health.SchemaRegistryHealthIndicator;
import io.streamthoughts.jikkou.schema.registry.models.V1SchemaRegistrySubject;
import io.streamthoughts.jikkou.schema.registry.reconciler.SchemaRegistrySubjectCollector;
import io.streamthoughts.jikkou.schema.registry.reconciler.SchemaRegistrySubjectController;
import io.streamthoughts.jikkou.schema.registry.transform.NormalizeSubjectSchemaTransformation;
import io.streamthoughts.jikkou.schema.registry.validation.AvroSchemaValidation;
import io.streamthoughts.jikkou.schema.registry.validation.CompatibilityLevelValidation;
import io.streamthoughts.jikkou.schema.registry.validation.SchemaCompatibilityValidation;
import io.streamthoughts.jikkou.spi.AbstractExtensionProvider;
import org.jetbrains.annotations.NotNull;

/**
 * Extension provider for Schema Registry.
 */
@Named("SchemaRegistry")
public final class SchemaRegistryExtensionProvider extends AbstractExtensionProvider {

    /**
     * {@inheritDoc}
     **/
    @Override
    public void registerExtensions(@NotNull ExtensionRegistry registry) {
        // Collectors
        registry.register(SchemaRegistrySubjectCollector.class, SchemaRegistrySubjectCollector::new);

        // Controllers
        registry.register(SchemaRegistrySubjectController.class, SchemaRegistrySubjectController::new);

        // Validations
        registry.register(AvroSchemaValidation.class, AvroSchemaValidation::new);
        registry.register(SchemaCompatibilityValidation.class, SchemaCompatibilityValidation::new);
        registry.register(CompatibilityLevelValidation.class, CompatibilityLevelValidation::new);

        // Transformations
        registry.register(NormalizeSubjectSchemaTransformation.class, NormalizeSubjectSchemaTransformation::new);

        // Health indicators
        registry.register(SchemaRegistryHealthIndicator.class, SchemaRegistryHealthIndicator::new);
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public void registerResources(@NotNull ResourceRegistry registry) {
        registerResource(registry, V1SchemaRegistrySubject.class);
        registerResource(registry, V1SchemaRegistrySubjectList.class);
    }
}
