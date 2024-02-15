/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.kafka.validation;

import io.streamthoughts.jikkou.core.models.ObjectMeta;
import io.streamthoughts.jikkou.core.validation.ValidationResult;
import io.streamthoughts.jikkou.kafka.models.V1KafkaPrincipalAuthorization;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class NoDuplicatePrincipalAllowedValidationTest {

    private final NoDuplicatePrincipalAllowedValidation validation = new NoDuplicatePrincipalAllowedValidation();

    @Test
    void shouldReturnErrorGivenDuplicates() {
        // Given
        var resources = List.of(
                V1KafkaPrincipalAuthorization.builder().withMetadata(ObjectMeta.builder().withName("foo").build()).build(),
                V1KafkaPrincipalAuthorization.builder().withMetadata(ObjectMeta.builder().withName("foo").build()).build()
        );
        // When
        ValidationResult result = validation.validate(resources);
        // Then
        Assertions.assertFalse(result.isValid());

    }

    @Test
    void shouldNotReturnErrorGivenNoDuplicate() {
        // Given
        var resources = List.of(
                V1KafkaPrincipalAuthorization.builder().withMetadata(ObjectMeta.builder().withName("foo").build()).build(),
                V1KafkaPrincipalAuthorization.builder().withMetadata(ObjectMeta.builder().withName("bar").build()).build()
        );
        // Then
        ValidationResult result = validation.validate(resources);
        // Then
        Assertions.assertTrue(result.isValid());
    }

}