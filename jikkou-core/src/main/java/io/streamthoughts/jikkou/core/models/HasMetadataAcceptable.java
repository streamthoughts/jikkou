/*
 * Copyright 2022 The original authors
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
package io.streamthoughts.jikkou.core.models;

import io.streamthoughts.jikkou.common.annotation.AnnotationResolver;
import io.streamthoughts.jikkou.common.utils.Strings;
import io.streamthoughts.jikkou.core.annotation.HandledResource;
import java.util.List;
import org.jetbrains.annotations.NotNull;

/**
 *
 */
public interface HasMetadataAcceptable {

    /**
     * Checks whether a given resource-type is acceptable.
     *
     * @param type the type of the resource.
     * @return {@code true} if the given resource is acceptable.
     */
    default boolean canAccept(@NotNull ResourceType type) {
        List<ResourceType> resources = getAcceptedResources(this.getClass());
        return resources.isEmpty() || resources.stream().anyMatch(resourceType -> resourceType.canAccept(type));
    }

    /**
     * Gets the acceptable resource types.
     *
     * @param clazz the class accepting resources.
     * @return the list of acceptable types.
     */
    static List<ResourceType> getAcceptedResources(final Class<?> clazz) {
        List<HandledResource> annotations = AnnotationResolver
                .findAllAnnotationsByType(clazz, HandledResource.class);

        return annotations.stream()
                .map(accept -> {
                    if (accept.type() != HasMetadata.class) {
                        return ResourceType.of(accept.type());
                    }
                    if (!Strings.isBlank(accept.apiVersion())) {
                        return ResourceType.of(accept.kind(), accept.apiVersion());
                    }

                    if (!Strings.isBlank(accept.kind())) {
                        return ResourceType.of(accept.kind());
                    }

                    throw new IllegalArgumentException(
                            "Invalid 'AcceptsResource' annotation on class '" + clazz.getName() + "'." +
                                    " At least one of the following must be specified: type, apiVersion or kind."
                    );
                })
                .toList();
    }
}
