/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.extension.aiven.validation;

import io.streamthoughts.jikkou.core.validation.ValidationResult;
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