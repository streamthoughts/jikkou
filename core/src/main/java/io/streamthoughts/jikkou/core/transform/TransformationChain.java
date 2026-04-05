/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.transform;

import io.streamthoughts.jikkou.core.ReconciliationContext;
import io.streamthoughts.jikkou.core.models.CoreAnnotations;
import io.streamthoughts.jikkou.core.models.HasItems;
import io.streamthoughts.jikkou.core.models.HasMetadata;
import io.streamthoughts.jikkou.core.models.ObjectMeta;
import io.streamthoughts.jikkou.core.models.ResourceType;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Transform an input {@link HasMetadata} into one ore multiple {@link HasMetadata}.
 */
public final class TransformationChain implements Transformation<HasMetadata> {

    private static final Logger LOG = LoggerFactory.getLogger(TransformationChain.class);

    private final List<Transformation<HasMetadata>> transformations;

    /**
     * Creates a new {@link TransformationChain} instance.
     *
     * @param transformations   the chain of transformations.
     */
    public TransformationChain(final List<Transformation<HasMetadata>> transformations) {
        this.transformations = Objects.requireNonNull(transformations, "transformations can't be null");
    }

    public @NotNull List<HasMetadata> transformAll(final @NotNull List<HasMetadata> toTransform,
                                                   final @NotNull HasItems otherResources,
                                                   final @NotNull ReconciliationContext context) {
        LOG.info("Starting transformation-chain execution on {} resources", toTransform.size());
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
        // Capture the original jikkou.io/provider annotation — it is read-only across transformations.
        Optional<String> originalProvider = CoreAnnotations.findProviderAnnotation(resource);

        Optional<HasMetadata> result = Optional.of(resource);
        Iterator<Transformation<HasMetadata>> iterator = transformations.iterator();
        while (iterator.hasNext() && result.isPresent()) {
            Transformation<HasMetadata> transformation = iterator.next();
            ResourceType type = ResourceType.of(resource);
            if (transformation.canAccept(type)) {
                result = transformation.transform(result.get(), otherResources, context);
                LOG.info("Completed transformation '{}' on resource of type: group={}, version={} and kind={}",
                        transformation.getName(),
                        type.group(),
                        type.apiVersion(),
                        type.kind()
                );
            }
        }

        // Restore the jikkou.io/provider annotation if a transformation mutated or removed it.
        if (result.isPresent()) {
            result = Optional.of(restoreProviderAnnotation(result.get(), originalProvider));
        }
        return result;
    }

    /**
     * Restores the {@code jikkou.io/provider} annotation to its original value.
     * If the annotation was present before transformations, it is always restored.
     * If it was absent before transformations, any value added by a transformation is removed.
     */
    private HasMetadata restoreProviderAnnotation(HasMetadata resource, Optional<String> originalProvider) {
        Optional<String> currentProvider = CoreAnnotations.findProviderAnnotation(resource);
        if (originalProvider.equals(currentProvider)) {
            return resource;
        }
        if (originalProvider.isPresent()) {
            // Restore original value
            return HasMetadata.addMetadataAnnotation(resource,
                    CoreAnnotations.JIKKOU_IO_PROVIDER, originalProvider.get());
        }
        // Original had no provider annotation but a transformation added one — remove it
        ObjectMeta meta = resource.getMetadata();
        if (meta == null) {
            return resource;
        }
        var annotations = new java.util.HashMap<>(meta.getAnnotations());
        annotations.remove(CoreAnnotations.JIKKOU_IO_PROVIDER);
        ObjectMeta cleaned = ObjectMeta.builder()
                .withName(meta.getName())
                .withLabels(meta.getLabels())
                .withAnnotations(annotations)
                .build();
        return resource.withMetadata(cleaned);
    }
}
