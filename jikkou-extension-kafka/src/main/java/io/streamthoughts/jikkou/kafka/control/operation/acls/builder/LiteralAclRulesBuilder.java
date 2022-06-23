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

import io.streamthoughts.jikkou.kafka.adapters.KafkaAccessResourceMatcherAdapter;
import io.streamthoughts.jikkou.kafka.control.operation.acls.AclRulesBuilder;
import io.streamthoughts.jikkou.kafka.model.AccessControlPolicy;
import io.streamthoughts.jikkou.kafka.model.AccessOperationPolicy;
import io.streamthoughts.jikkou.kafka.models.V1KafkaAccessPermission;
import io.streamthoughts.jikkou.kafka.models.V1KafkaAccessResourceMatcher;
import io.streamthoughts.jikkou.kafka.models.V1KafkaAccessRoleObject;
import io.streamthoughts.jikkou.kafka.models.V1KafkaAccessUserObject;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.kafka.common.resource.PatternType;
import org.apache.kafka.common.resource.ResourceType;
import org.jetbrains.annotations.NotNull;

public class LiteralAclRulesBuilder extends AbstractAclRulesBuilder implements AclRulesBuilder {

    /**
     * Creates a new {@link LiteralAclRulesBuilder} instance.
     */
    public LiteralAclRulesBuilder() {
    }


    /** {@inheritDoc} */
    @Override
    public List<AccessControlPolicy> toAccessControlPolicy(@NotNull final Collection<V1KafkaAccessRoleObject> groups,
                                                           @NotNull final V1KafkaAccessUserObject user) {
        List<V1KafkaAccessRoleObject> userGroups = filterAclRolesForUser(groups, user);
        return createAclsForLiteralOrPrefixPermissions(user, userGroups);

    }

    /** {@inheritDoc} */
    @Override
    public List<V1KafkaAccessUserObject> toAccessUserObjects(final Collection<AccessControlPolicy> rules) {

        return rules
                .stream()
                .collect(Collectors.groupingBy(AccessControlPolicy::principal))
                .entrySet()
                .stream()
                .map(e -> buildAccessUserObject(e.getKey(), e.getValue()))
                .collect(Collectors.toList());
    }

    private V1KafkaAccessUserObject buildAccessUserObject(final String principal,
                                                          final List<AccessControlPolicy> accessControlPolicies) {

        List<V1KafkaAccessPermission> permissions = accessControlPolicies
                .stream()
                .collect(Collectors.groupingBy(ResourcePattern::new))
                .entrySet()
                .stream()
                .map(entry -> {
                    ResourcePattern pattern = entry.getKey();
                    List<AccessControlPolicy> policies = entry.getValue();
                    var resource = V1KafkaAccessResourceMatcher
                            .builder()
                            .withPattern(pattern.pattern)
                            .withType(pattern.resourceType)
                            .withPatternType(pattern.patternType)
                            .build();

                    final List<AccessOperationPolicy> operations = policies
                            .stream()
                            .map(a -> AccessOperationPolicy.builder()
                                    .withHost(a.host())
                                    .withOperation(a.operation())
                                    .build()
                           )
                            .toList();

                    return V1KafkaAccessPermission.builder()
                            .withAllowOperations(operations)
                            .withResource(resource)
                            .build();
                }).toList();

        return V1KafkaAccessUserObject
                .builder()
                .withPrincipal(principal)
                .withPermissions(permissions)
                .build();
    }

    private List<AccessControlPolicy> createAclsForLiteralOrPrefixPermissions(final V1KafkaAccessUserObject user,
                                                                              final List<V1KafkaAccessRoleObject> groups) {

        final Stream<V1KafkaAccessPermission> userPermissions = user
                .getPermissions().stream();

        final Stream<V1KafkaAccessPermission> groupsPermissions = groups.stream()
                .flatMap(g -> g.getPermissions().stream());

        List<V1KafkaAccessPermission> permissions = Stream.concat(userPermissions, groupsPermissions)
                .filter(it -> !KafkaAccessResourceMatcherAdapter.from(it.getResource()).isPatternOfTypeMatchRegex())
                .distinct()
                .collect(Collectors.toList());

        return createAllAclsFor(user.getPrincipal(), permissions);
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
