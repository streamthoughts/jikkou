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
package io.streamthoughts.jikkou.extension.aiven.validation;

import static org.junit.jupiter.api.Assertions.*;

import io.streamthoughts.jikkou.api.error.ValidationException;
import io.streamthoughts.jikkou.extension.aiven.models.V1SchemaRegistryAclEntry;
import io.streamthoughts.jikkou.extension.aiven.models.V1SchemaRegistryAclEntrySpec;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class SchemaRegistryAclEntryValidationTest {

    @Test
    void shouldNotThrowExceptionForValidSubjectPattern() {
        // Given
        SchemaRegistryAclEntryValidation validation = new SchemaRegistryAclEntryValidation();

        // When - Then
        Assertions.assertDoesNotThrow(() -> validation.validate(V1SchemaRegistryAclEntry
                .builder()
                .withSpec(
                        V1SchemaRegistryAclEntrySpec
                                .builder()
                                .withResource("Subject:")
                                .build()
                )
                .build()
                )
        );
    }

    @Test
    void shouldNotThrowExceptionForValidConfigPattern() {
        // Given
        SchemaRegistryAclEntryValidation validation = new SchemaRegistryAclEntryValidation();

        // When - Then
        Assertions.assertDoesNotThrow(() -> validation.validate(V1SchemaRegistryAclEntry
                        .builder()
                        .withSpec(
                                V1SchemaRegistryAclEntrySpec
                                        .builder()
                                        .withResource("Config:")
                                        .build()
                        )
                        .build()
                )
        );
    }

    @Test
    void shouldThrowExceptionForInvalidResourcePrefix() {
        // Given
        SchemaRegistryAclEntryValidation validation = new SchemaRegistryAclEntryValidation();

        // When - Then
        Assertions.assertThrows(ValidationException.class, () -> validation.validate(V1SchemaRegistryAclEntry
                        .builder()
                        .withSpec(
                                V1SchemaRegistryAclEntrySpec
                                        .builder()
                                        .withResource("Invalid:")
                                        .build()
                        )
                        .build()
                )
        );
    }

    @Test
    void shouldThrowExceptionForInvalidResourcePattern() {
        // Given
        SchemaRegistryAclEntryValidation validation = new SchemaRegistryAclEntryValidation();

        // When - Then
        Assertions.assertThrows(ValidationException.class, () -> validation.validate(V1SchemaRegistryAclEntry
                        .builder()
                        .withSpec(
                                V1SchemaRegistryAclEntrySpec
                                        .builder()
                                        .withResource("<this is invalid>")
                                        .build()
                        )
                        .build()
                )
        );
    }

}