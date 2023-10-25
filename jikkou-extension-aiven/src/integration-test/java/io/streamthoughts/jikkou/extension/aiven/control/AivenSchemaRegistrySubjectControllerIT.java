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
package io.streamthoughts.jikkou.extension.aiven.control;

import io.streamthoughts.jikkou.core.ReconciliationContext;
import io.streamthoughts.jikkou.core.ReconciliationMode;
import io.streamthoughts.jikkou.core.models.ObjectMeta;
import io.streamthoughts.jikkou.core.reconcilier.ChangeResult;
import io.streamthoughts.jikkou.core.reconcilier.ChangeType;
import io.streamthoughts.jikkou.core.reconcilier.Reconcilier;
import io.streamthoughts.jikkou.core.reconcilier.change.JsonValueChange;
import io.streamthoughts.jikkou.core.reconcilier.change.ValueChange;
import io.streamthoughts.jikkou.extension.aiven.AbstractAivenIntegrationTest;
import io.streamthoughts.jikkou.schema.registry.SchemaRegistryAnnotations;
import io.streamthoughts.jikkou.schema.registry.change.SchemaSubjectChange;
import io.streamthoughts.jikkou.schema.registry.change.SchemaSubjectChangeOptions;
import io.streamthoughts.jikkou.schema.registry.model.CompatibilityLevels;
import io.streamthoughts.jikkou.schema.registry.model.SchemaHandle;
import io.streamthoughts.jikkou.schema.registry.model.SchemaType;
import io.streamthoughts.jikkou.schema.registry.models.V1SchemaRegistrySubject;
import io.streamthoughts.jikkou.schema.registry.models.V1SchemaRegistrySubjectChange;
import io.streamthoughts.jikkou.schema.registry.models.V1SchemaRegistrySubjectSpec;
import java.util.Collections;
import java.util.List;
import okhttp3.mockwebserver.MockResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("integration")
public class AivenSchemaRegistrySubjectControllerIT extends AbstractAivenIntegrationTest {

    public static final String TEST_SUBJECT = "test";

    public static final String AVRO_SCHEMA_V1 = """
            {
              "namespace": "example.avro",
              "type": "record",
              "name": "User",
              "fields": [
                 {"name": "name", "type": "string"}
              ]
            }
            """;

    public static final String AVRO_SCHEMA_V2 = """
            {
              "namespace": "example.avro",
              "type": "record",
              "name": "User",
              "fields": [
                 {"name": "name", "type": "string"},
                 {"name": "favorite_color", "type": "string"}
              ]
            }
            """;

    private AivenSchemaRegistrySubjectController controller;


    @BeforeEach
    public void beforeEach() {
        controller = new AivenSchemaRegistrySubjectController(getAivenApiConfig());
    }

    @Test
    void shouldCreateSchemaRegistrySubject() {
        // Given
        enqueueResponse(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setResponseCode(200)
                .setBody("""
                        {
                            "subjects": [ ]
                        }
                        """)
        );
        // Update Schema
        enqueueResponse(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setResponseCode(200)
                .setBody("""
                        {
                            "version": 1
                        }
                        """)
        );
        // Update Config
        enqueueResponse(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setResponseCode(200)
                .setBody("""
                        {}
                        """)
        );
        V1SchemaRegistrySubject resource = V1SchemaRegistrySubject.builder()
                .withMetadata(ObjectMeta.builder()
                        .withName(TEST_SUBJECT)
                        .build()
                )
                .withSpec(V1SchemaRegistrySubjectSpec
                        .builder()
                        .withSchemaType(SchemaType.AVRO)
                        .withSchema(new SchemaHandle(AVRO_SCHEMA_V1))
                        .withCompatibilityLevel(CompatibilityLevels.BACKWARD)
                        .build())
                .build();

        // When
        Reconcilier<V1SchemaRegistrySubject, SchemaSubjectChange> reconcilier = new Reconcilier<>(controller);
        List<ChangeResult<SchemaSubjectChange>> results = reconcilier.reconcile
                (
                        List.of(resource),
                        ReconciliationMode.CREATE,
                        ReconciliationContext.builder().dryRun(false).build()
                );
        // Then
        Assertions.assertNotNull(results);
        V1SchemaRegistrySubjectChange change = V1SchemaRegistrySubjectChange
                .builder()
                .withMetadata(ObjectMeta
                        .builder()
                        .withName(TEST_SUBJECT)
                        .withAnnotation(SchemaRegistryAnnotations.JIKKOU_IO_SCHEMA_REGISTRY_SCHEMA_ID, 1)
                        .build()
                )
                .withChange(SchemaSubjectChange
                        .builder()
                        .withChangeType(ChangeType.ADD)
                        .withSchema(JsonValueChange.withAfterValue(AVRO_SCHEMA_V1))
                        .withSchemaType(ValueChange.withAfterValue(SchemaType.AVRO))
                        .withSubject(TEST_SUBJECT)
                        .withOptions(new SchemaSubjectChangeOptions())
                        .withCompatibilityLevels(ValueChange.withAfterValue(CompatibilityLevels.BACKWARD))
                        .withReferences(ValueChange.withAfterValue(Collections.emptyList()))
                        .build()
                )
                .build();
        Assertions.assertEquals(change, results.get(0).data());
    }

    @Test
    void shouldUpdateSchemaRegistrySubject() {
        // Given
        enqueueResponse(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setResponseCode(200)
                .setBody("""
                        {
                            "subjects": [ "test" ]
                        }
                        """)
        );
        enqueueResponse(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setResponseCode(200)
                .setBody("""
                        {
                        	"version": {
                        		"subject": "test",
                        		"id": 1,
                        		"schemaType": "AVRO",
                        		"schema": "{\\"namespace\\": \\"example.avro\\",\\"type\\": \\"record\\",\\"name\\": \\"User\\",\\"fields\\": [{\\"name\\": \\"name\\",\\"type\\": \\"string\\"}]}"
                        	}
                        }
                        """)
        );
        enqueueResponse(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setResponseCode(200)
                .setBody("""
                        {
                          "compatibilityLevel": "BACKWARD"
                        }
                        """)
        );
        enqueueResponse(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setResponseCode(200)
                .setBody("""
                        {
                            "version": 1
                        }
                        """)
        );
        V1SchemaRegistrySubject resource = V1SchemaRegistrySubject.builder()
                .withMetadata(ObjectMeta.builder()
                        .withName(TEST_SUBJECT)
                        .build()
                )
                .withSpec(V1SchemaRegistrySubjectSpec
                        .builder()
                        .withSchemaType(SchemaType.AVRO)
                        .withSchema(new SchemaHandle(AVRO_SCHEMA_V2))
                        .withCompatibilityLevel(CompatibilityLevels.BACKWARD)
                        .build())
                .build();

        // When
        Reconcilier<V1SchemaRegistrySubject, SchemaSubjectChange> reconcilier = new Reconcilier<>(controller);
        List<ChangeResult<SchemaSubjectChange>> results = reconcilier.reconcile
                (
                        List.of(resource),
                        ReconciliationMode.UPDATE,
                        ReconciliationContext.builder().dryRun(false).build()
                );
        // Then
        Assertions.assertNotNull(results);
        V1SchemaRegistrySubjectChange change = V1SchemaRegistrySubjectChange
                .builder()
                .withMetadata(ObjectMeta
                        .builder()
                        .withName(TEST_SUBJECT)
                        .withAnnotation(SchemaRegistryAnnotations.JIKKOU_IO_SCHEMA_REGISTRY_SCHEMA_ID, 1)
                        .build()
                )
                .withChange(SchemaSubjectChange
                        .builder()
                        .withChangeType(ChangeType.UPDATE)
                        .withSchema(JsonValueChange.with(AVRO_SCHEMA_V1, AVRO_SCHEMA_V2))
                        .withSchemaType(ValueChange.none(SchemaType.AVRO))
                        .withCompatibilityLevels(ValueChange.none(CompatibilityLevels.BACKWARD))
                        .withSubject(TEST_SUBJECT)
                        .withOptions(new SchemaSubjectChangeOptions())
                        .withReferences(ValueChange.none(Collections.emptyList()))
                        .build()
                )
                .build();
        Assertions.assertEquals(change, results.get(0).data());
    }
}
