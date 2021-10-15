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

import java.io.Serializable;
import java.util.*;

public class V1AccessUserObject implements Serializable {

    private final String principal;

    private final Set<String> roles;

    private final Set<V1AccessPermission> permissions;

    /**
     * Creates a new {@link V1AccessUserObject} instance.
     *
     * @param principal     the principal of user.
     * @param roles         the roles of user.
     * @param permissions   the permission of the user.
     */
    @JsonCreator
    V1AccessUserObject(@JsonProperty("principal") final String principal,
                       @JsonProperty("roles") final Set<String> roles,
                       @JsonProperty("permissions") final Set<V1AccessPermission> permissions) {
        this.principal = Objects.requireNonNull(principal, "principal cannot be null");;
        this.roles = Objects.requireNonNull(roles, "roles cannot be null");;
        this.permissions = permissions == null ? Collections.emptySet() : Collections.unmodifiableSet(permissions);
    }

    @JsonProperty
    public String principal() {
        return principal;
    }

    @JsonProperty
    public Set<String> roles() {
        return roles;
    }

    @JsonProperty
    public Set<V1AccessPermission> permissions() {
        return permissions;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        V1AccessUserObject that = (V1AccessUserObject) o;
        return Objects.equals(principal, that.principal) &&
               Objects.equals(roles, that.roles) &&
               Objects.equals(permissions, that.permissions);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Objects.hash(principal, roles, permissions);
    }

    @Override
    public String toString() {
        return "V1AccessUserObject{" +
                "principal='" + principal + '\'' +
                ", roles=" + roles +
                ", permissions=" + permissions +
                '}';
    }

    /**
     * Creates a new builder.
     *
     * @return new {@link Builder} instance.
     */
    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder {

        private String principal;
        private final Set<String> roles  = new HashSet<>();
        private final Set<V1AccessPermission> permissions  = new HashSet<>();

        /**
         * Creates a new {@link Builder} instance.
         */
        Builder() {}

        public Builder withPrincipal(final String principal) {
            this.principal = principal;
            return this;
        }

        public Builder withRoles(final Collection<String> roles) {
            this.roles.addAll(roles);
            return this;
        }

        public Builder withPermission(final V1AccessPermission permission) {
            this.permissions.add(permission);
            return this;
        }

        public V1AccessUserObject build() {
            return new V1AccessUserObject(principal, roles, permissions);
        }
    }

}
