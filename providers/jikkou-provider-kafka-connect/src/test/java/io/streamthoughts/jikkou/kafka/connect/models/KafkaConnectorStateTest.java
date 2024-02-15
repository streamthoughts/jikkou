/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.kafka.connect.models;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class KafkaConnectorStateTest {

    @Test
    void shouldGetEnumFromValidString() {
        KafkaConnectorState value = KafkaConnectorState.fromValue("running");
        Assertions.assertEquals(KafkaConnectorState.RUNNING, value);
    }

    @Test
    void shouldThrowIllegalFromUnknownString() {
        Assertions.assertThrows(IllegalArgumentException.class, () ->  KafkaConnectorState.fromValue("dummy"));
    }

}