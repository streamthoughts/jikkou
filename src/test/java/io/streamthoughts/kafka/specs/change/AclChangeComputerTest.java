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
package io.streamthoughts.kafka.specs.change;

import io.streamthoughts.kafka.specs.resources.acl.AccessControlPolicy;
import org.apache.kafka.common.acl.AclOperation;
import org.apache.kafka.common.acl.AclPermissionType;
import org.apache.kafka.common.resource.PatternType;
import org.apache.kafka.common.resource.ResourceType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static io.streamthoughts.kafka.specs.change.Change.OperationType.ADD;
import static io.streamthoughts.kafka.specs.change.Change.OperationType.DELETE;
import static io.streamthoughts.kafka.specs.change.Change.OperationType.NONE;

public class AclChangeComputerTest {

    private static AccessControlPolicy makeAclPolicy(final String topicName) {
        return AccessControlPolicy.newBuilder()
                .withPrincipal("User:test")
                .withPatternType(PatternType.LITERAL)
                .withOperation(AclOperation.ALL)
                .withPermission(AclPermissionType.ALLOW)
                .withResourcePattern(topicName)
                .withResourceType(ResourceType.TOPIC)
                .withHost("*")
                .build();
    }

    @Test
    public void testReturnAddAclChange() {
        // Given
        AccessControlPolicy policy = makeAclPolicy("topic");

        List<AccessControlPolicy> actualState = Collections.emptyList();
        List<AccessControlPolicy> expectedState  = List.of(policy);

        // When
        Map<AccessControlPolicy, AclChange> changes = new AclChangeComputer()
                .computeChanges(actualState, expectedState, new AclChangeComputer.AclChangeOptions(false))
                .stream()
                .collect(Collectors.toMap(AclChange::getKey, it -> it));

        // Then
        AclChange change = changes.get(policy);
        Assertions.assertEquals(ADD, change.getOperation());
    }

    @Test
    public void testReturnDeleteAclChange() {
        // Given
        AccessControlPolicy policyA = makeAclPolicy("topicA");
        AccessControlPolicy policyB = makeAclPolicy("topicB");

        List<AccessControlPolicy> actualState = List.of(policyA);
        List<AccessControlPolicy> expectedState  = List.of(policyB);

        // When
        Map<AccessControlPolicy, AclChange> changes = new AclChangeComputer()
                .computeChanges(actualState, expectedState, new AclChangeComputer.AclChangeOptions(false))
                .stream()
                .collect(Collectors.toMap(AclChange::getKey, it -> it));

        // Then
        Assertions.assertEquals(DELETE, changes.get(policyA).getOperation());
    }

    @Test
    public void testReturnNoneAclChange() {
        // Given
        AccessControlPolicy policy = makeAclPolicy("topic");

        List<AccessControlPolicy> actualState = List.of(policy);
        List<AccessControlPolicy> expectedState  = List.of(policy);

        // When
        Map<AccessControlPolicy, AclChange> changes = new AclChangeComputer()
                .computeChanges(actualState, expectedState, new AclChangeComputer.AclChangeOptions(false))
                .stream()
                .collect(Collectors.toMap(AclChange::getKey, it -> it));

        // Then
        AclChange change = changes.get(policy);
        Assertions.assertEquals(NONE, change.getOperation());
    }

    @Test
    public void testReturnDeleteAclChangeGivenOrphanTrue() {
        // Given
        AccessControlPolicy policy = makeAclPolicy("topic");

        List<AccessControlPolicy> actualState = List.of(policy);
        List<AccessControlPolicy> expectedState  = List.of();

        // When
        Map<AccessControlPolicy, AclChange> changes = new AclChangeComputer()
                .computeChanges(actualState, expectedState, new AclChangeComputer.AclChangeOptions(true))
                .stream()
                .collect(Collectors.toMap(AclChange::getKey, it -> it));

        // Then
        AclChange change = changes.get(policy);
        Assertions.assertEquals(DELETE, change.getOperation());
    }

    @Test
    public void testReturnNoneAclChangeGivenOrphanFalse() {
        // Given
        AccessControlPolicy policy = makeAclPolicy("topic");

        List<AccessControlPolicy> actualState = List.of(policy);
        List<AccessControlPolicy> expectedState  = List.of();

        // When
        Map<AccessControlPolicy, AclChange> changes = new AclChangeComputer()
                .computeChanges(actualState, expectedState, new AclChangeComputer.AclChangeOptions(false))
                .stream()
                .collect(Collectors.toMap(AclChange::getKey, it -> it));

        // Then
        Assertions.assertTrue(changes.isEmpty());
    }
}