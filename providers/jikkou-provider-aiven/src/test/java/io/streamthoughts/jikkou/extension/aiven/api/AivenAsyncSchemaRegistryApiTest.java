/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.extension.aiven.api;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.streamthoughts.jikkou.extension.aiven.api.data.MessageErrorsResponse;
import java.util.Collections;
import org.junit.jupiter.api.Test;

class AivenAsyncSchemaRegistryApiTest {

    private static final String TEST_SUBJECT = "test-subject";

    @Test
    void shouldCallDeleteWhenPermanentIsFalse() {
        // Given
        AivenApiClient apiClient = mock(AivenApiClient.class);
        when(apiClient.deleteSchemaRegistrySubject(eq(TEST_SUBJECT)))
                .thenReturn(new MessageErrorsResponse("", Collections.emptyList()));

        AivenAsyncSchemaRegistryApi api = new AivenAsyncSchemaRegistryApi(apiClient);

        // When
        api.deleteSubjectVersions(TEST_SUBJECT, false).block();

        // Then
        verify(apiClient, times(1)).deleteSchemaRegistrySubject(TEST_SUBJECT);
    }

    @Test
    void shouldCallDeleteWhenPermanentIsTrue() {
        // Given
        AivenApiClient apiClient = mock(AivenApiClient.class);
        when(apiClient.deleteSchemaRegistrySubject(eq(TEST_SUBJECT)))
                .thenReturn(new MessageErrorsResponse("", Collections.emptyList()));

        AivenAsyncSchemaRegistryApi api = new AivenAsyncSchemaRegistryApi(apiClient);

        // When
        api.deleteSubjectVersions(TEST_SUBJECT, true).block();

        // Then
        verify(apiClient, times(1)).deleteSchemaRegistrySubject(TEST_SUBJECT);
    }
}