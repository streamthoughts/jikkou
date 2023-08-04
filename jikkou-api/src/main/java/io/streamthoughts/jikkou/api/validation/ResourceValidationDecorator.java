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
package io.streamthoughts.jikkou.api.validation;

import io.streamthoughts.jikkou.api.error.ValidationException;
import io.streamthoughts.jikkou.api.extensions.ResourceInterceptorDecorator;
import io.streamthoughts.jikkou.api.model.HasMetadata;
import java.util.List;
import org.jetbrains.annotations.NotNull;

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
    public void validate(@NotNull final List<T> resources) throws ValidationException {
        try {
            extension.validate(resources);
        } catch (ValidationException ex) {
            reThrow(ex);
        }
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public void validate(@NotNull final T resource) throws ValidationException {
        try {
            extension.validate(resource);
        } catch (ValidationException ex) {
            reThrow(ex);
        }
    }
    
    private void reThrow(final ValidationException ex) {
        List<ValidationException> exceptions = ex.asList()
                .stream()
                .map(e -> new ValidationException(e.getMessage(), getName()))
                .toList();
        throw new ValidationException(exceptions);
    }
}
