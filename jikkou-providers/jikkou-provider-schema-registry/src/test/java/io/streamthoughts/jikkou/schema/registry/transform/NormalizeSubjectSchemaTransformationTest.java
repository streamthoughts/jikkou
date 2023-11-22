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
package io.streamthoughts.jikkou.schema.registry.transform;

import io.streamthoughts.jikkou.core.ReconciliationContext;
import io.streamthoughts.jikkou.core.models.DefaultResourceListObject;
import io.streamthoughts.jikkou.schema.registry.model.SchemaHandle;
import io.streamthoughts.jikkou.schema.registry.model.SchemaType;
import io.streamthoughts.jikkou.schema.registry.models.V1SchemaRegistrySubject;
import io.streamthoughts.jikkou.schema.registry.models.V1SchemaRegistrySubjectSpec;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

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
    void shouldNormalizeSchemaForAvro() {
        // Given
        V1SchemaRegistrySubject subject = V1SchemaRegistrySubject.builder()
                .withSpec(V1SchemaRegistrySubjectSpec
                        .builder()
                        .withSchemaType(SchemaType.AVRO)
                        .withSchema(new SchemaHandle(DUMMY_JSON_STRING))
                        .build())
                .build();
        // When
        var transform = new NormalizeSubjectSchemaTransformation();
        V1SchemaRegistrySubject result = transform.transform(subject, DefaultResourceListObject.empty(), ReconciliationContext.Default.EMPTY)
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
        // When
        var transform = new NormalizeSubjectSchemaTransformation();
        V1SchemaRegistrySubject result = transform.transform(subject, DefaultResourceListObject.empty(), ReconciliationContext.Default.EMPTY)
                .orElse(null);

        // Then
        Assertions.assertNotNull(result);
        Assertions.assertEquals(CANONICAL_DUMMY_JSON_STRING, result.getSpec().getSchema().value());
    }
}