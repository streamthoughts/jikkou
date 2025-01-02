/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.validation;

import io.streamthoughts.jikkou.core.exceptions.ValidationException;
import io.streamthoughts.jikkou.core.models.CoreAnnotations;
import io.streamthoughts.jikkou.core.models.HasMetadata;
import io.streamthoughts.jikkou.core.models.ResourceList;
import io.streamthoughts.jikkou.core.models.ResourceType;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Transform an input {@link HasMetadata} into one ore multiple {@link HasMetadata}.
 */
public final class ValidationChain implements Validation<HasMetadata> {

    private static final Logger LOG = LoggerFactory.getLogger(ValidationChain.class);

    private final List<Validation> validations;

    /**
     * Creates a new {@link ValidationChain} instance.
     *
     * @param validations the list of validations.
     */
    public ValidationChain(final List<Validation> validations) {
        this.validations = Objects.requireNonNull(validations, "validations can't be null");
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public ValidationResult validate(@NotNull final List<HasMetadata> resources) {
        LOG.info("Starting validation-chain execution on {} resources", resources.size());
        return validate(ResourceList.of(resources).groupByType());
    }

    @SuppressWarnings("unchecked")
    public ValidationResult validate(@NotNull final Map<ResourceType, List<HasMetadata>> resources) {
        List<ValidationError> errors = new LinkedList<>();
        for (Map.Entry<ResourceType, List<HasMetadata>> entry : resources.entrySet()) {
            ResourceType type = entry.getKey();
            List<HasMetadata> resourcesHavingSameType = entry.getValue();

            for (Validation<HasMetadata> validation : validations) {
                try {
                    if (validation.canAccept(type)) {
                        List<HasMetadata> hasMetadata = filterCandidateToValidation(resourcesHavingSameType);
                        ValidationResult rs = validation.validate(hasMetadata);
                        errors.addAll(rs.errors());
                        LOG.info("Completed validation {} on resources of type: group={}, version={} and kind={}",
                                validation.getName(),
                                type.group(),
                                type.apiVersion(),
                                type.kind()
                        );
                    }
                } catch (ValidationException e) {
                    errors.add(new ValidationError(e.getLocalizedMessage()));
                }
            }
        }
        if (errors.isEmpty()) return ValidationResult.success();

        return new ValidationResult(errors);
    }

    @NotNull
    private static List<HasMetadata> filterCandidateToValidation(@NotNull List<HasMetadata> resources) {
        return resources.stream()
                .filter(Predicate.not(CoreAnnotations::isAnnotatedWithByPassValidation))
                .collect(Collectors.toList());
    }
}
