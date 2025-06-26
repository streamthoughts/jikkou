/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.schema.registry.transform;

import io.streamthoughts.jikkou.core.ReconciliationContext;
import io.streamthoughts.jikkou.core.config.Configuration;
import io.streamthoughts.jikkou.core.data.SchemaHandle;
import io.streamthoughts.jikkou.core.data.SchemaType;
import io.streamthoughts.jikkou.core.extension.ExtensionContext;
import io.streamthoughts.jikkou.core.models.ResourceList;
import io.streamthoughts.jikkou.schema.registry.SchemaRegistryExtensionProvider;
import io.streamthoughts.jikkou.schema.registry.models.V1SchemaRegistrySubject;
import io.streamthoughts.jikkou.schema.registry.models.V1SchemaRegistrySubjectSpec;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;

class NormalizeSubjectSchemaTransformationTest {

    static final String DUMMY_JSON_STRING = """
        { "field3": { "field4": "value4"}, "field2": "value2", "field1": "value1" } }
        """;

    static final String CANONICAL_DUMMY_JSON_STRING = "{\"field1\":\"value1\",\"field2\":\"value2\",\"field3\":{\"field4\":\"value4\"}}";

    @Test
    void shouldHaveEmptyConstructor() {
        Assertions.assertDoesNotThrow(NormalizeSubjectSchemaTransformation::new);
    }

    @Test
    void shouldNotNormalize() {
        // Given
        V1SchemaRegistrySubject subject = V1SchemaRegistrySubject.builder()
            .withSpec(V1SchemaRegistrySubjectSpec
                .builder()
                .withSchemaType(SchemaType.AVRO)
                .withSchema(new SchemaHandle(DUMMY_JSON_STRING))
                .build())
            .build();
        var transform = new NormalizeSubjectSchemaTransformation();
        ExtensionContext extensionContext = Mockito.mock(ExtensionContext.class);
        Mockito.when(extensionContext.provider()).thenAnswer((Answer<SchemaRegistryExtensionProvider>) invocationOnMock -> {
            SchemaRegistryExtensionProvider provider = new SchemaRegistryExtensionProvider();
            provider.configure(Configuration.of(
                    SchemaRegistryExtensionProvider.Config.SCHEMA_REGISTRY_URL.key(), "http://localhost:2181",
                    SchemaRegistryExtensionProvider.Config.NORMALIZE_SCHEMAS_ENABLED.key(), false
                )
            );
            return provider;
        });
        transform.init(extensionContext);

        // When
        V1SchemaRegistrySubject result = transform.transform(subject, ResourceList.empty(), ReconciliationContext.Default.EMPTY)
            .orElse(null);

        // Then
        Assertions.assertNotNull(result);
        Assertions.assertEquals(DUMMY_JSON_STRING, result.getSpec().getSchema().value());
    }

    @Test
    void shouldNormalizeSchemaForAvro() {
        // Given
        V1SchemaRegistrySubject subject = V1SchemaRegistrySubject.builder()
            .withSpec(V1SchemaRegistrySubjectSpec
                .builder()
                .withSchemaType(SchemaType.AVRO)
                .withSchema(new SchemaHandle(DUMMY_JSON_STRING))
                .build())
            .build();
        var transform = new NormalizeSubjectSchemaTransformation();
        ExtensionContext extensionContext = Mockito.mock(ExtensionContext.class);
        Mockito.when(extensionContext.provider()).thenAnswer((Answer<SchemaRegistryExtensionProvider>) invocationOnMock -> {
            SchemaRegistryExtensionProvider provider = new SchemaRegistryExtensionProvider();
            provider.configure(Configuration.of(
                    SchemaRegistryExtensionProvider.Config.SCHEMA_REGISTRY_URL.key(), "http://localhost:2181"
                )
            );
            return provider;
        });
        transform.init(extensionContext);
        // When
        V1SchemaRegistrySubject result = transform.transform(subject, ResourceList.empty(), ReconciliationContext.Default.EMPTY)
            .orElse(null);

        // Then
        Assertions.assertNotNull(result);
        Assertions.assertEquals(CANONICAL_DUMMY_JSON_STRING, result.getSpec().getSchema().value());
    }

    @Test
    void shouldNormalizeSchemaForJson() {
        // Given
        V1SchemaRegistrySubject subject = V1SchemaRegistrySubject.builder()
            .withSpec(V1SchemaRegistrySubjectSpec
                .builder()
                .withSchemaType(SchemaType.JSON)
                .withSchema(new SchemaHandle(DUMMY_JSON_STRING))
                .build())
            .build();
        var transform = new NormalizeSubjectSchemaTransformation();
        ExtensionContext extensionContext = Mockito.mock(ExtensionContext.class);
        Mockito.when(extensionContext.provider()).thenAnswer((Answer<SchemaRegistryExtensionProvider>) invocationOnMock -> {
            SchemaRegistryExtensionProvider provider = new SchemaRegistryExtensionProvider();
            provider.configure(Configuration.of(
                    SchemaRegistryExtensionProvider.Config.SCHEMA_REGISTRY_URL.key(), "http://localhost:2181"
                )
            );
            return provider;
        });
        transform.init(extensionContext);
        // When
        V1SchemaRegistrySubject result = transform.transform(subject, ResourceList.empty(), ReconciliationContext.Default.EMPTY)
            .orElse(null);

        // Then
        Assertions.assertNotNull(result);
        Assertions.assertEquals(CANONICAL_DUMMY_JSON_STRING, result.getSpec().getSchema().value());
    }
}