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

import io.streamthoughts.kafka.specs.model.V1AccessRoleObject;
import io.streamthoughts.kafka.specs.model.V1AccessUserObject;

import java.util.Collection;
import java.util.LinkedList;

/**
 * Default interface to convert from and to {@link AclRulesBuilder} instance.
 */
public interface AclRulesBuilder {

    Collection<AccessControlPolicy> toAccessControlPolicy(final Collection<V1AccessRoleObject> groups,
                                                          final V1AccessUserObject user);

    Collection<V1AccessUserObject> toAccessUserObjects(final Collection<AccessControlPolicy> rules);

    default boolean canBuildAclUserPolicy() {
        return true;
    }

    static AclRulesBuilder combines(final AclRulesBuilder...builders) {

        return new AclRulesBuilder() {

            /**
             * {@inheritDoc}
             */
            @Override
            public Collection<AccessControlPolicy> toAccessControlPolicy(final Collection<V1AccessRoleObject> groups,
                                                                         final V1AccessUserObject user) {

                Collection<AccessControlPolicy> rules = new LinkedList<>();
                for (AclRulesBuilder b : builders) {
                    rules.addAll(b.toAccessControlPolicy(groups, user));
                }
                return rules;
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public Collection<V1AccessUserObject> toAccessUserObjects(final Collection<AccessControlPolicy> rules) {
                Collection<V1AccessUserObject> policies = new LinkedList<>();
                for (AclRulesBuilder b : builders) {
                    if (b.canBuildAclUserPolicy()) {
                        policies.addAll(b.toAccessUserObjects(rules));
                    }
                }
                return policies;
            }
        };
    }

}
