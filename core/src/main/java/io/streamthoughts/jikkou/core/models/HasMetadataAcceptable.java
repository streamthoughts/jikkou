/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.models;

import io.streamthoughts.jikkou.common.annotation.AnnotationResolver;
import io.streamthoughts.jikkou.common.utils.Strings;
import io.streamthoughts.jikkou.core.annotation.SupportedResource;
import java.util.List;
import org.jetbrains.annotations.NotNull;

/**
 *
 */
public interface HasMetadataAcceptable {

    /**
     * Checks whether a given resource-type is supported.
     *
     * @param type The type of the resource.
     * @return {@code true} if the given resource is supported.
     */
    default boolean canAccept(@NotNull ResourceType type) {
        List<ResourceType> resources = getSupportedResources(this.getClass());
        return resources.isEmpty() || resources.stream().anyMatch(resourceType -> resourceType. canAccept(type));
    }

    /**
     * Gets the supported resource types.
     *
     * @param clazz The class supporting resources.
     * @return The list of supporting types.
     */
    static List<ResourceType> getSupportedResources(final Class<?> clazz) {
        List<SupportedResource> annotations = AnnotationResolver
                .findAllAnnotationsByType(clazz, SupportedResource.class);

        return annotations.stream()
                .map(accept -> {
                    if (accept.type() != HasMetadata.class) {
                        return ResourceType.of(accept.type());
                    }
                    if (!Strings.isNullOrEmpty(accept.apiVersion())) {
                        return ResourceType.of(accept.kind(), accept.apiVersion());
                    }

                    if (!Strings.isNullOrEmpty(accept.kind())) {
                        return ResourceType.of(accept.kind());
                    }

                    throw new IllegalArgumentException(
                            "Invalid 'SupportedResource' annotation on class '" + clazz.getName() + "'." +
                                    " At least one of the following must be specified: type, apiVersion or kind."
                    );
                })
                .toList();
    }
}
