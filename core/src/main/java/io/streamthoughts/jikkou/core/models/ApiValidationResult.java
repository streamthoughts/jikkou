/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.models;

import io.streamthoughts.jikkou.core.exceptions.ValidationException;
import io.streamthoughts.jikkou.core.validation.ValidationError;
import java.util.Collections;
import java.util.List;

public final class ApiValidationResult {

    private final ResourceList<HasMetadata> resources;

    private final List<ValidationError> errors;

    /**
     * Creates a new {@link ApiValidationResult} instance.
     *
     * @param resources the list of resources that was validated.
     */
    public ApiValidationResult(ResourceList<HasMetadata> resources) {
        this(resources, null);
    }

    /**
     * Creates a new {@link ApiValidationResult} instance.
     *
     * @param errors the list of validation errors.
     */
    public ApiValidationResult(List<ValidationError> errors) {
        this(null, errors);
    }

    private ApiValidationResult(ResourceList<HasMetadata> resources, List<ValidationError> errors) {
        this.resources = resources;
        this.errors = errors;
    }

    /**
     * Gets the results of the resource validation execution.
     *
     * @return  the {@link ResourceList}.
     * @throws ValidationException if validation constraint errors was returned during validation process.
     */
    public ResourceList<HasMetadata> get() {
        if (errors != null) {
            throw new ValidationException(errors);
        }
        return resources;
    }

    /**
     *  Gets the list of validation constraint errors returned during validation process.
     *
     * @return  the list of errors.
     */
    public List<ValidationError> errors() {
        return errors == null ? Collections.emptyList() : errors;
    }

    /**
     * Checks if the validation was successful or not.
     *
     * @return  {@code true} if the validation was successful, otherwise {@code false}.
     */
    public boolean isValid() {
        return errors == null;
    }
}
