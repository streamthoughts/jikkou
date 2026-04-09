/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.jikkou.kafka.change.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.jikkou.core.models.ObjectMeta;
import io.jikkou.core.models.change.ResourceChange;
import io.jikkou.core.reconciler.Operation;
import io.jikkou.kafka.model.user.V1KafkaUser;
import io.jikkou.kafka.model.user.V1KafkaUserAuthentication;
import io.jikkou.kafka.model.user.V1KafkaUserSpec;
import java.util.List;
import org.junit.jupiter.api.Test;

class UserChangeComputerTest {

    private final UserChangeComputer computer = new UserChangeComputer();
    private final UserChangeComputer.UserChangeFactory factory = new UserChangeComputer.UserChangeFactory();

    @Test
    void shouldComputeCreateChangeForNewUser() {
        // Given
        V1KafkaUser after = buildUser("testuser", List.of(
            new V1KafkaUserAuthentication.ScramSha512("password", 8192, null)
        ));

        // When
        List<ResourceChange> changes = computer.computeChanges(List.of(), List.of(after));

        // Then
        assertEquals(1, changes.size());
        ResourceChange change = changes.getFirst();
        assertEquals(Operation.CREATE, change.getSpec().getOp());
        assertEquals("testuser", change.getMetadata().getName());

        var stateChanges = change.getSpec().getChanges().all();
        assertEquals(1, stateChanges.size());
        assertEquals(Operation.CREATE, stateChanges.getFirst().getOp());
        assertNotNull(stateChanges.getFirst().getAfter());
    }

    @Test
    void shouldCreateDeleteChangeForScramSha512User() {
        // Given
        V1KafkaUser before = buildUser("testuser", List.of(
            new V1KafkaUserAuthentication.ScramSha512(null, 8192, null)
        ));

        // When
        ResourceChange change = factory.createChangeForDelete("testuser", before);

        // Then
        assertEquals(Operation.DELETE, change.getSpec().getOp());
        assertEquals("testuser", change.getMetadata().getName());

        var stateChanges = change.getSpec().getChanges().all();
        assertEquals(1, stateChanges.size());
        assertEquals(Operation.DELETE, stateChanges.getFirst().getOp());
        assertNotNull(stateChanges.getFirst().getBefore());

        V1KafkaUserAuthentication.ScramSha512 beforeAuth =
            (V1KafkaUserAuthentication.ScramSha512) stateChanges.getFirst().getBefore();
        assertEquals(8192, beforeAuth.iterations());
    }

    @Test
    void shouldCreateDeleteChangeForScramSha256User() {
        // Given
        V1KafkaUser before = buildUser("testuser", List.of(
            new V1KafkaUserAuthentication.ScramSha256(null, 4096, null)
        ));

        // When
        ResourceChange change = factory.createChangeForDelete("testuser", before);

        // Then
        assertEquals(Operation.DELETE, change.getSpec().getOp());

        var stateChanges = change.getSpec().getChanges().all();
        assertEquals(1, stateChanges.size());
        assertEquals(Operation.DELETE, stateChanges.getFirst().getOp());

        V1KafkaUserAuthentication.ScramSha256 beforeAuth =
            (V1KafkaUserAuthentication.ScramSha256) stateChanges.getFirst().getBefore();
        assertEquals(4096, beforeAuth.iterations());
    }

    @Test
    void shouldCreateDeleteChangeWithMultipleAuthentications() {
        // Given
        V1KafkaUser before = buildUser("testuser", List.of(
            new V1KafkaUserAuthentication.ScramSha512(null, 8192, null),
            new V1KafkaUserAuthentication.ScramSha256(null, 4096, null)
        ));

        // When
        ResourceChange change = factory.createChangeForDelete("testuser", before);

        // Then
        assertEquals(Operation.DELETE, change.getSpec().getOp());

        var stateChanges = change.getSpec().getChanges().all();
        assertEquals(2, stateChanges.size());
        stateChanges.forEach(sc -> assertEquals(Operation.DELETE, sc.getOp()));
    }

    @Test
    void shouldComputeUpdateChangeForExistingUser() {
        // Given
        V1KafkaUser before = buildUser("testuser", List.of(
            new V1KafkaUserAuthentication.ScramSha512(null, 8192, null)
        ));
        V1KafkaUser after = buildUser("testuser", List.of(
            new V1KafkaUserAuthentication.ScramSha256("newpassword", 4096, null)
        ));

        // When
        List<ResourceChange> changes = computer.computeChanges(List.of(before), List.of(after));

        // Then
        assertEquals(1, changes.size());
        assertEquals("testuser", changes.getFirst().getMetadata().getName());
    }

    @Test
    void shouldComputeNoChangeForIdenticalUser() {
        // Given
        V1KafkaUser before = buildUser("testuser", List.of(
            new V1KafkaUserAuthentication.ScramSha512(null, 8192, null)
        ));
        V1KafkaUser after = buildUser("testuser", List.of(
            new V1KafkaUserAuthentication.ScramSha512(null, 8192, null)
        ));

        // When
        List<ResourceChange> changes = computer.computeChanges(List.of(before), List.of(after));

        // Then
        assertEquals(1, changes.size());
        assertEquals(Operation.NONE, changes.getFirst().getSpec().getOp());
    }

    private static V1KafkaUser buildUser(String name, List<V1KafkaUserAuthentication> authentications) {
        return V1KafkaUser.builder()
            .withMetadata(new ObjectMeta(name))
            .withSpec(V1KafkaUserSpec.builder()
                .withAuthentications(authentications)
                .build())
            .build();
    }
}
