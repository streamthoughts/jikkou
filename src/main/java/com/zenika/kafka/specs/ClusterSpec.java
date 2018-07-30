/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.zenika.kafka.specs;

import com.zenika.kafka.specs.acl.AclGroupPolicy;
import com.zenika.kafka.specs.acl.AclUserPolicy;
import com.zenika.kafka.specs.resources.Named;
import com.zenika.kafka.specs.resources.TopicResource;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * The Kafka cluster specification.
 */
public class ClusterSpec implements Serializable {

    private Map<String, TopicResource> topics;

    private Map<String, AclGroupPolicy> aclGroupPolicies;

    private final Collection<AclUserPolicy> aclUsers;

    /**
     * Creates a new {@link ClusterSpec} instance.
     * @param topics  the topic list
     */
    public ClusterSpec(final Collection<TopicResource> topics) {
        this(topics, Collections.emptyList(), Collections.emptyList());
    }

    /**
     * Creates a new {@link ClusterSpec} instance.
     * @param topics  the topic list
     */
    public ClusterSpec(final Collection<TopicResource> topics,
                       final Collection<AclGroupPolicy> aclGroupPolicies,
                       final Collection<AclUserPolicy> aclUsers) {
        Objects.requireNonNull(topics, "topics cannot be null");
        Objects.requireNonNull(topics, "aclGroupPolicies cannot be null");
        Objects.requireNonNull(topics, "aclUsers cannot be null");
        this.topics = Named.keyByName(topics);
        this.aclGroupPolicies = Named.keyByName(aclGroupPolicies);
        this.aclUsers = aclUsers;
    }

    public Map<String, AclGroupPolicy> getAclGroupPolicies() {
        return aclGroupPolicies;
    }

    public Collection<AclUserPolicy> getAclUsers() {
        return aclUsers;
    }

    public Collection<TopicResource> getTopics() {
        return new ArrayList<>(topics.values());
    }

    public Collection<TopicResource> getTopics(Collection<String> filter) {
        if (filter.isEmpty()) return getTopics();
        return topics.values()
                .stream()
                .filter(t -> filter.contains(t.name()))
                .collect(Collectors.toList());
    }

    public void setTopics(Collection<TopicResource> topics) {
        this.topics = topics.stream().collect(Collectors.toMap(TopicResource::name, o -> o));
    }
}
