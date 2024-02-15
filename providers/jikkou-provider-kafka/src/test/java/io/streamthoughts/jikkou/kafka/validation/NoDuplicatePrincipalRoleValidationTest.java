/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.kafka.validation;

import io.streamthoughts.jikkou.core.models.ObjectMeta;
import io.streamthoughts.jikkou.core.validation.ValidationResult;
import io.streamthoughts.jikkou.kafka.models.V1KafkaPrincipalRole;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class NoDuplicatePrincipalRoleValidationTest {

    public static final V1KafkaPrincipalRole TEST_ROLE = V1KafkaPrincipalRole
            .builder()
            .withMetadata(ObjectMeta
                    .builder()
                    .withName("topic")
                    .build())
            .build();

    private final NoDuplicatePrincipalRoleValidation validation = new NoDuplicatePrincipalRoleValidation();

    @Test
    void shouldReturnErrorGivenDuplicates() {
        // When
        ValidationResult result = validation.validate(List.of(TEST_ROLE, TEST_ROLE));
        // Then
        Assertions.assertFalse(result.isValid());
    }

    @Test
    void shouldNotReturnErrorGivenNoDuplicate() {
        // When
        ValidationResult result = validation.validate(List.of(TEST_ROLE));
        // Then
        Assertions.assertTrue(result.isValid());
    }
}