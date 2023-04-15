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
import io.streamthoughts.jikkou.api.model.ObjectMeta;
import io.streamthoughts.jikkou.kafka.internals.KafkaTopics;
import io.streamthoughts.jikkou.kafka.models.V1KafkaTopic;
import io.streamthoughts.jikkou.kafka.models.V1KafkaTopicSpec;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TopicNameRegexValidationTest {

    TopicNameRegexValidation validation;

    @BeforeEach
    void before() {
        validation = new TopicNameRegexValidation();
    }

    @Test
    void shouldThrowExceptionForMissingConfig() {
        Assertions.assertThrows(ConfigException.class, () -> validation.configure(Configuration.empty()));
    }

    @Test
    void shouldThrowExceptionForInvalidRegex() {
        Assertions.assertThrows(ConfigException.class, () -> {
            new TopicNameRegexValidation("");
        });
    }

    @Test
    void shouldThrowExceptionForTopicNameNotMatching() {
        Assertions.assertThrows(ValidationException.class, () -> {
            var validation = new TopicNameRegexValidation("test-");
            var topic = V1KafkaTopic.builder()
                    .withMetadata(ObjectMeta
                            .builder()
                            .withName("dummy")
                            .build()
                    )
                    .withSpec(V1KafkaTopicSpec.builder()
                            .withPartitions(KafkaTopics.NO_NUM_PARTITIONS)
                            .withReplicas(KafkaTopics.NO_REPLICATION_FACTOR)
                            .build()
                    )
                    .build();
            validation.validate(topic);
        });
    }
}