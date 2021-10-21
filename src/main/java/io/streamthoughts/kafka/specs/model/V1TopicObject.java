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
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.streamthoughts.kafka.specs.error.KafkaSpecsException;
import io.streamthoughts.kafka.specs.resources.ConfigValue;
import io.streamthoughts.kafka.specs.resources.Configs;
import io.streamthoughts.kafka.specs.resources.Named;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.*;

/**
 * A Kafka topic resource.
 */
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public final class V1TopicObject extends ConfigMapRefs<V1TopicObject> implements Named, Serializable {

    public static final int NO_NUM_PARTITIONS = -1;
    public static final short NO_REPLICATION_FACTOR = -1;

    private final String name;

    private final Integer partitions;

    private final Short replicationFactor;

    private final Configs configs;

    /**
     * Creates a new {@link V1TopicObject} instance.
     *
     * @param name the topic name.
     */
    public V1TopicObject(final String name) {
        this(name, null, null);
    }

    /**
     * Creates a new {@link V1TopicObject} instance.
     *
     * @param name        the topic name
     * @param partitions  the number of partitions
     * @param replication the replication factor.
     */
    public V1TopicObject(final String name, final Integer partitions, final Short replication) {
        this(name, partitions, replication, new HashMap<>(), new HashSet<>());
    }

    /**
     * Creates a new {@link V1TopicObject} instance.
     *
     * @param name        the topic name.
     * @param partitions  the number of partitions.
     * @param replication the replication factor.
     * @param configs     the topic configs to override.
     */
    @JsonCreator
    public V1TopicObject(@JsonProperty("name") final String name,
                         @JsonProperty("partitions") final Integer partitions,
                         @JsonProperty("replication_factor") final Short replication,
                         @JsonProperty("configs") final Map<String, Object> configs,
                         @JsonProperty("config_map_refs") final Set<String> configMaps) {
        this(name, partitions, replication, Configs.of(configs), configMaps);
    }

    /**
     * Creates a new {@link V1TopicObject} instance.
     *
     * @param name        the topic name.
     * @param partitions  the number of partitions.
     * @param replication the replication factor.
     * @param configs     the topic configs to override.
     */
    public V1TopicObject(final String name,
                         final Integer partitions,
                         final Short replication,
                         final Configs configs) {
        this(name, partitions, replication, configs, Collections.emptySet());
    }

    /**
     * Creates a new {@link V1TopicObject} instance.
     *
     * @param name        the topic name.
     * @param partitions  the number of partitions.
     * @param replication the replication factor.
     * @param configs     the topic configs to override.
     */
    public V1TopicObject(final String name,
                         final Integer partitions,
                         final Short replication,
                         final Configs configs,
                         final Set<String> configMapRefs) {
        super(configMapRefs);
        this.name = Objects.requireNonNull(name, "'name' should not be null");
        this.partitions = partitions;
        this.replicationFactor = replication;
        this.configs =  Optional.ofNullable(configs).orElse(Configs.empty());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @JsonProperty
    public String name() {
        return name;
    }

    /**
     * @return the number of partitions for this topic.
     */
    @JsonProperty
    public Optional<Integer> partitions() {
        return Optional.ofNullable(partitions);
    }

    /**
     * @return the replication factor for this topic.
     */
    @JsonProperty
    public Optional<Short> replicationFactor() {
        return Optional.ofNullable(replicationFactor);
    }

    @JsonIgnore
    public Integer partitionsOrDefault() {
        return Optional.ofNullable(partitions).orElse(NO_NUM_PARTITIONS);
    }

    @JsonProperty
    public Configs configs() {
        return configs;
    }

    @Override
    public V1TopicObject addConfigs(@NotNull Map<String, Object> configs) {
        final HashMap<String, Object> merged = new HashMap<>();
        merged.putAll(configs);
        merged.putAll(this.configs.toMap());
        return new V1TopicObject(name, partitions, replicationFactor, Configs.of(configs));
    }

    @JsonIgnore
    public Short replicationFactorOrDefault() {
        return Optional.ofNullable(replicationFactor).orElse(NO_REPLICATION_FACTOR);
    }

    public V1TopicObject addConfigValue(final ConfigValue config) {
        this.configs.add(config);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof V1TopicObject)) return false;
        if (!super.equals(o)) return false;
        V1TopicObject that = (V1TopicObject) o;
        return Objects.equals(name, that.name) &&
                Objects.equals(partitions, that.partitions) &&
                Objects.equals(replicationFactor, that.replicationFactor) &&
                Objects.equals(configs, that.configs);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), name, partitions, replicationFactor, configs);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "V1TopicObject{" +
                "name='" + name + '\'' +
                ", partitions=" + partitions +
                ", replicationFactor=" + replicationFactor +
                ", configMapRefs=" + configMapRefs +
                ", configs=" + configs +
                '}';
    }
}