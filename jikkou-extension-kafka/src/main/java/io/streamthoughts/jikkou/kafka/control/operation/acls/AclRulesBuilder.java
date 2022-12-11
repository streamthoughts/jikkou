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
package io.streamthoughts.jikkou.kafka.control.operation.acls;

import io.streamthoughts.jikkou.kafka.model.AccessControlPolicy;
import io.streamthoughts.jikkou.kafka.models.V1KafkaAccessRoleObject;
import io.streamthoughts.jikkou.kafka.models.V1KafkaAccessUserObject;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Default interface to convert from and to {@link AclRulesBuilder} instance.
 */
public interface AclRulesBuilder {

    List<AccessControlPolicy> toAccessControlPolicy(final Collection<V1KafkaAccessRoleObject> roles,
                                                    final V1KafkaAccessUserObject user);

    List<V1KafkaAccessUserObject> toAccessUserObjects(final Collection<AccessControlPolicy> rules);

    default boolean canBuildAclUserPolicy() {
        return true;
    }

    static AclRulesBuilder combines(final AclRulesBuilder...builders) {

        return new AclRulesBuilder() {

            /**
             * {@inheritDoc}
             */
            @Override
            public List<AccessControlPolicy> toAccessControlPolicy(final Collection<V1KafkaAccessRoleObject> roles,
                                                                  final V1KafkaAccessUserObject user) {

                List<AccessControlPolicy> rules = new LinkedList<>();
                for (AclRulesBuilder b : builders) {
                    rules.addAll(b.toAccessControlPolicy(roles, user));
                }
                return rules;
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public List<V1KafkaAccessUserObject> toAccessUserObjects(final Collection<AccessControlPolicy> rules) {
                List<V1KafkaAccessUserObject> policies = new LinkedList<>();
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
