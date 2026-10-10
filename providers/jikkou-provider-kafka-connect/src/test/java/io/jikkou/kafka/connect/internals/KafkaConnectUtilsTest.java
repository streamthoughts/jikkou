/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.jikkou.kafka.connect.internals;

import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class KafkaConnectUtilsTest {

    @Test
    void shouldRemoveCommonAndInternalConnectorConfig() {
        Map<String, Object> config = Map.of(
            "name", "test",
            "connector.class", "FileStreamSink",
            "tasks.max", "1",
            "__internal.config.version", "12",
            "topics", "connect-test"
        );

        Map<String, Object> result = KafkaConnectUtils.removeCommonConnectorConfig(config);

        Assertions.assertEquals(Map.of("topics", "connect-test"), result);
    }
}
