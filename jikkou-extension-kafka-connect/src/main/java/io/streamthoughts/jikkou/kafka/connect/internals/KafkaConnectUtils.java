/*
 * Copyright 2023 The original authors
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
package io.streamthoughts.jikkou.kafka.connect.internals;

import io.streamthoughts.jikkou.api.model.Configs;
import io.streamthoughts.jikkou.kafka.connect.KafkaConnectConstants;
import java.util.HashMap;
import java.util.Map;

public final class KafkaConnectUtils {

    public static Configs removeCommonConnectorConfig(Configs config) {
        return removeCommonConnectorConfig(config.toMap());
    }
    public static Configs removeCommonConnectorConfig(Map<String, Object> config) {
        Map<String, Object> mutableConfig = new HashMap<>(config);
        mutableConfig.remove(KafkaConnectConstants.CONNECTOR_NAME_CONFIG);
        mutableConfig.remove(KafkaConnectConstants.CONNECTOR_CLASS_CONFIG);
        mutableConfig.remove(KafkaConnectConstants.CONNECTOR_TASKS_MAX_CONFIG);
        return Configs.of(mutableConfig);
    }
}
