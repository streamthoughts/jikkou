/*
 * Copyright 2021 StreamThoughts.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.streamthoughts.kafka.specs.validations;

import io.streamthoughts.kafka.specs.error.KafkaSpecsException;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * The top-level validation exception.
 *
 * @see Validation
 */
public class ValidationException extends KafkaSpecsException {

    private final Validation validation;
    private final List<ValidationException> errors;
    private String suffixMessage = "";
    private String errorSuffixMessage = "";

    public ValidationException(final List<ValidationException> errors) {
        this(null, null, errors);
    }

    public ValidationException(final String message, final Validation validation) {
        this(message, validation, null);
    }

    private ValidationException(final String message,
                                final Validation validation,
                                final List<ValidationException> errors) {
        super(message);
        this.validation = validation;
        this.errors = Optional.ofNullable(errors).orElse(Collections.emptyList());
    }

    public Optional<Validation> getValidation() {
        return Optional.ofNullable(validation);
    }

    public List<ValidationException> getErrors() {
        return errors;
    }

    /**
     * Sets the suffix to be used for formatting the returned message.
     *
     * @param suffixMessage     the suffix to set.
     * @return                  {@code this}.
     */
    public ValidationException suffixMessage(final String suffixMessage) {
        this.suffixMessage = suffixMessage;
        return this;
    }

    /**
     * Sets the suffix to be used for formatting the returned message containing nested errors.
     *
     * @param errorSuffixMessage     the suffix to set.
     * @return                       {@code this}.
     */
    public ValidationException errorSuffixMessage(final String errorSuffixMessage) {
        this.errorSuffixMessage = errorSuffixMessage;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getMessage() {
        final String message;
        if (!errors.isEmpty()) {
            message = errors.stream()
                .map(e -> e.suffixMessage(errorSuffixMessage).getMessage())
                .collect(Collectors.joining("\n"));
        } else {
            message = getFormattedMessage();
        }
        return String.format("%s%s", suffixMessage, message);
    }

    private String getFormattedMessage() {
        final String message = super.getMessage();
        return Optional.ofNullable(validation)
        .map(Validation::name).map(s -> String.format("[%s]: %s", s, message))
        .orElse(message);
    }
}
