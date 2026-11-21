/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.jikkou.schema.registry.change.handler;

import static io.jikkou.schema.registry.change.SchemaSubjectChangeComputer.DATA_COMPATIBILITY_LEVEL;
import static io.jikkou.schema.registry.change.SchemaSubjectChangeComputer.DATA_MODE;
import static io.jikkou.schema.registry.change.SchemaSubjectChangeComputer.DATA_REFERENCES;
import static io.jikkou.schema.registry.change.SchemaSubjectChangeComputer.DATA_SCHEMA;
import static io.jikkou.schema.registry.change.SchemaSubjectChangeComputer.DATA_SCHEMA_TYPE;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.jikkou.core.data.SchemaAndType;
import io.jikkou.core.data.SchemaType;
import io.jikkou.core.models.ObjectMeta;
import io.jikkou.core.models.change.GenericResourceChange;
import io.jikkou.core.models.change.ResourceChange;
import io.jikkou.core.models.change.ResourceChangeSpec;
import io.jikkou.core.models.change.StateChange;
import io.jikkou.core.reconciler.ChangeResponse;
import io.jikkou.core.reconciler.Operation;
import io.jikkou.schema.registry.api.AsyncSchemaRegistryApi;
import io.jikkou.schema.registry.api.data.CompatibilityObject;
import io.jikkou.schema.registry.api.data.SubjectSchemaId;
import io.jikkou.schema.registry.api.data.SubjectSchemaRegistration;
import io.jikkou.schema.registry.model.CompatibilityLevels;
import io.jikkou.schema.registry.models.V1SchemaRegistrySubject;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mockito;
import reactor.core.publisher.Mono;

class UpdateSchemaSubjectChangeHandlerTest {

    private static final String TEST_SUBJECT = "test-subject";

    private static final Map<String, Object> DEFAULT_DATA = Map.of(
            "permanentDelete", false,
            "normalizeSchema", false,
            "schemaId", "",
            "version", ""
    );

    @Test
    void shouldRegisterSchemaBeforeUpdatingCompatibilityWhenTightening() {
        // Given
        AsyncSchemaRegistryApi api = mock(AsyncSchemaRegistryApi.class);
        when(api.registerSubjectVersion(eq(TEST_SUBJECT), any(), anyBoolean()))
                .thenReturn(Mono.just(new SubjectSchemaId(1)));
        when(api.updateSubjectCompatibilityLevel(eq(TEST_SUBJECT), any()))
                .thenReturn(Mono.just(new CompatibilityObject(CompatibilityLevels.BACKWARD.name())));

        UpdateSchemaSubjectChangeHandler handler = new UpdateSchemaSubjectChangeHandler(api);
        ResourceChange change = createUpdateChange(
                CompatibilityLevels.NONE, CompatibilityLevels.BACKWARD,
                "schema-v1", "schema-v2"
        );

        // When
        List<ChangeResponse> responses = handler.handleChanges(List.of(change));
        responses.forEach(r -> r.getResults().join());

        // Then - schema must be registered BEFORE compatibility is updated
        InOrder inOrder = Mockito.inOrder(api);
        inOrder.verify(api).registerSubjectVersion(eq(TEST_SUBJECT), any(), anyBoolean());
        inOrder.verify(api).updateSubjectCompatibilityLevel(eq(TEST_SUBJECT), any());
    }

    @Test
    void shouldUpdateCompatibilityBeforeRegisteringSchemaWhenLoosening() {
        // Given
        AsyncSchemaRegistryApi api = mock(AsyncSchemaRegistryApi.class);
        when(api.updateSubjectCompatibilityLevel(eq(TEST_SUBJECT), any()))
                .thenReturn(Mono.just(new CompatibilityObject(CompatibilityLevels.NONE.name())));
        when(api.registerSubjectVersion(eq(TEST_SUBJECT), any(), anyBoolean()))
                .thenReturn(Mono.just(new SubjectSchemaId(1)));

        UpdateSchemaSubjectChangeHandler handler = new UpdateSchemaSubjectChangeHandler(api);
        ResourceChange change = createUpdateChange(
                CompatibilityLevels.BACKWARD, CompatibilityLevels.NONE,
                "schema-v1", "schema-v2"
        );

        // When
        List<ChangeResponse> responses = handler.handleChanges(List.of(change));
        responses.forEach(r -> r.getResults().join());

        // Then - compatibility must be updated BEFORE schema is registered
        InOrder inOrder = Mockito.inOrder(api);
        inOrder.verify(api).updateSubjectCompatibilityLevel(eq(TEST_SUBJECT), any());
        inOrder.verify(api).registerSubjectVersion(eq(TEST_SUBJECT), any(), anyBoolean());
    }

    @Test
    void shouldOnlyUpdateCompatibilityWhenSchemaUnchanged() {
        // Given
        AsyncSchemaRegistryApi api = mock(AsyncSchemaRegistryApi.class);
        when(api.updateSubjectCompatibilityLevel(eq(TEST_SUBJECT), any()))
                .thenReturn(Mono.just(new CompatibilityObject(CompatibilityLevels.BACKWARD.name())));

        UpdateSchemaSubjectChangeHandler handler = new UpdateSchemaSubjectChangeHandler(api);
        ResourceChange change = createUpdateChange(
                CompatibilityLevels.NONE, CompatibilityLevels.BACKWARD,
                "schema-v1", "schema-v1"
        );

        // When
        List<ChangeResponse> responses = handler.handleChanges(List.of(change));
        responses.forEach(r -> r.getResults().join());

        // Then
        verify(api).updateSubjectCompatibilityLevel(eq(TEST_SUBJECT), any());
        verify(api, never()).registerSubjectVersion(any(), any(), anyBoolean());
    }

    @Test
    void shouldOnlyRegisterSchemaWhenCompatibilityUnchanged() {
        // Given
        AsyncSchemaRegistryApi api = mock(AsyncSchemaRegistryApi.class);
        when(api.registerSubjectVersion(eq(TEST_SUBJECT), any(), anyBoolean()))
                .thenReturn(Mono.just(new SubjectSchemaId(1)));

        UpdateSchemaSubjectChangeHandler handler = new UpdateSchemaSubjectChangeHandler(api);
        ResourceChange change = createUpdateChange(
                CompatibilityLevels.BACKWARD, CompatibilityLevels.BACKWARD,
                "schema-v1", "schema-v2"
        );

        // When
        List<ChangeResponse> responses = handler.handleChanges(List.of(change));
        responses.forEach(r -> r.getResults().join());

        // Then
        verify(api).registerSubjectVersion(eq(TEST_SUBJECT), any(), anyBoolean());
        verify(api, never()).updateSubjectCompatibilityLevel(any(), any());
    }

    @Test
    void shouldUpdateSchemaWhenReferencesUpdated() {
        // Given
        AsyncSchemaRegistryApi api = mock(AsyncSchemaRegistryApi.class);
        when(api.updateSubjectCompatibilityLevel(eq(TEST_SUBJECT), any()))
                .thenReturn(Mono.just(new CompatibilityObject(CompatibilityLevels.BACKWARD.name())));
        when(api.registerSubjectVersion(eq(TEST_SUBJECT), any(), anyBoolean()))
                .thenReturn(Mono.just(new SubjectSchemaId(1)));

        List<Map<String, Object>> referencesBefore = List.of(
                Map.of(
                        "name", "com.example.kafka.shared.Metadata",
                        "subject", "com.example.kafka.shared.Metadata",
                        "version", 1
                )
        );

        List<Map<String, Object>> referencesAfter = List.of(
                Map.of(
                        "name", " com.example.kafka.shared.Metadata",
                        "subject", "com.example.kafka.shared.Metadata",
                        "version", 2
                )
        );

        UpdateSchemaSubjectChangeHandler handler = new UpdateSchemaSubjectChangeHandler(api);
        ResourceChange change = createUpdateChange(
                CompatibilityLevels.BACKWARD, CompatibilityLevels.BACKWARD,
                "schema-v1", "schema-v1",
                referencesBefore, referencesAfter
        );

        // When
        List<ChangeResponse> responses = handler.handleChanges(List.of(change));
        responses.forEach(r -> r.getResults().join());

        // Then
        verify(api).registerSubjectVersion(eq(TEST_SUBJECT), any(), anyBoolean());
        verify(api, never()).updateSubjectCompatibilityLevel(any(), any());
    }

    @Test
    void shouldRegisterSchemaWithIntegerIdAndVersionWhenAnnotationsAreSet() {
        // Given
        AsyncSchemaRegistryApi api = mock(AsyncSchemaRegistryApi.class);
        when(api.registerSubjectVersion(eq(TEST_SUBJECT), any(), anyBoolean()))
                .thenReturn(Mono.just(new SubjectSchemaId(1001)));

        UpdateSchemaSubjectChangeHandler handler = new UpdateSchemaSubjectChangeHandler(api);
        ResourceChange change = createUpdateChange(
                CompatibilityLevels.BACKWARD, CompatibilityLevels.BACKWARD,
                "schema-v1", "schema-v2",
                Collections.emptyList(), Collections.emptyList(),
                Map.of("permanentDelete", false, "normalizeSchema", false, "schemaId", "1001", "version", "1")
        );

        // When
        List<ChangeResponse> responses = handler.handleChanges(List.of(change));
        responses.forEach(r -> r.getResults().join());

        // Then
        ArgumentCaptor<SubjectSchemaRegistration> captor = ArgumentCaptor.forClass(SubjectSchemaRegistration.class);
        verify(api).registerSubjectVersion(eq(TEST_SUBJECT), captor.capture(), anyBoolean());
        Assertions.assertEquals(1001, captor.getValue().id());
        Assertions.assertEquals(1, captor.getValue().version());
    }

    @Test
    void shouldRegisterSchemaWithoutIdAndVersionWhenAnnotationsAreUnset() {
        // Given
        AsyncSchemaRegistryApi api = mock(AsyncSchemaRegistryApi.class);
        when(api.registerSubjectVersion(eq(TEST_SUBJECT), any(), anyBoolean()))
                .thenReturn(Mono.just(new SubjectSchemaId(1)));

        UpdateSchemaSubjectChangeHandler handler = new UpdateSchemaSubjectChangeHandler(api);
        ResourceChange change = createUpdateChange(
                CompatibilityLevels.BACKWARD, CompatibilityLevels.BACKWARD,
                "schema-v1", "schema-v2"
        );

        // When
        List<ChangeResponse> responses = handler.handleChanges(List.of(change));
        responses.forEach(r -> r.getResults().join());

        // Then
        ArgumentCaptor<SubjectSchemaRegistration> captor = ArgumentCaptor.forClass(SubjectSchemaRegistration.class);
        verify(api).registerSubjectVersion(eq(TEST_SUBJECT), captor.capture(), anyBoolean());
        Assertions.assertNull(captor.getValue().id());
        Assertions.assertNull(captor.getValue().version());
    }

    @Test
    void shouldNotRegisterSchemaWhenSchemaIdAnnotationIsNotAnInteger() {
        // Given
        AsyncSchemaRegistryApi api = mock(AsyncSchemaRegistryApi.class);

        UpdateSchemaSubjectChangeHandler handler = new UpdateSchemaSubjectChangeHandler(api);
        ResourceChange change = createUpdateChange(
                CompatibilityLevels.BACKWARD, CompatibilityLevels.BACKWARD,
                "schema-v1", "schema-v2",
                Collections.emptyList(), Collections.emptyList(),
                Map.of("permanentDelete", false, "normalizeSchema", false, "schemaId", "abc", "version", "")
        );

        // When
        List<ChangeResponse> responses = handler.handleChanges(List.of(change));
        responses.forEach(r -> r.getResults().join());

        // Then
        verify(api, never()).registerSubjectVersion(any(), any(), anyBoolean());
        Assertions.assertTrue(responses.getFirst().getResults().join().stream().anyMatch(m -> m.getError().isPresent()));
    }

    private ResourceChange createUpdateChange(
            CompatibilityLevels compatBefore,
            CompatibilityLevels compatAfter,
            String schemaBefore,
            String schemaAfter) {
        return createUpdateChange(compatBefore, compatAfter, schemaBefore, schemaAfter, Collections.emptyList(), Collections.emptyList());
    }

    private ResourceChange createUpdateChange(
            CompatibilityLevels compatBefore,
            CompatibilityLevels compatAfter,
            String schemaBefore,
            String schemaAfter,
            List<Map<String, Object>> referencesBefore,
            List<Map<String, Object>> referencesAfter) {
        return createUpdateChange(compatBefore, compatAfter, schemaBefore, schemaAfter,
                referencesBefore, referencesAfter, DEFAULT_DATA);
    }

    private ResourceChange createUpdateChange(
            CompatibilityLevels compatBefore,
            CompatibilityLevels compatAfter,
            String schemaBefore,
            String schemaAfter,
            List<Map<String, Object>> referencesBefore,
            List<Map<String, Object>> referencesAfter,
            Map<String, Object> data) {

        StateChange compatChange = compatBefore.equals(compatAfter)
                ? StateChange.none(DATA_COMPATIBILITY_LEVEL, compatBefore)
                : StateChange.update(DATA_COMPATIBILITY_LEVEL, compatBefore, compatAfter);

        SchemaAndType schemaAndTypeBefore = new SchemaAndType(schemaBefore, SchemaType.AVRO);
        SchemaAndType schemaAndTypeAfter = new SchemaAndType(schemaAfter, SchemaType.AVRO);
        StateChange schemaChange = schemaBefore.equals(schemaAfter)
                ? StateChange.none(DATA_SCHEMA, schemaAndTypeBefore)
                : StateChange.update(DATA_SCHEMA, schemaAndTypeBefore, schemaAndTypeAfter);

        StateChange referencesChange = referencesBefore.equals(referencesAfter)
                ? StateChange.none(DATA_REFERENCES, referencesBefore)
                : StateChange.update(DATA_REFERENCES, referencesBefore, referencesAfter);

        return GenericResourceChange
                .builder(V1SchemaRegistrySubject.class)
                .withMetadata(ObjectMeta
                        .builder()
                        .withName(TEST_SUBJECT)
                        .build())
                .withSpec(ResourceChangeSpec
                        .builder()
                        .withOperation(Operation.UPDATE)
                        .withData(data)
                        .withChange(compatChange)
                        .withChange(schemaChange)
                        .withChange(StateChange.none(DATA_SCHEMA_TYPE, SchemaType.AVRO))
                        .withChange(referencesChange)
                        .withChange(StateChange.none(DATA_MODE, null))
                        .build())
                .build();
    }
}
