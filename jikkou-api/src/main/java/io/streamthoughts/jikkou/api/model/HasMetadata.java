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
package io.streamthoughts.jikkou.api.model;

import io.streamthoughts.jikkou.annotation.ApiVersion;
import io.streamthoughts.jikkou.annotation.Kind;
import io.streamthoughts.jikkou.annotation.Transient;
import io.streamthoughts.jikkou.common.annotation.AnnotationResolver;
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
     * @param objectMeta the object meta to set.
     * @return a new object.
     */
    HasMetadata withMetadata(ObjectMeta objectMeta);

    /**
     * Gets resource api version.
     *
     * @return the API Version of this resource.
     */
    String getApiVersion();

    /**
     * Gets resource kind.
     *
     * @return the kind of this resource.
     */
    String getKind();

    /**
     * Check whether this resource should not be part of the reconciliation process.
     *
     * @return {@link true} if this class is annotated with {@link Transient}, otherwise return {@link false}.
     */
    static boolean isTransient(final Class<? extends Resource> clazz) {
        return AnnotationResolver.isAnnotatedWith(clazz, Transient.class);
    }

    /**
     * Gets the Version of the given resource class.
     *
     * @param clazz the resource class for which to extract the Version.
     * @return the Version or {@code null}.
     */
    static String getApiVersion(final Class<? extends Resource> clazz) {
        ApiVersion version = clazz.getAnnotation(ApiVersion.class);
        if (version != null) {
            return version.value();
        }
        return null;
    }

    /**
     * Gets the Kind of the given resource class.
     *
     * @param clazz the resource class for which to extract the Kind.
     * @return the Kind or {@code null}.
     */
    static String getKind(final Class<? extends Resource> clazz) {
        Kind kind = clazz.getAnnotation(Kind.class);
        if (kind != null) {
            return kind.value();
        }
        return clazz.getSimpleName();
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
    static Optional<Object> getMetadataAnnotation(@NotNull HasMetadata resource,
                                                  @NotNull String annotationKey) {
        return resource.optionalMetadata()
                .stream()
                .map(meta -> meta.getAnnotation(annotationKey))
                .flatMap(Optional::stream)
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
