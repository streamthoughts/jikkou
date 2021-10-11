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
import io.streamthoughts.kafka.specs.resources.Named;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class V1SpecsObject implements Serializable {

    private Map<String, V1TopicObject> topics;

    private final V1SecurityObject security;

    private final Collection<V1BrokerObject> brokers;

    /**
     * Creates a new {@link V1SpecFile} instance.
     */
    public static V1SpecsObject withTopics(final Collection<V1TopicObject> topics) {
        return new V1SpecsObject(Collections.emptyList(), topics, null);
    }

    /**
     * Creates a new {@link V1SpecFile} instance.
     */
    public static V1SpecsObject withBrokers(final Collection<V1BrokerObject> brokers) {
        return new V1SpecsObject(brokers, Collections.emptyList(), null);
    }

    /**
     * Creates a new {@link V1SpecFile} instance.
     */
    public static V1SpecsObject withSecurity(final V1SecurityObject security) {
        return new V1SpecsObject(
                Collections.emptyList(),
                Collections.emptyList(),
                security
        );
    }

    /**
     * Creates a new {@link V1SpecFile} instance.
     */
    public V1SpecsObject() {
        this(null, null, null);
    }

    /**
     * Creates a new {@link V1SpecFile} instance.
     */
    @JsonCreator
    public V1SpecsObject(@JsonProperty("brokers") final Collection<V1BrokerObject> brokers,
                         @JsonProperty("topics") final Collection<V1TopicObject> topics,
                         @JsonProperty("security") final V1SecurityObject security) {
        this.brokers = Optional.ofNullable(brokers).orElse(Collections.emptyList());
        this.topics = Named.keyByName(Optional.ofNullable(topics).orElse(Collections.emptyList()));
        this.security = security;
    }

    @JsonProperty
    public Collection<V1TopicObject> topics() {
        return new ArrayList<>(topics.values());
    }

    @JsonProperty
    public Collection<V1BrokerObject> brokers() {
        return brokers;
    }

    @JsonProperty
    public Optional<V1SecurityObject> security() {
        return Optional.ofNullable(security);
    }

    public Collection<V1TopicObject> topics(final Predicate<V1TopicObject> predicate) {
        if (predicate == null) return topics();
        return topics.values()
                .stream()
                .filter(predicate)
                .collect(Collectors.toList());
    }

    public void setTopics(final Collection<V1TopicObject> topics) {
        this.topics = topics.stream().collect(Collectors.toMap(V1TopicObject::name, o -> o));
    }
}
