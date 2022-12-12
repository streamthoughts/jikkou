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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;

public final class ResourceList implements Listeable<HasMetadata> {

    public static ResourceList of(final HasMetadata... resources) {
        return new ResourceList(Arrays.asList(resources));
    }

    private final List<HasMetadata> items;

    /**
     * Creates a new {@link ResourceList} instance.
     *
     * @param resources the list of resources.
     */
    public ResourceList(@NotNull List<HasMetadata> resources) {
        this.items = Collections.unmodifiableList(resources);
    }

    /** {@inheritDoc} */
    public List<HasMetadata> items() {
        return items;
    }

    /**
     * Get the {@link ResourceList} matching the given kind.
     *
     * @param kind  the Kind of the resource.
     * @return      the filtered {@link ResourceList}.
     */
    public ResourceList allResourcesForKind(@NotNull final String kind) {
        return new ResourceList(items.stream()
                .filter(resource -> resource.getKind().equals(kind))
                .toList()
        );
    }

    /**
     * Get the {@link ResourceList} matching the given kinds.
     *
     * @param kinds  the list of Kinds of the resources.
     * @return       the filtered {@link ResourceList}.
     */
    public ResourceList allResourcesForKinds(@NotNull final String... kinds) {
        return allResourcesForKinds(Arrays.asList(kinds));
    }

    /**
     * Get the {@link ResourceList} matching the given kinds.
     *
     * @param kinds  the list of Kinds of the resources.
     * @return       the filtered {@link ResourceList}.
     */
    public ResourceList allResourcesForKinds(@NotNull final List<String> kinds) {
        if (kinds.isEmpty()) {
            return new ResourceList(items);
        }

        return new ResourceList(items.stream()
                .filter(it -> kinds.contains(it.getKind()))
                .toList()
        );
    }

    /**
     * Get the {@link ResourceList} matching the given version.
     *
     * @param version  the Version of the resource.
     * @return         the filtered {@link ResourceList}.
     */
    public ResourceList allResourcesForApiVersion(@NotNull final String version) {
        return new ResourceList(items.stream()
                .filter(resource -> resource.getApiVersion().equals(version))
                .toList()
        );
    }

    public Optional<HasMetadata> findResourceByName(@NotNull final String name) {
        return items.stream()
                .filter(it -> it.optionalMetadata()
                        .map(ObjectMeta::getName)
                        .filter(n -> n.equals(name))
                        .isPresent())
                .findFirst();
    }

    @SuppressWarnings("unchecked")
    public <T extends HasMetadata> List<T> getAllResourcesForClass(final Class<T> clazz) {
        return (List<T>) items.stream()
                .filter(resource -> clazz.isAssignableFrom(resource.getClass()))
                .toList();
    }
}
