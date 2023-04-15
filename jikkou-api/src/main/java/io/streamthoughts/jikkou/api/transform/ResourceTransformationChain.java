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
package io.streamthoughts.jikkou.api.transform;

import io.streamthoughts.jikkou.api.model.HasItems;
import io.streamthoughts.jikkou.api.model.HasMetadata;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;

/**
 * Transform an input {@link HasMetadata} into one ore multiple {@link HasMetadata}.
 */
public class ResourceTransformationChain implements ResourceTransformation<HasMetadata> {

    private final List<ResourceTransformation<HasMetadata>> transformations;

    public ResourceTransformationChain(final List<ResourceTransformation<HasMetadata>> transformations) {
        this.transformations = transformations;
    }

    public @NotNull List<HasMetadata> transformAll(@NotNull List<HasMetadata> toTransform,
                                                   @NotNull HasItems otherResources) {
        return toTransform.stream()
                .map(resource -> transform(resource, otherResources))
                .flatMap(Optional::stream)
                .toList();
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public @NotNull Optional<HasMetadata> transform(@NotNull HasMetadata toTransform, @NotNull HasItems otherResources) {
        Optional<HasMetadata> result = Optional.of(toTransform);
        Iterator<ResourceTransformation<HasMetadata>> iterator = transformations.iterator();
        while (iterator.hasNext() && result.isPresent()) {
            ResourceTransformation<HasMetadata> transformation = iterator.next();
            if (transformation.canAccept(toTransform)) {
                result = transformation.transform(result.get(), otherResources);
            }
        }
        return result;
    }
}
