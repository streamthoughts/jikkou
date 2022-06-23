/*
 * Copyright 2022 StreamThoughts.
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

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.streamthoughts.jikkou.kafka.model.AccessControlPolicy;
import io.streamthoughts.jikkou.kafka.model.AccessOperationPolicy;
import io.streamthoughts.jikkou.kafka.models.V1KafkaAccessPermission;
import io.streamthoughts.jikkou.kafka.models.V1KafkaAccessResourceMatcher;
import io.streamthoughts.jikkou.kafka.models.V1KafkaAccessRoleObject;
import io.streamthoughts.jikkou.kafka.models.V1KafkaAccessUserObject;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.apache.kafka.common.acl.AclOperation;
import org.apache.kafka.common.resource.PatternType;
import org.apache.kafka.common.resource.ResourceType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class LiteralAclRulesBuilderTest {

    static final List<V1KafkaAccessRoleObject> EMPTY_GROUP = Collections.emptyList();

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

        var permission = V1KafkaAccessPermission.builder()
                    .withResource(V1KafkaAccessResourceMatcher.builder()
                        .withPattern(TOPIC_TEST_A)
                        .withPatternType(PatternType.LITERAL)
                        .withType(ResourceType.TOPIC)
                        .build()
                    )
                .withAllowOperations(List.of(
                        AccessOperationPolicy
                                .builder()
                                .withOperation(AclOperation.CREATE)
                                .withHost(WILDCARD)
                                .build()
                ))
                .build();

        var user = V1KafkaAccessUserObject.builder()
                .withPrincipal(USER_TYPE + SIMPLE_USER)
                .withPermissions(List.of(permission))
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

        var permission = V1KafkaAccessPermission.builder()
                .withResource(V1KafkaAccessResourceMatcher.builder()
                        .withPattern(TOPIC_WITH_WILDCARD)
                        .withPatternType(PatternType.PREFIXED)
                        .withType(ResourceType.TOPIC)
                        .build()
                )
                .withAllowOperations(List.of(
                        AccessOperationPolicy.builder()
                                .withOperation(AclOperation.CREATE)
                                .withHost(WILDCARD)
                                .build()
                        )
                )
                .build();

        var user = V1KafkaAccessUserObject.builder()
                .withPrincipal(USER_TYPE + SIMPLE_USER)
                .withPermissions(List.of(permission))
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