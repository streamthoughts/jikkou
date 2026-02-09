/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.schema.registry.change.handler;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.streamthoughts.jikkou.core.models.ObjectMeta;
import io.streamthoughts.jikkou.core.models.change.GenericResourceChange;
import io.streamthoughts.jikkou.core.models.change.ResourceChange;
import io.streamthoughts.jikkou.core.models.change.ResourceChangeSpec;
import io.streamthoughts.jikkou.core.reconciler.ChangeResponse;
import io.streamthoughts.jikkou.core.reconciler.Operation;
import io.streamthoughts.jikkou.schema.registry.api.AsyncSchemaRegistryApi;
import io.streamthoughts.jikkou.schema.registry.models.V1SchemaRegistrySubject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;
import reactor.core.publisher.Mono;

class DeleteSchemaSubjectChangeHandlerTest {

    private static final String TEST_SUBJECT = "test-subject";

    @Test
    void shouldPerformSoftDeleteOnlyWhenPermanentDeleteIsFalse() {
        // Given
        AsyncSchemaRegistryApi api = mock(AsyncSchemaRegistryApi.class);
        when(api.deleteSubjectVersions(eq(TEST_SUBJECT), eq(false)))
            .thenReturn(Mono.just(List.of(1, 2)));

        DeleteSchemaSubjectChangeHandler handler = new DeleteSchemaSubjectChangeHandler(api);
        ResourceChange change = createDeleteChange(false);

        // When
        List<ChangeResponse> responses = handler.handleChanges(List.of(change));
        responses.forEach(r -> r.getResults().join());

        // Then
        verify(api).deleteSubjectVersions(TEST_SUBJECT, false);
        verify(api, never()).deleteSubjectVersions(eq(TEST_SUBJECT), eq(true));
    }

    @Test
    void shouldPerformSoftDeleteOnlyWhenPermanentDeleteIsEmpty() {
        // Given
        AsyncSchemaRegistryApi api = mock(AsyncSchemaRegistryApi.class);
        when(api.deleteSubjectVersions(eq(TEST_SUBJECT), eq(false)))
            .thenReturn(Mono.just(List.of(1, 2)));

        DeleteSchemaSubjectChangeHandler handler = new DeleteSchemaSubjectChangeHandler(api);
        ResourceChange change = createDeleteChange(null);

        // When
        List<ChangeResponse> responses = handler.handleChanges(List.of(change));
        responses.forEach(r -> r.getResults().join());

        // Then
        verify(api).deleteSubjectVersions(TEST_SUBJECT, false);
        verify(api, never()).deleteSubjectVersions(eq(TEST_SUBJECT), eq(true));
    }

    @Test
    void shouldPerformSoftDeleteThenHardDeleteWhenPermanentDeleteIsTrue() {
        // Given
        AsyncSchemaRegistryApi api = mock(AsyncSchemaRegistryApi.class);
        when(api.deleteSubjectVersions(eq(TEST_SUBJECT), eq(false)))
            .thenReturn(Mono.just(List.of(1, 2)));
        when(api.deleteSubjectVersions(eq(TEST_SUBJECT), eq(true)))
            .thenReturn(Mono.just(List.of(1, 2)));

        DeleteSchemaSubjectChangeHandler handler = new DeleteSchemaSubjectChangeHandler(api);
        ResourceChange change = createDeleteChange(true);

        // When
        List<ChangeResponse> responses = handler.handleChanges(List.of(change));
        responses.forEach(r -> r.getResults().join());

        // Then
        InOrder inOrder = Mockito.inOrder(api);
        inOrder.verify(api).deleteSubjectVersions(TEST_SUBJECT, false);
        inOrder.verify(api).deleteSubjectVersions(TEST_SUBJECT, true);
    }

    private ResourceChange createDeleteChange(Boolean permanentDelete) {
        Map<String, Object> delete = Map.of(
            "normalizeSchema", false,
            "schemaId", "",
            "version", ""
        );
        if (permanentDelete != null) {
            delete = new HashMap<>(delete);
            delete.put("permanentDelete", permanentDelete);
        }
        return GenericResourceChange
            .builder(V1SchemaRegistrySubject.class)
            .withMetadata(ObjectMeta
                .builder()
                .withName(TEST_SUBJECT)
                .build())
            .withSpec(ResourceChangeSpec
                .builder()
                .withOperation(Operation.DELETE)
                .withData(delete)
                .build())
            .build();
    }
}