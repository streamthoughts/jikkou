/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.validation;

import io.streamthoughts.jikkou.core.models.HasMetadata;
import io.streamthoughts.jikkou.core.resource.InterceptorDecorator;
import java.util.List;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * This class can be used to decorate a validation with a different name and priority.
 *
 * @param <T> type of resources accepted by the validation.
 */
public class ValidationDecorator<T extends HasMetadata>
        extends InterceptorDecorator<Validation<T>, ValidationDecorator<T>>
        implements Validation<T> {

    /**
     * Creates a new {@link ValidationDecorator} instance.
     *
     * @param validation the validation to delegate, must not be {@code null}.
     */
    public ValidationDecorator(final @NotNull Validation<T> validation) {
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
