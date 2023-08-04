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
package io.streamthoughts.jikkou.api.validation;

import io.streamthoughts.jikkou.annotation.ExtensionType;
import io.streamthoughts.jikkou.annotation.Reflectable;
import io.streamthoughts.jikkou.api.error.ValidationException;
import io.streamthoughts.jikkou.api.extensions.ResourceInterceptor;
import io.streamthoughts.jikkou.api.model.HasMetadata;
import io.streamthoughts.jikkou.api.transform.ResourceTransformation;
import io.streamthoughts.jikkou.common.annotation.InterfaceStability.Evolving;
import java.util.ArrayList;
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
@ExtensionType("Validation")
public interface ResourceValidation<T extends HasMetadata> extends ResourceInterceptor {

    /**
     * Validates the given the resource.
     *
     * @param resources              the resource objects to validate.
     * @throws ValidationException   if the given {@link HasMetadata} object is not valid.
     */
    default void validate(@NotNull final List<T> resources) throws ValidationException {
        List<ValidationException> exceptions = new ArrayList<>(resources.size());
        for (T resource : resources) {
            try {
                validate(resource);
            } catch (ValidationException e) {
                exceptions.add(e);
            }
        }
        if (!exceptions.isEmpty()) {
            throw new ValidationException(exceptions);
        }
    }

    /**
     * Validates the given the resource.
     *
     * @param resource              the resource object to validate.
     * @throws ValidationException  if the given {@link HasMetadata} object is not valid.
     */
    default void validate(@NotNull final T resource) throws ValidationException {}
}
