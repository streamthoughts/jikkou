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

import io.streamthoughts.jikkou.core.resource.validation.ValidationResult;
import io.streamthoughts.jikkou.extension.aiven.models.V1SchemaRegistryAclEntry;
import io.streamthoughts.jikkou.extension.aiven.models.V1SchemaRegistryAclEntrySpec;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class SchemaRegistryAclEntryValidationTest {

    @Test
    void shouldNotReturnErrorForValidSubjectPattern() {
        // Given
        SchemaRegistryAclEntryValidation validation = new SchemaRegistryAclEntryValidation();

        // When
        ValidationResult result = validation.validate(V1SchemaRegistryAclEntry
                .builder()
                .withSpec(
                        V1SchemaRegistryAclEntrySpec
                                .builder()
                                .withResource("Subject:")
                                .build()
                )
                .build()
        );
        // Then
        Assertions.assertTrue(result.isValid());
    }

    @Test
    void shouldNotReturnErrorForValidConfigPattern() {
        // Given
        SchemaRegistryAclEntryValidation validation = new SchemaRegistryAclEntryValidation();

        // When
        ValidationResult result = validation.validate(V1SchemaRegistryAclEntry
                .builder()
                .withSpec(
                        V1SchemaRegistryAclEntrySpec
                                .builder()
                                .withResource("Config:")
                                .build()
                )
                .build()
        );
        // Then
        Assertions.assertTrue(result.isValid());
    }

    @Test
    void shouldNotReturnErrorForValidPatternIncludingNoSpecialCharacter() {
        // Given
        SchemaRegistryAclEntryValidation validation = new SchemaRegistryAclEntryValidation();

        // When
        ValidationResult result = validation.validate(V1SchemaRegistryAclEntry
                .builder()
                .withSpec(
                        V1SchemaRegistryAclEntrySpec
                                .builder()
                                .withResource("Subject:__thisIs_A_Test-topic.json-value")
                                .build()
                )
                .build()
        );
        // Then
        Assertions.assertTrue(result.isValid());
    }

    @Test
    void shouldNotReturnErrorForValidPatternIncludingGlobCharacter() {
        // Given
        SchemaRegistryAclEntryValidation validation = new SchemaRegistryAclEntryValidation();

        // When
        ValidationResult result = validation.validate(V1SchemaRegistryAclEntry
                .builder()
                .withSpec(
                        V1SchemaRegistryAclEntrySpec
                                .builder()
                                .withResource("Subject:__*-?-value")
                                .build()
                )
                .build()
        );
        // Then
        Assertions.assertTrue(result.isValid());
    }

    @Test
    void shouldReturnErrorForInvalidResourcePrefix() {
        // Given
        SchemaRegistryAclEntryValidation validation = new SchemaRegistryAclEntryValidation();

        // When
        ValidationResult result = validation.validate(V1SchemaRegistryAclEntry
                .builder()
                .withSpec(
                        V1SchemaRegistryAclEntrySpec
                                .builder()
                                .withResource("Invalid:")
                                .build()
                )
                .build()
        );
        // Then
        Assertions.assertFalse(result.isValid());

    }

    @Test
    void shouldReturnErrorForInvalidResourcePattern() {
        // Given
        SchemaRegistryAclEntryValidation validation = new SchemaRegistryAclEntryValidation();

        // When
        ValidationResult result = validation.validate(V1SchemaRegistryAclEntry
                .builder()
                .withSpec(
                        V1SchemaRegistryAclEntrySpec
                                .builder()
                                .withResource("<this is invalid>")
                                .build()
                )
                .build());
        // Then
        Assertions.assertFalse(result.isValid());
    }

}