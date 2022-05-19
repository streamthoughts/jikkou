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
package io.streamthoughts.jikkou.api.change;

import io.streamthoughts.jikkou.api.resources.acl.AccessControlPolicy;
import org.apache.kafka.common.acl.AclOperation;
import org.apache.kafka.common.acl.AclPermissionType;
import org.apache.kafka.common.resource.PatternType;
import org.apache.kafka.common.resource.ResourceType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static io.streamthoughts.jikkou.api.change.Change.OperationType.ADD;
import static io.streamthoughts.jikkou.api.change.Change.OperationType.DELETE;
import static io.streamthoughts.jikkou.api.change.Change.OperationType.NONE;

public class AclChangeComputerTest {

    public static final AclChangeOptions DEFAULT_ACL_CHANGE_OPTIONS = new AclChangeOptions();

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
    public void should_return_add_changes_when_acl_not_exist() {
        // Given
        AccessControlPolicy policy = makeAclPolicy("???");

        List<AccessControlPolicy> actualState = Collections.emptyList();
        List<AccessControlPolicy> expectedState  = List.of(policy);

        // When
        var changes = new AclChangeComputer()
                .computeChanges(actualState, expectedState, DEFAULT_ACL_CHANGE_OPTIONS)
                .stream()
                .collect(Collectors.toMap(AclChange::getKey, it -> it));

        // Then
        AclChange change = changes.get(policy);
        Assertions.assertEquals(ADD, change.getOperation());
    }

    @Test
    public void should_return_delete_changes_given_delete_orphans_options_true() {
        // Given
        AccessControlPolicy actual = makeAclPolicy("???");

        List<AccessControlPolicy> actualState = List.of(actual);
        List<AccessControlPolicy> expectedState  = List.of();

        // When
        var changes = new AclChangeComputer()
                .computeChanges(actualState, expectedState, DEFAULT_ACL_CHANGE_OPTIONS.withDeleteOrphans(true))
                .stream()
                .collect(Collectors.toMap(AclChange::getKey, it -> it));

        // Then
        Assertions.assertEquals(DELETE, changes.get(actual).getOperation());
    }

    @Test
    public void should_not_return_delete_changes_given_delete_orphans_options_false() {
        // Given
        AccessControlPolicy actual = makeAclPolicy("???");

        List<AccessControlPolicy> actualState = List.of(actual);
        List<AccessControlPolicy> expectedState  = List.of();

        // When
        var changes = new AclChangeComputer()
                .computeChanges(actualState, expectedState, DEFAULT_ACL_CHANGE_OPTIONS.withDeleteOrphans(false))
                .stream()
                .collect(Collectors.toMap(AclChange::getKey, it -> it));

        // Then
        Assertions.assertTrue(changes.isEmpty());
    }

    @Test
    public void should_return_non_changes_given_identical_acl() {
        // Given
        List<AccessControlPolicy> actualState = List.of(makeAclPolicy("???"));
        List<AccessControlPolicy> expectedState  = List.of(makeAclPolicy("???"));

        // When
        var changes = new AclChangeComputer()
                .computeChanges(actualState, expectedState, DEFAULT_ACL_CHANGE_OPTIONS)
                .stream()
                .collect(Collectors.toMap(AclChange::getKey, it -> it));

        // Then
        AclChange change = changes.get(makeAclPolicy("???"));
        Assertions.assertEquals(NONE, change.getOperation());
    }
}