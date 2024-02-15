/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.models;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ResourceConverterTest {

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