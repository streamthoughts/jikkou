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
package io.streamthoughts.jikkou.api.resources;

import io.streamthoughts.jikkou.api.model.ConfigValue;
import io.streamthoughts.jikkou.api.model.Configs;
import java.util.Set;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ConfigsTest {

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

}