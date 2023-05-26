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
package io.streamthoughts.jikkou.kafka.control.handlers.acls.builder;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.streamthoughts.jikkou.api.model.ObjectMeta;
import io.streamthoughts.jikkou.kafka.model.KafkaAclBinding;
import io.streamthoughts.jikkou.kafka.models.V1KafkaPrincipalAcl;
import io.streamthoughts.jikkou.kafka.models.V1KafkaPrincipalAuthorization;
import io.streamthoughts.jikkou.kafka.models.V1KafkaPrincipalAuthorizationSpec;
import io.streamthoughts.jikkou.kafka.models.V1KafkaResourceMatcher;
import java.util.Collection;
import org.apache.kafka.common.acl.AclOperation;
import org.apache.kafka.common.acl.AclPermissionType;
import org.apache.kafka.common.resource.PatternType;
import org.apache.kafka.common.resource.ResourceType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class LiteralKafkaAclBindingBuilderTest {

    static final String TOPIC_TEST_A = "topic-test-a";
    static final String USER_TYPE = "User:";
    static final String SIMPLE_USER = "SimpleUser";
    static final String WILDCARD = "*";
    static final String TOPIC_WITH_WILDCARD = "topic-*";

    private LiteralKafkaAclBindingBuilder builder;

    @BeforeEach
    public void setUp() {
        this.builder = new LiteralKafkaAclBindingBuilder();
    }

    @Test
    void shouldBuildAclRulesGivenUserWithLiteralPermissionAndNoGroup() {

        var resource = new V1KafkaPrincipalAuthorization()
                .toBuilder()
                .withMetadata(ObjectMeta
                        .builder()
                        .withName(USER_TYPE + SIMPLE_USER)
                        .build()
                )
                .withSpec(V1KafkaPrincipalAuthorizationSpec
                        .builder()
                        .withAcl(V1KafkaPrincipalAcl
                                .builder()
                                .withResource(V1KafkaResourceMatcher
                                        .builder()
                                        .withPattern(TOPIC_TEST_A)
                                        .withPatternType(PatternType.LITERAL)
                                        .withType(ResourceType.TOPIC)
                                        .build())
                                .withType(AclPermissionType.ALLOW)
                                .withOperation(AclOperation.CREATE)
                                .withHost(WILDCARD)
                                .build()
                        )
                        .build()
                )
                .build();

        Collection<KafkaAclBinding> rules = builder.toKafkaAclBindings(resource);

        assertEquals(1, rules.size());
        KafkaAclBinding rule = rules.iterator().next();

        assertEquals(WILDCARD, rule.getHost());
        assertEquals(AclOperation.CREATE, rule.getOperation());
        assertEquals(USER_TYPE + SIMPLE_USER, rule.getPrincipal());
        assertEquals(TOPIC_TEST_A, rule.getResourcePattern());
        assertEquals(ResourceType.TOPIC, rule.getResourceType());
    }

    @Test
    void shouldBuildAclRulesGivenUserWithLiteralAndWildcardPermissionAndNoGroup() {

        var resource = new V1KafkaPrincipalAuthorization()
                .toBuilder()
                .withMetadata(ObjectMeta
                        .builder()
                        .withName(USER_TYPE + SIMPLE_USER)
                        .build()
                )
                .withSpec(V1KafkaPrincipalAuthorizationSpec
                        .builder()
                        .withAcl(V1KafkaPrincipalAcl
                                .builder()
                                .withResource(V1KafkaResourceMatcher
                                        .builder()
                                        .withPattern(TOPIC_WITH_WILDCARD)
                                        .withPatternType(PatternType.PREFIXED)
                                        .withType(ResourceType.TOPIC)
                                        .build())
                                .withType(AclPermissionType.ALLOW)
                                .withOperation(AclOperation.CREATE)
                                .withHost(WILDCARD)
                                .build()
                        )
                        .build()
                )
                .build();

        Collection<KafkaAclBinding> rules = builder.toKafkaAclBindings(resource);

        assertEquals(1, rules.size());
        KafkaAclBinding rule = rules.iterator().next();

        assertEquals(WILDCARD, rule.getHost());
        assertEquals(AclOperation.CREATE, rule.getOperation());
        assertEquals(USER_TYPE + SIMPLE_USER, rule.getPrincipal());
        assertEquals(TOPIC_WITH_WILDCARD, rule.getResourcePattern());
        assertEquals(ResourceType.TOPIC, rule.getResourceType());
    }

}