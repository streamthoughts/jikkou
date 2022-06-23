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

import io.streamthoughts.jikkou.api.models.ConfigMap;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a list of {@link ConfigMap}.
 */
public final class ConfigMapList implements Listeable<ConfigMap> {

    private final List<ConfigMap> items;

    private final Map<String, ConfigMap> indexedByName;

    /**
     * Creates a new {@link ConfigMapList} instance.
     *
     * @param items     the list of {@link ConfigMap}.
     */
    public ConfigMapList(final @NotNull List<ConfigMap> items) {
        verifyNoDuplicateConfigMapName(items);
        this.items = Collections.unmodifiableList(items);
        this.indexedByName = items.stream().collect(Collectors.toMap(it -> it.getMetadata().getName(), it -> it));
    }

    /** {@inheritDoc} */
    @Override
    public List<ConfigMap> items() {
        return items;
    }

    /**
     * Optionally returns the {@link ConfigMap} for the specified name.
     *
     * @param name  name of ConfigMap that should be returned.
     * @return      an optional ConfigMap.
     */
    public Optional<ConfigMap> findByName(final @NotNull String name) {
        return Optional.ofNullable(indexedByName.get(name));
    }

    /**
     * Returns {@code true} if this list contains a {@link ConfigMap} for the specified name.
     *
     * @param name  name whose presence in this list is to be tested
     * @return      {@code true} if this list contains a {@link ConfigMap} for the specified name.
     */
    public boolean containsConfigMap(final @NotNull String name) {
        return indexedByName.containsKey(name);
    }

    private void verifyNoDuplicateConfigMapName(final List<ConfigMap> items) {
        Map<String, List<ConfigMap>> duplicates = items.stream()
                .collect(Collectors.groupingBy(it -> it.getMetadata().getName()))
                .entrySet()
                .stream()
                .filter(it -> it.getValue().size() > 1)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        if (!duplicates.isEmpty()) {
            throw new IllegalArgumentException("Duplicate ConfigMap name: " + duplicates.keySet());
        }
    }
}
