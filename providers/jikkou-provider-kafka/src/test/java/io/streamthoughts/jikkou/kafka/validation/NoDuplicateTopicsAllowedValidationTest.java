/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.kafka.validation;

import io.streamthoughts.jikkou.core.models.ObjectMeta;
import io.streamthoughts.jikkou.core.validation.ValidationResult;
import io.streamthoughts.jikkou.kafka.models.V1KafkaTopic;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class NoDuplicateTopicsAllowedValidationTest {

    public static final V1KafkaTopic TEST_TOPIC = V1KafkaTopic
            .builder()
            .withMetadata(ObjectMeta
                    .builder()
                    .withName("topic")
                    .build())
            .build();
    private final NoDuplicateTopicsAllowedValidation validation = new NoDuplicateTopicsAllowedValidation();

    @Test
    void shouldThrowExceptionForDuplicate() {
        // When
        ValidationResult result = validation.validate(List.of(TEST_TOPIC, TEST_TOPIC));
        // Then
        Assertions.assertFalse(result.isValid());
    }

    @Test
    void shouldNotReturnErrorForDuplicate() {
        // When
        ValidationResult result = validation.validate(List.of(TEST_TOPIC));
        // Then
        Assertions.assertTrue(result.isValid());
    }
}