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
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import io.streamthoughts.kafka.specs.resources.Named;
import org.apache.kafka.common.resource.PatternType;
import org.apache.kafka.common.resource.ResourceType;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class V1AccessRoleObject implements Named, Serializable {

    /**
     * The group name.
     */
    private final String name;
    /**
     * The resource permission.
     */
    private final V1AccessPermission resource;

    /**
     * Creates a new {@link V1AccessRoleObject} instance.
     *
     * @param name      the group policy name.
     * @param resource  the resource permission to
     */
    @JsonCreator
    V1AccessRoleObject(@JsonProperty("name") final String name,
                       @JsonProperty("resource") final V1AccessResourceMatcher resource,
                       @JsonProperty("allow_operations") final Set<V1AccessOperationPolicy> operations) {
        Objects.requireNonNull(name, "name cannot be null");
        Objects.requireNonNull(resource, "resource cannot be null");
        this.name = name;
        this.resource = new V1AccessPermission(resource, operations);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String name() {
        return name;
    }

    @JsonUnwrapped
    public V1AccessPermission permission() {
        return resource;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        V1AccessRoleObject that = (V1AccessRoleObject) o;
        return Objects.equals(name, that.name) && Objects.equals(resource, that.resource);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Objects.hash(name, resource);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "V1AccessRoleObject{" +
                "name='" + name + '\'' +
                ", resource=" + resource +
                '}';
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder {

        private String name;

        private String pattern;

        private PatternType patternType;

        private ResourceType type;

        private final Set<V1AccessOperationPolicy> operations = new HashSet<>();

        Builder() {
        }

        public Builder withPatternType(final PatternType patternType) {
            this.patternType = patternType;
            return this;
        }

        public Builder withName(final String name) {
            this.name = name;
            return this;
        }

        public Builder withPattern(final String pattern) {
            this.pattern = pattern;
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

        public V1AccessRoleObject build() {
            return new V1AccessRoleObject(
                    name,
                    new V1AccessResourceMatcher(pattern, patternType, type),
                    operations
            );
        }
    }
}
