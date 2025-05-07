/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.schema.registry.validation;

import io.streamthoughts.jikkou.core.data.SchemaHandle;
import io.streamthoughts.jikkou.core.data.SchemaType;
import io.streamthoughts.jikkou.core.extension.ExtensionContext;
import io.streamthoughts.jikkou.core.models.ObjectMeta;
import io.streamthoughts.jikkou.core.validation.ValidationError;
import io.streamthoughts.jikkou.core.validation.ValidationResult;
import io.streamthoughts.jikkou.schema.registry.models.V1SchemaRegistrySubject;
import io.streamthoughts.jikkou.schema.registry.models.V1SchemaRegistrySubjectSpec;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class AvroSchemaValidationTest {

    private static final String TEST_SCHEMA = """
            {
            "type":"record",
            "name":"ExampleRecord",
            "fields": [
               {
                "name":"field1",
                "type": ["null", "string"],
                "doc":"Field 1",
                "default": null
               },
               {
                "name":"field2",
                "type":"int"
               },
               {
                "name":"nestedRecord",
                "type": {
                  "type":"record",
                  "name":"NestedRecord",
                  "fields": [
                     {
                      "name":"nestedField1",
                      "type":"string",
                      "doc":"Nested Field 1"
                     },
                     {
                      "name":"nestedField2",
                      "type":"int"
                     }
                   ]
                 }
               }
             ]
            }
            """;

    @Test
    void shouldReturnErrorForMissingDocField() {
        // Given
        ExtensionContext context = Mockito.mock(ExtensionContext.class);
        Mockito.when(context.configuration()).thenReturn(AvroSchemaValidation.RECORD_FIELD_MUST_HAVE_DOC.asConfiguration(true));
        AvroSchemaValidation validation = new AvroSchemaValidation();
        validation.init(context);

        // When
        ValidationResult result = validation.validate(V1SchemaRegistrySubject
                .builder()
                .withMetadata(ObjectMeta
                        .builder()
                        .withName("test")
                        .build()
                )
                .withSpec(V1SchemaRegistrySubjectSpec
                        .builder()
                        .withSchemaType(SchemaType.AVRO)
                        .withSchema(new SchemaHandle(TEST_SCHEMA))
                        .build()
                )
                .build()
        );

        // Then
        List<ValidationError> exceptions = result.errors();
        Assertions.assertEquals(5, exceptions.size());
        Assertions.assertEquals("Invalid subject schema 'test'. Missing 'doc' field for record: 'ExampleRecord'", exceptions.get(0).message());
        Assertions.assertEquals("Invalid subject schema 'test'. Missing 'doc' field for field: 'field2'", exceptions.get(1).message());
        Assertions.assertEquals("Invalid subject schema 'test'. Missing 'doc' field for field: 'nestedRecord'", exceptions.get(2).message());
        Assertions.assertEquals("Invalid subject schema 'test'. Missing 'doc' field for record: 'NestedRecord'", exceptions.get(3).message());
        Assertions.assertEquals("Invalid subject schema 'test'. Missing 'doc' field for field: 'nestedRecord.nestedField2'", exceptions.get(4).message());
    }

    @Test
    void shouldReturnErrorForNonNullableFields() {
        // Given
        ExtensionContext context = Mockito.mock(ExtensionContext.class);
        Mockito.when(context.configuration()).thenReturn(AvroSchemaValidation.RECORD_FIELDS_MUST_BE_NULLABLE.asConfiguration(true));
        AvroSchemaValidation validation = new AvroSchemaValidation();
        validation.init(context);

        // When
        ValidationResult result = validation.validate(V1SchemaRegistrySubject
                .builder()
                .withMetadata(ObjectMeta
                        .builder()
                        .withName("test")
                        .build()
                )
                .withSpec(V1SchemaRegistrySubjectSpec
                        .builder()
                        .withSchemaType(SchemaType.AVRO)
                        .withSchema(new SchemaHandle(TEST_SCHEMA))
                        .build()
                )
                .build()
        );

        // Then
        List<ValidationError> errors = result.errors();
        Assertions.assertEquals(4, errors.size());
        Assertions.assertEquals("Invalid subject schema 'test'. Non-nullable field found: field2", errors.get(0).message());
        Assertions.assertEquals("Invalid subject schema 'test'. Non-nullable field found: nestedRecord", errors.get(1).message());
        Assertions.assertEquals("Invalid subject schema 'test'. Non-nullable field found: nestedRecord.nestedField1", errors.get(2).message());
        Assertions.assertEquals("Invalid subject schema 'test'. Non-nullable field found: nestedRecord.nestedField2", errors.get(3).message());
    }

    @Test
    void shouldReturnErrorForNonOptionalFields() {
        // Given
        ExtensionContext context = Mockito.mock(ExtensionContext.class);
        Mockito.when(context.configuration()).thenReturn(AvroSchemaValidation.RECORD_FIELDS_MUST_BE_OPTIONAL.asConfiguration(true));
        AvroSchemaValidation validation = new AvroSchemaValidation();
        validation.init(context);

        // When
        ValidationResult result = validation.validate(V1SchemaRegistrySubject
                .builder()
                .withMetadata(ObjectMeta
                        .builder()
                        .withName("test")
                        .build()
                )
                .withSpec(V1SchemaRegistrySubjectSpec
                        .builder()
                        .withSchemaType(SchemaType.AVRO)
                        .withSchema(new SchemaHandle(TEST_SCHEMA))
                        .build()
                )
                .build()
        );

        // Then
        List<ValidationError> errors = result.errors();
        Assertions.assertEquals(4, errors.size());
        Assertions.assertEquals("Invalid subject schema 'test'. Missing default value for field: field2", errors.get(0).message());
        Assertions.assertEquals("Invalid subject schema 'test'. Missing default value for field: nestedRecord", errors.get(1).message());
        Assertions.assertEquals("Invalid subject schema 'test'. Missing default value for field: nestedRecord.nestedField1", errors.get(2).message());
        Assertions.assertEquals("Invalid subject schema 'test'. Missing default value for field: nestedRecord.nestedField2", errors.get(3).message());
    }
}