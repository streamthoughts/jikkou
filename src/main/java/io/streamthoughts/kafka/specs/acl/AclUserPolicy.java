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

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;

public class AclUserPolicy {

    private final String principal;

    private final Set<String> groups;

    private final Set<AclResourcePermission> permissions;

    /**
     * Creates a new {@link AclUserPolicy} instance.
     *
     * @param principal
     * @param groups
     * @param permissions
     */
    @JsonCreator
    AclUserPolicy(@JsonProperty("principal") final String principal,
                  @JsonProperty("groups") final Set<String> groups,
                  @JsonProperty("permissions") final Set<AclResourcePermission> permissions) {
        Objects.requireNonNull(principal, "principal cannot be null");
        Objects.requireNonNull(principal, "groups cannot be null");
        Objects.requireNonNull(principal, "permissions cannot be null");
        this.principal = principal;
        this.groups = groups;
        this.permissions = permissions == null ? Collections.emptySet() : Collections.unmodifiableSet(permissions);
    }

    public String principal() {
        return principal;
    }

    public Set<String> groups() {
        return groups;
    }

    public Set<AclResourcePermission> permissions() {
        return permissions;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AclUserPolicy that = (AclUserPolicy) o;
        return Objects.equals(principal, that.principal) &&
               Objects.equals(groups, that.groups) &&
               Objects.equals(permissions, that.permissions);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Objects.hash(principal, groups, permissions);
    }

    @Override
    public String toString() {
        return "AclUserPolicy{" +
                "principal='" + principal + '\'' +
                ", groups=" + groups +
                ", permissions=" + permissions +
                '}';
    }
}
