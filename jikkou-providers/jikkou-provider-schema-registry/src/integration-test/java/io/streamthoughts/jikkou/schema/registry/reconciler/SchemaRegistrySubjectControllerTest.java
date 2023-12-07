/*
 * Copyright 2023 The original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.streamthoughts.jikkou.schema.registry.reconciler;

import io.streamthoughts.jikkou.core.ReconciliationContext;
import io.streamthoughts.jikkou.core.ReconciliationMode;
import io.streamthoughts.jikkou.core.data.TypeConverter;
import io.streamthoughts.jikkou.core.models.ObjectMeta;
import io.streamthoughts.jikkou.core.models.change.ResourceChange;
import io.streamthoughts.jikkou.core.reconciler.ChangeResult;
import io.streamthoughts.jikkou.core.reconciler.Operation;
import io.streamthoughts.jikkou.core.reconciler.Reconciler;
import io.streamthoughts.jikkou.schema.registry.AbstractIntegrationTest;
import io.streamthoughts.jikkou.schema.registry.SchemaRegistryAnnotations;
import io.streamthoughts.jikkou.schema.registry.model.SchemaHandle;
import io.streamthoughts.jikkou.schema.registry.model.SchemaType;
import io.streamthoughts.jikkou.schema.registry.models.V1SchemaRegistrySubject;
import io.streamthoughts.jikkou.schema.registry.models.V1SchemaRegistrySubjectSpec;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SchemaRegistrySubjectControllerTest extends AbstractIntegrationTest {

    private SchemaRegistrySubjectController controller;

    @BeforeEach
    void beforeEach() {
        controller = new SchemaRegistrySubjectController(getSchemaRegistryClientConfiguration());
    }

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
        Reconciler<V1SchemaRegistrySubject, ResourceChange> reconciler = new Reconciler<>(controller);
        List<ChangeResult> results = reconciler.reconcile(
                List.of(resource),
                ReconciliationMode.CREATE,
                ReconciliationContext.builder().dryRun(false).build()
        );
        // Then
        Assertions.assertEquals(1, results.size());
        ChangeResult change = results.get(0);
        ResourceChange data = change.change();
        Assertions.assertEquals(Optional.of(1), data.getMetadata().findAnnotationByKey(SchemaRegistryAnnotations.JIKKOU_IO_SCHEMA_REGISTRY_SCHEMA_ID));
        Assertions.assertEquals(Operation.CREATE, data.getSpec().getOp());
        Assertions.assertEquals(SchemaType.AVRO, data.getSpec().getChanges().getLast("schemaType", TypeConverter.of(SchemaType.class)).getAfter());
    }
}