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
package io.streamthoughts.kafka.specs.validations;

import io.streamthoughts.kafka.specs.model.V1SpecsObject;
import io.streamthoughts.kafka.specs.model.V1TopicObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

public class NoDuplicateTopicsAllowedValidationTest {

    public static final V1TopicObject TEST_TOPIC = new V1TopicObject("test");
    private final NoDuplicateTopicsAllowedValidation validation = new NoDuplicateTopicsAllowedValidation();

    @Test
    public void should_throw_validation_exception_given_duplicate() {
        Assertions.assertThrows(ValidationException.class, () -> {
            validation.validate(new V1SpecsObject().topics(List.of(TEST_TOPIC, TEST_TOPIC)));
        });
    }

    @Test
    public void should_not_throw_validation_exception_given_duplicate() {
        Assertions.assertThrows(ValidationException.class, () -> {
            validation.validate(new V1SpecsObject().topics(List.of(TEST_TOPIC, TEST_TOPIC)));
        });
    }
}