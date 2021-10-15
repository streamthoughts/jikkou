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

import io.streamthoughts.kafka.specs.model.V1AccessOperationPolicy;
import io.streamthoughts.kafka.specs.model.V1AccessPermission;
import io.streamthoughts.kafka.specs.model.V1AccessResourceMatcher;
import io.streamthoughts.kafka.specs.model.V1AccessRoleObject;
import io.streamthoughts.kafka.specs.model.V1AccessUserObject;
import io.streamthoughts.kafka.specs.resources.acl.AccessControlPolicy;
import io.streamthoughts.kafka.specs.resources.acl.AclRulesBuilder;
import org.apache.kafka.common.resource.PatternType;
import org.apache.kafka.common.resource.ResourceType;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LiteralAclRulesBuilder extends AbstractAclRulesBuilder implements AclRulesBuilder {

    /**
     * Creates a new {@link LiteralAclRulesBuilder} instance.
     */
    public LiteralAclRulesBuilder() {
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<AccessControlPolicy> toAccessControlPolicy(final Collection<V1AccessRoleObject> groups,
                                                                 final V1AccessUserObject user) {
        Objects.requireNonNull(groups, "groups cannot be null");
        Objects.requireNonNull(user, "user cannot be null");

        List<V1AccessRoleObject> userGroups = filterAclRolesForUser(groups, user);
        return createAclsForLiteralOrPrefixPermissions(user, userGroups);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<V1AccessUserObject> toAccessUserObjects(final Collection<AccessControlPolicy> rules) {

        return rules
                .stream()
                .collect(Collectors.groupingBy(AccessControlPolicy::principal))
                .entrySet()
                .stream()
                .map(e -> buildAccessUserObject(e.getKey(), e.getValue()))
                .collect(Collectors.toList());
    }

    private V1AccessUserObject buildAccessUserObject(final String principal,
                                                     final List<AccessControlPolicy> accessControlPolicies) {

        final V1AccessUserObject.Builder builder = V1AccessUserObject
                .newBuilder()
                .withPrincipal(principal);

        accessControlPolicies
                .stream()
                .collect(Collectors.groupingBy(ResourcePattern::new))
                .forEach((pattern, policies) -> {
                    final V1AccessResourceMatcher resource = V1AccessResourceMatcher
                            .newBuilder()
                            .withPattern(pattern.pattern)
                            .withType(pattern.resourceType)
                            .withPatternType(pattern.patternType)
                            .build();

                    final Set<V1AccessOperationPolicy> operations = policies
                            .stream()
                            .map(a -> new V1AccessOperationPolicy(a.operation(), a.host()))
                            .collect(Collectors.toSet());

                    final V1AccessPermission permission = V1AccessPermission
                            .newBuilder()
                            .allow(operations)
                            .onResource(resource)
                            .build();
                    builder.withPermission(permission);
                });
        return builder.build();
    }

    private Collection<AccessControlPolicy> createAclsForLiteralOrPrefixPermissions(final V1AccessUserObject user,
                                                                                    final List<V1AccessRoleObject> groups) {

        final Stream<V1AccessPermission> userPermissions = user.permissions().stream();
        final Stream<V1AccessPermission> groupsPermissions = groups.stream().flatMap(g -> g.permissions().stream());

        List<V1AccessPermission> permissions = Stream.concat(userPermissions, groupsPermissions)
                .filter(p -> !p.resource().isPatternOfTypeMatchRegex())
                .distinct()
                .collect(Collectors.toList());

        return createAllAclsFor(user.principal(), permissions);
    }

    public static class ResourcePattern {

        public final String pattern;
        public final ResourceType resourceType;
        public final PatternType patternType;

        public ResourcePattern(final AccessControlPolicy policy) {
            this(policy.resourcePattern(), policy.resourceType(), policy.patternType());
        }

        public ResourcePattern(final String pattern,
                               final ResourceType resourceType,
                               final PatternType patternType) {
            this.pattern = pattern;
            this.resourceType = resourceType;
            this.patternType = patternType;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ResourcePattern that = (ResourcePattern) o;
            return Objects.equals(pattern, that.pattern) &&
                   Objects.equals(resourceType, that.resourceType) &&
                   Objects.equals(patternType, that.patternType);
        }

        @Override
        public int hashCode() {
            return Objects.hash(pattern, resourceType, patternType);
        }
    }

}
