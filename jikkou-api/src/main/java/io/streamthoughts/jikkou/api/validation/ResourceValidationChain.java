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
package io.streamthoughts.jikkou.api.validation;

import io.streamthoughts.jikkou.JikkouMetadataAnnotations;
import io.streamthoughts.jikkou.api.error.ValidationException;
import io.streamthoughts.jikkou.api.model.GenericResourceListObject;
import io.streamthoughts.jikkou.api.model.HasMetadata;
import io.streamthoughts.jikkou.api.model.HasPriority;
import io.streamthoughts.jikkou.api.model.ResourceType;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;

/**
 * Transform an input {@link HasMetadata} into one ore multiple {@link HasMetadata}.
 */
public class ResourceValidationChain implements ResourceValidation<HasMetadata> {

    private final List<ResourceValidation<HasMetadata>> validations;

    public ResourceValidationChain(final List<ResourceValidation<HasMetadata>> validations) {
        this.validations = validations
                .stream()
                .sorted(Comparator.comparing(HasPriority::getPriority))
                .toList();
    }

    /** {@inheritDoc} **/
    @Override
    public void validate(@NotNull final List<HasMetadata> resources) {

        List<HasMetadata> filtered = resources.stream()
                .filter(Predicate.not(JikkouMetadataAnnotations::isAnnotatedWithByPassValidation))
                .collect(Collectors.toList());

        Map<ResourceType, List<HasMetadata>> grouped = new GenericResourceListObject(filtered)
                .groupByType();

        List<ValidationException> exceptions = new ArrayList<>(resources.size());
        for (Map.Entry<ResourceType, List<HasMetadata>> entry : grouped.entrySet()) {
            ResourceType type = entry.getKey();
            List<HasMetadata> resourcesHavingSameType = entry.getValue();
            for (ResourceValidation<HasMetadata> validation : validations) {
                try {
                    if (validation.canAccept(type)) {
                        validation.validate(resourcesHavingSameType);
                    }
                } catch (ValidationException e) {
                    exceptions.add(e);
                }
            }
        }
        if (!exceptions.isEmpty()) {
            throw new ValidationException(exceptions);
        }
    }
}
