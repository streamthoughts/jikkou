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
package io.streamthoughts.kafka.specs.resources;

import org.apache.kafka.common.config.TopicConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TopicResourceTest {

    private static final String TOPIC_TEST          = "test";
    private static final short DEFAULT_REPLICATION_FACTOR = 1;

    private TopicResource defaultTopic;

    @BeforeEach
    public void setUp() {
        Configs defaultTopicConfigs = new Configs();
        defaultTopicConfigs.add(new ConfigValue(TopicConfig.MIN_IN_SYNC_REPLICAS_CONFIG, "1",  true));
        defaultTopicConfigs.add(new ConfigValue(TopicConfig.UNCLEAN_LEADER_ELECTION_ENABLE_CONFIG, "false",  true));
        defaultTopicConfigs.add(new ConfigValue(TopicConfig.CLEANUP_POLICY_COMPACT, TopicConfig.CLEANUP_POLICY_DELETE,  true));

        this.defaultTopic = new TopicResource(TOPIC_TEST, 1, DEFAULT_REPLICATION_FACTOR, defaultTopicConfigs);
    }

   @Test
   public void should_not_detect_configs_changes_given_topic_resource_with_different_partitions() {
       TopicResource resource = new TopicResource(TOPIC_TEST, 10, DEFAULT_REPLICATION_FACTOR, Configs.empty());
       Assertions.assertFalse(resource.containsConfigsChanges(defaultTopic));
   }
}