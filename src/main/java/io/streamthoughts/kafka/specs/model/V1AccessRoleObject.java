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
import io.streamthoughts.kafka.specs.resources.Named;

import java.io.Serializable;
import java.util.*;

public class V1AccessRoleObject implements Named, Serializable {

    /**
     * The group name.
     */
    private final String name;
    /**
     * The role permissions.
     */
    private final List<V1AccessPermission> permissions;

    /**
     * Creates a new {@link V1AccessRoleObject} instance.
     *
     * @param name      the group policy name.
     * @param permissions   the permission of the role.
     */
    @JsonCreator
    V1AccessRoleObject(@JsonProperty("name") final String name,
                       @JsonProperty("permissions") final List<V1AccessPermission> permissions) {
        Objects.requireNonNull(name, "name cannot be null");
        Objects.requireNonNull(permissions, "permissions cannot be null");
        this.name = name;
        this.permissions = Collections.unmodifiableList(permissions);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String name() {
        return name;
    }

    public List<V1AccessPermission> permissions() {
        return permissions;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        V1AccessRoleObject that = (V1AccessRoleObject) o;
        return Objects.equals(name, that.name) &&
               Objects.equals(permissions, that.permissions);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Objects.hash(name, permissions);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "V1AccessRoleObject{" +
                "name='" + name + '\'' +
                ", permissions=" + permissions +
                '}';
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder {

        private String name;

        private final List<V1AccessPermission> permissions = new LinkedList<>();

        Builder() {
        }

        public Builder withPermission(final V1AccessPermission permission) {
            Objects.requireNonNull(permission);
            this.permissions.add(permission);
            return this;
        }

        public Builder withPermissions(final Collection<V1AccessPermission> permissions) {
            Objects.requireNonNull(permissions);
            this.permissions.addAll(permissions);
            return this;
        }

        public Builder withName(final String name) {
            this.name = name;
            return this;
        }

        public V1AccessRoleObject build() {
            return new V1AccessRoleObject(
                    name,
                    permissions
            );
        }
    }
}
