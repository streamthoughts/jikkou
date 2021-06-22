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
package io.streamthoughts.kafka.specs.acl;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import io.streamthoughts.kafka.specs.resources.Named;

import java.util.Objects;
import java.util.Set;

public class AclGroupPolicy implements Named {

    /**
     * The group name.
     */
    private final String name;
    /**
     * The ressource permission.
     */
    private final AclResourcePermission resource;

    /**
     * Creates a new {@link AclGroupPolicy} instance.
     *
     * @param name      the group policy name.
     * @param resource  the resource permission to
     */
    @JsonCreator
    AclGroupPolicy(@JsonProperty("name") final String name,
                   @JsonProperty("resource") final AclResourceMatcher resource,
                   @JsonProperty("allow_operations") final Set<AclOperationPolicy> operations) {
        Objects.requireNonNull(name, "name cannot be null");
        Objects.requireNonNull(resource, "resource cannot be null");
        this.name = name;
        this.resource = new AclResourcePermission(resource, operations);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String name() {
        return name;
    }

    @JsonUnwrapped
    public AclResourcePermission permission() {
        return resource;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "AclGroupPolicy{" +
                "name=" + name +
                ", resource=" + resource +
                '}';
    }
}
