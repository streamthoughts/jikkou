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
package io.streamthoughts.jikkou.client;

import io.streamthoughts.jikkou.common.utils.PropertiesUtils;
import io.streamthoughts.jikkou.core.config.ConfigProperty;
import io.streamthoughts.jikkou.kafka.internals.KafkaUtils;
import io.streamthoughts.jikkou.runtime.JikkouConfig;
import java.util.HashMap;
import java.util.Properties;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class JikkouConfigTest {

    public static final ConfigProperty<Properties> ADMIN_CLIENT_CONFIG = ConfigProperty
            .ofMap("kafka.client")
            .orElse(HashMap::new)
            .map(KafkaUtils::getAdminClientConfigs)
            .map(PropertiesUtils::fromMap);

    @Test
    void shouldLoadReferenceConfigFile() {
        // Given
        JikkouConfig config = JikkouConfig.load();

        // When
        Properties properties = ADMIN_CLIENT_CONFIG.get(config);

        // Then
        Assertions.assertNotNull(properties);
        Assertions.assertEquals(1, properties.size(), "props: " + properties);
        Assertions.assertEquals("localhost:9092", properties.get("bootstrap.servers"));
    }
}