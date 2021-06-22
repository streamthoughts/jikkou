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

import org.apache.kafka.common.resource.PatternType;
import org.apache.kafka.common.resource.ResourceType;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Builder class to create new {@link AclUserPolicy} instances.
 */
public class AclUserPolicyBuilder {

    private String principal;
    private final Set<String> groups  = new HashSet<>();
    private final Set<AclResourcePermission> permissions  = new HashSet<>();

    /**
     * Creates a new builder.
     *
     * @return new {@link AclUserPolicyBuilder} instance.
     */
    public static AclUserPolicyBuilder newBuilder() {
        return new AclUserPolicyBuilder();
    }

    /**
     * Creates a new {@link AclUserPolicyBuilder} instance.
     */
    private AclUserPolicyBuilder() {
    }

    public AclUserPolicyBuilder principal(final String principal) {
        this.principal = principal;
        return this;
    }

    public AclUserPolicyBuilder groups(final Collection<String> groups) {
        this.groups.addAll(groups);
        return this;
    }

    public AclUserPolicyBuilder addPermission(final String pattern,
                                              final PatternType patternType,
                                              final ResourceType type,
                                              final Set<AclOperationPolicy> operations) {
        this.permissions.add(
            new AclResourcePermission(new AclResourceMatcher(pattern, patternType, type)
            ,operations)
        );
        return this;
    }

    public AclUserPolicy build() {
        return new AclUserPolicy(principal, groups, permissions);
    }
}
