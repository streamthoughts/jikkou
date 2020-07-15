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

import org.apache.kafka.common.acl.AclOperation;
import org.apache.kafka.common.acl.AclPermissionType;
import org.apache.kafka.common.resource.PatternType;
import org.apache.kafka.common.resource.ResourceType;

public class AclRuleBuilder {

    private String principalType;
    private String principalName;
    private String resource;
    private PatternType patternType = PatternType.LITERAL;
    private ResourceType resourceType;
    private AclPermissionType permission;
    private AclOperation operation;
    private String host;

    public AclRuleBuilder withPrincipal(final String principal) {
        String[] split = principal.split(":");
        this.principalType = split[0];
        this.principalName = split[1];
        return this;
    }
    public AclRuleBuilder withPrincipalType(final String principalType) {
        this.principalType = principalType;
        return this;
    }

    public AclRuleBuilder withPrincipalName(final String principalName) {
        this.principalName = principalName;
        return this;
    }

    public AclRuleBuilder withResourcePattern(final String resource) {
        this.resource = resource;
        return this;
    }

    public AclRuleBuilder withPatternType(final PatternType patternType) {
        this.patternType = patternType;
        return this;
    }

    public AclRuleBuilder withResourceType(final ResourceType resourceType) {
        this.resourceType = resourceType;
        return this;
    }

    public AclRuleBuilder withPermission(final AclPermissionType permission) {
        this.permission = permission;
        return this;
    }

    public AclRuleBuilder withhOperation(final AclOperation operation) {
        this.operation = operation;
        return this;
    }

    public AclRuleBuilder withHost(final String host) {
        this.host = host;
        return this;
    }

    public AclRule build() {
        return new AclRule(principalType, principalName, resource, patternType, resourceType, permission, operation, host);
    }
}