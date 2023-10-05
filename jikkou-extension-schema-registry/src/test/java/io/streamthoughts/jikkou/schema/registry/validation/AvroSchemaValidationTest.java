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
package io.streamthoughts.jikkou.schema.registry.validation;

import io.streamthoughts.jikkou.api.model.ObjectMeta;
import io.streamthoughts.jikkou.api.validation.ValidationError;
import io.streamthoughts.jikkou.api.validation.ValidationResult;
import io.streamthoughts.jikkou.schema.registry.model.SchemaHandle;
import io.streamthoughts.jikkou.schema.registry.model.SchemaType;
import io.streamthoughts.jikkou.schema.registry.models.V1SchemaRegistrySubject;
import io.streamthoughts.jikkou.schema.registry.models.V1SchemaRegistrySubjectSpec;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

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
        AvroSchemaValidation validation = new AvroSchemaValidation();
        validation.configure(AvroSchemaValidation.RECORD_FIELD_MUST_HAVE_DOC.asConfiguration(true));

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
        AvroSchemaValidation validation = new AvroSchemaValidation();
        validation.configure(AvroSchemaValidation.RECORD_FIELDS_MUST_BE_NULLABLE.asConfiguration(true));

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
        AvroSchemaValidation validation = new AvroSchemaValidation();
        validation.configure(AvroSchemaValidation.RECORD_FIELDS_MUST_BE_OPTIONAL.asConfiguration(true));

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