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

import io.streamthoughts.jikkou.api.config.Configuration;
import io.streamthoughts.jikkou.api.error.ConfigException;
import io.streamthoughts.jikkou.api.error.ValidationException;
import io.streamthoughts.jikkou.kafka.internals.KafkaConstants;
import io.streamthoughts.jikkou.kafka.models.V1KafkaTopicObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TopicNameRegexValidationTest {

    TopicNameRegexValidation validation;

    @BeforeEach
    public void before() {
        validation = new TopicNameRegexValidation();
    }

    @Test
    public void should_throw_exception_when_no_pattern_is_configured() {
        Assertions.assertThrows(ConfigException.class, () -> validation.configure(Configuration.empty()));
    }

    @Test
    public void should_throw_exception_when_invalid_pattern_is_configured() {
        Assertions.assertThrows(ConfigException.class, () -> {
            new TopicNameRegexValidation("");
        });
    }

    @Test
    public void should_throw_exception_given_topic_not_matching() {
        Assertions.assertThrows(ValidationException.class, () -> {
            var validation = new TopicNameRegexValidation("test-");

            var topic = V1KafkaTopicObject.builder()
                    .withName("dummy")
                    .withPartitions(KafkaConstants.NO_NUM_PARTITIONS)
                    .withReplicationFactor(KafkaConstants.NO_REPLICATION_FACTOR)
                    .build();
            validation.validateTopic(topic);
        });
    }

}