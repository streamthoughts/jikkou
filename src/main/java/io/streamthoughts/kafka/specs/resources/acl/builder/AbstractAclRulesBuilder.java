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
package io.streamthoughts.kafka.specs.resources.acl.builder;

import io.streamthoughts.kafka.specs.resources.acl.AccessControlPolicy;
import io.streamthoughts.kafka.specs.resources.acl.AclRulesBuilder;
import io.streamthoughts.kafka.specs.model.V1AccessOperationPolicy;
import io.streamthoughts.kafka.specs.model.V1AccessPermission;
import io.streamthoughts.kafka.specs.model.V1AccessUserObject;
import io.streamthoughts.kafka.specs.model.V1AccessResourceMatcher;
import io.streamthoughts.kafka.specs.model.V1AccessRoleObject;
import org.apache.kafka.common.acl.AclPermissionType;
import org.apache.kafka.common.resource.PatternType;
import org.apache.kafka.common.resource.ResourceType;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

abstract class AbstractAclRulesBuilder implements AclRulesBuilder {


    Collection<AccessControlPolicy> createAllAclsFor(final String principal,
                                                     final Collection<V1AccessPermission> permissions) {
        return createAllAclsFor(principal, permissions, null, null, null);
    }

    Collection<AccessControlPolicy> createAllAclsFor(final String principal,
                                                     final Collection<V1AccessPermission> permissions,
                                                     final String overrideResourcePattern,
                                                     final PatternType overridePatternType,
                                                     final ResourceType overrideResourceType
    ) {

        List<AccessControlPolicy> rules = new LinkedList<>();
        for (V1AccessPermission permission : permissions) {
            for (V1AccessOperationPolicy operation : permission.operations()) {

                final V1AccessResourceMatcher resource = permission.resource();
                rules.add(createNewAcl(
                        principal,
                        (overrideResourcePattern == null) ? resource.pattern() : overrideResourcePattern,
                        (overridePatternType == null) ? resource.patternType() : overridePatternType,
                        (overrideResourceType == null) ? resource.type() : overrideResourceType,
                        operation));
            }
        }
        return rules;
    }

    /**
     * Keeps only groups attached to the specified user.
     *
     * @param groups    the groups to be filtered
     * @param user      the user to be used.
     * @return          a new list of {@link V1AccessRoleObject} instances.
     */
    List<V1AccessRoleObject> filterAclRolesForUser(final Collection<V1AccessRoleObject> groups,
                                                   final V1AccessUserObject user) {
        return groups.stream()
                .filter(gr -> user.roles().contains(gr.name()))
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
     * @return                a new {@link AccessControlPolicy} instance.
     */
    private AccessControlPolicy createNewAcl(final String principal,
                                             final String resourcePattern,
                                             final PatternType patternType,
                                             final ResourceType resourceType,
                                             final V1AccessOperationPolicy operation) {
        return AccessControlPolicy.newBuilder()
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
