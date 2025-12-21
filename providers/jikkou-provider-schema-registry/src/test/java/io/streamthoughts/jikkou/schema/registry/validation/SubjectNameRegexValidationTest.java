/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.schema.registry.validation;

import static io.streamthoughts.jikkou.schema.registry.validation.SubjectNameRegexValidation.VALIDATION_SUBJECT_NAME_REGEX_CONFIG;

import io.streamthoughts.jikkou.core.config.Configuration;
import io.streamthoughts.jikkou.core.exceptions.ConfigException;
import io.streamthoughts.jikkou.core.extension.ExtensionContext;
import io.streamthoughts.jikkou.core.models.ObjectMeta;
import io.streamthoughts.jikkou.core.validation.ValidationResult;
import io.streamthoughts.jikkou.schema.registry.models.V1SchemaRegistrySubject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class SubjectNameRegexValidationTest {

    SubjectNameRegexValidation validation;

    @BeforeEach
    void before() {
        validation = new SubjectNameRegexValidation();
    }

    @Test
    void shouldThrowExceptionForMissingConfig() {
        ExtensionContext context = Mockito.mock(ExtensionContext.class);
        Mockito.when(context.configuration()).thenReturn(Configuration.empty());
        Assertions.assertThrows(ConfigException.class, () -> validation.init(context));
    }

    @Test
    void shouldThrowExceptionForInvalidRegex() {
        // Given
        ExtensionContext context = Mockito.mock(ExtensionContext.class);
        Mockito.when(context.configuration()).thenReturn(VALIDATION_SUBJECT_NAME_REGEX_CONFIG.asConfiguration(""));

        // When
        Assertions.assertThrows(ConfigException.class, () -> new SubjectNameRegexValidation().init(context));
    }

    @Test
    void shouldNotThrowExceptionForSubjectNameNotMatching() {
        // Given
        ExtensionContext context = Mockito.mock(ExtensionContext.class);
        Mockito.when(context.configuration()).thenReturn(VALIDATION_SUBJECT_NAME_REGEX_CONFIG.asConfiguration("(test)(|-(key|value))"));

        var validation = new SubjectNameRegexValidation();
        validation.init(context);

        new SubjectNameRegexValidation();
        var subject = V1SchemaRegistrySubject.builder()
                .withMetadata(ObjectMeta
                        .builder()
                        .withName("test-key")
                        .build()
                )
                .build();

        // When
        ValidationResult result = validation.validate(subject);

        // Then
        Assertions.assertTrue(result.isValid());
    }
}
