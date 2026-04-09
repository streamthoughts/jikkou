/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.jikkou.extension.confluent.change;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.jikkou.core.models.change.GenericResourceChange;
import io.jikkou.core.models.change.ResourceChange;
import io.jikkou.core.models.change.ResourceChangeSpec;
import io.jikkou.core.models.change.StateChange;
import io.jikkou.core.reconciler.ChangeResponse;
import io.jikkou.core.reconciler.Operation;
import io.jikkou.extension.confluent.api.ConfluentCloudApiClient;
import io.jikkou.extension.confluent.api.data.RoleBindingData;
import io.jikkou.extension.confluent.models.V1RoleBinding;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class RoleBindingChangeHandlerTest {

    static final String TEST_PRINCIPAL = "User:sa-test";
    static final String TEST_ROLE = "CloudClusterAdmin";
    static final String TEST_CRN = "crn://confluent.cloud/organization=org-123";
    static final String TEST_ID = "rb-abc123";

    @Test
    void shouldCallCreateRoleBinding() {
        ConfluentCloudApiClient api = mock(ConfluentCloudApiClient.class);
        RoleBindingData data = new RoleBindingData(TEST_PRINCIPAL, TEST_ROLE, TEST_CRN);
        when(api.createRoleBinding(any())).thenReturn(
            new RoleBindingData(TEST_ID, TEST_PRINCIPAL, TEST_ROLE, TEST_CRN));

        RoleBindingChangeHandler.Create handler = new RoleBindingChangeHandler.Create(api);

        ResourceChange change = GenericResourceChange
            .builder(V1RoleBinding.class)
            .withSpec(ResourceChangeSpec.builder()
                .withOperation(Operation.CREATE)
                .withChange(StateChange.create("entry", data))
                .build())
            .build();

        List<ChangeResponse> responses = handler.handleChanges(List.of(change));

        Assertions.assertEquals(1, responses.size());
        // Wait for async completion
        responses.getFirst().getResults().join();
        verify(api, times(1)).createRoleBinding(any());
    }

    @Test
    void shouldCallDeleteRoleBinding() {
        ConfluentCloudApiClient api = mock(ConfluentCloudApiClient.class);
        RoleBindingData data = new RoleBindingData(TEST_ID, TEST_PRINCIPAL, TEST_ROLE, TEST_CRN);

        RoleBindingChangeHandler.Delete handler = new RoleBindingChangeHandler.Delete(api);

        ResourceChange change = GenericResourceChange
            .builder(V1RoleBinding.class)
            .withSpec(ResourceChangeSpec.builder()
                .withOperation(Operation.DELETE)
                .withChange(StateChange.delete("entry", data))
                .build())
            .build();

        List<ChangeResponse> responses = handler.handleChanges(List.of(change));

        Assertions.assertEquals(1, responses.size());
        responses.getFirst().getResults().join();
        verify(api, times(1)).deleteRoleBinding(TEST_ID);
    }
}
