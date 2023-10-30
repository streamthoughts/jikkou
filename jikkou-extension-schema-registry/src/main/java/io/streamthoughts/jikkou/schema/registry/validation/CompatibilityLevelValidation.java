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
package io.streamthoughts.jikkou.schema.registry.validation;

import io.streamthoughts.jikkou.core.annotation.HandledResource;
import io.streamthoughts.jikkou.core.config.ConfigProperty;
import io.streamthoughts.jikkou.core.config.Configuration;
import io.streamthoughts.jikkou.core.exceptions.ConfigException;
import io.streamthoughts.jikkou.core.exceptions.ValidationException;
import io.streamthoughts.jikkou.core.validation.Validation;
import io.streamthoughts.jikkou.core.validation.ValidationError;
import io.streamthoughts.jikkou.core.validation.ValidationResult;
import io.streamthoughts.jikkou.schema.registry.model.CompatibilityLevels;
import io.streamthoughts.jikkou.schema.registry.models.V1SchemaRegistrySubject;
import java.util.List;
import org.jetbrains.annotations.NotNull;

@HandledResource(type = V1SchemaRegistrySubject.class)
public class CompatibilityLevelValidation implements Validation<V1SchemaRegistrySubject> {

    public static final ConfigProperty<List<CompatibilityLevels>> VALIDATION_COMPATIBILITY_CONFIG = ConfigProperty
            .ofList("compatibilityLevels")
            .description("Set of compatibility levels accepted for subject schemas.")
            .map(l -> l.stream().map(String::toUpperCase).map(CompatibilityLevels::valueOf).toList());

    private List<CompatibilityLevels> accepted;

    /**
     * {@inheritDoc}
     */
    @Override
    public void configure(@NotNull Configuration config) throws ConfigException {
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
