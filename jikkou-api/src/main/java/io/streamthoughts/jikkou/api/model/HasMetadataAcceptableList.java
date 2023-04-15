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

import java.util.Collections;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public final class HasMetadataAcceptableList<T extends HasMetadataAcceptable> implements Listeable<T> {

    private final List<T> items;

    /**
     * Creates a new {@link HasMetadataAcceptableList} instance.
     *
     * @param items   the list of acceptable of type {@code T}.
     */
    public HasMetadataAcceptableList(@NotNull final List<T> items) {
        this.items = Collections.unmodifiableList(items);
    }

    /** {@inheritDoc} */
    @Override
    public List<T> getItems() {
        return items;
    }


    /**
     * Get the {@link HasMetadataAcceptableList} accepting the given resource.
     *
     * @param resource  the resource.
     * @return          the filtered {@link HasMetadataAcceptableList}.
     */
    public HasMetadataAcceptableList<T> allResourcesAccepting(@NotNull final HasMetadata resource) {
        return allResourcesAccepting(ResourceType.create(resource));
    }

    /**
     * Get the {@link HasMetadataAcceptableList} accepting the given resource.
     *
     * @param resource  the resource.
     * @return          the filtered {@link HasMetadataAcceptableList}.
     */
    public HasMetadataAcceptableList<T> allResourcesAccepting(@NotNull final ResourceType resource) {
        return new HasMetadataAcceptableList<>(items.stream()
                .filter(it -> it.canAccept(resource))
                .toList()
        );
    }
}
