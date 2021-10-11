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
import io.streamthoughts.kafka.specs.model.V1AccessPrincipalObject;
import io.streamthoughts.kafka.specs.model.V1AccessRoleObject;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LiteralAclRulesBuilder extends AbstractAclRulesBuilder implements AclRulesBuilder {

    /**
     * Creates a new {@link LiteralAclRulesBuilder} instance.
     */
    public LiteralAclRulesBuilder() {}


    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<AccessControlPolicy> toAccessControlPolicy(final Collection<V1AccessRoleObject> groups,
                                                                 final V1AccessPrincipalObject user) {
        Objects.requireNonNull(groups, "groups cannot be null");
        Objects.requireNonNull(user, "user cannot be null");

        List<V1AccessRoleObject> userGroups = filterAclGroupsForUser(groups, user);
        return createAclsForLiteralOrPrefixPermissions(user, userGroups);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<V1AccessPrincipalObject> toAclUserPolicy(final Collection<AccessControlPolicy> rules) {

        return rules
                .stream()
                .collect(Collectors.groupingBy(AccessControlPolicy::principalName))
                .entrySet()
                .stream()
                .map(e -> buildAclUserPolicy(e.getKey(), e.getValue()))
                .collect(Collectors.toList());
    }

    private V1AccessPrincipalObject buildAclUserPolicy(final String principal, final List<AccessControlPolicy> rules) {
        V1AccessPrincipalObject.Builder builder = V1AccessPrincipalObject
                .newBuilder()
                .principal(principal);

        Map<String, List<AccessControlPolicy>> aclGroupsByResource = rules
                .stream()
                .collect(Collectors.groupingBy(AccessControlPolicy::resourcePattern));

        aclGroupsByResource.forEach( (resource, acls) -> {
            Set<V1AccessOperationPolicy> policies = acls.stream()
                    .map(a -> new V1AccessOperationPolicy(a.operation(), a.host()))
                    .collect(Collectors.toSet());
            builder.addPermission(resource, acls.get(0).patternType(), acls.get(0).resourceType(), policies);

        });
        return builder.build();
    }

    private Collection<AccessControlPolicy> createAclsForLiteralOrPrefixPermissions(final V1AccessPrincipalObject user,
                                                                                    final List<V1AccessRoleObject> groups) {

        final Stream<V1AccessPermission> userPermissions = user.permissions().stream();
        final Stream<V1AccessPermission> groupsPermissions = groups.stream().map(V1AccessRoleObject::permission);

        List<V1AccessPermission> permissions = Stream.concat(userPermissions, groupsPermissions)
                .filter(p -> !p.resource().isPatternOfTypeMatchRegex())
                .collect(Collectors.toList());
        
        return createAllAclsFor(user.principal(), permissions);
    }

}
