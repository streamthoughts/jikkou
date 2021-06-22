/*
 * Copyright 2020 StreamThoughts.
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
package io.streamthoughts.kafka.specs.resources;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A Kafka topic resource.
 */
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public final class TopicResource implements ClusterResource, Named, Serializable {

    private static final Logger LOG = LoggerFactory.getLogger(TopicResource.class);

    private final String name;

    private final Integer partitions;

    private final Short replicationFactor;

    private final Configs configs;

    /**
     * Creates a new {@link TopicResource} instance.
     *
     * @param name          the topic name.
     */
    public TopicResource(final String name) {
        this(name, null, null);
    }

    /**
     * Creates a new {@link TopicResource} instance.
     *
     * @param name          the topic name
     * @param partitions    the number of partitions
     * @param replication   the replication factor.
     */
    public TopicResource(final String name, final Integer partitions, final Short replication) {
        this(name, partitions, replication, Collections.emptyMap());
    }

    /**
     * Creates a new {@link TopicResource} instance.
     *
     * @param name          the topic name.
     * @param partitions    the number of partitions.
     * @param replication   the replication factor.
     * @param configs       the topic configs to override.
     */
    @JsonCreator
    public TopicResource(@JsonProperty("name") final String name,
                         @JsonProperty("partitions") final Integer partitions,
                         @JsonProperty("replication_factor") final Short replication,
                         @JsonProperty("configs") final Map<String, Object> configs) {
        this(name, partitions, replication, toConfigs(configs));
    }

    /**
     * Creates a new {@link TopicResource} instance.
     *
     * @param name          the topic name.
     * @param partitions    the number of partitions.
     * @param replication   the replication factor.
     * @param configs       the topic configs to override.
     */
    public TopicResource(final String name,
                         final Integer partitions,
                         final Short replication,
                         final Configs configs) {
        this.name = name;
        this.partitions = partitions;
        this.replicationFactor = replication;
        this.configs = configs;
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
    public int partitions() {
        return partitions;
    }

    /**
     * @return the replication factor for this topic.
     */
    @JsonProperty
    public short replicationFactor() {
        return replicationFactor;
    }

    @JsonProperty
    public Configs configs() {
        return configs;
    }

    public TopicResource addConfigValue(final ConfigValue config) {
        this.configs.add(config);
        return this;
    }

    /**
     * Checks whether the specified resource has configuration differences with this.
     *
     * @param resource  the {@link TopicResource} to check.
     * @return          <code>true</code> if {@literal resource} has config changes.
     */
    public boolean containsConfigsChanges(final TopicResource resource) {

        if (!this.name.equals(resource.name)) {
            throw new IllegalArgumentException(
                    "Can't check changes on resources with different names " + this.name + "<>" + resource.name);
        }

        if ((this.partitions != null && !this.partitions.equals(resource.partitions)) ||
            (this.replicationFactor != null && !this.replicationFactor.equals(resource.replicationFactor))
        ) {
          LOG.warn("Topic partitions and/or replication-factor change is not supported!" +
                  " You should consider altering topic through scripts 'kafka-topics' or 'kafka-configs'");
        }

        return this.configs.containsChanges(resource.configs);

    }

    /**
     * Removes all default configuration from the specified resource.
     *
     * @param resource  the resource from which to delete defaults.
     * @return          a new {@link TopicResource} instance.
     */
    public TopicResource dropDefaultConfigs(final TopicResource resource) {
        Configs withoutDefaultConfigs = this.configs.filters(resource.configs.defaultConfigs());
        return new TopicResource(this.name, this.partitions, this.replicationFactor, withoutDefaultConfigs);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TopicResource)) return false;
        TopicResource that = (TopicResource) o;
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
        return Objects.hash(name, partitions, replicationFactor, configs);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "TopicResource{" +
                "name=" + name +
                ", partitions=" + partitions +
                ", replication_factor=" + replicationFactor +
                ", configs=" + configs +
                '}';
    }

    public static Configs toConfigs(final Map<String, Object> configs) {
        if (configs == null || configs.isEmpty())  return new Configs();

        final Set<ConfigValue> values = configs.entrySet()
                .stream()
                .map(e -> new ConfigValue(e.getKey(), e.getValue()))
                .collect(Collectors.toSet());

        return new Configs(values);
    }
}