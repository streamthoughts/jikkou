/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.exceptions;

import io.streamthoughts.jikkou.core.validation.Validation;
import io.streamthoughts.jikkou.core.validation.ValidationError;
import java.util.List;

/**
 * The top-level validation exception.
 *
 * @see Validation
 */
public class ValidationException extends JikkouRuntimeException {
    private final List<ValidationError> errors;

    /**
     * Creates a new {@link ValidationException} with the specified exceptions.
     *
     * @param errors    the list of exceptions.
     */
    public ValidationException(final List<ValidationError> errors) {
        super();
        this.errors = errors;
    }

    public List<ValidationError> errors() {
        return errors;
    }
}
