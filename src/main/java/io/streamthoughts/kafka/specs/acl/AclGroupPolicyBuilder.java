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

import java.util.HashSet;
import java.util.Set;

/**
 * Builder class to create new {@link AclGroupPolicy} instances.
 */
public class AclGroupPolicyBuilder {

    private String name;

    private String pattern;

    private PatternType patternType;

    private ResourceType type;

    private Set<AclOperationPolicy> operations = new HashSet<>();

    /**
     * Creates a new builder.
     *
     * @return new {@link AclGroupPolicyBuilder} instance.
     */
    public static AclGroupPolicyBuilder newBuilder() {
        return new AclGroupPolicyBuilder();
    }

    /**
     * Creates a new {@link AclGroupPolicyBuilder} instance.
     */
    private AclGroupPolicyBuilder() {
    }

    public AclGroupPolicyBuilder withPatternType(final PatternType patternType) {
        this.patternType = patternType;
        return this;
    }

    public AclGroupPolicyBuilder withName(final String name) {
        this.name = name;
        return this;
    }

    public AclGroupPolicyBuilder withPattern(final String pattern) {
        this.pattern = pattern;
        return this;
    }


    public AclGroupPolicyBuilder onResourceType(final ResourceType type) {
        this.type = type;
        return this;
    }

    public AclGroupPolicyBuilder allow(final AclOperationPolicy operation) {
        this.operations.add(operation);
        return this;
    }

    public AclGroupPolicy build() {
        return new AclGroupPolicy(
                name,
                new AclResourceMatcher(pattern, patternType, type),
                operations
        );
    }
}
