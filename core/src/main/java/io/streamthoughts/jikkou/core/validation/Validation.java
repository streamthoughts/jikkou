/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.validation;

import io.streamthoughts.jikkou.common.annotation.InterfaceStability.Evolving;
import io.streamthoughts.jikkou.core.annotation.Reflectable;
import io.streamthoughts.jikkou.core.exceptions.ValidationException;
import io.streamthoughts.jikkou.core.extension.ExtensionCategory;
import io.streamthoughts.jikkou.core.extension.annotations.Category;
import io.streamthoughts.jikkou.core.models.HasMetadata;
import io.streamthoughts.jikkou.core.resource.Interceptor;
import io.streamthoughts.jikkou.core.transform.Transformation;
import java.util.LinkedList;
import java.util.List;
import org.jetbrains.annotations.NotNull;

/**
 * Interface for validating all resources of the same before executing any
 * reconciliation operation. Validations are always applied after transformations.
 *
 * @param <T> type of resources accepted by the validation.
 * @see Transformation
 */
@Reflectable
@Evolving
@Category(ExtensionCategory.VALIDATION)
public interface Validation<T extends HasMetadata> extends Interceptor {

    /**
     * Validates the given the resource.
     *
     * @param resources the resource objects to validate.
     * @return The ValidationResult.
     */
    default ValidationResult validate(@NotNull final List<T> resources) {
        List<ValidationError> errors = new LinkedList<>();
        for (T resource : resources) {
            try {
                ValidationResult rs = validate(resource);
                errors.addAll(rs.errors());
            } catch (ValidationException e) {
                errors.add(new ValidationError(getName(), resource, e.getLocalizedMessage()));
            }
        }
        if (errors.isEmpty()) return ValidationResult.success();

        return new ValidationResult(errors);
    }

    /**
     * Validates the specified resource.
     *
     * @param resource The resource to be validated.
     * @return The ValidationResult.
     */
    default ValidationResult validate(@NotNull final T resource) {
        return ValidationResult.success();
    }
}
