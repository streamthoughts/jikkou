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
import io.streamthoughts.kafka.specs.resources.Configs;
import io.streamthoughts.kafka.specs.resources.Named;

import java.util.Map;
import java.util.Objects;

/**
 * A ConfigMap represents a named {@link Configs}.
 *
 * @see V1TopicObject
 */
public class V1ConfigMap implements Named {

    private final String name;
    private final Configs configs;

    /**
     * Creates a new {@link V1ConfigMap} instance.
     *
     * @param name      the config name.
     * @param configs   the config properties.
     */
    @JsonCreator
    public V1ConfigMap(@JsonProperty("name")final String name,
                       @JsonProperty("configs") final Map<String, Object> configs) {
        this.name = Objects.requireNonNull(name, "'name' must not be null");
        this.configs = Configs.of(Objects.requireNonNull(configs, "'configs' must not be null"));
    }

    @JsonProperty("configs")
    public Configs configs() {
        return configs;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String name() {
        return name;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        V1ConfigMap that = (V1ConfigMap) o;
        return Objects.equals(name, that.name) && Objects.equals(configs, that.configs);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Objects.hash(name, configs);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "V1ConfigMap{" +
                "name='" + name + '\'' +
                ", configs=" + configs +
                '}';
    }
}
