/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.jikkou.extension.confluent.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.jikkou.extension.confluent.api.data.ListMetadata;
import io.jikkou.extension.confluent.api.data.RoleBindingData;
import io.jikkou.extension.confluent.api.data.RoleBindingListResponse;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ConfluentCloudApiClientTest {

    static final String TEST_CRN = "crn://confluent.cloud/organization=org-123";

    @Test
    void shouldListAllRoleBindingsWithPagination() {
        ConfluentCloudApi api = mock(ConfluentCloudApi.class);

        // First page returns data with a next page token
        RoleBindingData rb1 = new RoleBindingData("rb-1", "User:sa-1", "CloudClusterAdmin", TEST_CRN);
        when(api.listRoleBindings(eq(TEST_CRN), anyInt(), eq(null)))
            .thenReturn(new RoleBindingListResponse(
                new ListMetadata(2, "next-token"),
                List.of(rb1)
            ));

        // Second page returns data with no next page token
        RoleBindingData rb2 = new RoleBindingData("rb-2", "User:sa-2", "DeveloperRead", TEST_CRN);
        when(api.listRoleBindings(eq(TEST_CRN), anyInt(), eq("next-token")))
            .thenReturn(new RoleBindingListResponse(
                new ListMetadata(2, null),
                List.of(rb2)
            ));

        ConfluentCloudApiClient client = new ConfluentCloudApiClient(api, TEST_CRN);
        List<RoleBindingData> results = client.listRoleBindings();

        Assertions.assertEquals(2, results.size());
        Assertions.assertEquals("rb-1", results.get(0).id());
        Assertions.assertEquals("rb-2", results.get(1).id());
        verify(api, times(2)).listRoleBindings(eq(TEST_CRN), anyInt(), any());
    }

    @Test
    void shouldListRoleBindingsWithSinglePage() {
        ConfluentCloudApi api = mock(ConfluentCloudApi.class);

        RoleBindingData rb1 = new RoleBindingData("rb-1", "User:sa-1", "CloudClusterAdmin", TEST_CRN);
        when(api.listRoleBindings(eq(TEST_CRN), anyInt(), eq(null)))
            .thenReturn(new RoleBindingListResponse(
                new ListMetadata(1, null),
                List.of(rb1)
            ));

        ConfluentCloudApiClient client = new ConfluentCloudApiClient(api, TEST_CRN);
        List<RoleBindingData> results = client.listRoleBindings();

        Assertions.assertEquals(1, results.size());
        verify(api, times(1)).listRoleBindings(eq(TEST_CRN), anyInt(), any());
    }

    @Test
    void shouldDelegateCreateRoleBinding() {
        ConfluentCloudApi api = mock(ConfluentCloudApi.class);
        RoleBindingData input = new RoleBindingData("User:sa-1", "CloudClusterAdmin", TEST_CRN);
        RoleBindingData created = new RoleBindingData("rb-1", "User:sa-1", "CloudClusterAdmin", TEST_CRN);
        when(api.createRoleBinding(input)).thenReturn(created);

        ConfluentCloudApiClient client = new ConfluentCloudApiClient(api, TEST_CRN);
        RoleBindingData result = client.createRoleBinding(input);

        Assertions.assertEquals("rb-1", result.id());
        verify(api, times(1)).createRoleBinding(input);
    }

    @Test
    void shouldDelegateDeleteRoleBinding() {
        ConfluentCloudApi api = mock(ConfluentCloudApi.class);

        ConfluentCloudApiClient client = new ConfluentCloudApiClient(api, TEST_CRN);
        client.deleteRoleBinding("rb-1");

        verify(api, times(1)).deleteRoleBinding("rb-1");
    }
}
