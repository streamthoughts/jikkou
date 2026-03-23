/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.kafka.change.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.streamthoughts.jikkou.core.models.ObjectMeta;
import io.streamthoughts.jikkou.core.models.change.GenericResourceChange;
import io.streamthoughts.jikkou.core.models.change.ResourceChange;
import io.streamthoughts.jikkou.core.models.change.ResourceChangeSpec;
import io.streamthoughts.jikkou.core.models.change.StateChange;
import io.streamthoughts.jikkou.core.reconciler.ChangeResponse;
import io.streamthoughts.jikkou.core.reconciler.Operation;
import io.streamthoughts.jikkou.kafka.model.user.V1KafkaUser;
import io.streamthoughts.jikkou.kafka.model.user.V1KafkaUserAuthentication;
import java.util.List;
import java.util.Map;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AlterUserScramCredentialsResult;
import org.apache.kafka.clients.admin.ScramMechanism;
import org.apache.kafka.clients.admin.UserScramCredentialAlteration;
import org.apache.kafka.clients.admin.UserScramCredentialDeletion;
import org.apache.kafka.clients.admin.UserScramCredentialUpsertion;
import org.apache.kafka.common.KafkaFuture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class UserChangeHandlerTest {

    private AdminClient adminClient;
    private UserChangeHandler handler;

    @BeforeEach
    void setUp() {
        adminClient = mock(AdminClient.class);
        handler = new UserChangeHandler(adminClient);
    }

    @Test
    void shouldSendUpsertionForCreateScramSha512() {
        // Given
        ResourceChange change = createUserChange(
            "testuser",
            Operation.CREATE,
            StateChange.create(
                "authentications.scram-sha-512",
                new V1KafkaUserAuthentication.ScramSha512("password123", 8192, null)
            )
        );

        mockAlterUserScramCredentials("testuser");

        // When
        List<ChangeResponse> responses = handler.handleChanges(List.of(change));

        // Then
        var captor = ArgumentCaptor.forClass(List.class);
        verify(adminClient).alterUserScramCredentials(captor.capture());

        List<UserScramCredentialAlteration> alterations = captor.getValue();
        assertEquals(1, alterations.size());
        assertInstanceOf(UserScramCredentialUpsertion.class, alterations.getFirst());

        UserScramCredentialUpsertion upsertion = (UserScramCredentialUpsertion) alterations.getFirst();
        assertEquals("testuser", upsertion.user());
        assertEquals(ScramMechanism.SCRAM_SHA_512, upsertion.credentialInfo().mechanism());
    }

    @Test
    void shouldSendUpsertionForCreateScramSha256() {
        // Given
        ResourceChange change = createUserChange(
            "testuser",
            Operation.CREATE,
            StateChange.create(
                "authentications.scram-sha-256",
                new V1KafkaUserAuthentication.ScramSha256("password123", 8192, null)
            )
        );

        mockAlterUserScramCredentials("testuser");

        // When
        List<ChangeResponse> responses = handler.handleChanges(List.of(change));

        // Then
        var captor = ArgumentCaptor.forClass(List.class);
        verify(adminClient).alterUserScramCredentials(captor.capture());

        List<UserScramCredentialAlteration> alterations = captor.getValue();
        assertEquals(1, alterations.size());
        assertInstanceOf(UserScramCredentialUpsertion.class, alterations.getFirst());

        UserScramCredentialUpsertion upsertion = (UserScramCredentialUpsertion) alterations.getFirst();
        assertEquals("testuser", upsertion.user());
        assertEquals(ScramMechanism.SCRAM_SHA_256, upsertion.credentialInfo().mechanism());
    }

    @Test
    void shouldSendDeletionForDeleteScramSha512() {
        // Given
        ResourceChange change = createUserChange(
            "testuser",
            Operation.DELETE,
            StateChange.delete(
                "authentications.scram-sha-512",
                new V1KafkaUserAuthentication.ScramSha512(null, 8192, null)
            )
        );

        mockAlterUserScramCredentials("testuser");

        // When
        List<ChangeResponse> responses = handler.handleChanges(List.of(change));

        // Then
        var captor = ArgumentCaptor.forClass(List.class);
        verify(adminClient).alterUserScramCredentials(captor.capture());

        List<UserScramCredentialAlteration> alterations = captor.getValue();
        assertEquals(1, alterations.size());
        assertInstanceOf(
            UserScramCredentialDeletion.class,
            alterations.getFirst(),
            "DELETE operation should produce a UserScramCredentialDeletion, not an upsertion"
        );

        UserScramCredentialDeletion deletion = (UserScramCredentialDeletion) alterations.getFirst();
        assertEquals("testuser", deletion.user());
        assertEquals(ScramMechanism.SCRAM_SHA_512, deletion.mechanism());
    }

    @Test
    void shouldSendDeletionForDeleteScramSha256() {
        // Given
        ResourceChange change = createUserChange(
            "testuser",
            Operation.DELETE,
            StateChange.delete(
                "authentications.scram-sha-256",
                new V1KafkaUserAuthentication.ScramSha256(null, 8192, null)
            )
        );

        mockAlterUserScramCredentials("testuser");

        // When
        List<ChangeResponse> responses = handler.handleChanges(List.of(change));

        // Then
        var captor = ArgumentCaptor.forClass(List.class);
        verify(adminClient).alterUserScramCredentials(captor.capture());

        List<UserScramCredentialAlteration> alterations = captor.getValue();
        assertEquals(1, alterations.size());
        assertInstanceOf(
            UserScramCredentialDeletion.class,
            alterations.getFirst(),
            "DELETE operation should produce a UserScramCredentialDeletion, not an upsertion"
        );

        UserScramCredentialDeletion deletion = (UserScramCredentialDeletion) alterations.getFirst();
        assertEquals("testuser", deletion.user());
        assertEquals(ScramMechanism.SCRAM_SHA_256, deletion.mechanism());
    }

    @Test
    void shouldSendUpsertionForUpdateScramSha512() {
        // Given
        ResourceChange change = createUserChange(
            "testuser",
            Operation.UPDATE,
            StateChange.update(
                "authentications.scram-sha-512",
                new V1KafkaUserAuthentication.ScramSha512(null, 8192, null),
                new V1KafkaUserAuthentication.ScramSha512("newpassword", 8192, null)
            )
        );

        mockAlterUserScramCredentials("testuser");

        // When
        List<ChangeResponse> responses = handler.handleChanges(List.of(change));

        // Then
        var captor = ArgumentCaptor.forClass(List.class);
        verify(adminClient).alterUserScramCredentials(captor.capture());

        List<UserScramCredentialAlteration> alterations = captor.getValue();
        assertEquals(1, alterations.size());
        assertInstanceOf(UserScramCredentialUpsertion.class, alterations.getFirst());
    }

    @Test
    void shouldReturnChangeResponsesForEachChange() {
        // Given
        ResourceChange change = createUserChange(
            "testuser",
            Operation.CREATE,
            StateChange.create(
                "authentications.scram-sha-512",
                new V1KafkaUserAuthentication.ScramSha512("password123", 8192, null)
            )
        );

        mockAlterUserScramCredentials("testuser");

        // When
        List<ChangeResponse> responses = handler.handleChanges(List.of(change));

        // Then
        assertEquals(1, responses.size());
        assertTrue(responses.getFirst().getChange() == change);
    }

    private ResourceChange createUserChange(String userName, Operation operation, StateChange stateChange) {
        return GenericResourceChange
            .builder(V1KafkaUser.class)
            .withMetadata(new ObjectMeta(userName))
            .withSpec(ResourceChangeSpec
                .builder()
                .withOperation(operation)
                .withChange(stateChange)
                .build()
            )
            .build();
    }

    @SuppressWarnings("unchecked")
    private void mockAlterUserScramCredentials(String userName) {
        AlterUserScramCredentialsResult result = mock(AlterUserScramCredentialsResult.class);
        KafkaFuture<Void> future = KafkaFuture.completedFuture(null);
        when(result.values()).thenReturn(Map.of(userName, future));
        when(adminClient.alterUserScramCredentials(org.mockito.ArgumentMatchers.anyList())).thenReturn(result);
    }
}
