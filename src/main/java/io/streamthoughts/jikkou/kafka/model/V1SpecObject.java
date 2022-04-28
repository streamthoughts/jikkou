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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class V1SpecObject implements Serializable {

    private final V1ConfigMaps configMaps;

    private final List<V1QuotaObject> quotas;

    private final List<V1TopicObject> topics;

    private final V1SecurityObject security;

    private final List<V1BrokerObject> brokers;

    /**
     * @param topics the {@link V1TopicObject} list.
     *
     * @return a new {@link V1SpecFile} instance.
     */
    public static V1SpecObject withTopics(final List<V1TopicObject> topics) {
        return new V1SpecObject(
            Collections.emptyList(),
            topics,
            null,
            Collections.emptyList(),
            Collections.emptyList()
        );
    }

    /**
     * @param brokers the {@link V1BrokerObject} list.
     *
     * @return a new {@link V1SpecFile} instance.
     */
    public static V1SpecObject withBrokers(final List<V1BrokerObject> brokers) {
        return new V1SpecObject(
                brokers,
                Collections.emptyList(),
                null,
                Collections.emptyList(),
                Collections.emptyList()
        );
    }

    /**
     * @param security the {@link V1SecurityObject}.
     *
     * @return a new {@link V1SpecFile} instance.
     */
    public static V1SpecObject withSecurity(final V1SecurityObject security) {
        return new V1SpecObject(
                Collections.emptyList(),
                Collections.emptyList(),
                security,
                Collections.emptyList(),
                Collections.emptyList()
        );
    }

    /**
     * @param quotas the list {@link V1QuotaObject}.
     *
     * @return a new {@link V1SpecFile} instance.
     */
    public static V1SpecObject withQuotas(final List<V1QuotaObject> quotas) {
        return new V1SpecObject(
                Collections.emptyList(),
                Collections.emptyList(),
                null,
                Collections.emptyList(),
                quotas
        );
    }

    /**
     * Creates a new {@link V1SpecFile} instance.
     */
    public V1SpecObject() {
        this(null, null, null, null, null);
    }

    /**
     * Creates a new {@link V1SpecFile} instance.
     *
     * @param brokers    the list of {@link V1BrokerObject}.
     * @param topics     the list of {@link V1TopicObject}.
     * @param security   the {@link V1SecurityObject}.
     * @param configMaps the list of {@link V1ConfigMap}.
     * @param quotas     the {@link V1QuotaObject}.
     */
    @JsonCreator
    public V1SpecObject(@JsonProperty("brokers") final List<V1BrokerObject> brokers,
                        @JsonProperty("topics") final List<V1TopicObject> topics,
                        @JsonProperty("security") final V1SecurityObject security,
                        @JsonProperty("config_maps") final List<V1ConfigMap> configMaps,
                        @JsonProperty("quotas") final List<V1QuotaObject> quotas) {
        this.brokers = Optional.ofNullable(brokers).orElse(Collections.emptyList());
        this.topics = Optional.ofNullable(topics).orElse(Collections.emptyList());
        this.quotas = Optional.ofNullable(quotas).orElse(Collections.emptyList());
        this.security = security;
        this.configMaps = new V1ConfigMaps(configMaps);
    }

    @JsonProperty
    public List<V1TopicObject> topics() {
        return topics;
    }

    public V1SpecObject topics(final List<V1TopicObject> topics) {
        return new V1SpecObject(brokers, topics, security, configMaps.all(), quotas);
    }

    @JsonProperty
    public Collection<V1BrokerObject> brokers() {
        return brokers;
    }

    @JsonProperty
    public Optional<V1SecurityObject> security() {
        return Optional.ofNullable(security);
    }

    public V1SpecObject quotas(final List<V1QuotaObject> quotas) {
        return new V1SpecObject(brokers, topics, security, configMaps.all(), quotas);
    }

    @JsonProperty
    public List<V1QuotaObject> quotas() {
        return quotas;
    }

    @JsonIgnore
    public V1ConfigMaps configMaps() {
        return configMaps;
    }

    @JsonIgnore
    public V1SpecObject configMaps(final List<V1ConfigMap> configMaps) {
        return new V1SpecObject(brokers, topics, security, configMaps, quotas);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        V1SpecObject that = (V1SpecObject) o;
        return Objects.equals(configMaps, that.configMaps) &&
               Objects.equals(topics, that.topics) &&
               Objects.equals(security, that.security) &&
               Objects.equals(brokers, that.brokers);
    }

    @Override
    public int hashCode() {
        return Objects.hash(configMaps, topics, security, brokers);
    }

    @Override
    public String toString() {
        return "V1SpecsObject{" +
                "configMaps=" + configMaps +
                ", topics=" + topics +
                ", security=" + security +
                ", brokers=" + brokers +
                '}';
    }
}
