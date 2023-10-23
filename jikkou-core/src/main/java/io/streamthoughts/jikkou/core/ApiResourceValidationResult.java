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
package io.streamthoughts.jikkou.core;

import io.streamthoughts.jikkou.core.exceptions.ValidationException;
import io.streamthoughts.jikkou.core.models.HasMetadata;
import io.streamthoughts.jikkou.core.models.ResourceListObject;
import io.streamthoughts.jikkou.core.resource.validation.ValidationError;
import java.util.Collections;
import java.util.List;

public final class ApiResourceValidationResult {

    private final ResourceListObject<HasMetadata> resources;

    private final List<ValidationError> errors;

    /**
     * Creates a new {@link ApiResourceValidationResult} instance.
     *
     * @param resources the list of resources that was validated.
     */
    public ApiResourceValidationResult(ResourceListObject<HasMetadata> resources) {
        this(resources, null);
    }

    /**
     * Creates a new {@link ApiResourceValidationResult} instance.
     *
     * @param errors the list of validation errors.
     */
    public ApiResourceValidationResult(List<ValidationError> errors) {
        this(null, errors);
    }

    private ApiResourceValidationResult(ResourceListObject<HasMetadata> resources,
                                       List<ValidationError> errors) {
        this.resources = resources;
        this.errors = errors;
    }

    /**
     * Gets the results of the resource validation execution.
     *
     * @return  the {@link ResourceListObject}.
     * @throws ValidationException if validation constraint errors was returned during validation process.
     */
    public ResourceListObject<HasMetadata> get() {
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
