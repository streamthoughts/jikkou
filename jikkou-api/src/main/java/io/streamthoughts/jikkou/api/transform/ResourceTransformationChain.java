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
package io.streamthoughts.jikkou.api.transform;

import io.streamthoughts.jikkou.api.ReconciliationContext;
import io.streamthoughts.jikkou.api.model.HasItems;
import io.streamthoughts.jikkou.api.model.HasMetadata;
import io.streamthoughts.jikkou.api.model.HasPriority;
import io.streamthoughts.jikkou.api.model.ResourceType;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Transform an input {@link HasMetadata} into one ore multiple {@link HasMetadata}.
 */
public class ResourceTransformationChain implements ResourceTransformation<HasMetadata> {

    private static final Logger LOG = LoggerFactory.getLogger(ResourceTransformationChain.class);

    private final List<ResourceTransformation<HasMetadata>> transformations;

    /**
     * Creates a new {@link ResourceTransformationChain} instance.
     *
     * @param transformations   the chain of transformations.
     */
    public ResourceTransformationChain(final List<ResourceTransformation<HasMetadata>> transformations) {
        this.transformations = transformations
                .stream()
                .sorted(Comparator.comparing(HasPriority::getPriority))
                .collect(Collectors.toList());
    }

    public @NotNull List<HasMetadata> transformAll(final @NotNull List<HasMetadata> toTransform,
                                                   final @NotNull HasItems otherResources,
                                                   final @NotNull ReconciliationContext context) {
        return toTransform.stream()
                .map(resource -> transform(resource, otherResources, context))
                .flatMap(Optional::stream)
                .toList();
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public @NotNull Optional<HasMetadata> transform(final @NotNull HasMetadata resource,
                                                    final @NotNull HasItems otherResources,
                                                    final @NotNull ReconciliationContext context) {
        Optional<HasMetadata> result = Optional.of(resource);
        Iterator<ResourceTransformation<HasMetadata>> iterator = transformations.iterator();
        while (iterator.hasNext() && result.isPresent()) {
            ResourceTransformation<HasMetadata> transformation = iterator.next();
            if (transformation.canAccept(ResourceType.create(resource))) {
                LOG.debug("Executing resource transformation '{}'.", transformation.getName());
                result = transformation.transform(result.get(), otherResources, context);
            }
        }
        return result;
    }
}
