/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.kafka.connect.internals;

import io.streamthoughts.jikkou.kafka.connect.KafkaConnectConstants;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class KafkaConnectUtils {

    public static Map<String, Object> removeCommonConnectorConfig(Map<String, Object> config) {
        Map<String, Object> mutableConfig = new HashMap<>(config);
        mutableConfig.remove(KafkaConnectConstants.CONNECTOR_NAME_CONFIG);
        mutableConfig.remove(KafkaConnectConstants.CONNECTOR_CLASS_CONFIG);
        mutableConfig.remove(KafkaConnectConstants.CONNECTOR_TASKS_MAX_CONFIG);
        return Collections.unmodifiableMap(mutableConfig);
    }
}
