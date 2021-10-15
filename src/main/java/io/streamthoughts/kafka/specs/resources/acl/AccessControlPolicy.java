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
package io.streamthoughts.kafka.specs.resources.acl;

import io.streamthoughts.kafka.specs.resources.Named;
import org.apache.kafka.common.acl.AclOperation;
import org.apache.kafka.common.acl.AclPermissionType;
import org.apache.kafka.common.resource.PatternType;
import org.apache.kafka.common.resource.ResourceType;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.Objects;

public class AccessControlPolicy implements Serializable, Named {

    private final String principalType;

    private final String principalName;

    private final String resourcePattern;

    private final PatternType patternType;

    private final ResourceType resourceType;

    private final AclOperation operation;

    private final AclPermissionType permission;

    private final String host;

    public static AccessControlPolicyBuilder newBuilder() {
        return new AccessControlPolicyBuilder();
    }

    /**
     * Creates a new {@link AccessControlPolicy} instance.
     */
    AccessControlPolicy(@NotNull final String principalType,
                        @NotNull final String principalName,
                        @NotNull final String resourcePattern,
                        @NotNull final PatternType patternType,
                        @NotNull final ResourceType resourceType,
                        @NotNull final AclPermissionType permission,
                        @NotNull final AclOperation operation,
                        @NotNull final String host) {
        this.principalType = principalType;
        this.principalName = Objects.requireNonNull(principalName, "'principalName' cannot be null");
        this.permission = Objects.requireNonNull(permission, "'permission' cannot be null");
        this.operation = Objects.requireNonNull(operation, "'operation' cannot be null");
        this.host = Objects.requireNonNull(host, "'host' cannot be null");
        this.resourcePattern = Objects.requireNonNull(resourcePattern, "'resourcePattern' cannot be null");
        this.patternType = Objects.requireNonNull(patternType, "'patternType' cannot be null");
        this.resourceType = Objects.requireNonNull(resourceType, "'resourceType' cannot be null");
    }

    public String principalName() {
        return principalName;
    }

    public AclPermissionType permission() {
        return permission;
    }

    public String principal() {
        return this.principalType + ":" + this.principalName;
    }

    public String principalType() {
        return principalType;
    }

    public String resourcePattern() {
        return resourcePattern;
    }

    public PatternType patternType() {
        return patternType;
    }

    public ResourceType resourceType() {
        return resourceType;
    }

    public AclOperation operation() {
        return operation;
    }

    public String host() {
        return host;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AccessControlPolicy that = (AccessControlPolicy) o;
        return Objects.equals(principalType, that.principalType) &&
               Objects.equals(principalName, that.principalName) &&
               Objects.equals(resourcePattern, that.resourcePattern) &&
                patternType == that.patternType &&
                resourceType == that.resourceType &&
                operation == that.operation &&
                permission == that.permission &&
                Objects.equals(host, that.host);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                principalType,
                principalName,
                resourcePattern,
                patternType,
                resourceType,
                operation,
                permission,
                host
        );
    }

    @Override
    public String name() {
        return principalName;
    }
}
