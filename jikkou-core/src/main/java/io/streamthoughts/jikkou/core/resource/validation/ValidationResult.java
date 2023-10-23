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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.jetbrains.annotations.NotNull;

/**
 *  A {@link ValidationResult} contains validation information either
 *  from a single {@link ResourceValidation} or from multiple {@link ResourceValidation} calls.
 *
 * @see ResourceValidation
 */
public class ValidationResult {

    public static ValidationResult failure(final ValidationError error) {
        return new ValidationResult(List.of(error));
    }

    public static ValidationResult success() {
        return VALID;
    }

    private static final ValidationResult VALID = new ValidationResult() {
        @Override
        public void addError(ValidationError error) {
            throw new UnsupportedOperationException("Cannot add error to successful validation result");
        }

        @Override
        public void addErrors(List<ValidationError> errors) {
            throw new UnsupportedOperationException("Cannot add error to successful validation result");
        }
    };

    private final List<ValidationError> errors;

    /**
     * Creates a new {@link ValidationResult} instance.
     */
    public ValidationResult() {
        this.errors = Collections.emptyList();
    }


    /**
     * Creates a new {@link ValidationResult} instance.
     */
    public ValidationResult(@NotNull List<? extends ValidationError> errors) {
        this.errors = new ArrayList<>(errors);
    }

    /**
     * Adds a given error to this result.
     *
     * @param error   the validation error.
     */
    public void addError(final ValidationError error) {
        this.errors.add(error);
    }

    /**
     * Adds a given errors to this result.
     *
     * @param errors   the validation errors.
     */
    public void addErrors(final List<ValidationError> errors) {
        this.errors.addAll(errors);
    }

    /**
     * Gets the list of errors.
     *
     * @return  the list of {@link ValidationError}.
     */
    public List<ValidationError> errors() {
        return Collections.unmodifiableList(errors);
    }

    /**
     * Checks if the validation was successful or not.
     *
     * @return  {@code true} if the validation was successful, otherwise {@code false}.
     */
    public boolean isValid() {
        return errors.isEmpty();
    }
}
