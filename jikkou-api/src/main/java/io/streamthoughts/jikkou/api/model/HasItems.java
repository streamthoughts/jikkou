/*
 * Copyright 2023 StreamThoughts.
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

import static java.util.stream.Collectors.groupingBy;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.streamthoughts.jikkou.api.error.DuplicateMetadataNameException;
import io.streamthoughts.jikkou.api.error.JikkouRuntimeException;
import io.streamthoughts.jikkou.api.selector.AggregateSelector;
import io.streamthoughts.jikkou.api.selector.ResourceSelector;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
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
     * Gets the resources matching the given list of selectors.
     *
     * @param selectors the list of selectors to apply.
     * @return the filtered resources.
     */
    default List<? extends HasMetadata> getAllMatching(final @NotNull List<ResourceSelector> selectors) {
        return getItems().stream()
                .filter(new AggregateSelector(selectors)::apply)
                .toList();
    }

    /**
     * Gets all the resources matching the given kind.
     *
     * @param resourceClass the class of the resource.
     * @return the filtered {@link GenericResourceListObject}.
     */
    default List<? extends HasMetadata> getAllByKind(Class<? extends HasMetadata> resourceClass) {
        return getAllByKind(HasMetadata.getKind(resourceClass));
    }

    /**
     * Gets all the resources matching the given kind.
     *
     * @param kind the Kind of the resource.
     * @return the filtered {@link GenericResourceListObject}.
     */
    default List<? extends HasMetadata> getAllByKind(@NotNull final String kind) {
        return getAllByKinds(kind);
    }

    /**
     * Gets all the resources matching the given kinds.
     *
     * @param kinds the list of Kinds of the resources.
     * @return the filtered {@link GenericResourceListObject}.
     */
    default List<? extends HasMetadata> getAllByKinds(@NotNull final String... kinds) {
        return getAllByKinds(Arrays.asList(kinds));
    }

    /**
     * Gets all the resources matching the given kinds.
     *
     * @param kinds the list of Kinds of the resources.
     * @return the filtered {@link GenericResourceListObject}.
     */
    default List<? extends HasMetadata> getAllByKinds(@NotNull final List<String> kinds) {
        if (kinds.isEmpty()) {
            return getItems();
        }

        return getItems().stream()
                .filter(it -> kinds.contains(it.getKind()))
                .toList();
    }

    /**
     * Gets all the resources matching the given version.
     *
     * @param version the Version of the resource.
     * @return the filtered {@link GenericResourceListObject}.
     */
    default List<? extends HasMetadata> getAllByApiVersion(@NotNull final String version) {
        return getItems().stream()
                .filter(resource -> resource.getApiVersion().equals(version))
                .toList();
    }

    /**
     * Gets all the resources grouped by type.
     *
     * @return the grouped resources.
     */
    @JsonIgnore
    default Map<ResourceType, List<HasMetadata>> groupByType() {
        return getItems().stream().collect(groupingBy(ResourceType::create));
    }

    /**
     * Gets the types of all included resources.
     *
     * @return the types.
     */
    @JsonIgnore
    default Set<ResourceType> getAllResourceTypes() {
        return getItems().stream().map(ResourceType::create).collect(Collectors.toSet());
    }

    /**
     * Finds a resource for the given name.
     *
     * @param name the resource name.
     * @return an optional of the resource.
     */
    default Optional<? extends HasMetadata> findByName(@NotNull final String name) {
        return getItems().stream()
                .filter(it -> it.optionalMetadata()
                        .map(ObjectMeta::getName)
                        .filter(n -> n.equals(name))
                        .isPresent())
                .findFirst();
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
        @SuppressWarnings("unchecked")
        List<T> all = (List<T>) getAllByKind(type);
        return all.stream()
                .filter(it -> it.optionalMetadata()
                        .map(ObjectMeta::getName)
                        .filter(n -> n.equals(name))
                        .isPresent())
                .findFirst()
                .orElseThrow(() -> new JikkouRuntimeException(
                        String.format("Cannot found resource of kind %s for name %s", HasMetadata.getKind(type), name)
                ));
    }

    @SuppressWarnings("unchecked")
    default <T extends HasMetadata> List<T> getAllByClass(final Class<T> clazz) {
        return (List<T>) getItems().stream()
                .filter(resource -> clazz.isAssignableFrom(resource.getClass()))
                .toList();
    }


    default void verifyNoDuplicateMetadataName() {
        Map<String, List<? extends HasMetadata>> duplicates = getItems().stream()
                .collect(groupingBy(it -> it.getMetadata().getName()))
                .entrySet()
                .stream()
                .filter(it -> it.getValue().size() > 1)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        if (!duplicates.isEmpty()) {
            throw new DuplicateMetadataNameException(duplicates.keySet());
        }
    }
}
