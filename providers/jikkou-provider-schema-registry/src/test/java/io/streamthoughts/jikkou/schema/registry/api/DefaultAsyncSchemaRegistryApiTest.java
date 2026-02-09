/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.schema.registry.api;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.Test;

class DefaultAsyncSchemaRegistryApiTest {

    private static final String TEST_SUBJECT = "test-subject";

    @Test
    void shouldDelegateSoftDeleteToApi() {
        // Given
        SchemaRegistryApi schemaRegistryApi = mock(SchemaRegistryApi.class);
        when(schemaRegistryApi.deleteSubjectVersions(eq(TEST_SUBJECT), eq(false)))
                .thenReturn(List.of(1, 2));

        DefaultAsyncSchemaRegistryApi asyncApi = new DefaultAsyncSchemaRegistryApi(schemaRegistryApi);

        // When
        asyncApi.deleteSubjectVersions(TEST_SUBJECT, false).block();

        // Then
        verify(schemaRegistryApi).deleteSubjectVersions(TEST_SUBJECT, false);
    }

    @Test
    void shouldDelegateHardDeleteToApi() {
        // Given
        SchemaRegistryApi schemaRegistryApi = mock(SchemaRegistryApi.class);
        when(schemaRegistryApi.deleteSubjectVersions(eq(TEST_SUBJECT), eq(true)))
                .thenReturn(List.of(1, 2));

        DefaultAsyncSchemaRegistryApi asyncApi = new DefaultAsyncSchemaRegistryApi(schemaRegistryApi);

        // When
        asyncApi.deleteSubjectVersions(TEST_SUBJECT, true).block();

        // Then
        verify(schemaRegistryApi).deleteSubjectVersions(TEST_SUBJECT, true);
    }
}