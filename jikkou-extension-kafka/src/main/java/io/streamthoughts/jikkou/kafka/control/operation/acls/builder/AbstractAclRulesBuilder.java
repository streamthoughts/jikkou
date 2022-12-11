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
package io.streamthoughts.jikkou.kafka.control.operation.acls.builder;

import io.streamthoughts.jikkou.kafka.control.operation.acls.AclRulesBuilder;
import io.streamthoughts.jikkou.kafka.model.AccessControlPolicy;
import io.streamthoughts.jikkou.kafka.model.AccessOperationPolicy;
import io.streamthoughts.jikkou.kafka.models.V1KafkaAccessPermission;
import io.streamthoughts.jikkou.kafka.models.V1KafkaAccessRoleObject;
import io.streamthoughts.jikkou.kafka.models.V1KafkaAccessUserObject;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.kafka.common.acl.AclPermissionType;
import org.apache.kafka.common.resource.PatternType;
import org.apache.kafka.common.resource.ResourceType;

abstract class AbstractAclRulesBuilder implements AclRulesBuilder {


    List<AccessControlPolicy> createAllAclsFor(final String principal,
                                               final Collection<V1KafkaAccessPermission> permissions) {
        return createAllAclsFor(principal, permissions, null, null, null);
    }

    List<AccessControlPolicy> createAllAclsFor(final String principal,
                                               final Collection<V1KafkaAccessPermission> permissions,
                                               final String overrideResourcePattern,
                                               final PatternType overridePatternType,
                                               final ResourceType overrideResourceType
    ) {

        List<AccessControlPolicy> rules = new LinkedList<>();
        for (V1KafkaAccessPermission permission : permissions) {
            for (AccessOperationPolicy operation : permission.getAllowOperations()) {

                var resource = permission.getResource();
                rules.add(newAccessControlPolicy(
                        principal,
                        (overrideResourcePattern == null) ? resource.getPattern() : overrideResourcePattern,
                        (overridePatternType == null) ? resource.getPatternType() : overridePatternType,
                        (overrideResourceType == null) ? resource.getType() : overrideResourceType,
                        operation));
            }
        }
        return rules;
    }

    /**
     * Keeps only groups attached to the specified user.
     *
     * @param groups the groups to be filtered
     * @param user   the user to be used.
     * @return a new list of {@link V1KafkaAccessRoleObject} instances.
     */
    List<V1KafkaAccessRoleObject> filterAclRolesForUser(final Collection<V1KafkaAccessRoleObject> groups,
                                                        final V1KafkaAccessUserObject user) {
        return groups.stream()
                .filter(gr -> user.getRoles().contains(gr.getName()))
                .collect(Collectors.toList());
    }

    /**
     * Creates a new {@link AccessControlPolicy} for the specified user, topic and operation.
     *
     * @param principal       the user principal to be used.
     * @param resourcePattern the resource pattern.
     * @param patternType     the pattern type.
     * @param resourceType    the resource type.
     * @param resourcePattern the resource on which to apply access control.
     * @param operation       the operation.
     * @return a new {@link AccessControlPolicy} instance.
     */
    private AccessControlPolicy newAccessControlPolicy(final String principal,
                                                       final String resourcePattern,
                                                       final PatternType patternType,
                                                       final ResourceType resourceType,
                                                       final AccessOperationPolicy operation) {
        return AccessControlPolicy.builder()
                .withPrincipal(principal)
                .withResourcePattern(resourcePattern)
                .withPatternType(patternType)
                .withResourceType(resourceType)
                .withPermission(AclPermissionType.ALLOW)
                .withOperation(operation.operation())
                .withHost(operation.host())
                .build();
    }
}
