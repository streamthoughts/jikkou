/*
 * Copyright 2021 StreamThoughts.
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
package io.streamthoughts.jikkou.kafka.transformations;

import io.streamthoughts.jikkou.api.model.Configs;
import io.streamthoughts.jikkou.api.model.ObjectMeta;
import io.streamthoughts.jikkou.api.model.ResourceList;
import io.streamthoughts.jikkou.api.models.ConfigMap;
import io.streamthoughts.jikkou.kafka.models.V1KafkaTopicList;
import io.streamthoughts.jikkou.kafka.models.V1KafkaTopicObject;
import io.streamthoughts.jikkou.kafka.models.V1KafkaTopicSpec;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ApplyConfigMapsTransformationTest {

    public static final String TEST_CONFIG_MAP_NAME = "configMap";

    private final static ConfigMap TEST_CONFIG_MAP = ConfigMap
            .builder()
            .withMetadata(ObjectMeta
                    .builder()
                    .withName(TEST_CONFIG_MAP_NAME)
                    .build())
            .withData(Configs.of("k1", "v1"))
            .build();

    private final static V1KafkaTopicObject TEST_TOPIC_OBJECT = V1KafkaTopicObject
            .builder()
            .withName("topic")
            .withPartitions(null)
            .withReplicationFactor(null)
            .withConfigs(Configs.empty())
            .withConfigMapRefs(Set.of(TEST_CONFIG_MAP_NAME))
            .build();


    @Test
    public void should_add_config_props_to_topic_given_valid_config_map() {
        var resource = V1KafkaTopicList.builder()
                .withSpec(V1KafkaTopicSpec
                        .builder()
                        .withTopics(List.of(TEST_TOPIC_OBJECT))
                        .build())
                .build();

        var transformed = new TopicConfigMapsTransformation()
                .transform(resource, new ResourceList(List.of(TEST_CONFIG_MAP)));

        V1KafkaTopicList transformedList = (V1KafkaTopicList) transformed;
        V1KafkaTopicObject topicObject = transformedList.getSpec().getTopics().get(0);

        Assertions.assertEquals("v1",
                topicObject.getConfigs().get("k1").value()
        );
    }
}