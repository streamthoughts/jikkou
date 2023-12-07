/*
 * Copyright 2023 The original authors
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
package io.streamthoughts.jikkou.kafka.change.acl;

import static io.streamthoughts.jikkou.core.reconciler.Operation.CREATE;
import static io.streamthoughts.jikkou.core.reconciler.Operation.DELETE;
import static io.streamthoughts.jikkou.core.reconciler.Operation.NONE;
import static io.streamthoughts.jikkou.core.reconciler.Operation.UPDATE;

import io.streamthoughts.jikkou.core.models.CoreAnnotations;
import io.streamthoughts.jikkou.core.models.ObjectMeta;
import io.streamthoughts.jikkou.core.models.change.GenericResourceChange;
import io.streamthoughts.jikkou.core.models.change.ResourceChange;
import io.streamthoughts.jikkou.core.models.change.ResourceChangeSpec;
import io.streamthoughts.jikkou.core.models.change.StateChange;
import io.streamthoughts.jikkou.kafka.change.acl.builder.LiteralKafkaAclBindingBuilder;
import io.streamthoughts.jikkou.kafka.model.KafkaAclBinding;
import io.streamthoughts.jikkou.kafka.models.V1KafkaPrincipalAcl;
import io.streamthoughts.jikkou.kafka.models.V1KafkaPrincipalAuthorization;
import io.streamthoughts.jikkou.kafka.models.V1KafkaPrincipalAuthorizationSpec;
import io.streamthoughts.jikkou.kafka.models.V1KafkaResourceMatcher;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.kafka.common.acl.AclOperation;
import org.apache.kafka.common.acl.AclPermissionType;
import org.apache.kafka.common.resource.PatternType;
import org.apache.kafka.common.resource.ResourceType;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class AclChangeComputerTest {

    static final String TEST_PRINCIPAL = "User:test";
    static final String TEST_TOPIC_A = "TOPIC_A";
    static final String TEST_TOPIC_B = "TOPIC_B";
    static final String ANY = "???";
    static final List<V1KafkaPrincipalAuthorization> EMPTY_STATE = Collections.emptyList();
    final KafkaAclBindingBuilder kafkaAclRulesBuilder = new LiteralKafkaAclBindingBuilder();
    final AclChangeComputer changeComputer = new AclChangeComputer(false, kafkaAclRulesBuilder);

    @Test
    void shouldReturnEmptyChangeForEmptyState() {
        // Given / When
        var changes = changeComputer
                .computeChanges(EMPTY_STATE, EMPTY_STATE)
                .stream()
                .collect(Collectors.toMap(it -> it.getSpec().getOp(), Function.identity()));

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
                .collect(Collectors.toMap(it -> it.getSpec().getOp(), Function.identity()));

        // Then
        Assertions.assertTrue(changes.isEmpty());
    }

    @Test
    void shouldReturnDeleteChangeForEmptyExpectedStateGivenDeleteOrphanTrue() {
        // Given
        V1KafkaPrincipalAuthorization resource = newKafkaPrincipalAuthorization(false);
        AclChangeComputer computer = new AclChangeComputer(true, kafkaAclRulesBuilder);

        //  When
        List<ResourceChange> changes = computer.computeChanges(List.of(resource), EMPTY_STATE);

        // Then
        List<ResourceChange> expected = List.of(
                GenericResourceChange.builder(V1KafkaPrincipalAuthorization.class)
                        .withMetadata(resource.getMetadata())
                        .withSpec(ResourceChangeSpec
                                .builder()
                                .withOperation(DELETE)
                                .withChange(StateChange.delete("acl", getAclBindingForPattern(ANY)))
                                .build()
                        ).build());
        Assertions.assertEquals(expected, changes);
    }


    @Test
    void shouldReturnCreateChangeForNewResource() {
        // Given
        var resource = newKafkaPrincipalAuthorization(false);
        List<V1KafkaPrincipalAuthorization> expectedState = List.of(resource);

        // When
        List<ResourceChange> changes = changeComputer.computeChanges(EMPTY_STATE, expectedState);

        // Then
        List<ResourceChange> expected = List.of(
                GenericResourceChange.builder(V1KafkaPrincipalAuthorization.class)
                        .withMetadata(resource.getMetadata())
                        .withSpec(ResourceChangeSpec
                                .builder()
                                .withOperation(CREATE)
                                .withChange(StateChange.create("acl", getAclBindingForPattern(ANY)))
                                .build()
                        ).build());
        Assertions.assertEquals(expected, changes);
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
                .collect(Collectors.toMap(it -> it.getSpec().getOp(), Function.identity()));

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
        List<ResourceChange> changes = changeComputer.computeChanges(actual, expect);

        // Then
        List<ResourceChange> expected = List.of(
                GenericResourceChange.builder(V1KafkaPrincipalAuthorization.class)
                        .withMetadata(resource.getMetadata())
                        .withSpec(ResourceChangeSpec
                                .builder()
                                .withOperation(NONE)
                                .withChange(StateChange.none("acl", getAclBindingForPattern(ANY)))
                                .build()
                        ).build());
        Assertions.assertEquals(expected, changes);
    }

    @Test
    void shouldReturnAddChangeForAuthorizationWithNewEntry() {
        // Given
        var aclTopicA = createTestKafkaPrincipalForTopic(TEST_TOPIC_A);
        var aclTopicB = createTestKafkaPrincipalForTopic(TEST_TOPIC_B);

        V1KafkaPrincipalAuthorization actual = newKafkaPrincipalAuthorization(false, List.of(aclTopicA));
        V1KafkaPrincipalAuthorization expect = newKafkaPrincipalAuthorization(false, List.of(aclTopicA, aclTopicB));

        List<V1KafkaPrincipalAuthorization> actualState = List.of(actual);
        List<V1KafkaPrincipalAuthorization> expectedState = List.of(expect);

        // When
        List<ResourceChange> changes = changeComputer.computeChanges(actualState, expectedState);

        // Then
        List<ResourceChange> expected = List.of(
                GenericResourceChange.builder(V1KafkaPrincipalAuthorization.class)
                        .withMetadata(expect.getMetadata())
                        .withSpec(ResourceChangeSpec
                                .builder()
                                .withOperation(UPDATE)
                                .withChange(StateChange.none("acl", getAclBindingForPattern(TEST_TOPIC_A)))
                                .withChange(StateChange.create("acl", getAclBindingForPattern(TEST_TOPIC_B)))
                                .build()
                        ).build());
        Assertions.assertEquals(expected, changes);
    }

    @NotNull
    private static KafkaAclBinding getAclBindingForPattern(String pattern) {
        return new KafkaAclBinding(
                "User:test",
                pattern,
                PatternType.LITERAL,
                ResourceType.TOPIC,
                AclOperation.ALL,
                AclPermissionType.ALLOW,
                "*"
        );
    }

    @Test
    void shouldReturnDeleteChangeForAuthorizationWithOrphanEntry() {
        // Given
        var aclTopicA = createTestKafkaPrincipalForTopic(TEST_TOPIC_A);
        var aclTopicB = createTestKafkaPrincipalForTopic(TEST_TOPIC_B);

        V1KafkaPrincipalAuthorization actual = newKafkaPrincipalAuthorization(false, List.of(aclTopicA, aclTopicB));
        V1KafkaPrincipalAuthorization expect = newKafkaPrincipalAuthorization(false, List.of(aclTopicA));

        List<V1KafkaPrincipalAuthorization> actualState = List.of(actual);
        List<V1KafkaPrincipalAuthorization> expectedState = List.of(expect);

        // When
        List<ResourceChange> changes = changeComputer.computeChanges(actualState, expectedState);

        // Then
        List<ResourceChange> expected = List.of(
                GenericResourceChange.builder(V1KafkaPrincipalAuthorization.class)
                        .withMetadata(actual.getMetadata())
                        .withSpec(ResourceChangeSpec
                                .builder()
                                .withOperation(UPDATE)
                                .withChange(StateChange.none("acl", getAclBindingForPattern(TEST_TOPIC_A)))
                                .withChange(StateChange.delete("acl", getAclBindingForPattern(TEST_TOPIC_B)))
                                .build()
                        ).build());
        Assertions.assertEquals(expected, changes);
    }

    private static V1KafkaPrincipalAuthorization newKafkaPrincipalAuthorization(boolean delete) {
        return newKafkaPrincipalAuthorization(delete, List.of(
                createTestKafkaPrincipalForTopic(ANY)
        ));
    }

    private static V1KafkaPrincipalAuthorization newKafkaPrincipalAuthorization(boolean delete,
                                                                                final List<V1KafkaPrincipalAcl> acls) {
        return V1KafkaPrincipalAuthorization
                .builder()
                .withMetadata(ObjectMeta
                        .builder()
                        .withName(TEST_PRINCIPAL)
                        .withAnnotation(CoreAnnotations.JIKKOU_IO_DELETE, delete)
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