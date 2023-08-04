/*
 * Copyright 2021 The original authors
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
    public static final List<V1KafkaPrincipalAuthorization> EMPTY_STATE = Collections.emptyList();
    private final KafkaAclBindingBuilder kafkaAclRulesBuilder = new LiteralKafkaAclBindingBuilder();
    private final AclChangeComputer changeComputer = new AclChangeComputer(kafkaAclRulesBuilder);

    @Test
    void shouldReturnEmptyChangeForEmptyState() {
        // Given / When
        var changes = changeComputer
                .computeChanges(EMPTY_STATE, EMPTY_STATE)
                .stream()
                .collect(Collectors.toMap(AclChange::getChangeType, it -> it));

        // Then
        Assertions.assertTrue(changes.isEmpty());
    }

    @Test
    void shouldReturnEmptyChangeForEmptyExpectedState() {
        // Given
        List<V1KafkaPrincipalAuthorization> actualState = List.of(newKafkaPrincipalAuthorization(false));

        //  When
        var changes = changeComputer
                .computeChanges(actualState, EMPTY_STATE)
                .stream()
                .collect(Collectors.toMap(AclChange::getChangeType, it -> it));

        // Then
        Assertions.assertTrue(changes.isEmpty());
    }

    @Test
    void shouldReturnDeleteChangeForEmptyExpectedStateGivenDeleteOrphanTrue() {
        // Given
        List<V1KafkaPrincipalAuthorization> actualState = List.of(newKafkaPrincipalAuthorization(false));
        AclChangeComputer computer = new AclChangeComputer(kafkaAclRulesBuilder);
        computer.setDeleteAclBindingForOrphanPrincipal(true);

        //  When
        var changes =  computer
                .computeChanges(actualState, EMPTY_STATE)
                .stream()
                .collect(Collectors.toMap(AclChange::getChangeType, it -> it));

        // Then
        Assertions.assertEquals(1, changes.size());
        Assertions.assertNotNull(changes.get(DELETE));
    }


    @Test
    void shouldReturnAddChangeForEmptyActualState() {
        // Given
        var authorization = newKafkaPrincipalAuthorization(false);
        List<V1KafkaPrincipalAuthorization> expectedState = List.of(authorization);

        // When
        var changes = changeComputer
                .computeChanges(EMPTY_STATE, expectedState)
                .stream()
                .collect(Collectors.toMap(AclChange::getChangeType, it -> it));

        // Then
        Assertions.assertEquals(1, changes.size());
        Assertions.assertNotNull(changes.get(ADD));
    }

    @Test
    void shouldReturnDeleteChangesForExitingAuthorizationDeleteTrue() {
        // Given
        V1KafkaPrincipalAuthorization actual = newKafkaPrincipalAuthorization(false);
        V1KafkaPrincipalAuthorization expect = newKafkaPrincipalAuthorization(true);

        List<V1KafkaPrincipalAuthorization> actualState = List.of(actual);
        List<V1KafkaPrincipalAuthorization> expectedState = List.of(expect);

        // When
        var changes = changeComputer
                .computeChanges(actualState, expectedState)
                .stream()
                .collect(Collectors.toMap(AclChange::getChangeType, it -> it));

        // Then
        Assertions.assertEquals(1, changes.size());
        Assertions.assertNotNull(changes.get(DELETE));
    }

    @Test
    void shouldReturnEmptyChangeForNotExistingAuthorizationDeleteTrue() {
        // Given
        V1KafkaPrincipalAuthorization resource = newKafkaPrincipalAuthorization(true);

        List<V1KafkaPrincipalAuthorization> actual = List.of();
        List<V1KafkaPrincipalAuthorization> expect = List.of(resource);

        // When
        var changes = changeComputer.computeChanges(actual, expect);

        // Then
        Assertions.assertTrue(changes.isEmpty());
    }

    @Test
    void shouldReturnNoneChangeForAuthorizationEquals() {
        // Given
        V1KafkaPrincipalAuthorization resource = newKafkaPrincipalAuthorization(false);

        List<V1KafkaPrincipalAuthorization> actual = List.of(resource);
        List<V1KafkaPrincipalAuthorization> expect = List.of(resource);

        // When
        var changes = changeComputer
                .computeChanges(actual, expect)
                .stream()
                .collect(Collectors.toMap(AclChange::getChangeType, it -> it));

        // Then
        Assertions.assertEquals(1, changes.size());
        Assertions.assertNotNull(changes.get(NONE));
    }

    @Test
    void shouldReturnAddChangeForAuthorizationWithNewEntry() {
        // Given
        var aclTopicA = createTestKafkaPrincipalForTopic("topic-A");
        var aclTopicB = createTestKafkaPrincipalForTopic("topic-B");

        V1KafkaPrincipalAuthorization actual = newKafkaPrincipalAuthorization(false, List.of(aclTopicA));
        V1KafkaPrincipalAuthorization expect = newKafkaPrincipalAuthorization(false, List.of(aclTopicA, aclTopicB));

        List<V1KafkaPrincipalAuthorization> actualState = List.of(actual);
        List<V1KafkaPrincipalAuthorization> expectedState = List.of(expect);

        // When
        var changes = changeComputer
                .computeChanges(actualState, expectedState)
                .stream()
                .collect(Collectors.toMap(AclChange::getChangeType, it -> it));

        // Then
        Assertions.assertEquals(2, changes.size());
        AclChange addChange = changes.get(ADD);
        Assertions.assertNotNull(addChange);
        Assertions.assertEquals("topic-B", addChange.getAclBindings().getResourcePattern());

        AclChange noneChange = changes.get(NONE);
        Assertions.assertNotNull(noneChange);
        Assertions.assertEquals("topic-A", noneChange.getAclBindings().getResourcePattern());
    }

    @Test
    void shouldReturnDeleteChangeForAuthorizationWithOrphanEntry() {
        // Given
        var aclTopicA = createTestKafkaPrincipalForTopic("topic-A");
        var aclTopicB = createTestKafkaPrincipalForTopic("topic-B");

        V1KafkaPrincipalAuthorization actual = newKafkaPrincipalAuthorization(false, List.of(aclTopicA, aclTopicB));
        V1KafkaPrincipalAuthorization expect = newKafkaPrincipalAuthorization(false, List.of(aclTopicA));

        List<V1KafkaPrincipalAuthorization> actualState = List.of(actual);
        List<V1KafkaPrincipalAuthorization> expectedState = List.of(expect);

        // When
        var changes = changeComputer
                .computeChanges(actualState, expectedState)
                .stream()
                .collect(Collectors.toMap(AclChange::getChangeType, it -> it));

        // Then
        Assertions.assertEquals(2, changes.size());
        AclChange deleteChange = changes.get(DELETE);
        Assertions.assertNotNull(deleteChange);
        Assertions.assertEquals("topic-B", deleteChange.getAclBindings().getResourcePattern());

        AclChange noneChange = changes.get(NONE);
        Assertions.assertNotNull(noneChange);
        Assertions.assertEquals("topic-A", noneChange.getAclBindings().getResourcePattern());
    }

    private static V1KafkaPrincipalAuthorization newKafkaPrincipalAuthorization(boolean delete) {
        return newKafkaPrincipalAuthorization(delete, List.of(
                createTestKafkaPrincipalForTopic("???")
        ));
    }

    private static V1KafkaPrincipalAuthorization newKafkaPrincipalAuthorization(boolean delete,
                                                                                final List<V1KafkaPrincipalAcl> acls) {
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
                        .withAcls(acls)
                        .build()
                )
                .build();
    }

    private static V1KafkaPrincipalAcl createTestKafkaPrincipalForTopic(final String pattern) {
        return V1KafkaPrincipalAcl
                .builder()
                .withType(AclPermissionType.ALLOW)
                .withOperation(AclOperation.ALL)
                .withResource(V1KafkaResourceMatcher
                        .builder()
                        .withPatternType(PatternType.LITERAL)
                        .withType(ResourceType.TOPIC)
                        .withPattern(pattern)
                        .build()
                )
                .withHost("*")
                .build();
    }
}