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
import io.streamthoughts.jikkou.kafka.models.V1KafkaTopicList;
import io.streamthoughts.jikkou.kafka.models.V1KafkaTopicObject;
import io.streamthoughts.jikkou.kafka.models.V1KafkaTopicSpec;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class NoDuplicateTopicsAllowedValidationTest {

    public static final V1KafkaTopicObject TEST_TOPIC = V1KafkaTopicObject
            .builder()
            .withName("test-topic")
            .build();
    private final NoDuplicateTopicsAllowedValidation validation = new NoDuplicateTopicsAllowedValidation();

    @Test
    public void should_throw_validation_exception_given_duplicate() {
        List<V1KafkaTopicObject> testTopic = List.of(TEST_TOPIC, TEST_TOPIC);
        Assertions.assertThrows(ValidationException.class, () -> {
            var resource = V1KafkaTopicList.builder()
                    .withSpec(V1KafkaTopicSpec.builder()
                            .withTopics(testTopic)
                            .build()
                    )
                    .build();
            validation.validate(resource);
        });
    }

    @Test
    public void should_not_throw_validation_exception_duplicate() {
        Assertions.assertDoesNotThrow(() -> {
            var resource = V1KafkaTopicList.builder()
                    .withSpec(V1KafkaTopicSpec.builder()
                            .withTopics(List.of(TEST_TOPIC))
                            .build()
                    )
                    .build();
            validation.validate(resource);
        });
    }
}