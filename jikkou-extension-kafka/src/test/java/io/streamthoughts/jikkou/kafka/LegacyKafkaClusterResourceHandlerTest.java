/*
 * Copyright 2022 StreamThoughts.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.streamthoughts.jikkou.kafka;

import static org.apache.kafka.common.config.TopicConfig.CLEANUP_POLICY_COMPACT;
import static org.apache.kafka.common.config.TopicConfig.CLEANUP_POLICY_CONFIG;

import io.streamthoughts.jikkou.api.model.Configs;
import io.streamthoughts.jikkou.api.model.HasMetadata;
import io.streamthoughts.jikkou.api.model.ResourceList;
import io.streamthoughts.jikkou.kafka.models.V1KafkaCluster;
import io.streamthoughts.jikkou.kafka.models.V1KafkaClusterSpec;
import io.streamthoughts.jikkou.kafka.models.V1KafkaTopicList;
import io.streamthoughts.jikkou.kafka.models.V1KafkaTopicObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class LegacyKafkaClusterResourceHandlerTest {

    private final LegacyKafkaClusterResourceHandler handler = new LegacyKafkaClusterResourceHandler();

    @Test
    void should_handle_legacy_kafka_resource_given_topics() {
        ResourceList resourceList = handler.handle(ResourceList.of(new V1KafkaCluster().toBuilder()
                .withSpec(V1KafkaClusterSpec
                        .builder()
                        .withTopic(V1KafkaTopicObject
                                .builder()
                                .withName("TOPIC-TEST")
                                .withPartitions(4)
                                .withReplicationFactor((short) 1)
                                .withConfigs(Configs.of(CLEANUP_POLICY_CONFIG, CLEANUP_POLICY_COMPACT))
                                .build()
                        )
                        .build()
                )
                .build()
        ));
        Assertions.assertNotNull(resourceList);
        Assertions.assertEquals(1, resourceList.size());
        HasMetadata resource = resourceList.allResourcesForKinds(HasMetadata.getKind(V1KafkaTopicList.class))
                .first();
        Assertions.assertNotNull(resource);
        V1KafkaTopicList kafkaTopicList = (V1KafkaTopicList) resource;
        V1KafkaTopicObject topicObject = kafkaTopicList.getSpec().getTopics().get(0);

        Assertions.assertEquals("TOPIC-TEST", topicObject.getName());
        Assertions.assertEquals(4, topicObject.getPartitions());
        Assertions.assertEquals((short)1, topicObject.getReplicationFactor());

        Configs configs = topicObject.getConfigs();
        Assertions.assertEquals(1, configs.size());
        Assertions.assertEquals(CLEANUP_POLICY_COMPACT, configs.get(CLEANUP_POLICY_CONFIG).value());
    }
}