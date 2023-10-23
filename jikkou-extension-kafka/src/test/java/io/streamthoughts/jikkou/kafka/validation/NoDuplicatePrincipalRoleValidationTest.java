/*
 * Copyright 2021 The original authors
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
package io.streamthoughts.jikkou.kafka.validation;

import io.streamthoughts.jikkou.core.models.ObjectMeta;
import io.streamthoughts.jikkou.core.resource.validation.ValidationResult;
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