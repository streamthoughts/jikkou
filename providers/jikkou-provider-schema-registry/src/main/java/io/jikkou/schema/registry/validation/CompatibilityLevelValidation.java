/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.jikkou.schema.registry.validation;

import io.jikkou.core.annotation.Description;
import io.jikkou.core.annotation.SupportedResource;
import io.jikkou.core.annotation.Title;
import io.jikkou.core.config.ConfigProperty;
import io.jikkou.core.config.Configuration;
import io.jikkou.core.exceptions.ConfigException;
import io.jikkou.core.exceptions.ValidationException;
import io.jikkou.core.extension.ExtensionContext;
import io.jikkou.core.validation.Validation;
import io.jikkou.core.validation.ValidationError;
import io.jikkou.core.validation.ValidationResult;
import io.jikkou.schema.registry.model.CompatibilityLevels;
import io.jikkou.schema.registry.models.V1SchemaRegistrySubject;
import java.util.List;
import org.jetbrains.annotations.NotNull;

@Title("Validate compatibility level")
@Description("Validates that Schema Registry subject resources specify a valid schema compatibility level.")
@SupportedResource(type = V1SchemaRegistrySubject.class)
public class CompatibilityLevelValidation implements Validation<V1SchemaRegistrySubject> {

    public static final ConfigProperty<List<CompatibilityLevels>> VALIDATION_COMPATIBILITY_CONFIG = ConfigProperty
            .ofList("compatibilityLevels")
            .displayName("Accepted Compatibility Levels")
            .description("Set of compatibility levels accepted for subject schemas.")
            .map(l -> l.stream().map(String::toUpperCase).map(CompatibilityLevels::valueOf).toList());

    private List<CompatibilityLevels> accepted;

    /**
     * {@inheritDoc}
     */
    @Override
    public void init(@NotNull final ExtensionContext context) {
        final Configuration config = context.configuration();

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
