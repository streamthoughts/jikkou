/*
 * Copyright 2021 StreamThoughts.
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

import io.streamthoughts.jikkou.api.resources.Configs;
import io.streamthoughts.jikkou.api.resources.Named;
import io.streamthoughts.jikkou.api.error.JikkouException;

import java.util.*;
import java.util.stream.Collectors;

/**
 * A ConfigMap represents a named {@link Configs}.
 *
 * @see V1TopicObject
 */
public class V1ConfigMaps {

    private final Map<String, V1ConfigMap> configs;

    /**
     * Creates a new {@link V1ConfigMaps} instance.
     *
     * @param configs   the config properties.
     */
    public V1ConfigMaps(final Collection<V1ConfigMap> configs) {
        Collection<V1ConfigMap> nonEmpty = Optional.ofNullable(configs).orElse(Collections.emptyList());
        Map<String, List<V1ConfigMap>> grouped = Named.groupByName(nonEmpty);
        this.configs = grouped.entrySet()
                .stream()
                .map(e -> {
                    if (e.getValue().size() > 1) {
                        throw new JikkouException("Duplicate configMaps for name '" + e.getKey() + "'");
                    }
                    return Map.entry(e.getKey(), e.getValue().get(0));
                })
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public List<V1ConfigMap> all() {
        return new ArrayList<>(configs.values());
    }

    public Optional<V1ConfigMap> findConfigMap(final String name) {
        return Optional.ofNullable(configs.get(name));
    }

}
