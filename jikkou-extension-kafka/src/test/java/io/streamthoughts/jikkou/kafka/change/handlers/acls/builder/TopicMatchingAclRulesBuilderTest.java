/*
 * Copyright 2022 The original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.streamthoughts.jikkou.kafka.change.handlers.acls.builder;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.streamthoughts.jikkou.api.model.ObjectMeta;
import io.streamthoughts.jikkou.kafka.model.KafkaAclBinding;
import io.streamthoughts.jikkou.kafka.models.V1KafkaPrincipalAcl;
import io.streamthoughts.jikkou.kafka.models.V1KafkaPrincipalAuthorization;
import io.streamthoughts.jikkou.kafka.models.V1KafkaPrincipalAuthorizationSpec;
import io.streamthoughts.jikkou.kafka.models.V1KafkaResourceMatcher;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import org.apache.kafka.clients.admin.TopicListing;
import org.apache.kafka.common.acl.AclOperation;
import org.apache.kafka.common.acl.AclPermissionType;
import org.apache.kafka.common.resource.PatternType;
import org.apache.kafka.common.resource.ResourceType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TopicMatchingAclRulesBuilderTest {

    static final String TOPIC_REGEX = "topic-.*";
    static final String TOPIC_TEST_A = "topic-test-a";
    static final String TOPIC_TEST_B = "topic-test-b";
    static final String USER_TYPE = "User:";
    static final String SIMPLE_USER = "SimpleUser";
    static final String WILDCARD = "*";

    private TopicMatchingAclRulesBuilder builder;

    private Supplier<Collection<TopicListing>> getSupplier() {
        return () -> Arrays.asList(
                new TopicListing(TOPIC_TEST_A, null,false),
                new TopicListing(TOPIC_TEST_B, null,false)
        );
    }

    @BeforeEach
    public void setUp() {
        this.builder = new TopicMatchingAclRulesBuilder();
        this.builder.setListTopics(CompletableFuture.supplyAsync(getSupplier()));
    }

    @Test
    void shouldBuildAclBindingsForTopicRegexAcl() {

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
                                        .withPattern(TOPIC_REGEX)
                                        .withPatternType(PatternType.MATCH)
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

        Collection<KafkaAclBinding> rules = this.builder.toKafkaAclBindings(resource);

        assertEquals(2, rules.size());

        String[] topics = new String[]{TOPIC_TEST_A, TOPIC_TEST_B};
        int i = 0;
        for (KafkaAclBinding rule : rules) {
            assertEquals(WILDCARD, rule.host());
            assertEquals(AclOperation.CREATE, rule.operation());
            assertEquals(USER_TYPE + SIMPLE_USER, rule.principal());
            assertEquals(topics[i], rule.resourcePattern());
            assertEquals(ResourceType.TOPIC, rule.resourceType());
            assertEquals(PatternType.LITERAL, rule.patternType());
            i++;
        }
    }
}