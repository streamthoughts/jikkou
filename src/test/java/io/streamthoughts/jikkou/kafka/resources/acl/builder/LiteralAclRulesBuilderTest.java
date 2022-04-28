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

import io.streamthoughts.jikkou.kafka.resources.acl.AccessControlPolicy;
import io.streamthoughts.jikkou.kafka.model.V1AccessOperationPolicy;
import io.streamthoughts.jikkou.kafka.model.V1AccessPermission;
import io.streamthoughts.jikkou.kafka.model.V1AccessResourceMatcher;
import io.streamthoughts.jikkou.kafka.model.V1AccessRoleObject;
import io.streamthoughts.jikkou.kafka.model.V1AccessUserObject;
import org.apache.kafka.common.acl.AclOperation;
import org.apache.kafka.common.resource.PatternType;
import org.apache.kafka.common.resource.ResourceType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LiteralAclRulesBuilderTest {

    static final List<V1AccessRoleObject> EMPTY_GROUP = Collections.emptyList();

    static final String TOPIC_TEST_A = "topic-test-a";
    static final String USER_TYPE = "User:";
    static final String SIMPLE_USER = "SimpleUser";
    static final String WILDCARD = "*";
    static final String TOPIC_WITH_WILDCARD = "topic-*";

    private LiteralAclRulesBuilder builder;

    @BeforeEach
    public void setUp() {
        this.builder = new LiteralAclRulesBuilder();
    }

    @Test
    public void shouldBuildAclRulesGivenUserWithLiteralPermissionAndNoGroup() {

        final V1AccessPermission permission = V1AccessPermission.newBuilder()
                .onResource(V1AccessResourceMatcher.newBuilder()
                        .withPattern(TOPIC_TEST_A)
                        .withPatternType(PatternType.LITERAL)
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

        assertEquals(1, rules.size());
        AccessControlPolicy rule = rules.iterator().next();

        assertEquals(WILDCARD, rule.host());
        assertEquals(AclOperation.CREATE, rule.operation());
        assertEquals(USER_TYPE + SIMPLE_USER, rule.principal());
        assertEquals(SIMPLE_USER, rule.principalName());
        assertEquals(TOPIC_TEST_A, rule.resourcePattern());
        assertEquals(ResourceType.TOPIC, rule.resourceType());
    }

    @Test
    public void shouldBuildAclRulesGivenUserWithLiteralAndWildcardPermissionAndNoGroup() {

        final V1AccessPermission permission = V1AccessPermission.newBuilder()
                .onResource(V1AccessResourceMatcher.newBuilder()
                        .withPattern(TOPIC_WITH_WILDCARD)
                        .withPatternType(PatternType.PREFIXED)
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

        assertEquals(1, rules.size());
        AccessControlPolicy rule = rules.iterator().next();

        assertEquals(WILDCARD, rule.host());
        assertEquals(AclOperation.CREATE, rule.operation());
        assertEquals(USER_TYPE + SIMPLE_USER, rule.principal());
        assertEquals(SIMPLE_USER, rule.principalName());
        assertEquals(TOPIC_WITH_WILDCARD, rule.resourcePattern());
        assertEquals(ResourceType.TOPIC, rule.resourceType());
    }

}