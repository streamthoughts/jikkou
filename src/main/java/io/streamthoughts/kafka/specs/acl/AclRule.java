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

import io.streamthoughts.kafka.specs.resources.ClusterResource;
import org.apache.kafka.common.acl.AclOperation;
import org.apache.kafka.common.acl.AclPermissionType;
import org.apache.kafka.common.resource.PatternType;
import org.apache.kafka.common.resource.ResourceType;

import java.util.Objects;


public class AclRule implements ClusterResource {

    private final String principalType;

    private final String principalName;

    private final String resourcePattern ;

    private final PatternType patternType ;

    private final ResourceType resourceType;

    private final AclOperation operation;

    private final AclPermissionType permission;

    private final String host;


    public static AclRuleBuilder newBuilder() {
        return new AclRuleBuilder();
    }


    /**
     * Creates a new {@link AclRule} instance.
     */
    AclRule(final String principalType,
            final String principalName,
            final String resourcePattern,
            final PatternType patternType,
            final ResourceType resourceType,
            final AclPermissionType permission,
            final AclOperation operation,
            final String host) {
        Objects.requireNonNull(principalName, "principalName cannot be null");
        Objects.requireNonNull(resourcePattern, "resourcePattern cannot be null");
        Objects.requireNonNull(patternType, "patternType cannot be null");
        Objects.requireNonNull(permission, "permission cannot be null");
        Objects.requireNonNull(resourceType, "resourceType cannot be null");
        Objects.requireNonNull(operation, "operation cannot be null");
        Objects.requireNonNull(host, "host cannot be null");
        this.principalType = principalType;
        this.principalName = principalName;
        this.resourcePattern = resourcePattern;
        this.patternType = patternType;
        this.permission = permission;
        this.resourceType = resourceType;
        this.operation = operation;
        this.host = host;
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
        AclRule rule = (AclRule) o;
        return Objects.equals(principalType, rule.principalType) &&
                Objects.equals(principalName, rule.principalName) &&
                Objects.equals(resourcePattern, rule.resourcePattern) &&
                patternType == rule.patternType &&
                resourceType == rule.resourceType &&
                operation == rule.operation &&
                permission == rule.permission &&
                Objects.equals(host, rule.host);
    }

    @Override
    public int hashCode() {

        return Objects.hash(principalType, principalName, resourcePattern, patternType, resourceType, operation, permission, host);
    }

    @Override
    public String toString() {
        return "AclRule{" +
                "principalType=" + principalType +
                ", principalName='" + principalName + '\'' +
                ", resourcePattern='" + resourcePattern + '\'' +
                ", patternType='" + patternType + '\'' +
                ", resourceType=" + resourceType +
                ", operation=" + operation +
                ", permission=" + permission +
                ", host='" + host + '\'' +
                '}';
    }
}
