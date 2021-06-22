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
package io.streamthoughts.kafka.specs;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.streamthoughts.kafka.specs.acl.AclUserPolicy;
import io.streamthoughts.kafka.specs.resources.AclsResource;
import io.streamthoughts.kafka.specs.resources.BrokerResource;
import io.streamthoughts.kafka.specs.resources.Named;
import io.streamthoughts.kafka.specs.resources.TopicResource;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * The Kafka cluster specification.
 */
public class ClusterSpec implements Serializable {

    private Map<String, TopicResource> topics;

    private final AclsResource acls;

    private final Collection<BrokerResource> brokers;

    /**
     * Creates a new {@link ClusterSpec} instance.
     */
    public static ClusterSpec withTopics(final Collection<TopicResource> topics) {
        return new ClusterSpec(Collections.emptyList(), topics, null);
    }

    /**
     * Creates a new {@link ClusterSpec} instance.
     */
    public static ClusterSpec withBrokers(final Collection<BrokerResource> brokers) {
        return new ClusterSpec(brokers, Collections.emptyList(), null);
    }

    /**
     * Creates a new {@link ClusterSpec} instance.
     */
    public static ClusterSpec withUserPolicy(final Collection<AclUserPolicy> aclUsers) {
        return new ClusterSpec(Collections.emptyList(), Collections.emptyList(), new AclsResource(null, aclUsers));
    }

    /**
     * Creates a new {@link ClusterSpec} instance.
     */
    @JsonCreator
    public ClusterSpec(@JsonProperty("brokers") final Collection<BrokerResource> brokers,
                       @JsonProperty("topics") final Collection<TopicResource> topics,
                       @JsonProperty("acls") final AclsResource acls) {
        this.brokers = Optional.ofNullable(brokers).orElse(Collections.emptyList());
        this.topics = Named.keyByName(Optional.ofNullable(topics).orElse(Collections.emptyList()));
        this.acls = acls;
    }

    public Collection<TopicResource> getTopics() {
        return new ArrayList<>(topics.values());
    }

    public Collection<BrokerResource> getBrokers() {
        return brokers;
    }

    public Optional<AclsResource> getAcls() {
        return Optional.ofNullable(acls);
    }

    public Collection<TopicResource> getTopics(final Predicate<TopicResource> predicate) {
        if (predicate == null) return getTopics();
        return topics.values()
                .stream()
                .filter(predicate)
                .collect(Collectors.toList());
    }

    public void setTopics(final Collection<TopicResource> topics) {
        this.topics = topics.stream().collect(Collectors.toMap(TopicResource::name, o -> o));
    }
}
