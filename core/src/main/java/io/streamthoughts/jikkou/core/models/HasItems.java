/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.streamthoughts.jikkou.core.exceptions.JikkouRuntimeException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a collection of resources.
 */
public interface HasItems {

    /**
     * Gets the resource object items.
     *
     * @return the items.
     */
    List<? extends HasMetadata> getItems();

    /**
     * Gets all the resources matching the given kind.
     *
     * @param resourceClass the class of the resource.
     * @return the filtered resources.
     */
    default List<? extends HasMetadata> getAllByKind(Class<? extends HasMetadata> resourceClass) {
        return getAllByKind(Resource.getKind(resourceClass));
    }

    /**
     * Gets all the resources matching the given kind.
     *
     * @param kind the Kind of the resource.
     * @return the filtered resources.
     */
    default List<? extends HasMetadata> getAllByKind(@NotNull final String kind) {
        return getAllByKinds(kind);
    }

    /**
     * Gets all the resources matching the given kinds.
     *
     * @param kinds the list of Kinds of the resources.
     * @return the filtered resources.
     */
    default List<? extends HasMetadata> getAllByKinds(@NotNull final String... kinds) {
        return getAllByKinds(Arrays.asList(kinds));
    }

    /**
     * Gets all the resources matching the given kinds.
     *
     * @param kinds the list of Kinds of the resources.
     * @return the filtered resources.
     */
    default List<? extends HasMetadata> getAllByKinds(@NotNull final List<String> kinds) {
        return Resources.allByKinds(getItems(), kinds);
    }

    /**
     * Gets all the resources matching the given type.
     *
     * @param type the resource type.
     * @return the filtered resources.
     */
    default List<? extends HasMetadata> getAllByType(@NotNull final ResourceType type) {
        return Resources.allByType(getItems(), type);
    }

    /**
     * Gets all the resources matching the given version.
     *
     * @param version the Version of the resource.
     * @return the filtered resources.
     */
    default List<? extends HasMetadata> getAllByApiVersion(@NotNull final String version) {
        return Resources.allByApiVersion(getItems(), version);
    }

    /**
     * Gets all the resources grouped by type.
     *
     * @return the grouped resources.
     */
    @JsonIgnore
    default Map<ResourceType, List<HasMetadata>> groupByType() {
        return Resources.groupByType(getItems());
    }

    /**
     * Finds a resource for the given name.
     *
     * @param name the resource name.
     * @return an optional of the resource.
     */
    default Optional<? extends HasMetadata> findByName(@NotNull final String name) {
        return Resources.findByName(getItems(), name);
    }

    /**
     * Gets the resource for the given name and type.
     *
     * @param name the resource name.
     * @param type the class-type of the resource.
     * @param <T>  the type of the resource.
     * @return the resource.
     * @throws JikkouRuntimeException if no resource exists for the given name and type.
     */
    default <T extends HasMetadata> T getByName(@NotNull final String name, final Class<T> type) {
        return Resources.getByName(getItems(), name, type);
    }

    default <T extends HasMetadata> List<T> getAllByClass(final Class<T> clazz) {
        return Resources.getAllByClass(getItems(), clazz);
    }
}
