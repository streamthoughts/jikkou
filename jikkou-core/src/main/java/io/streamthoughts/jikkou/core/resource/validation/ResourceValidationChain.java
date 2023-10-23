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

import io.streamthoughts.jikkou.core.exceptions.ValidationException;
import io.streamthoughts.jikkou.core.models.CoreAnnotations;
import io.streamthoughts.jikkou.core.models.GenericResourceListObject;
import io.streamthoughts.jikkou.core.models.HasMetadata;
import io.streamthoughts.jikkou.core.models.HasPriority;
import io.streamthoughts.jikkou.core.models.ResourceType;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Transform an input {@link HasMetadata} into one ore multiple {@link HasMetadata}.
 */
public class ResourceValidationChain implements ResourceValidation<HasMetadata> {

    private static final Logger LOG = LoggerFactory.getLogger(ResourceValidationChain.class);

    private final List<ResourceValidation> validations;

    /**
     * Creates a new {@link ResourceValidationChain} instance.
     *
     * @param validations the list of validations.
     */
    public ResourceValidationChain(final List<ResourceValidation> validations) {
        this.validations = validations
                .stream()
                .sorted(Comparator.comparing(HasPriority::getPriority))
                .toList();
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public ValidationResult validate(@NotNull final List<HasMetadata> resources) {
        LOG.info("Starting validation-chain execution on {} resources", resources.size());
        return validate(new GenericResourceListObject<>(resources).groupByType());
    }

    public ValidationResult validate(@NotNull final Map<ResourceType, List<HasMetadata>> resources) {
        List<ValidationError> errors = new LinkedList<>();
        for (Map.Entry<ResourceType, List<HasMetadata>> entry : resources.entrySet()) {
            ResourceType type = entry.getKey();
            List<HasMetadata> resourcesHavingSameType = entry.getValue();
            for (ResourceValidation validation : validations) {
                try {
                    if (validation.canAccept(type)) {
                        ValidationResult rs = validation.validate(filterCandidateToValidation(resourcesHavingSameType));
                        errors.addAll(rs.errors());
                        LOG.info("Completed validation {} on resources of type: group={}, version={} and kind={}",
                                validation.getName(),
                                type.getGroup(),
                                type.getApiVersion(),
                                type.getKind()
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
