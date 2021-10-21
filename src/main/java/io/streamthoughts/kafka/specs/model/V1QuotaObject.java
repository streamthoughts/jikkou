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
package io.streamthoughts.kafka.specs.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class V1QuotaObject extends ConfigMapRefs<V1QuotaObject> {

    private final V1QuotaType type;
    private final V1QuotaEntityObject entity;
    private final V1QuotaLimitsObject configs;

    /**
     * Creates a new {@link V1QuotaObject} instance.
     *
     * @param type    the quota-entity.
     * @param entity  the quota-entity.
     * @param configs the quota-configs.
     */
    public V1QuotaObject(final V1QuotaType type,
                         final V1QuotaEntityObject entity,
                         final V1QuotaLimitsObject configs) {
        this(type, entity, configs, Collections.emptySet());
    }

    /**
     * Creates a new {@link V1QuotaObject} instance.
     *
     * @param type    the quota-entity.
     * @param entity  the quota-entity.
     * @param configs the quota-configs.
     */
    @JsonCreator
    public V1QuotaObject(@JsonProperty("type") final V1QuotaType type,
                         @JsonProperty("entity") final V1QuotaEntityObject entity,
                         @JsonProperty("configs") final V1QuotaLimitsObject configs,
                         @JsonProperty("config_map_refs") final Set<String> configMapRefs) {
        super(configMapRefs);
        this.type = Objects.requireNonNull(type, "'type' cannot be null");
        this.entity = entity;
        this.configs = configs;
    }

    @JsonProperty("type")
    public V1QuotaType type() {
        return type;
    }

    @JsonProperty("entity")
    public V1QuotaEntityObject entity() {
        return entity;
    }

    @JsonProperty("configs")
    public V1QuotaLimitsObject configs() {
        return configs;
    }

    @Override
    public V1QuotaObject addConfigs(@NotNull final Map<String, Object> configs) {
        final Map<String, Double> doubles = configs.entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, it -> Double.parseDouble(it.getValue().toString())));
        final HashMap<String, Double> merged = new HashMap<>(doubles);
        Optional.ofNullable(this.configs).map(V1QuotaLimitsObject::toMapDouble).ifPresent(merged::putAll);
        return new V1QuotaObject(type, entity, new V1QuotaLimitsObject(merged));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        V1QuotaObject that = (V1QuotaObject) o;
        return type == that.type && Objects.equals(entity, that.entity) && Objects.equals(configs, that.configs);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, entity, configs);
    }

    @Override
    public String toString() {
        return "V1QuotaObject{" +
                "type=" + type +
                ", entity=" + entity +
                ", configs=" + configs +
                '}';
    }
}
