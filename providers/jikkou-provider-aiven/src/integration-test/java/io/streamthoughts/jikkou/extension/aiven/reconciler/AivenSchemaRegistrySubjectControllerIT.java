/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.extension.aiven.reconciler;

import static io.streamthoughts.jikkou.schema.registry.change.SchemaSubjectChangeComputer.DATA_COMPATIBILITY_LEVEL;
import static io.streamthoughts.jikkou.schema.registry.change.SchemaSubjectChangeComputer.DATA_REFERENCES;
import static io.streamthoughts.jikkou.schema.registry.change.SchemaSubjectChangeComputer.DATA_SCHEMA;
import static io.streamthoughts.jikkou.schema.registry.change.SchemaSubjectChangeComputer.DATA_SCHEMA_TYPE;

import io.streamthoughts.jikkou.core.DefaultApi;
import io.streamthoughts.jikkou.core.JikkouApi;
import io.streamthoughts.jikkou.core.ReconciliationContext;
import io.streamthoughts.jikkou.core.ReconciliationMode;
import io.streamthoughts.jikkou.core.config.Configuration;
import io.streamthoughts.jikkou.core.data.json.Json;
import io.streamthoughts.jikkou.core.extension.ClassExtensionAliasesGenerator;
import io.streamthoughts.jikkou.core.extension.DefaultExtensionDescriptorFactory;
import io.streamthoughts.jikkou.core.extension.DefaultExtensionFactory;
import io.streamthoughts.jikkou.core.extension.DefaultExtensionRegistry;
import io.streamthoughts.jikkou.core.models.ObjectMeta;
import io.streamthoughts.jikkou.core.models.ResourceListObject;
import io.streamthoughts.jikkou.core.models.change.GenericResourceChange;
import io.streamthoughts.jikkou.core.models.change.ResourceChange;
import io.streamthoughts.jikkou.core.models.change.ResourceChangeSpec;
import io.streamthoughts.jikkou.core.models.change.StateChange;
import io.streamthoughts.jikkou.core.reconciler.ChangeResult;
import io.streamthoughts.jikkou.core.reconciler.Operation;
import io.streamthoughts.jikkou.core.resource.DefaultResourceRegistry;
import io.streamthoughts.jikkou.extension.aiven.AbstractAivenIntegrationTest;
import io.streamthoughts.jikkou.extension.aiven.ApiVersions;
import io.streamthoughts.jikkou.schema.registry.SchemaRegistryAnnotations;
import io.streamthoughts.jikkou.schema.registry.model.CompatibilityLevels;
import io.streamthoughts.jikkou.schema.registry.model.SchemaHandle;
import io.streamthoughts.jikkou.schema.registry.model.SchemaType;
import io.streamthoughts.jikkou.schema.registry.models.V1SchemaRegistrySubject;
import io.streamthoughts.jikkou.schema.registry.models.V1SchemaRegistrySubjectSpec;
import java.util.Collections;
import java.util.List;
import java.util.Map;
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
              "fields" : [ {
                "name" : "name",
                "type" : "string"
              } ]
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

    private volatile JikkouApi api;

    @BeforeEach
    public void beforeEach() {
        DefaultExtensionRegistry registry = new DefaultExtensionRegistry(
                new DefaultExtensionDescriptorFactory(),
                new ClassExtensionAliasesGenerator()
        );
        AivenSchemaRegistrySubjectController controller = new AivenSchemaRegistrySubjectController(getAivenApiConfig());
        api = DefaultApi.builder(new DefaultExtensionFactory(registry, Configuration.empty()), new DefaultResourceRegistry())
                .register(AivenSchemaRegistrySubjectController.class, () -> controller)
                .build();
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
        V1SchemaRegistrySubject resource = V1SchemaRegistrySubject
                .builder()
                .withApiVersion(ApiVersions.KAFKA_AIVEN_V1BETA1)
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
        List<ChangeResult> results = api.reconcile(
                        ResourceListObject.of(List.of(resource)),
                        ReconciliationMode.CREATE,
                        ReconciliationContext.builder().dryRun(false).build()
                )
                .results();
        // Then
        ChangeResult result = results.getFirst();
        ResourceChange actual = result.change();
        ResourceChange expected = GenericResourceChange
                .builder(V1SchemaRegistrySubject.class)
                .withApiVersion(ApiVersions.KAFKA_AIVEN_V1BETA1)
                .withMetadata(ObjectMeta
                        .builder()
                        .withName(TEST_SUBJECT)
                        .withAnnotation(SchemaRegistryAnnotations.JIKKOU_IO_SCHEMA_REGISTRY_SCHEMA_ID, 1)
                        .build()
                )
                .withSpec(ResourceChangeSpec
                        .builder()
                        .withOperation(Operation.CREATE)
                        .withData(Map.of(
                                "permanentDelete", false,
                                "normalizeSchema", false
                        ))
                        .withChange(StateChange.create(DATA_COMPATIBILITY_LEVEL, CompatibilityLevels.BACKWARD))
                        .withChange(StateChange.create(DATA_SCHEMA, AVRO_SCHEMA_V1))
                        .withChange(StateChange.create(DATA_SCHEMA_TYPE, SchemaType.AVRO))
                        .withChange(StateChange.create(DATA_REFERENCES, Collections.emptyList()))
                        .build()
                )
                .build();
        Assertions.assertEquals(expected, actual);
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
                .withApiVersion(ApiVersions.KAFKA_AIVEN_V1BETA1)
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
        List<ChangeResult> results = api.reconcile
                (
                        ResourceListObject.of(List.of(resource)),
                        ReconciliationMode.UPDATE,
                        ReconciliationContext.builder().dryRun(false).build()
                )
                .results();
        // Then
        ChangeResult result = results.getFirst();
        ResourceChange actual = result.change();
        ResourceChange expected = GenericResourceChange
                .builder(V1SchemaRegistrySubject.class)
                .withApiVersion(ApiVersions.KAFKA_AIVEN_V1BETA1)
                .withMetadata(ObjectMeta
                        .builder()
                        .withName(TEST_SUBJECT)
                        .withAnnotation(SchemaRegistryAnnotations.JIKKOU_IO_SCHEMA_REGISTRY_SCHEMA_ID, 1)
                        .build()
                )
                .withSpec(ResourceChangeSpec
                        .builder()
                        .withOperation(Operation.UPDATE)
                        .withData(Map.of(
                                "permanentDelete", false,
                                "normalizeSchema", false
                        ))
                        .withChange(StateChange.none(DATA_COMPATIBILITY_LEVEL, CompatibilityLevels.BACKWARD))
                        .withChange(StateChange.update(DATA_SCHEMA, Json.normalize(AVRO_SCHEMA_V1), Json.normalize(AVRO_SCHEMA_V2)))
                        .withChange(StateChange.none(DATA_SCHEMA_TYPE, SchemaType.AVRO))
                        .withChange(StateChange.none(DATA_REFERENCES, Collections.emptyList()))
                        .build()
                )
                .build();
        Assertions.assertEquals(expected, actual);
    }
}
