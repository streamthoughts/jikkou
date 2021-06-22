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

import java.util.Objects;
import java.util.Set;

/**
 * Represents a binding between resources pattern and a set of allowed operations.
 */
public class AclResourcePermission {

    private final AclResourceMatcher resourcePattern;
    private final Set<AclOperationPolicy> operations;

    /**
     * Creates a new {@link AclResourcePermission} instance.
     *
     * @param resource   apply operations to all resources matching specified pattern.
     * @param operations        list operations to allowed.
     */
    @JsonCreator
    AclResourcePermission(@JsonProperty("resource") final AclResourceMatcher resource,
                          @JsonProperty("allow_operations") final Set<AclOperationPolicy> operations) {
        this.resourcePattern = Objects.requireNonNull(resource, "'resource' should not be null");
        this.operations = Objects.requireNonNull(operations, "'operations' should not be null");
    }

    public Set<AclOperationPolicy> operations() {
        return operations;
    }

    public AclResourceMatcher resource() {
        return resourcePattern;
    }

    public String[] operationLiterals() {
      return this.operations
                .stream()
                .map(AclOperationPolicy::toLiteral)
                .toArray(String[]::new);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AclResourcePermission)) return false;
        AclResourcePermission that = (AclResourcePermission) o;
        return Objects.equals(resourcePattern, that.resourcePattern) &&
                Objects.equals(operations, that.operations);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Objects.hash(resourcePattern, operations);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "AclResourcePermission{" +
                "resourcePattern=" + resourcePattern +
                ", operations=" + operations +
                '}';
    }
}
