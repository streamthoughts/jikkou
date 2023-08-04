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
package io.streamthoughts.jikkou.api.model;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ConfigsTest {

    @Test
    void should_create_empty_config_given_null_map() {
        Configs configs = Configs.of(null);
        Assertions.assertEquals(0, configs.size());
    }

    @Test
    void should_create_config_given_non_empty_map() {
        Configs configs = Configs.of(Map.of(
                "k1", "v1",
                "k2", "v2",
                "k3", "v3"
        ));

        int expectedSize = 3;
        Assertions.assertEquals(expectedSize, configs.size());
        IntStream.rangeClosed(1, expectedSize).forEach(idx -> {
            Assertions.assertEquals("v" + idx, configs.get("k" + idx).value());
        });
    }

    @Test
    void should_add_new_value_given_non_empty_config() {
        Configs configs = Configs.of(Map.of("k1", "v1"));
        Assertions.assertEquals(1, configs.size());
        configs.add(new ConfigValue("k2", "v2"));
        Assertions.assertEquals(2, configs.size());
    }

    @Test
    void should_filter_all_values_not_given_configs() {
        // Given
        Configs configsIn = Configs.of(IntStream
                .rangeClosed(1, 4)
                .boxed()
                .collect(Collectors.toMap(idx -> "k" + idx, idx -> idx)));

        Configs configsOut = Configs.of(IntStream
                .rangeClosed(1, 4)
                .filter(idx -> idx % 2 == 0)
                .boxed()
                .collect(Collectors.toMap(idx -> "k" + idx, idx -> idx)));

        // When
        Configs result = configsIn.filterAllNotContainedIn(configsOut);

        // Then
        Assertions.assertEquals(2, result.size());
        IntStream
                .rangeClosed(1, 4)
                .filter(idx -> idx % 2 != 0)
                .boxed()
                .forEach(idx -> Assertions.assertEquals(idx, result.get("k" + idx).value()));
    }
}