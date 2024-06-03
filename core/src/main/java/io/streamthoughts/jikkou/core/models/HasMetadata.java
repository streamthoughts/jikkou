/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.models;

import io.streamthoughts.jikkou.common.annotation.InterfaceStability.Evolving;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;

/**
 * Represents an individual resource object.
 */
@Evolving
public interface HasMetadata extends Resource {

    /**
     * Gets resource metadata.
     *
     * @return the metadata of this resource object.
     */
    ObjectMeta getMetadata();

    /**
     * Gets a new object with metadata updated.
     *
     * @param metadata the object meta to set.
     * @return a new object.
     */
    default HasMetadata withMetadata(ObjectMeta metadata) {
        throw new UnsupportedOperationException();
    }

    /**
     * Gets the optional metadata.
     *
     * @return an optional metadata.
     */
    default Optional<ObjectMeta> optionalMetadata() {
        return Optional.ofNullable(getMetadata());
    }

    /**
     * Static helper method get a single metadata annotation from the given resource.
     *
     * @param resource      the resource.
     * @param annotationKey the key of the annotation
     * @return the optional value for the annotation.
     */
    static Optional<NamedValue> getMetadataAnnotation(@NotNull HasMetadata resource,
                                                      @NotNull String annotationKey) {
        return resource.optionalMetadata()
                .stream()
                .map(meta -> meta.findAnnotationByKey(annotationKey))
                .flatMap(Optional::stream)
                .map(value -> new NamedValue(annotationKey, value))
                .findFirst();
    }

    /**
     * Static helper method to add a single metadata annotation to the given resource.
     *
     * @param resource        the resource
     * @param annotationKey   the key of the annotation to add.
     * @param annotationValue the value of the annotation to add.
     * @return a new {@link HasMetadata}.
     */
    @NotNull
    @SuppressWarnings("unchecked")
    static <T extends HasMetadata> T addMetadataAnnotation(@NotNull T resource,
                                                           @NotNull String annotationKey,
                                                           @NotNull Object annotationValue) {
        ObjectMeta objectMeta = resource.optionalMetadata()
                .map(ObjectMeta::toBuilder)
                .or(() -> Optional.of(ObjectMeta.builder()))
                .map(builder -> builder.withAnnotation(annotationKey, annotationValue))
                .map(ObjectMeta.ObjectMetaBuilder::build)
                .get();
        return (T) resource.withMetadata(objectMeta);
    }

    static <T extends HasMetadata> List<T> sortByName(final List<T> resources) {
        return resources.stream()
                .sorted(Comparator.comparing(it -> it.getMetadata().getName()))
                .toList();
    }
}
