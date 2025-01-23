/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.models;

import static java.util.stream.Collectors.groupingBy;

import io.streamthoughts.jikkou.core.exceptions.DuplicateMetadataNameException;
import io.streamthoughts.jikkou.core.exceptions.JikkouRuntimeException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;

public interface Resources {

    /**
     * Gets all the resources matching the given kind.
     *
     * @param resourceClass the class of the resource.
     * @return the filtered resources.
     */
    public static List<? extends HasMetadata> allByKind(final List<? extends HasMetadata> items,
                                                        final Class<? extends HasMetadata> resourceClass) {
        return allByKind(items, Resource.getKind(resourceClass));
    }

    /**
     * Gets all the resources matching the given kind.
     *
     * @param kind the Kind of the resource.
     * @return the filtered resources.
     */
    static List<? extends HasMetadata> allByKind(final List<? extends HasMetadata> items,
                                                 @NotNull final String kind) {
        return allByKinds(items, kind);
    }

    /**
     * Gets all the resources matching the given kinds.
     *
     * @param kinds the list of Kinds of the resources.
     * @return the filtered resources.
     */
    static List<? extends HasMetadata> allByKinds(final List<? extends HasMetadata> items,
                                                  @NotNull final String... kinds) {
        return allByKinds(items, Arrays.asList(kinds));
    }

    /**
     * Gets all the resources matching the given kinds.
     *
     * @param kinds the list of Kinds of the resources.
     * @return the filtered resources.
     */
    static List<? extends HasMetadata> allByKinds(final List<? extends HasMetadata> items,
                                                  @NotNull final List<String> kinds) {
        if (kinds.isEmpty()) {
            return items;
        }

        return items.stream()
            .filter(it -> kinds.contains(it.getKind()))
            .toList();
    }

    /**
     * Gets all the resources matching the given type.
     *
     * @param type the resource type.
     * @return the filtered resources.
     */
    static List<? extends HasMetadata> allByType(final List<? extends HasMetadata> items,
                                                 @NotNull final ResourceType type) {
        return items.stream()
            .filter(resource -> type.canAccept(ResourceType.of(resource)))
            .toList();
    }

    /**
     * Gets all the resources matching the given version.
     *
     * @param version the Version of the resource.
     * @return the filtered resources.
     */
    static List<? extends HasMetadata> allByApiVersion(final List<? extends HasMetadata> items,
                                                       @NotNull final String version) {
        return items.stream()
            .filter(resource -> resource.getApiVersion().equals(version))
            .toList();
    }

    /**
     * Gets all the resources grouped by type.
     *
     * @return the grouped resources.
     */
    static Map<ResourceType, List<HasMetadata>> groupByType(final List<? extends HasMetadata> items) {
        return items.stream().collect(groupingBy(ResourceType::of));
    }

    /**
     * Gets the types of all included resources.
     *
     * @return the types.
     */
    static Set<ResourceType> allResourceTypes(final List<? extends HasMetadata> items) {
        return items.stream().map(ResourceType::of).collect(Collectors.toSet());
    }

    /**
     * Finds a resource for the given name.
     *
     * @param name the resource name.
     * @return an optional of the resource.
     */
    static Optional<? extends HasMetadata> findByName(final List<? extends HasMetadata> items, @NotNull final String name) {
        return items.stream()
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
    static <T extends HasMetadata> T getByName(final List<? extends HasMetadata> items,
                                               @NotNull final String name,
                                               final Class<T> type) {
        @SuppressWarnings("unchecked")
        List<T> all = (List<T>) allByKind(items, type);
        return all.stream()
            .filter(it -> it.optionalMetadata()
                .map(ObjectMeta::getName)
                .filter(n -> n.equals(name))
                .isPresent())
            .findFirst()
            .orElseThrow(() -> new JikkouRuntimeException(
                String.format("Cannot found resource of kind %s for name %s", Resource.getKind(type), name)
            ));
    }

    @SuppressWarnings("unchecked")
    static <T extends HasMetadata> List<T> getAllByClass(final List<? extends HasMetadata> items,
                                                         final Class<T> clazz) {
        return (List<T>) items.stream()
            .filter(resource -> clazz.isAssignableFrom(resource.getClass()))
            .toList();
    }


    static void verifyNoDuplicateMetadataName(final List<? extends HasMetadata> items) {
        Map<String, List<? extends HasMetadata>> duplicates = items.stream()
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
