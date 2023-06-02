/*
 * Copyright 2023 StreamThoughts.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.streamthoughts.jikkou.schema.registry.control;

import io.streamthoughts.jikkou.api.ReconciliationContext;
import io.streamthoughts.jikkou.api.ReconciliationMode;
import io.streamthoughts.jikkou.api.control.ChangeResult;
import io.streamthoughts.jikkou.api.control.ChangeType;
import io.streamthoughts.jikkou.api.model.ObjectMeta;
import io.streamthoughts.jikkou.schema.registry.AbstractIntegrationTest;
import io.streamthoughts.jikkou.schema.registry.change.SchemaSubjectChange;
import io.streamthoughts.jikkou.schema.registry.model.SchemaHandle;
import io.streamthoughts.jikkou.schema.registry.model.SchemaType;
import io.streamthoughts.jikkou.schema.registry.models.V1SchemaRegistrySubject;
import io.streamthoughts.jikkou.schema.registry.models.V1SchemaRegistrySubjectSpec;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SchemaRegistryControllerTest extends AbstractIntegrationTest {

    private SchemaRegistryController controller;

    @BeforeEach
    void beforeEach() {
        controller = new SchemaRegistryController(getSchemaRegistryClientConfiguration());
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
        List<ChangeResult<SchemaSubjectChange>> results = controller.reconcile(
                List.of(resource),
                ReconciliationMode.CREATE,
                ReconciliationContext.with(false)
        );
        // Then
        Assertions.assertNotNull(results);
        Assertions.assertEquals(1, results.size());

        ChangeResult<SchemaSubjectChange> change = results.get(0);
        Assertions.assertEquals(ChangeType.ADD, change.resource().getChangeType());
        Assertions.assertEquals(SchemaType.AVRO, change.resource().getSchemaType().getAfter());
    }
}