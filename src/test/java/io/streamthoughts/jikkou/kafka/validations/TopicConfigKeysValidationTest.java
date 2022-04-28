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
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.streamthoughts.jikkou.kafka.validations;

import io.streamthoughts.jikkou.kafka.model.V1TopicObject;
import io.streamthoughts.jikkou.kafka.resources.Configs;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TopicConfigKeysValidationTest {

    TopicConfigKeysValidation validation;

    @BeforeEach
    public void before() {
        validation = new TopicConfigKeysValidation();
    }

    @Test
    public void should_throw_exception_given_not_valid_topic_config_key() {
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            validation.validateTopic(
                    new V1TopicObject(
                            "test",
                            -1,
                            (short) -1,
                            Configs.of(Map.of("bad.key1", "???", "bad.key2", "???"))
                    )
            );
        });
        Assertions.assertEquals(2, exception.getErrors().size());

        exception.printStackTrace();
    }

    @Test
    public void should_not_throw_exception_given_valid_topic_config_key() {
        assertDoesNotThrow( () -> {
            validation.validateTopic(
                    new V1TopicObject(
                            "test",
                            -1,
                            (short) -1,
                            Configs.of(Map.of("retention.ms", "???"))
                    )
            );
        });
    }
}