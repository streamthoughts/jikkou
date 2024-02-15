/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.kafka.change.acl.builder;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.streamthoughts.jikkou.core.models.ObjectMeta;
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

        assertEquals(WILDCARD, rule.host());
        assertEquals(AclOperation.CREATE, rule.operation());
        assertEquals(USER_TYPE + SIMPLE_USER, rule.principal());
        assertEquals(TOPIC_TEST_A, rule.resourcePattern());
        assertEquals(ResourceType.TOPIC, rule.resourceType());
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

        assertEquals(WILDCARD, rule.host());
        assertEquals(AclOperation.CREATE, rule.operation());
        assertEquals(USER_TYPE + SIMPLE_USER, rule.principal());
        assertEquals(TOPIC_WITH_WILDCARD, rule.resourcePattern());
        assertEquals(ResourceType.TOPIC, rule.resourceType());
    }

}