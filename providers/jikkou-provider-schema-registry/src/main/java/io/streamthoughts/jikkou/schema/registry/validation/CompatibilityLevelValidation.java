/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.schema.registry.validation;

import io.streamthoughts.jikkou.core.annotation.SupportedResource;
import io.streamthoughts.jikkou.core.config.ConfigProperty;
import io.streamthoughts.jikkou.core.config.Configuration;
import io.streamthoughts.jikkou.core.exceptions.ConfigException;
import io.streamthoughts.jikkou.core.exceptions.ValidationException;
import io.streamthoughts.jikkou.core.extension.ExtensionContext;
import io.streamthoughts.jikkou.core.validation.Validation;
import io.streamthoughts.jikkou.core.validation.ValidationError;
import io.streamthoughts.jikkou.core.validation.ValidationResult;
import io.streamthoughts.jikkou.schema.registry.model.CompatibilityLevels;
import io.streamthoughts.jikkou.schema.registry.models.V1SchemaRegistrySubject;
import java.util.List;
import org.jetbrains.annotations.NotNull;

@SupportedResource(type = V1SchemaRegistrySubject.class)
public class CompatibilityLevelValidation implements Validation<V1SchemaRegistrySubject> {

    public static final ConfigProperty<List<CompatibilityLevels>> VALIDATION_COMPATIBILITY_CONFIG = ConfigProperty
            .ofList("compatibilityLevels")
            .description("Set of compatibility levels accepted for subject schemas.")
            .map(l -> l.stream().map(String::toUpperCase).map(CompatibilityLevels::valueOf).toList());

    private List<CompatibilityLevels> accepted;

    /**
     * {@inheritDoc}
     */
    public void init(@NotNull final ExtensionContext context) {
        final Configuration config = context.appConfiguration();

        accepted = VALIDATION_COMPATIBILITY_CONFIG.getOptional(config)
                .orElseThrow(() -> new ConfigException(
                        String.format("The '%s' configuration property is required for %s",
                                VALIDATION_COMPATIBILITY_CONFIG.key(),
                                CompatibilityLevelValidation.class.getSimpleName()
                        )
                ));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ValidationResult validate(@NotNull V1SchemaRegistrySubject resource) throws ValidationException {
        CompatibilityLevels compatibilityLevel = resource.getSpec().getCompatibilityLevel();
        if (compatibilityLevel != null && !accepted.contains(compatibilityLevel)) {
            String error = String.format(
                    "Compatibility level '%s' is not accepted for SchemaRegistrySubject '%s'. Must be one of: %s",
                    compatibilityLevel,
                    resource.getMetadata().getName(),
                    accepted
            );
            return ValidationResult.failure(new ValidationError(getName(), resource, error));
        }
        return ValidationResult.success();
    }
}
