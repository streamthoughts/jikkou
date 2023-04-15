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

import io.streamthoughts.jikkou.api.error.ValidationException;
import io.streamthoughts.jikkou.api.model.GenericResourceListObject;
import io.streamthoughts.jikkou.api.model.HasMetadata;
import io.streamthoughts.jikkou.api.model.HasMetadataAcceptableList;
import io.streamthoughts.jikkou.api.model.ResourceType;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.NotNull;

/**
 * Transform an input {@link HasMetadata} into one ore multiple {@link HasMetadata}.
 */
public class ResourceValidationChain implements ResourceValidation<HasMetadata> {

    private final HasMetadataAcceptableList<? extends ResourceValidation<HasMetadata>> validations;

    public ResourceValidationChain(final List<? extends ResourceValidation<HasMetadata>> validations) {
        this.validations = new HasMetadataAcceptableList<>(validations);
    }

    /** {@inheritDoc} **/
    @Override
    public void validate(@NotNull final List<HasMetadata> resources) {

        Map<ResourceType, List<HasMetadata>> grouped = new GenericResourceListObject(resources).groupByType();

        List<ValidationException> exceptions = new ArrayList<>(resources.size());
        for (Map.Entry<ResourceType, List<HasMetadata>> entry : grouped.entrySet()) {
            List<? extends ResourceValidation<HasMetadata>> resourceValidations
                    = validations.allResourcesAccepting(entry.getKey())
                    .getItems();
            for (ResourceValidation<HasMetadata> validation : resourceValidations) {
                try {
                    validation.validate(resources);
                } catch (ValidationException e) {
                    if (e.getErrors() != null && !e.getErrors().isEmpty()) {
                        exceptions.addAll(e.getErrors());
                    } else {
                        exceptions.add(e);
                    }
                }
            }
        }
        if (!exceptions.isEmpty()) {
            throw new ValidationException(exceptions);
        }
    }
}
