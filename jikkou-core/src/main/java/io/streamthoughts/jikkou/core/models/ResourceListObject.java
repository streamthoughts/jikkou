/*
 * Copyright 2023 The original authors
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


import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.streamthoughts.jikkou.core.annotation.Reflectable;
import io.streamthoughts.jikkou.core.models.generics.GenericResourceListObject;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;

/**
 * List of resource objects.
 *
 * @param <E> The type of resource.
 */
@Reflectable
@JsonDeserialize(as = GenericResourceListObject.class)
public interface ResourceListObject<E extends HasMetadata>
        extends HasItems,
        Listeable<E>,
        HasMetadata {

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
     * Gets the resources grouped keyed by name.
     * @return  the resources.
     */
    default Map<String, E> keyByName() {
        return getItems()
                .stream()
                .collect(Collectors.toMap(i -> i.getMetadata().getName(), i -> i));
    }

    /**
     * Gets the {@link DefaultResourceListObject} matching the given kind.
     *
     * @param resourceClass the class of the resource.
     * @return the filtered {@link DefaultResourceListObject}.
     */
    default List<E> getAllByKind(Class<? extends HasMetadata> resourceClass) {
        return getAllByKind(Resource.getKind(resourceClass));
    }

    /**
     * Gets the {@link DefaultResourceListObject} matching the given kind.
     *
     * @param kind the Kind of the resource.
     * @return the filtered {@link DefaultResourceListObject}.
     */
    default List<E> getAllByKind(@NotNull final String kind) {
        return getAllByKinds(kind);
    }

    /**
     * Gets the {@link DefaultResourceListObject} matching the given kinds.
     *
     * @param kinds the list of Kinds of the resources.
     * @return the filtered {@link DefaultResourceListObject}.
     */
    default List<E> getAllByKinds(@NotNull final String... kinds) {
        return getAllByKinds(Arrays.asList(kinds));
    }

    /**
     * Gets the {@link DefaultResourceListObject} matching the given kinds.
     *
     * @param kinds the list of Kinds of the resources.
     * @return the filtered {@link DefaultResourceListObject}.
     */
    @SuppressWarnings("unchecked")
    default List<E> getAllByKinds(@NotNull final List<String> kinds) {
        return (List<E>) HasItems.super.getAllByKinds(kinds);
    }

    /**
     * Gets the {@link DefaultResourceListObject} matching the given version.
     *
     * @param version the Version of the resource.
     * @return the filtered {@link DefaultResourceListObject}.
     */
    @SuppressWarnings("unchecked")
    default List<E> getAllByApiVersion(@NotNull final String version) {
        return (List<E>) HasItems.super.getAllByApiVersion(version);
    }

    default Optional<E> findByName(@NotNull final String name) {
        return (Optional<E>) HasItems.super.findByName(name);
    }
}
