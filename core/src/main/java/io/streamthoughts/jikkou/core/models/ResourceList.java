/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.models;

import static java.util.stream.Collectors.groupingBy;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.streamthoughts.jikkou.core.annotation.Reflectable;
import io.streamthoughts.jikkou.core.models.generics.GenericResourceList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;

/**
 * List of resource objects.
 *
 * @param <E> The type of resource.
 */
@Reflectable
@JsonDeserialize(as = GenericResourceList.class)
public interface ResourceList<E extends HasMetadata>
        extends HasItems,
        Listeable<E>,
        HasMetadata {

    /**
     * Factory method to construct a new {@link ResourceList} instance.
     *
     * @return the new {@link ResourceList}.
     */
    static <T extends HasMetadata> ResourceList<T> empty() {
        return of(List.of());
    }

    /**
     * Factory method to construct a new {@link ResourceList} instance.
     *
     * @return the new {@link ResourceList}.
     */
    @SafeVarargs
    static <T extends HasMetadata> ResourceList<T> of(final T... resources) {
        return of(Arrays.asList(resources));
    }

    /**
     * Factory method to construct a new {@link ResourceList} instance.
     *
     * @return the new {@link ResourceList}.
     */
    static <T extends HasMetadata> ResourceList<T> of(final List<T> resources) {
        return new GenericResourceList.Builder<T>().withItems(resources).build();
    }

    /**
     * Gets the resource object items.
     *
     * @return the items.
     */
    @Override
    List<E> getItems();

    /**
     * Gets the resource object items.
     *
     * @return the items.
     */
    @Override
    ObjectMeta getMetadata();

    /**
     * Groups all elements using the key return by the given classifier function.
     *
     * @param classifier – the classifier function mapping input elements to keys
     *
     * @return the grouped elements.
     */
    @JsonIgnore
    default <K> Map<K, List<E>> groupBy(Function<E, K> classifier) {
        return getItems().stream().collect(groupingBy(classifier));
    }

    /**
     * Gets the resources grouped keyed by name.
     *
     * @return the resources.
     */
    default Map<String, E> keyByName() {
        return getItems()
            .stream()
            .collect(Collectors.toMap(i -> i.getMetadata().getName(), i -> i));
    }

    /**
     * Gets the {@link ResourceList} matching the given kind.
     *
     * @param resourceClass the class of the resource.
     * @return the filtered {@link ResourceList}.
     */
    @Override
    default List<E> getAllByKind(Class<? extends HasMetadata> resourceClass) {
        return getAllByKind(Resource.getKind(resourceClass));
    }

    /**
     * Gets the {@link ResourceList} matching the given kind.
     *
     * @param kind the Kind of the resource.
     * @return the filtered {@link ResourceList}.
     */
    @Override
    default List<E> getAllByKind(@NotNull final String kind) {
        return getAllByKinds(kind);
    }

    /**
     * Gets the {@link ResourceList} matching the given kinds.
     *
     * @param kinds the list of Kinds of the resources.
     * @return the filtered {@link ResourceList}.
     */
    @Override
    default List<E> getAllByKinds(@NotNull final String... kinds) {
        return getAllByKinds(Arrays.asList(kinds));
    }

    /**
     * Gets the {@link ResourceList} matching the given kinds.
     *
     * @param kinds the list of Kinds of the resources.
     * @return the filtered {@link ResourceList}.
     */
    @Override
    @SuppressWarnings("unchecked")
    default List<E> getAllByKinds(@NotNull final List<String> kinds) {
        return (List<E>) HasItems.super.getAllByKinds(kinds);
    }

    /**
     * Gets the {@link ResourceList} matching the given version.
     *
     * @param version the Version of the resource.
     * @return the filtered {@link ResourceList}.
     */
    @Override
    @SuppressWarnings("unchecked")
    default List<E> getAllByApiVersion(@NotNull final String version) {
        return (List<E>) HasItems.super.getAllByApiVersion(version);
    }

    @Override
    @SuppressWarnings("unchecked")
    default Optional<E> findByName(@NotNull final String name) {
        return (Optional<E>) HasItems.super.findByName(name);
    }
}
