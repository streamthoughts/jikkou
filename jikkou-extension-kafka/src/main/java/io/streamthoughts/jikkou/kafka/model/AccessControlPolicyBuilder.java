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
package io.streamthoughts.jikkou.kafka.model;

import org.apache.kafka.common.acl.AclOperation;
import org.apache.kafka.common.acl.AclPermissionType;
import org.apache.kafka.common.resource.PatternType;
import org.apache.kafka.common.resource.ResourceType;

/**
 * Builder class to create a new {@link AccessControlPolicy} object.
 */
public class AccessControlPolicyBuilder {

    private static final String PRINCIPAL_TYPE_SEPARATOR = ":";

    private String principalType;
    private String principalName;
    private String resource;
    private PatternType patternType = PatternType.LITERAL;
    private ResourceType resourceType;
    private AclPermissionType permission;
    private AclOperation operation;
    private String host;

    public AccessControlPolicyBuilder withPrincipal(final String principal) {
        String[] split = principal.split(PRINCIPAL_TYPE_SEPARATOR);
        this.principalType = split[0];
        this.principalName = split[1];
        return this;
    }

    public AccessControlPolicyBuilder withPrincipalType(final String principalType) {
        this.principalType = principalType;
        return this;
    }

    public AccessControlPolicyBuilder withPrincipalName(final String principalName) {
        this.principalName = principalName;
        return this;
    }

    public AccessControlPolicyBuilder withResourcePattern(final String resource) {
        this.resource = resource;
        return this;
    }

    public AccessControlPolicyBuilder withPatternType(final PatternType patternType) {
        this.patternType = patternType;
        return this;
    }

    public AccessControlPolicyBuilder withResourceType(final ResourceType resourceType) {
        this.resourceType = resourceType;
        return this;
    }

    public AccessControlPolicyBuilder withPermission(final AclPermissionType permission) {
        this.permission = permission;
        return this;
    }

    public AccessControlPolicyBuilder withOperation(final AclOperation operation) {
        this.operation = operation;
        return this;
    }

    public AccessControlPolicyBuilder withHost(final String host) {
        this.host = host;
        return this;
    }

    public AccessControlPolicy build() {
        return new AccessControlPolicy(
                principalType,
                principalName,
                resource,
                patternType,
                resourceType,
                permission,
                operation,
                host
        );
    }
}