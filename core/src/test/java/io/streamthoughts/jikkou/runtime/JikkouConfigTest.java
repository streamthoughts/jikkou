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
package io.streamthoughts.jikkou.runtime;

import java.util.Collections;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class JikkouConfigTest {

    @Test
    void shouldLoadEmptyConfigurationByDefault() {
        // Given
        JikkouConfig config = JikkouConfig.load();
        // Then
        Assertions.assertNotNull(config);
        Assertions.assertEquals(Collections.emptySet(), config.keys());
    }

    @Test
    void shouldCreateEmptyConfiguration() {
        // Given
        JikkouConfig config = JikkouConfig.empty();
        // Then
        Assertions.assertNotNull(config);
        Assertions.assertEquals(Collections.emptySet(), config.keys());
    }

    @Test
    void shouldCreateConfigurationFromMap() {
        // Given
        Map<String, String> map = Map.of("prop1", "value1", "prop2", "value2");
        // When
        JikkouConfig config = JikkouConfig.create(map, false);
        // Then
        Assertions.assertNotNull(config);
        Assertions.assertEquals(map, config.asMap());
    }
}