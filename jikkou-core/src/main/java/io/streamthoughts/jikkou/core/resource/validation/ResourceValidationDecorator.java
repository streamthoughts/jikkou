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
package io.streamthoughts.jikkou.core.resource.validation;

import io.streamthoughts.jikkou.core.models.HasMetadata;
import io.streamthoughts.jikkou.core.resource.ResourceInterceptorDecorator;
import java.util.List;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * This class can be used to decorate a validation with a different name and priority.
 *
 * @param <T> type of resources accepted by the validation.
 */
public class ResourceValidationDecorator<T extends HasMetadata>
        extends ResourceInterceptorDecorator<ResourceValidation<T>, ResourceValidationDecorator<T>>
        implements ResourceValidation<T> {

    /**
     * Creates a new {@link ResourceValidationDecorator} instance.
     *
     * @param validation the validation to delegate, must not be {@code null}.
     */
    public ResourceValidationDecorator(final @NotNull ResourceValidation<T> validation) {
        super(validation);
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public ValidationResult validate(@NotNull final List<T> resources) {
        return withEnrichedValidationError(null, extension.validate(resources));
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public ValidationResult validate(@NotNull final T resource) {
        return withEnrichedValidationError(resource, extension.validate(resource));
    }

    @NotNull
    private ValidationResult withEnrichedValidationError(@Nullable T resource,
                                                         @NotNull ValidationResult result) {
        if (result.isValid()) return result;
        List<EnrichedValidationError> errors = result.errors().stream()
                .map(err -> new EnrichedValidationError(
                        getName(),
                        Optional.ofNullable(err.resource()).orElse(resource),
                        err.message(),
                        err.details()
                )).toList();
        return new ValidationResult(errors);
    }
}
