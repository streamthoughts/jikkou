/*
 * Copyright 2021 StreamThoughts.
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
package io.streamthoughts.jikkou.kafka.control.change;

import static io.streamthoughts.jikkou.api.control.ChangeType.ADD;
import static io.streamthoughts.jikkou.api.control.ChangeType.DELETE;
import static io.streamthoughts.jikkou.api.control.ChangeType.NONE;

import io.streamthoughts.jikkou.JikkouMetadataAnnotations;
import io.streamthoughts.jikkou.api.model.ObjectMeta;
import io.streamthoughts.jikkou.kafka.control.handlers.acls.KafkaAclBindingBuilder;
import io.streamthoughts.jikkou.kafka.control.handlers.acls.builder.LiteralKafkaAclBindingBuilder;
import io.streamthoughts.jikkou.kafka.models.V1KafkaPrincipalAcl;
import io.streamthoughts.jikkou.kafka.models.V1KafkaPrincipalAuthorization;
import io.streamthoughts.jikkou.kafka.models.V1KafkaPrincipalAuthorizationSpec;
import io.streamthoughts.jikkou.kafka.models.V1KafkaResourceMatcher;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.kafka.common.acl.AclOperation;
import org.apache.kafka.common.acl.AclPermissionType;
import org.apache.kafka.common.resource.PatternType;
import org.apache.kafka.common.resource.ResourceType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class AclChangeComputerTest {

    public static final String TEST_PRINCIPAL = "User:test";
    private final KafkaAclBindingBuilder kafkaAclRulesBuilder = new LiteralKafkaAclBindingBuilder();
    private final AclChangeComputer changeComputer = new AclChangeComputer(kafkaAclRulesBuilder);


    private static V1KafkaPrincipalAuthorization newKafkaPrincipalAuthorization(boolean delete) {
        return V1KafkaPrincipalAuthorization
                .builder()
                .withMetadata(ObjectMeta
                        .builder()
                        .withName(TEST_PRINCIPAL)
                        .withAnnotation(JikkouMetadataAnnotations.JIKKOU_IO_DELETE, delete)
                        .build()
                )
                .withSpec(V1KafkaPrincipalAuthorizationSpec
                        .builder()
                        .withAcl(V1KafkaPrincipalAcl
                                .builder()
                                .withType(AclPermissionType.ALLOW)
                                .withOperation(AclOperation.ALL)
                                .withResource(V1KafkaResourceMatcher
                                        .builder()
                                        .withPatternType(PatternType.LITERAL)
                                        .withType(ResourceType.TOPIC)
                                        .withPattern("topic-test")
                                        .build()
                                )
                                .withHost("*")
                                .build()
                        )
                        .build()
                )
                .build();
    }

    @Test
    void shouldReturnAddChangesForNewAuthorization() {
        // Given
        var authorization = newKafkaPrincipalAuthorization(false);

        List<V1KafkaPrincipalAuthorization> actualState = Collections.emptyList();

        List<V1KafkaPrincipalAuthorization> expectedState = List.of(authorization);

        // When
        var changes = changeComputer
                .computeChanges(actualState, expectedState)
                .stream()
                .collect(Collectors.toMap(AclChange::getAclBindings, it -> it));

        // Then
        AclChange change = changes.entrySet().iterator().next().getValue();
        Assertions.assertEquals(ADD, change.getChangeType());
    }

    @Test
    void shouldReturnDeleteChangesForDeleteAuthorization() {
        // Given
        V1KafkaPrincipalAuthorization actual = newKafkaPrincipalAuthorization(false);
        V1KafkaPrincipalAuthorization expect = newKafkaPrincipalAuthorization(true);

        List<V1KafkaPrincipalAuthorization> actualState = List.of(actual);
        List<V1KafkaPrincipalAuthorization> expectedState = List.of(expect);

        // When
        var changes = changeComputer
                .computeChanges(actualState, expectedState)
                .stream()
                .collect(Collectors.toMap(AclChange::getAclBindings, it -> it));

        // Then
        AclChange change = changes.entrySet().iterator().next().getValue();
        Assertions.assertEquals(DELETE, change.getChangeType());
    }

    @Test
    void shouldReturnNoneChangeForExistingAuthorization() {
        // Given
        V1KafkaPrincipalAuthorization resource = newKafkaPrincipalAuthorization(false);

        List<V1KafkaPrincipalAuthorization> actual = List.of(resource);
        List<V1KafkaPrincipalAuthorization> expect = List.of(resource);

        // When
        var changes = changeComputer
                .computeChanges(actual, expect)
                .stream()
                .collect(Collectors.toMap(AclChange::getAclBindings, it -> it));

        // Then
        AclChange change = changes.entrySet().iterator().next().getValue();
        Assertions.assertEquals(NONE, change.getChangeType());
    }
}