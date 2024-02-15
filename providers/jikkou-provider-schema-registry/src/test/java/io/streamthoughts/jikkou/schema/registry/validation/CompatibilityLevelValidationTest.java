/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.schema.registry.validation;

import io.streamthoughts.jikkou.core.config.Configuration;
import io.streamthoughts.jikkou.core.extension.ExtensionContext;
import io.streamthoughts.jikkou.core.models.ObjectMeta;
import io.streamthoughts.jikkou.core.validation.ValidationError;
import io.streamthoughts.jikkou.core.validation.ValidationResult;
import io.streamthoughts.jikkou.schema.registry.model.CompatibilityLevels;
import io.streamthoughts.jikkou.schema.registry.models.V1SchemaRegistrySubject;
import io.streamthoughts.jikkou.schema.registry.models.V1SchemaRegistrySubjectSpec;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class CompatibilityLevelValidationTest {

    private static final Configuration CONFIGURATION = CompatibilityLevelValidation.VALIDATION_COMPATIBILITY_CONFIG
            .asConfiguration(List.of(CompatibilityLevels.FORWARD.name()));

    private CompatibilityLevelValidation validation;

    @BeforeEach
    public void beforeEach() {
        ExtensionContext context = Mockito.mock(ExtensionContext.class);
        Mockito.when(context.appConfiguration()).thenReturn(CONFIGURATION);
        validation = new CompatibilityLevelValidation();
        validation.init(context);

    }

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

        // When
        ValidationResult result = validation.validate(subject);

        // Then
        Assertions.assertFalse(result.isValid());
        ValidationError error = result.errors().get(0);
        Assertions.assertEquals("Compatibility level 'BACKWARD' is not accepted for SchemaRegistrySubject 'test'. Must be one of: [FORWARD]", error.message());
    }
}