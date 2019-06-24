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
package io.streamthoughts.kafka.specs.acl.builder;

import io.streamthoughts.kafka.specs.acl.AclGroupPolicy;
import io.streamthoughts.kafka.specs.acl.AclOperationPolicy;
import io.streamthoughts.kafka.specs.acl.AclRule;
import io.streamthoughts.kafka.specs.acl.AclUserPolicy;
import io.streamthoughts.kafka.specs.acl.AclUserPolicyBuilder;
import org.apache.kafka.common.acl.AclOperation;
import org.apache.kafka.common.resource.PatternType;
import org.apache.kafka.common.resource.ResourceType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LiteralAclRulesBuilderTest {

    static final List<AclGroupPolicy> EMPTY_GROUP = Collections.emptyList();

    static final String TOPIC_TEST_A        = "topic-test-a";
    static final String USER_TYPE           = "User:";
    static final String SIMPLE_USER         = "SimpleUser";
    static final String WILDCARD            = "*";
    static final String TOPIC_WITH_WILDCARD = "topic-*";

    private LiteralAclRulesBuilder builder;

    @BeforeEach
    public void setUp() {
        this.builder = new LiteralAclRulesBuilder();
    }

    @Test
    public void shouldBuildAclRulesGivenUserWithLiteralPermissionAndNoGroup() {

        AclUserPolicy user = AclUserPolicyBuilder.newBuilder()
                .principal(USER_TYPE + SIMPLE_USER)
                .addPermission(TOPIC_TEST_A,
                        PatternType.LITERAL,
                        ResourceType.TOPIC,
                        Collections.singleton(new AclOperationPolicy(AclOperation.CREATE)))
                .build();
        Collection<AclRule> rules = this.builder.toAclRules(EMPTY_GROUP, user);

        assertEquals(1, rules.size());
        AclRule rule = rules.iterator().next();

        assertEquals(WILDCARD, rule.host());
        assertEquals(AclOperation.CREATE, rule.operation());
        assertEquals(USER_TYPE +  SIMPLE_USER, rule.principal());
        assertEquals(SIMPLE_USER, rule.principalName());
        assertEquals(TOPIC_TEST_A, rule.resourcePattern());
        assertEquals(ResourceType.TOPIC, rule.resourceType());
    }

    @Test
    public void shouldBuildAclRulesGivenUserWithLiteralAndWildcardPermissionAndNoGroup() {

        AclUserPolicy user = AclUserPolicyBuilder.newBuilder()
                .principal(USER_TYPE + SIMPLE_USER)
                .addPermission(TOPIC_WITH_WILDCARD,
                        PatternType.PREFIXED,
                        ResourceType.TOPIC,
                        Collections.singleton(new AclOperationPolicy(AclOperation.CREATE)))
                .build();
        Collection<AclRule> rules = this.builder.toAclRules(EMPTY_GROUP, user);

        assertEquals(1, rules.size());
        AclRule rule = rules.iterator().next();

        assertEquals(WILDCARD, rule.host());
        assertEquals(AclOperation.CREATE, rule.operation());
        assertEquals(USER_TYPE +  SIMPLE_USER, rule.principal());
        assertEquals(SIMPLE_USER, rule.principalName());
        assertEquals(TOPIC_WITH_WILDCARD, rule.resourcePattern());
        assertEquals(ResourceType.TOPIC, rule.resourceType());
    }

}