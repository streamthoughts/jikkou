/*
 * Copyright 2022 The original authors
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
package io.streamthoughts.jikkou.core.models;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ResourceTypeTest {

    @Test
    void should_create_given_kind_only() {
        ResourceType type = ResourceType.of("KafkaTopicList", null);
        Assertions.assertEquals("KafkaTopicList", type.kind());
    }

    @Test
    void should_create_given_kind_and_version() {
        ResourceType type = ResourceType.of("KafkaTopicList", "kafka.jikkou.io/v1beta2");
        Assertions.assertEquals("KafkaTopicList", type.kind());
        Assertions.assertEquals("v1beta2", type.apiVersion());
        Assertions.assertEquals("kafka.jikkou.io", type.group());
    }
}