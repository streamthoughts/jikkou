/*
 * Copyright 2021 The original authors
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

import io.streamthoughts.jikkou.common.annotation.InterfaceStability.Evolving;
import io.streamthoughts.jikkou.core.annotation.Category;
import io.streamthoughts.jikkou.core.annotation.Reflectable;
import io.streamthoughts.jikkou.core.exceptions.ValidationException;
import io.streamthoughts.jikkou.core.models.HasMetadata;
import io.streamthoughts.jikkou.core.resource.ResourceInterceptor;
import io.streamthoughts.jikkou.core.resource.transform.ResourceTransformation;
import java.util.LinkedList;
import java.util.List;
import org.jetbrains.annotations.NotNull;

/**
 * This interface is used to validate all resources of the same before executing any
 * reconciliation operation. Validations are always applied after transformations.
 *
 * @see ResourceTransformation
 *
 * @param <T> type of resources accepted by the validation.
 */
@Reflectable
@Evolving
@Category("Validation")
public interface ResourceValidation<T extends HasMetadata> extends ResourceInterceptor {

    /**
     * Validates the given the resource.
     *
     * @param resources              the resource objects to validate.
     * @throws ValidationException   if the given {@link HasMetadata} object is not valid.
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
     * Validates the given the resource.
     *
     * @param resource              the resource object to validate.
     * @throws ValidationException  if the given {@link HasMetadata} object is not valid.
     */
    default ValidationResult validate(@NotNull final T resource)  {
        return ValidationResult.success();
    }
}
