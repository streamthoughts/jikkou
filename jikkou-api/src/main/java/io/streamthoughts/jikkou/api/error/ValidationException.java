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
package io.streamthoughts.jikkou.api.error;

import io.streamthoughts.jikkou.api.validation.ResourceValidation;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * The top-level validation exception.
 *
 * @see ResourceValidation
 */
public class ValidationException extends JikkouRuntimeException {

    private final String suffixMessage = String.format("%s:%n%n", ValidationException.class.getName());

    private final String name;
    private final List<ValidationException> errors;

    /**
     * Creates a new {@link ValidationException} with the specified exceptions.
     *
     * @param errors    the list of exceptions.
     */
    public ValidationException(final List<ValidationException> errors) {
        this(null, null, errors);
    }

    /**
     * Creates a new {@link ValidationException} for the given message and validation.
     *
     * @param message   the error message.
     * @param validation the validation that failed.
     */
    public ValidationException(final String message,
                               final ResourceValidation<?> validation) {
        this(message, validation.getName());
    }

    /**
     * Creates a new {@link ValidationException} for the given message and validation.
     *
     * @param message   the error message.
     * @param validation the name of the validation that failed.
     */
    public ValidationException(final String message,
                               final String validation) {
        this(message, validation, null);
    }

    private ValidationException(final String message,
                                final String name,
                                final List<ValidationException> errors) {
        super(message);
        this.name = name;
        this.errors = Optional.ofNullable(errors).orElse(Collections.emptyList());
    }

    /**
     * Gets name of the validation that failed, if this instance was created
     * using the constructor {@link ValidationException#ValidationException(String, ResourceValidation)},
     * otherwise this method returns {@code null}.
     *
     * @return  the validation name or {@code null}.
     */
    public String getName() {
        return name;
    }

    /**
     * Gets all the errors attached to this exception, if this instance was created
     * using the constructor {@link ValidationException#ValidationException(List)},
     * otherwise this method returns an empty list.
     *
     * @return  a list of exceptions.
     */
    public List<ValidationException> getExceptions() {
        return errors;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getMessage() {
        if (errors == null || errors.isEmpty()) {
            return super.getMessage();
        }
        String message = asList()
                .stream()
                .map(ValidationException::getFormattedMessage)
                .collect(Collectors.joining());
        return String.format("%s%s", suffixMessage, message);
    }

    private String getFormattedMessage() {
        final String message = super.getMessage();
        return Optional.ofNullable(name)
        .map(s -> String.format("- %s: %s%n", s, message))
        .orElse(message);
    }

    public List<ValidationException> asList() {
        if (this.errors == null || this.errors.isEmpty()) {
            return List.of(this);
        }
        return this.errors
                .stream()
                .map(ValidationException::asList)
                .flatMap(Collection::stream)
                .toList();
    }
}
