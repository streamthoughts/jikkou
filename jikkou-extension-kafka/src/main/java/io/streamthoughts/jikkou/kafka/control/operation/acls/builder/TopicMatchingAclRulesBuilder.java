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
import io.streamthoughts.jikkou.kafka.internals.KafkaUtils;
import io.streamthoughts.jikkou.kafka.model.AccessControlPolicy;
import io.streamthoughts.jikkou.kafka.models.V1KafkaAccessPermission;
import io.streamthoughts.jikkou.kafka.models.V1KafkaAccessRoleObject;
import io.streamthoughts.jikkou.kafka.models.V1KafkaAccessUserObject;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.TopicListing;
import org.apache.kafka.common.resource.PatternType;
import org.apache.kafka.common.resource.ResourceType;

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
    public boolean canBuildAclUserPolicy() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<AccessControlPolicy> toAccessControlPolicy(final Collection<V1KafkaAccessRoleObject> groups,
                                                           final V1KafkaAccessUserObject user) {
        Objects.requireNonNull(groups, "groups cannot be null");
        Objects.requireNonNull(user, "user cannot be null");

        List<V1KafkaAccessRoleObject> userGroups = filterAclRolesForUser(groups, user);

        CompletableFuture<List<AccessControlPolicy>> future = getListTopics().thenApply(topics -> topics.stream()
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
    public List<V1KafkaAccessUserObject> toAccessUserObjects(final Collection<AccessControlPolicy> rules) {
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
            listTopics = KafkaUtils.listTopics(client);
        }
        return listTopics;
    }

    private Collection<AccessControlPolicy> createAclForUserPoliciesMatchingTopic(final V1KafkaAccessUserObject user,
                                                                                  final TopicListing topic) {
        var permissions = user.getPermissions();
        if (permissions == null) {
            return Collections.emptyList();
        }

        permissions = permissions
                .stream()
                .filter(p -> p.getResource().getType() == ResourceType.TOPIC)
                .filter(p -> KafkaAccessResourceMatcherAdapter.from(p.getResource()).isPatternOfTypeMatchRegex())
                .collect(Collectors.toList());

        return createAllAclsFor(user.getPrincipal(),
                filterPermissionMatchingTopic(permissions, topic),
                topic.name(),
                PatternType.LITERAL,
                ResourceType.TOPIC);
    }

    private Collection<AccessControlPolicy> createAclForRolePoliciesMatchingTopic(final V1KafkaAccessUserObject user,
                                                                                  final List<V1KafkaAccessRoleObject> groups,
                                                                                  final TopicListing topic) {
        List<V1KafkaAccessPermission> permissions = groups.stream()
                .flatMap(g -> g.getPermissions().stream())
                .filter(p -> p.getResource().getType() == ResourceType.TOPIC)
                .filter(p -> KafkaAccessResourceMatcherAdapter.from(p.getResource()).isPatternOfTypeMatchRegex())
                .distinct()
                .collect(Collectors.toList());

        return createAllAclsFor(user.getPrincipal(),
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
     * @return          a new list of {@link V1KafkaAccessPermission} instances.
     */
    private Collection<V1KafkaAccessPermission> filterPermissionMatchingTopic(final Collection<V1KafkaAccessPermission> groups,
                                                                              final TopicListing topic) {
        return groups.stream()
            .filter(permission -> {
                String regex = permission.getResource().getPattern();
                regex = regex.substring(1, regex.length() - 1);
                Matcher matcher = Pattern.compile(regex).matcher(topic.name());
                return matcher.matches();
            }).collect(Collectors.toSet());
    }
}
