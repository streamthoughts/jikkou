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
package io.streamthoughts.jikkou.api.processor;

import io.streamthoughts.jikkou.api.error.JikkouException;
import io.streamthoughts.jikkou.api.model.V1SpecFile;
import io.streamthoughts.jikkou.api.model.V1SpecObject;
import io.streamthoughts.jikkou.api.transforms.ApplyConfigMapsTransformation;
import io.streamthoughts.jikkou.api.transforms.Transformation;
import io.streamthoughts.jikkou.api.validations.NoDuplicateRolesAllowedValidation;
import io.streamthoughts.jikkou.api.validations.NoDuplicateTopicsAllowedValidation;
import io.streamthoughts.jikkou.api.validations.NoDuplicateUsersAllowedValidation;
import io.streamthoughts.jikkou.api.validations.QuotasEntityValidation;
import io.streamthoughts.jikkou.api.validations.Validation;
import io.streamthoughts.jikkou.api.validations.ValidationException;
import io.vavr.Lazy;
import io.vavr.collection.List;
import io.vavr.control.Try;
import org.jetbrains.annotations.NotNull;

import static com.google.common.base.Predicates.instanceOf;
import static io.vavr.API.$;
import static io.vavr.API.Case;
import static io.vavr.API.Match;

/**
 * Default implementation for the {@link Processor}.
 */
public final class DefaultProcessor implements Processor<DefaultProcessor> {

    private final java.util.List<Lazy<Transformation>> transformations;

    private final java.util.List<Lazy<Validation>> validations;

    /**
     * Creates a new {@link DefaultProcessor} instance for the specified {@code config}.
     */
    public DefaultProcessor() {
        this(builtInTransformations(), builtInValidations());
    }

    private static java.util.List<Lazy<Transformation>> builtInTransformations() {
        return java.util.List.of(
                Lazy.of(ApplyConfigMapsTransformation::new),
                Lazy.of(ApplyConfigMapsTransformation::new)
        );
    }

    private static java.util.List<Lazy<Validation>> builtInValidations() {
        return java.util.List.of(
                Lazy.of(NoDuplicateTopicsAllowedValidation::new),
                Lazy.of(NoDuplicateUsersAllowedValidation::new),
                Lazy.of(NoDuplicateRolesAllowedValidation::new),
                Lazy.of(QuotasEntityValidation::new)
        );
    }

    /**
     * Creates a new {@link DefaultProcessor} instance.
     *
     * @param transformations the list of {@link Transformation} to register.
     * @param validations     the list of {@link Validation} to register.
     */
    private DefaultProcessor(final @NotNull java.util.List<Lazy<Transformation>> transformations,
                             final @NotNull java.util.List<Lazy<Validation>> validations) {
        this.transformations = transformations;
        this.validations = validations;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull DefaultProcessor withTransformation(@NotNull final Lazy<Transformation> transformation) {
        return new DefaultProcessor(
                List.ofAll(transformations).append(transformation).toJavaList(),
                validations
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull DefaultProcessor withValidation(@NotNull final Lazy<Validation> validation) {
        return new DefaultProcessor(
                transformations,
                List.ofAll(validations).append(validation).toJavaList()
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull V1SpecFile apply(@NotNull final V1SpecFile file) {

        // (1) run all transformations onto the given V1SpecsObject
        final V1SpecObject v1SpecsObject = List.ofAll(transformations)
                .map(Lazy::get)
                .foldLeft(file.spec(), (specsObject, transformation) -> transformation.transform(specsObject));

        // (2) run all validations and get all ValidationExceptions
        final java.util.List<ValidationException> errors = List.ofAll(validations)
                .map(Lazy::get)
                .map(validation -> Try.run(() -> validation.validate(v1SpecsObject)))
                .map(Try::failed)
                .flatMap(Try::toOption)
                .map(throwable -> Match(throwable).of(
                        Case($(instanceOf(ValidationException.class)), t -> (ValidationException) t),
                        Case($(), t -> {
                            throw new JikkouException(t);
                        })
                ))
                .toJavaList();

        // (3) may throw all validation errors
        if (!errors.isEmpty()) {
            throw new ValidationException(errors).withSuffixMessage("Validation rule violations:");
        }

        return new V1SpecFile(file.metadata(), v1SpecsObject);
    }
}
