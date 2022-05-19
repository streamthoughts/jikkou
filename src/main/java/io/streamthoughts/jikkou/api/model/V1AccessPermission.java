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
package io.streamthoughts.jikkou.api.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Represents a binding between resources pattern and a set of allowed operations.
 */
public class V1AccessPermission implements Serializable {

    private final V1AccessResourceMatcher resource;
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
        this.resource = Objects.requireNonNull(resource, "'resource' should not be null");
        this.operations = Objects.requireNonNull(operations, "'operations' should not be null");
    }

    @JsonProperty("allow_operations")
    public Set<V1AccessOperationPolicy> operations() {
        return operations;
    }

    @JsonProperty("resource")
    public V1AccessResourceMatcher resource() {
        return resource;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof V1AccessPermission)) return false;
        V1AccessPermission that = (V1AccessPermission) o;
        return Objects.equals(resource, that.resource) &&
                Objects.equals(operations, that.operations);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Objects.hash(resource, operations);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "V1AccessPermission{" +
                "resource=" + resource +
                ", operations=" + operations +
                '}';
    }

    /**
     * Creates a new builder.
     *
     * @return new {@link V1AccessUserObject.Builder} instance.
     */
    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder {

        private V1AccessResourceMatcher resource;
        private final Set<V1AccessOperationPolicy> operations = new HashSet<>();

        public Builder onResource(final V1AccessResourceMatcher resource) {
            this.resource = resource;
            return this;
        }

        public Builder allow(final Set<V1AccessOperationPolicy> operations) {
            this.operations.addAll(operations);
            return this;
        }

        public Builder allow(final V1AccessOperationPolicy operation) {
            this.operations.add(operation);
            return this;
        }

        public V1AccessPermission build() {
            return new V1AccessPermission(resource, operations);
        }
    }
}
