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

import java.util.Collection;
import java.util.LinkedList;

/**
 * Default interface to convert from and to {@link AclRulesBuilder} instance.
 */
public interface AclRulesBuilder {

    Collection<AclRule> toAclRules(final Collection<AclGroupPolicy> groups, final AclUserPolicy user);

    Collection<AclUserPolicy> toAclUserPolicy(final Collection<AclRule> rules);

    default boolean canBuildAclUserPolicy() {
        return true;
    }

    static AclRulesBuilder combines(final AclRulesBuilder...builders) {

        return new AclRulesBuilder() {

            /**
             * {@inheritDoc}
             */
            @Override
            public Collection<AclRule> toAclRules(final Collection<AclGroupPolicy> groups, final AclUserPolicy user) {

                Collection<AclRule> rules = new LinkedList<>();
                for (AclRulesBuilder b : builders) {
                    rules.addAll(b.toAclRules(groups, user));
                }
                return rules;
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public Collection<AclUserPolicy> toAclUserPolicy(final Collection<AclRule> rules) {
                Collection<AclUserPolicy> policies = new LinkedList<>();
                for (AclRulesBuilder b : builders) {
                    if (b.canBuildAclUserPolicy()) {
                        policies.addAll(b.toAclUserPolicy(rules));
                    }
                }
                return policies;
            }
        };
    }

}
