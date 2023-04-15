/*
 * Copyright 2021 StreamThoughts.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.streamthoughts.jikkou.kafka.validations;

import io.streamthoughts.jikkou.api.error.ValidationException;
import io.streamthoughts.jikkou.api.model.ObjectMeta;
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
    void shouldThrowExceptionDuplicate() {
        Assertions.assertThrows(ValidationException.class, () -> validation.validate(List.of(TEST_ROLE, TEST_ROLE)));
    }

    @Test
    void shouldNotThrowExceptionDuplicate() {
        Assertions.assertDoesNotThrow(() -> validation.validate(List.of(TEST_ROLE)));
    }
}