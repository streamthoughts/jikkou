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
package io.streamthoughts.jikkou.core.models;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ConfigsTest {

    private Configs defaultTopicConfigs;

    @BeforeEach
    public void setUp() {
        defaultTopicConfigs = new Configs();
        defaultTopicConfigs.add(new ConfigValue("key1", "1"));
        defaultTopicConfigs.add(new ConfigValue("key2", "false"));
        defaultTopicConfigs.add(new ConfigValue("key3", "foo"));
    }

    @Test
    public void should_filter_on_non_equals_configs() {
        Configs configs = new Configs(defaultTopicConfigs.values());
        // override default config entry
        configs.add(new ConfigValue("key3", "bar"));
        // add a new config entry
        configs.add(new ConfigValue("key4", "10000"));

        Assertions.assertEquals(4, configs.size());
        Configs result = configs.filterAllNotContainedIn(defaultTopicConfigs);

        Assertions.assertEquals(2, result.size());
        Assertions.assertNotNull(configs.get("key4"));
        Assertions.assertNotNull(configs.get("key3"));
    }

    @Test
    public void should_compare_two_configs() {
        final Configs config1 = new Configs(Set.of(new ConfigValue("key", 1)));
        final Configs config2 = new Configs(Set.of(new ConfigValue("key", 1)));
        Assertions.assertEquals(config1, config2);
    }

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