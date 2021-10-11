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

import io.streamthoughts.kafka.specs.model.V1AccessRoleObject;
import io.streamthoughts.kafka.specs.model.V1AccessPermission;
import io.streamthoughts.kafka.specs.resources.acl.AccessControlPolicy;
import io.streamthoughts.kafka.specs.resources.acl.AclRulesBuilder;
import io.streamthoughts.kafka.specs.model.V1AccessPrincipalObject;
import io.streamthoughts.kafka.specs.internal.AdminClientUtils;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.TopicListing;
import org.apache.kafka.common.resource.PatternType;
import org.apache.kafka.common.resource.ResourceType;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TopicMatchingAclRulesBuilder extends AbstractAclRulesBuilder implements AclRulesBuilder {

    private AdminClient client;
    private CompletableFuture<Collection<TopicListing>> listTopics;

    TopicMatchingAclRulesBuilder() {}

    /**
     * Creates a new {@link TopicMatchingAclRulesBuilder} instance.
     *
     * @param client    the kafka admin client to be used.
     */
    public TopicMatchingAclRulesBuilder(final AdminClient client) {
        Objects.requireNonNull(client, "client cannot be null");
        this.client = client;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<AccessControlPolicy> toAccessControlPolicy(final Collection<V1AccessRoleObject> groups,
                                                                 final V1AccessPrincipalObject user) {
        Objects.requireNonNull(groups, "groups cannot be null");
        Objects.requireNonNull(user, "user cannot be null");

        List<V1AccessRoleObject> userGroups = filterAclRolesForUser(groups, user);

        CompletableFuture<List<AccessControlPolicy>> future = getListTopics().thenApply((topics) -> topics.stream()
                .flatMap(topic -> {
                    Collection<AccessControlPolicy> roleAclBindings = createAclForRolePoliciesMatchingTopic(user, userGroups, topic);
                    Collection<AccessControlPolicy> userAclBindings = createAclForUserPoliciesMatchingTopic(user, topic);
                    return Stream.concat(roleAclBindings.stream(), userAclBindings.stream());
                }).collect(Collectors.toList()));

        return future.join();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<V1AccessPrincipalObject> toAclUserPolicy(final Collection<AccessControlPolicy> rules) {
        throw new UnsupportedOperationException();
    }

    /**
     * Only for testing purpose.
     *
     * @param topics    the list of topics.
     */
    void setListTopics(final CompletableFuture<Collection<TopicListing>> topics) {
        this.listTopics = topics;
    }

    private CompletableFuture<Collection<TopicListing>> getListTopics() {
        if (listTopics == null) {
            this.listTopics = AdminClientUtils.listTopics(client);
        }
        return listTopics;
    }

    private Collection<AccessControlPolicy> createAclForUserPoliciesMatchingTopic(final V1AccessPrincipalObject user,
                                                                                  final TopicListing topic) {
        List<V1AccessPermission> permissions = user.permissions()
                .stream()
                .filter(p -> p.resource().type() == ResourceType.TOPIC)
                .filter(p -> p.resource().isPatternOfTypeMatchRegex())
                .collect(Collectors.toList());
        return createAllAclsFor(user.principal(),
                filterPermissionMatchingTopic(permissions, topic),
                topic.name(),
                PatternType.LITERAL,
                ResourceType.TOPIC);
    }

    private Collection<AccessControlPolicy> createAclForRolePoliciesMatchingTopic(final V1AccessPrincipalObject user,
                                                                                  final List<V1AccessRoleObject> groups,
                                                                                  final TopicListing topic) {
        List<V1AccessPermission> permissions = groups.stream()
                .map(V1AccessRoleObject::permission)
                .filter(p -> p.resource().type() == ResourceType.TOPIC)
                .filter(p -> p.resource().isPatternOfTypeMatchRegex())
                .collect(Collectors.toList());

        return createAllAclsFor(user.principal(),
                filterPermissionMatchingTopic(permissions, topic),
                topic.name(),
                PatternType.LITERAL,
                ResourceType.TOPIC);
    }

    /**
     * Keeps only #@link AclResourcePermission matching the specified topic.
     *
     * @param groups    the permissions to be filtered
     * @param topic     the topic to be used.
     * @return          a new list of {@link V1AccessPermission} instances.
     */
    private Collection<V1AccessPermission> filterPermissionMatchingTopic(final Collection<V1AccessPermission> groups,
                                                                         final TopicListing topic) {
        return groups.stream()
            .filter(permission -> {
                String regex = permission.resource().pattern();
                regex = regex.substring(1, regex.length() - 1);
                Matcher matcher = Pattern.compile(regex).matcher(topic.name());
                return matcher.matches();
            }).collect(Collectors.toSet());
    }
}
