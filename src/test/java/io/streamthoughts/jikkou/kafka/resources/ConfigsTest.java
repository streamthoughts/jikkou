/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.streamthoughts.jikkou.kafka.resources;

import org.apache.kafka.common.config.TopicConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

public class ConfigsTest {

    private Configs defaultTopicConfigs;


    @BeforeEach
    public void setUp() {
        defaultTopicConfigs = new Configs();
        defaultTopicConfigs.add(new ConfigValue(TopicConfig.MIN_IN_SYNC_REPLICAS_CONFIG, "1"));
        defaultTopicConfigs.add(new ConfigValue(TopicConfig.UNCLEAN_LEADER_ELECTION_ENABLE_CONFIG, "false"));
        defaultTopicConfigs.add(new ConfigValue(TopicConfig.CLEANUP_POLICY_CONFIG, TopicConfig.CLEANUP_POLICY_DELETE));
    }

    @Test
    public void should_filter_on_non_equals_configs() {
        Configs configs = new Configs(defaultTopicConfigs.values());
        // override default config entry
        configs.add(new ConfigValue(TopicConfig.CLEANUP_POLICY_CONFIG, TopicConfig.CLEANUP_POLICY_COMPACT));
        // add a new config entry
        configs.add(new ConfigValue(TopicConfig.MAX_MESSAGE_BYTES_CONFIG, "10000"));

        Assertions.assertEquals(4, configs.size());
        Configs result = configs.filters(defaultTopicConfigs);

        Assertions.assertEquals(2, result.size());
        Assertions.assertNotNull(configs.get(TopicConfig.MAX_MESSAGE_BYTES_CONFIG));
        Assertions.assertNotNull(configs.get(TopicConfig.CLEANUP_POLICY_CONFIG));
    }

    @Test
    public void should_compare_two_configs() {
        final Configs config1 = new Configs(Set.of(new ConfigValue(TopicConfig.MAX_MESSAGE_BYTES_CONFIG, 1)));
        final Configs config2 = new Configs(Set.of(new ConfigValue(TopicConfig.MAX_MESSAGE_BYTES_CONFIG, 1)));
        Assertions.assertEquals(config1, config2);
    }

}