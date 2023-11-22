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
package io.streamthoughts.jikkou.schema.registry.reconcilier;

import io.streamthoughts.jikkou.core.ReconciliationContext;
import io.streamthoughts.jikkou.core.ReconciliationMode;
import io.streamthoughts.jikkou.core.models.HasMetadataChange;
import io.streamthoughts.jikkou.core.models.ObjectMeta;
import io.streamthoughts.jikkou.core.reconcilier.ChangeResult;
import io.streamthoughts.jikkou.core.reconcilier.ChangeType;
import io.streamthoughts.jikkou.core.reconcilier.Reconcilier;
import io.streamthoughts.jikkou.schema.registry.AbstractIntegrationTest;
import io.streamthoughts.jikkou.schema.registry.SchemaRegistryAnnotations;
import io.streamthoughts.jikkou.schema.registry.change.SchemaSubjectChange;
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
        Reconcilier<V1SchemaRegistrySubject, SchemaSubjectChange> reconcilier = new Reconcilier<>(controller);
        List<ChangeResult<SchemaSubjectChange>> results = reconcilier.reconcile(
                List.of(resource),
                ReconciliationMode.CREATE,
                ReconciliationContext.builder().dryRun(false).build()
        );
        // Then
        Assertions.assertEquals(1, results.size());
        ChangeResult<SchemaSubjectChange> change = results.get(0);
        HasMetadataChange<SchemaSubjectChange> data = change.data();
        Assertions.assertEquals(Optional.of(1), data.getMetadata().findAnnotationByKey(SchemaRegistryAnnotations.JIKKOU_IO_SCHEMA_REGISTRY_SCHEMA_ID));
        Assertions.assertEquals(ChangeType.ADD, data.getChange().operation());
        Assertions.assertEquals(SchemaType.AVRO, data.getChange().getSchemaType().getAfter());
    }
}