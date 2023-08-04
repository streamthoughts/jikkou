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

import static org.junit.jupiter.api.Assertions.*;

import io.streamthoughts.jikkou.api.error.ValidationException;
import io.streamthoughts.jikkou.api.model.ObjectMeta;
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
    void shouldThrowExceptionForMissingDocField() {
        // Given
        AvroSchemaValidation validation = new AvroSchemaValidation();
        validation.configure(AvroSchemaValidation.RECORD_FIELD_MUST_HAVE_DOC.asConfiguration(true));

        // When
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            validation.validate(V1SchemaRegistrySubject
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
        });

        // Then
        List<ValidationException> exceptions = exception.getExceptions();
        Assertions.assertEquals(5, exceptions.size());
        Assertions.assertEquals("Invalid subject schema 'test'. Missing 'doc' field for record: 'ExampleRecord'", exceptions.get(0).getMessage());
        Assertions.assertEquals("Invalid subject schema 'test'. Missing 'doc' field for field: 'field2'", exceptions.get(1).getMessage());
        Assertions.assertEquals("Invalid subject schema 'test'. Missing 'doc' field for field: 'nestedRecord'", exceptions.get(2).getMessage());
        Assertions.assertEquals("Invalid subject schema 'test'. Missing 'doc' field for record: 'NestedRecord'", exceptions.get(3).getMessage());
        Assertions.assertEquals("Invalid subject schema 'test'. Missing 'doc' field for field: 'nestedRecord.nestedField2'", exceptions.get(4).getMessage());
    }

    @Test
    void shouldThrowExceptionForNonNullableFields() {
        // Given
        AvroSchemaValidation validation = new AvroSchemaValidation();
        validation.configure(AvroSchemaValidation.RECORD_FIELDS_MUST_BE_NULLABLE.asConfiguration(true));

        // When
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            validation.validate(V1SchemaRegistrySubject
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
        });

        // Then
        List<ValidationException> exceptions = exception.getExceptions();
        Assertions.assertEquals(4, exceptions.size());
        Assertions.assertEquals("Invalid subject schema 'test'. Non-nullable field found: field2", exceptions.get(0).getMessage());
        Assertions.assertEquals("Invalid subject schema 'test'. Non-nullable field found: nestedRecord", exceptions.get(1).getMessage());
        Assertions.assertEquals("Invalid subject schema 'test'. Non-nullable field found: nestedRecord.nestedField1", exceptions.get(2).getMessage());
        Assertions.assertEquals("Invalid subject schema 'test'. Non-nullable field found: nestedRecord.nestedField2", exceptions.get(3).getMessage());
    }

    @Test
    void shouldThrowExceptionForNonOptionalFields() {
        // Given
        AvroSchemaValidation validation = new AvroSchemaValidation();
        validation.configure(AvroSchemaValidation.RECORD_FIELDS_MUST_BE_OPTIONAL.asConfiguration(true));

        // When
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            validation.validate(V1SchemaRegistrySubject
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
        });

        // Then
        List<ValidationException> exceptions = exception.getExceptions();
        Assertions.assertEquals(4, exceptions.size());
        Assertions.assertEquals("Invalid subject schema 'test'. Missing default value for field: field2", exceptions.get(0).getMessage());
        Assertions.assertEquals("Invalid subject schema 'test'. Missing default value for field: nestedRecord", exceptions.get(1).getMessage());
        Assertions.assertEquals("Invalid subject schema 'test'. Missing default value for field: nestedRecord.nestedField1", exceptions.get(2).getMessage());
        Assertions.assertEquals("Invalid subject schema 'test'. Missing default value for field: nestedRecord.nestedField2", exceptions.get(3).getMessage());
    }
}