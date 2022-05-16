/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.streamthoughts.jikkou.kafka.resources.acl.builder;

import io.streamthoughts.jikkou.kafka.model.V1AccessOperationPolicy;
import io.streamthoughts.jikkou.kafka.model.V1AccessPermission;
import io.streamthoughts.jikkou.kafka.model.V1AccessResourceMatcher;
import io.streamthoughts.jikkou.kafka.model.V1AccessRoleObject;
import io.streamthoughts.jikkou.kafka.model.V1AccessUserObject;
import io.streamthoughts.jikkou.kafka.resources.acl.AccessControlPolicy;
import org.apache.kafka.clients.admin.TopicListing;
import org.apache.kafka.common.acl.AclOperation;
import org.apache.kafka.common.resource.PatternType;
import org.apache.kafka.common.resource.ResourceType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TopicMatchingAclRulesBuilderTest {


    static final List<V1AccessRoleObject> EMPTY_GROUP = Collections.emptyList();

    static final String TOPIC_REGEX = "/topic-.*/";
    static final String TOPIC_TEST_A = "topic-test-a";
    static final String TOPIC_TEST_B = "topic-test-b";
    static final String USER_TYPE = "User:";
    static final String SIMPLE_USER = "SimpleUser";
    static final String WILDCARD = "*";
    static final String TOPIC_WITH_WILDCARD = "topic-*";
    static final String SIMPLE_GROUP = "SimpleGroup";

    private TopicMatchingAclRulesBuilder builder;

    private Supplier<Collection<TopicListing>> getSupplier() {
        return () -> Arrays.asList(
                new TopicListing(TOPIC_TEST_A, false),
                new TopicListing(TOPIC_TEST_B, false)
        );
    }

    @BeforeEach
    public void setUp() {
        this.builder = new TopicMatchingAclRulesBuilder();
        this.builder.setListTopics(CompletableFuture.supplyAsync(getSupplier()));
    }

    @Test
    public void shouldBuildAclRulesGivenUserWithRegexPermissionAndNoGroup() {

        final V1AccessPermission permission = V1AccessPermission.newBuilder()
                .onResource(V1AccessResourceMatcher.newBuilder()
                        .withPattern(TOPIC_REGEX)
                        .withPatternType(PatternType.MATCH)
                        .withType(ResourceType.TOPIC)
                        .build()
                )
                .allow(new V1AccessOperationPolicy(AclOperation.CREATE))
                .build();

        final V1AccessUserObject user = V1AccessUserObject.newBuilder()
                .withPrincipal(USER_TYPE + SIMPLE_USER)
                .withPermission(permission)
                .build();

        Collection<AccessControlPolicy> rules = this.builder.toAccessControlPolicy(EMPTY_GROUP, user);

        assertEquals(2, rules.size());

        String[] topics = new String[]{TOPIC_TEST_A, TOPIC_TEST_B};
        int i = 0;
        for (AccessControlPolicy rule : rules) {
            assertEquals(WILDCARD, rule.host());
            assertEquals(AclOperation.CREATE, rule.operation());
            assertEquals(USER_TYPE + SIMPLE_USER, rule.principal());
            assertEquals(SIMPLE_USER, rule.principalName());
            assertEquals(topics[i], rule.resourcePattern());
            assertEquals(ResourceType.TOPIC, rule.resourceType());
            assertEquals(PatternType.LITERAL, rule.patternType());
            i++;
        }
    }

    @Test
    public void shouldBuildAclRulesGivenUserAndGroupWithRegexPermission() {

        V1AccessRoleObject group = V1AccessRoleObject.newBuilder()
                .withName(SIMPLE_GROUP)
                .withPermission(V1AccessPermission.newBuilder()
                    .allow(new V1AccessOperationPolicy(AclOperation.CREATE))
                    .onResource(V1AccessResourceMatcher
                            .newBuilder()
                            .withType(ResourceType.TOPIC)
                            .withPatternType(PatternType.MATCH)
                            .withPattern(TOPIC_REGEX)
                            .build()
                    )
                    .build())
                .build();

        V1AccessUserObject user = V1AccessUserObject.newBuilder()
                .withPrincipal(USER_TYPE + SIMPLE_USER)
                .withRoles(Collections.singleton(SIMPLE_GROUP))
                .build();
        Collection<AccessControlPolicy> rules = this.builder.toAccessControlPolicy(Collections.singleton(group), user);

        assertEquals(2, rules.size());

        String[] topics = new String[]{TOPIC_TEST_A, TOPIC_TEST_B};
        int i = 0;
        for (AccessControlPolicy rule : rules) {
            assertEquals(WILDCARD, rule.host());
            assertEquals(AclOperation.CREATE, rule.operation());
            assertEquals(USER_TYPE + SIMPLE_USER, rule.principal());
            assertEquals(SIMPLE_USER, rule.principalName());
            assertEquals(topics[i], rule.resourcePattern());
            assertEquals(ResourceType.TOPIC, rule.resourceType());
            assertEquals(PatternType.LITERAL, rule.patternType());
            i++;
        }
    }
}