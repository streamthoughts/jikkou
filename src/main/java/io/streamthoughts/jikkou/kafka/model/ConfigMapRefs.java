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
package io.streamthoughts.jikkou.kafka.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.streamthoughts.jikkou.kafka.error.JikkouException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Represents a set of {@link V1ConfigMap}.
 */
public abstract class ConfigMapRefs<T extends ConfigMapRefs<T>> {

    protected final Set<String> configMapRefs;

    /**
     * Creates a new {@link ConfigMapRefs} instance.
     *
     * @param configMapRefs set of config-map refs.
     */
    public ConfigMapRefs(@Nullable final Set<String> configMapRefs) {
        this.configMapRefs = Optional.ofNullable(configMapRefs).orElse(Collections.emptySet());
    }

    @SuppressWarnings("unchecked")
    public T addConfigMapRef(final String configMapRef) {
        this.configMapRefs.add(configMapRef);
        return (T)this;
    }

    /**
     * @return the configMaps associated to this topic.
     */
    @JsonIgnore
    public Set<String> configMapRefs() {
        return configMapRefs;
    }

    /**
     *
     * @param configMaps    the {@link V1ConfigMaps} to apply on this object.
     * @return              a new {@link V1TopicObject}.
     */
    public T applyConfigMaps(@NotNull final V1ConfigMaps configMaps) {
        Map<String, Object> newConfigs = new HashMap<>();
        this.configMapRefs.forEach(name -> newConfigs.putAll(
                configMaps.findConfigMap(name)
                        .orElseThrow(() -> new JikkouException("configmap '" + name + "' not found"))
                        .configs()
                        .toMap())
        );
        return addConfigs(newConfigs);
    }

    public abstract T addConfigs(@NotNull final Map<String, Object> configs);

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ConfigMapRefs)) return false;
        ConfigMapRefs<?> that = (ConfigMapRefs<?>) o;
        return Objects.equals(configMapRefs, that.configMapRefs);
    }

    @Override
    public int hashCode() {
        return Objects.hash(configMapRefs);
    }
}
