/*
 * Copyright 2022 StreamThoughts.
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
package io.streamthoughts.jikkou.api.model;

import io.streamthoughts.jikkou.api.annotations.AcceptsResource;
import io.streamthoughts.jikkou.api.converter.ResourceConverter;
import io.streamthoughts.jikkou.api.error.JikkouRuntimeException;
import io.streamthoughts.jikkou.common.annotation.AnnotationResolver;
import io.streamthoughts.jikkou.common.utils.Classes;
import java.util.List;
import java.util.function.Predicate;
import org.jetbrains.annotations.NotNull;

public interface HasMetadataAcceptable {

    /**
     * Checks whether a given resource-type is acceptable.
     *
     * @param type      the type of the resource.
     * @return          {@code true} if the given resource is acceptable.
     */
    default boolean canAccept(@NotNull ResourceType type) {
        return canAccept(this, resourceType -> resourceType.canAccept(type));
    }

    default ResourceConverter<HasMetadata, HasMetadata> getResourceConverter(@NotNull HasMetadata resource) {
        return getResourceConverter(ResourceType.create(resource.getClass()));
    }

    @SuppressWarnings("unchecked")
    default ResourceConverter<HasMetadata, HasMetadata> getResourceConverter(@NotNull ResourceType resource) {
        List<AcceptsResource> annotations = AnnotationResolver
                .findAllAnnotationsByType(this.getClass(), AcceptsResource.class);

        return annotations.stream()
                .filter(annot -> {
                    var type = annot.type() != HasMetadata.class ?
                            ResourceType.create(annot.type()) :
                            ResourceType.create(annot.kind(), annot.version());
                    return type.canAccept(resource);
                })
                .findFirst()
                .map(AcceptsResource::converter)
                .map(Classes::newInstance)
                .orElseThrow(() -> new JikkouRuntimeException(
                        "Cannot found any converter for type '" + resource.getKind()  + "'"
                ));
    }


    /**
     * Gets the acceptable resource types.
     *
     * @param clazz the class accepting resources.
     * @return      the list of acceptable types.
     */
    static List<ResourceType> getAcceptedResources(final Class<?> clazz) {
        List<AcceptsResource> acceptsResources = AnnotationResolver
                .findAllAnnotationsByType(clazz, AcceptsResource.class);

        return acceptsResources.stream()
                .map(accept -> {
                    if (accept.type() != HasMetadata.class) {
                        return ResourceType.create(accept.type());
                    }
                    return ResourceType.create(accept.kind(), accept.version());
                })
                .toList();
    }

    private static boolean canAccept(@NotNull HasMetadataAcceptable acceptable,
                                     @NotNull Predicate<ResourceType> predicate) {
        List<ResourceType> resources = getAcceptedResources(acceptable.getClass());
        return resources.isEmpty() || resources.stream().anyMatch(predicate);
    }
}
