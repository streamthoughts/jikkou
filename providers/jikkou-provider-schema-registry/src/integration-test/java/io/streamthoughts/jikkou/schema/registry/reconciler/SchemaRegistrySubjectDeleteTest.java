/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.schema.registry.reconciler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.streamthoughts.jikkou.core.ReconciliationContext;
import io.streamthoughts.jikkou.core.ReconciliationMode;
import io.streamthoughts.jikkou.core.config.Configuration;
import io.streamthoughts.jikkou.core.data.SchemaHandle;
import io.streamthoughts.jikkou.core.data.SchemaType;
import io.streamthoughts.jikkou.core.models.ApiChangeResultList;
import io.streamthoughts.jikkou.core.models.CoreAnnotations;
import io.streamthoughts.jikkou.core.models.ObjectMeta;
import io.streamthoughts.jikkou.core.models.ResourceList;
import io.streamthoughts.jikkou.core.models.change.ResourceChange;
import io.streamthoughts.jikkou.core.reconciler.ChangeResult;
import io.streamthoughts.jikkou.core.reconciler.Operation;
import io.streamthoughts.jikkou.core.selector.Selectors;
import io.streamthoughts.jikkou.schema.registry.BaseExtensionProviderIT;
import io.streamthoughts.jikkou.schema.registry.SchemaRegistryAnnotations;
import io.streamthoughts.jikkou.schema.registry.models.V1SchemaRegistrySubject;
import io.streamthoughts.jikkou.schema.registry.models.V1SchemaRegistrySubjectSpec;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class SchemaRegistrySubjectDeleteTest extends BaseExtensionProviderIT {

    @Test
    void shouldSoftDeleteSchemaSubject() {
        // Given - register a schema first
        V1SchemaRegistrySubject resource = createSubjectResource(TEST_SUBJECT);
        api.reconcile(
                ResourceList.of(List.of(resource)),
                ReconciliationMode.CREATE,
                ReconciliationContext.builder().dryRun(false).build());

        // When - delete the schema (soft delete only)
        V1SchemaRegistrySubject deleteResource = V1SchemaRegistrySubject.builder()
                .withMetadata(ObjectMeta.builder()
                        .withName(TEST_SUBJECT)
                        .withAnnotations(Map.of(
                                CoreAnnotations.JIKKOU_IO_DELETE, true))
                        .build())
                .withSpec(V1SchemaRegistrySubjectSpec.builder()
                        .withSchemaType(SchemaType.AVRO)
                        .withSchema(new SchemaHandle(AVRO_SCHEMA))
                        .build())
                .build();

        ApiChangeResultList result = api.reconcile(
                ResourceList.of(List.of(deleteResource)),
                ReconciliationMode.DELETE,
                ReconciliationContext.builder().dryRun(false).build());

        // Then
        List<ChangeResult> results = result.results();
        assertEquals(1, results.size());
        ResourceChange change = results.getFirst().change();
        assertEquals(Operation.DELETE, change.getSpec().getOp());

        // Verify subject is no longer listed
        ResourceList<V1SchemaRegistrySubject> subjects =
                api.listResources(V1SchemaRegistrySubject.class, Selectors.NO_SELECTOR, Configuration.empty());
        assertTrue(subjects.stream().noneMatch(s -> TEST_SUBJECT.equals(s.getMetadata().getName())));
    }

    @Test
    void shouldPermanentlyDeleteSchemaSubject() {
        // Given - register a schema first
        V1SchemaRegistrySubject resource = createSubjectResource(TEST_SUBJECT);
        api.reconcile(
                ResourceList.of(List.of(resource)),
                ReconciliationMode.CREATE,
                ReconciliationContext.builder().dryRun(false).build());

        // When - permanently delete the schema (soft + hard delete)
        V1SchemaRegistrySubject deleteResource = V1SchemaRegistrySubject.builder()
                .withMetadata(ObjectMeta.builder()
                        .withName(TEST_SUBJECT)
                        .withAnnotations(Map.of(
                                CoreAnnotations.JIKKOU_IO_DELETE, true,
                                SchemaRegistryAnnotations.SCHEMA_REGISTRY_PERMANANTE_DELETE, true))
                        .build())
                .withSpec(V1SchemaRegistrySubjectSpec.builder()
                        .withSchemaType(SchemaType.AVRO)
                        .withSchema(new SchemaHandle(AVRO_SCHEMA))
                        .build())
                .build();

        ApiChangeResultList result = api.reconcile(
                ResourceList.of(List.of(deleteResource)),
                ReconciliationMode.DELETE,
                ReconciliationContext.builder().dryRun(false).build());

        // Then
        List<ChangeResult> results = result.results();
        assertEquals(1, results.size());
        ResourceChange change = results.getFirst().change();
        assertEquals(Operation.DELETE, change.getSpec().getOp());

        // Verify subject is no longer listed
        ResourceList<V1SchemaRegistrySubject> subjects =
                api.listResources(V1SchemaRegistrySubject.class, Selectors.NO_SELECTOR, Configuration.empty());
        assertTrue(subjects.stream().noneMatch(s -> TEST_SUBJECT.equals(s.getMetadata().getName())));

        // Verify the subject can be re-registered (only possible after hard delete)
        V1SchemaRegistrySubject reRegister = createSubjectResource(TEST_SUBJECT);
        ApiChangeResultList reRegisterResult = api.reconcile(
                ResourceList.of(List.of(reRegister)),
                ReconciliationMode.CREATE,
                ReconciliationContext.builder().dryRun(false).build());
        assertEquals(1, reRegisterResult.results().size());
        assertEquals(Operation.CREATE, reRegisterResult.results().getFirst().change().getSpec().getOp());
    }

    private V1SchemaRegistrySubject createSubjectResource(String subjectName) {
        return V1SchemaRegistrySubject.builder()
                .withMetadata(ObjectMeta.builder()
                        .withName(subjectName)
                        .build())
                .withSpec(V1SchemaRegistrySubjectSpec.builder()
                        .withSchemaType(SchemaType.AVRO)
                        .withSchema(new SchemaHandle(AVRO_SCHEMA))
                        .build())
                .build();
    }
}