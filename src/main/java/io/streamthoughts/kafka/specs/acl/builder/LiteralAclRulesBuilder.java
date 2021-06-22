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

import io.streamthoughts.kafka.specs.acl.AclGroupPolicy;
import io.streamthoughts.kafka.specs.acl.AclOperationPolicy;
import io.streamthoughts.kafka.specs.acl.AclResourcePermission;
import io.streamthoughts.kafka.specs.acl.AclRule;
import io.streamthoughts.kafka.specs.acl.AclRulesBuilder;
import io.streamthoughts.kafka.specs.acl.AclUserPolicy;
import io.streamthoughts.kafka.specs.acl.AclUserPolicyBuilder;

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
    public LiteralAclRulesBuilder() {
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<AclRule> toAclRules(final Collection<AclGroupPolicy> groups,
                                          final AclUserPolicy user) {
        Objects.requireNonNull(groups, "groups cannot be null");
        Objects.requireNonNull(user, "user cannot be null");

        List<AclGroupPolicy> userGroups = filterAclGroupsForUser(groups, user);
        return createAclsForLiteralOrPrefixPermissions(user, userGroups);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<AclUserPolicy> toAclUserPolicy(final Collection<AclRule> rules) {

        return rules
                .stream()
                .collect(Collectors.groupingBy(AclRule::principalName))
                .entrySet()
                .stream()
                .map(e -> buildAclUserPolicy(e.getKey(), e.getValue()))
                .collect(Collectors.toList());
    }

    private AclUserPolicy buildAclUserPolicy(final String principal, List<AclRule> rules) {
        AclUserPolicyBuilder builder = AclUserPolicyBuilder.newBuilder()
                .principal(principal);

        Map<String, List<AclRule>> aclGroupsByResource = rules
                .stream()
                .collect(Collectors.groupingBy(AclRule::resourcePattern));

        aclGroupsByResource.forEach( (resource, acls) -> {
            Set<AclOperationPolicy> policies = acls.stream()
                    .map(a -> new AclOperationPolicy(a.operation(), a.host()))
                    .collect(Collectors.toSet());
            builder.addPermission(resource, acls.get(0).patternType(), acls.get(0).resourceType(), policies);

        });
        return builder.build();
    }


    private Collection<AclRule> createAclsForLiteralOrPrefixPermissions(final AclUserPolicy user,
                                                                        final List<AclGroupPolicy> groups) {

        final Stream<AclResourcePermission> userPermissions = user.permissions().stream();
        final Stream<AclResourcePermission> groupsPermissions = groups.stream().map(AclGroupPolicy::permission);

        List<AclResourcePermission> permissions = Stream.concat(userPermissions, groupsPermissions)
                .filter(p -> !p.resource().isPatternOfTypeMatchRegex())
                .collect(Collectors.toList());
        
        return createAllAclsFor(user.principal(), permissions);
    }

}
