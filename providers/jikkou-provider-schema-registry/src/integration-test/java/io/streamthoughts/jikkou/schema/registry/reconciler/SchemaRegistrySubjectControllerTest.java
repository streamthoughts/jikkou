/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.schema.registry.reconciler;

import io.streamthoughts.jikkou.core.ReconciliationContext;
import io.streamthoughts.jikkou.core.ReconciliationMode;
import io.streamthoughts.jikkou.core.data.SchemaHandle;
import io.streamthoughts.jikkou.core.data.SchemaType;
import io.streamthoughts.jikkou.core.data.TypeConverter;
import io.streamthoughts.jikkou.core.models.ApiChangeResultList;
import io.streamthoughts.jikkou.core.models.ObjectMeta;
import io.streamthoughts.jikkou.core.models.ResourceList;
import io.streamthoughts.jikkou.core.models.change.ResourceChange;
import io.streamthoughts.jikkou.core.reconciler.ChangeResult;
import io.streamthoughts.jikkou.core.reconciler.Operation;
import io.streamthoughts.jikkou.schema.registry.BaseExtensionProviderIT;
import io.streamthoughts.jikkou.schema.registry.SchemaRegistryAnnotations;
import io.streamthoughts.jikkou.schema.registry.model.Modes;
import io.streamthoughts.jikkou.schema.registry.models.V1SchemaRegistrySubject;
import io.streamthoughts.jikkou.schema.registry.models.V1SchemaRegistrySubjectSpec;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class SchemaRegistrySubjectControllerTest extends BaseExtensionProviderIT {

    @Test
    void shouldRegisterSchemaForNewResource() {
        // Given
        V1SchemaRegistrySubject resource = V1SchemaRegistrySubject.builder()
            .withMetadata(ObjectMeta.builder()
                .withName(TEST_SUBJECT)
                .build()
            )
            .withSpec(V1SchemaRegistrySubjectSpec
                .builder()
                .withSchemaType(SchemaType.AVRO)
                .withSchema(new SchemaHandle(AVRO_SCHEMA))
                .build())
            .build();
        // When
        ApiChangeResultList result = api.reconcile(
            ResourceList.of(List.of(resource)),
            ReconciliationMode.CREATE,
            ReconciliationContext.builder().dryRun(false).build()
        );
        // Then
        List<ChangeResult> results = result.results();
        Assertions.assertEquals(1, results.size());
        ChangeResult change = results.getFirst();
        ResourceChange data = change.change();
        Assertions.assertEquals(Optional.of(1), data.getMetadata().findAnnotationByKey(SchemaRegistryAnnotations.SCHEMA_REGISTRY_SCHEMA_ID));
        Assertions.assertEquals(Operation.CREATE, data.getSpec().getOp());
        Assertions.assertEquals(SchemaType.AVRO, data.getSpec().getChanges().getLast("schemaType", TypeConverter.of(SchemaType.class)).getAfter());
    }

    @Test
    void shouldImportSchemaForNewResource() {
        // Given
        V1SchemaRegistrySubject resource = V1SchemaRegistrySubject.builder()
            .withMetadata(ObjectMeta.builder()
                .withName(TEST_SUBJECT)
                .withAnnotations(Map.of(
                    SchemaRegistryAnnotations.SCHEMA_REGISTRY_SCHEMA_ID, 123,
                    SchemaRegistryAnnotations.SCHEMA_REGISTRY_SCHEMA_VERSION, 4
                ))
                .build()
            )
            .withSpec(V1SchemaRegistrySubjectSpec
                .builder()
                .withSchemaType(SchemaType.AVRO)
                .withSchema(new SchemaHandle(AVRO_SCHEMA))
                .withMode(Modes.IMPORT)
                .build())
            .build();
        // When
        ApiChangeResultList result = api.reconcile(
            ResourceList.of(List.of(resource)),
            ReconciliationMode.CREATE,
            ReconciliationContext.builder().dryRun(false).build()
        );
        // Then
        List<ChangeResult> results = result.results();
        Assertions.assertEquals(1, results.size());
        ChangeResult change = results.getFirst();
        ResourceChange data = change.change();
        Assertions.assertEquals(Optional.of(123), data.getMetadata().findAnnotationByKey(SchemaRegistryAnnotations.SCHEMA_REGISTRY_SCHEMA_ID));
        Assertions.assertEquals(Optional.of(4), data.getMetadata().findAnnotationByKey(SchemaRegistryAnnotations.SCHEMA_REGISTRY_SCHEMA_VERSION));
        Assertions.assertEquals(Operation.CREATE, data.getSpec().getOp());
        Assertions.assertEquals(SchemaType.AVRO, data.getSpec().getChanges().getLast("schemaType", TypeConverter.of(SchemaType.class)).getAfter());
        Assertions.assertEquals(Modes.IMPORT, data.getSpec().getChanges().getLast("mode", TypeConverter.of(Modes.class)).getAfter());
    }
}