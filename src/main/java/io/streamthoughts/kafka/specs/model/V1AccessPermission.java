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
package io.streamthoughts.kafka.specs.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.kafka.common.resource.PatternType;
import org.apache.kafka.common.resource.ResourceType;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Represents a binding between resources pattern and a set of allowed operations.
 */
public class V1AccessPermission implements Serializable {

    private final V1AccessResourceMatcher resourcePattern;
    private final Set<V1AccessOperationPolicy> operations;

    /**
     * Creates a new {@link V1AccessPermission} instance.
     *
     * @param resource   apply operations to all resources matching specified pattern.
     * @param operations list operations to allowed.
     */
    @JsonCreator
    V1AccessPermission(@JsonProperty("resource") final V1AccessResourceMatcher resource,
                       @JsonProperty("allow_operations") final Set<V1AccessOperationPolicy> operations) {
        this.resourcePattern = Objects.requireNonNull(resource, "'resource' should not be null");
        this.operations = Objects.requireNonNull(operations, "'operations' should not be null");
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public Set<V1AccessOperationPolicy> operations() {
        return operations;
    }

    public V1AccessResourceMatcher resource() {
        return resourcePattern;
    }

    public String[] operationLiterals() {
        return this.operations
                .stream()
                .map(V1AccessOperationPolicy::toLiteral)
                .toArray(String[]::new);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof V1AccessPermission)) return false;
        V1AccessPermission that = (V1AccessPermission) o;
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

    public static class Builder {
        private ResourceType type;
        private String pattern;
        private PatternType patternType;
        private final Set<V1AccessOperationPolicy> operations = new HashSet<>();

        public Builder withPattern(final String pattern) {
            this.pattern = pattern;
            return this;
        }

        public Builder withPatternType(final PatternType patternType) {
            this.patternType = patternType;
            return this;
        }

        public Builder onResourceType(final ResourceType type) {
            this.type = type;
            return this;
        }

        public Builder allow(final V1AccessOperationPolicy operation) {
            this.operations.add(operation);
            return this;
        }

        public V1AccessPermission build() {
            return new V1AccessPermission(new V1AccessResourceMatcher(pattern, patternType, type), operations);
        }

    }
}
