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
import io.streamthoughts.jikkou.kafka.models.V1KafkaAccessRoleObject;
import io.streamthoughts.jikkou.kafka.models.V1KafkaAuthorizationList;
import io.streamthoughts.jikkou.kafka.models.V1KafkaAuthorizationSpec;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class NoDuplicateRolesAllowedValidationTest {

    private static final V1KafkaAccessRoleObject TEST_ROLE = new V1KafkaAccessRoleObject("role", Collections.emptyList());

    private final NoDuplicateRolesAllowedValidation validation = new NoDuplicateRolesAllowedValidation();

    @Test
    public void should_throw_validation_exception_given_duplicate() {

        var roles = new ArrayList<V1KafkaAccessRoleObject>();
        roles.add(TEST_ROLE);
        roles.add(TEST_ROLE);
        Assertions.assertThrows(ValidationException.class, () -> {
            V1KafkaAuthorizationList resource = V1KafkaAuthorizationList
                    .builder()
                    .withSpec(V1KafkaAuthorizationSpec.builder()
                            .withRoles(roles)
                            .build()
                    )
                    .build();
            validation.validate(resource);
        });
    }

    @Test
    public void should_not_throw_validation_exception_given_duplicate() {
        Assertions.assertDoesNotThrow(() -> {
            V1KafkaAuthorizationList resource = V1KafkaAuthorizationList
                    .builder()
                    .withSpec(V1KafkaAuthorizationSpec.builder()
                            .withRoles(List.of(TEST_ROLE))
                            .build()
                    )
                    .build();
            validation.validate(resource);
        });
    }
}