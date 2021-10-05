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
package io.streamthoughts.kafka.specs.acl.builder;

import io.streamthoughts.kafka.specs.acl.AclRoleBasedPolicy;
import io.streamthoughts.kafka.specs.acl.AclOperationPolicy;
import io.streamthoughts.kafka.specs.acl.AclResourceMatcher;
import io.streamthoughts.kafka.specs.acl.AclResourcePermission;
import io.streamthoughts.kafka.specs.acl.AclRule;
import io.streamthoughts.kafka.specs.acl.AclRulesBuilder;
import io.streamthoughts.kafka.specs.acl.AclUserPolicy;
import org.apache.kafka.common.acl.AclPermissionType;
import org.apache.kafka.common.resource.PatternType;
import org.apache.kafka.common.resource.ResourceType;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

abstract class AbstractAclRulesBuilder implements AclRulesBuilder {


    Collection<AclRule> createAllAclsFor(final String principal,
                                         final Collection<AclResourcePermission> permissions) {
        return createAllAclsFor(principal, permissions, null, null, null);
    }

    Collection<AclRule> createAllAclsFor(final String principal,
                                         final Collection<AclResourcePermission> permissions,
                                         final String overrideResourcePattern,
                                         final PatternType overridePatternType,
                                         final ResourceType overrideResourceType
    ) {

        List<AclRule> rules = new LinkedList<>();
        for (AclResourcePermission permission : permissions) {
            for (AclOperationPolicy operation : permission.operations()) {

                final AclResourceMatcher resource = permission.resource();
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
     * @return          a new list of {@link AclRoleBasedPolicy} instances.
     */
    List<AclRoleBasedPolicy> filterAclGroupsForUser(final Collection<AclRoleBasedPolicy> groups,
                                                    final AclUserPolicy user) {
        return groups.stream()
                .filter(gr -> user.roles().contains(gr.name()))
                .collect(Collectors.toList());
    }

    /**
     * Creates a new {@link AclRule} for the specified user, topic and operation.
     *
     * @param principal       the user principal to be used.
     * @param resourcePattern the resource pattern.
     * @param patternType     the pattern type.
     * @param resourceType    the resource type.
     * @param resourcePattern the resource on which to apply access control.
     * @param operation       the operation.
     * @return                a new {@link AclRule} instance.
     */
    private AclRule createNewAcl(final String principal,
                                 final String resourcePattern,
                                 final PatternType patternType,
                                 final ResourceType resourceType,
                                 final AclOperationPolicy operation) {
        return AclRule.newBuilder()
                .withPrincipal(principal)
                .withResourcePattern(resourcePattern)
                .withPatternType(patternType)
                .withResourceType(resourceType)
                .withPermission(AclPermissionType.ALLOW)
                .withhOperation(operation.operation())
                .withHost(operation.host())
                .build();
    }
}
