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

import io.streamthoughts.jikkou.api.config.Configuration;
import io.streamthoughts.jikkou.api.model.ObjectMeta;
import io.streamthoughts.jikkou.api.validation.ValidationError;
import io.streamthoughts.jikkou.api.validation.ValidationResult;
import io.streamthoughts.jikkou.schema.registry.model.CompatibilityLevels;
import io.streamthoughts.jikkou.schema.registry.models.V1SchemaRegistrySubject;
import io.streamthoughts.jikkou.schema.registry.models.V1SchemaRegistrySubjectSpec;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class CompatibilityLevelValidationTest {

    private static final Configuration CONFIGURATION = CompatibilityLevelValidation.VALIDATION_COMPATIBILITY_CONFIG
            .asConfiguration(List.of(CompatibilityLevels.FORWARD.name()));

    @Test
    void shouldReturnSuccessForAcceptedCompatibilityLevel() {
        // Given
        V1SchemaRegistrySubject subject = V1SchemaRegistrySubject
                .builder()
                .withMetadata(ObjectMeta.builder().withName("test").build())
                .withSpec(V1SchemaRegistrySubjectSpec
                        .builder()
                        .withCompatibilityLevel(CompatibilityLevels.FORWARD)
                        .build()
                )
                .build();

        CompatibilityLevelValidation validation = new CompatibilityLevelValidation();
        validation.configure(CONFIGURATION);

        // When
        ValidationResult result = validation.validate(subject);

        // Then
        Assertions.assertTrue(result.isValid());
    }

    @Test
    void shouldReturnFailureForRejectedCompatibilityLevel() {
        // Given
        V1SchemaRegistrySubject subject = V1SchemaRegistrySubject
                .builder()
                .withMetadata(ObjectMeta.builder().withName("test").build())
                .withSpec(V1SchemaRegistrySubjectSpec
                        .builder()
                        .withCompatibilityLevel(CompatibilityLevels.BACKWARD)
                        .build()
                )
                .build();

        CompatibilityLevelValidation validation = new CompatibilityLevelValidation();
        validation.configure(CONFIGURATION);

        // When
        ValidationResult result = validation.validate(subject);

        // Then
        Assertions.assertFalse(result.isValid());
        ValidationError error = result.errors().get(0);
        Assertions.assertEquals("Compatibility level 'BACKWARD' is not accepted for SchemaRegistrySubject 'test'. Must be one of: [FORWARD]", error.message());
    }
}