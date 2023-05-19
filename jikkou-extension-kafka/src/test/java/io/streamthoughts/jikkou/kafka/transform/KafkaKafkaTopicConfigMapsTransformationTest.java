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
package io.streamthoughts.jikkou.kafka.transform;

import io.streamthoughts.jikkou.api.model.Configs;
import io.streamthoughts.jikkou.api.model.GenericResourceListObject;
import io.streamthoughts.jikkou.api.model.ObjectMeta;
import io.streamthoughts.jikkou.api.models.ConfigMap;
import io.streamthoughts.jikkou.kafka.models.V1KafkaTopic;
import io.streamthoughts.jikkou.kafka.models.V1KafkaTopicSpec;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class KafkaKafkaTopicConfigMapsTransformationTest {

    static final String TEST_CONFIG_MAP_NAME = "configMap";
    static final String TEST_TOPIC = "topic";
    static final String TEST_CONFIG_K1 = "k1";
    static final String TEST_CONFIG_K2 = "k2";
    private final static ConfigMap TEST_CONFIG_MAP = ConfigMap
            .builder()
            .withMetadata(ObjectMeta
                    .builder()
                    .withName(TEST_CONFIG_MAP_NAME)
                    .build())
            .withData(Configs.of(TEST_CONFIG_K1, "v1"))
            .build();


    @Test
    void shouldAddConfigPropsToTopicGivenValidConfigMapRefForTopicWithNoConfigs() {
        // Given
        var resource = V1KafkaTopic.builder()
                .withMetadata(ObjectMeta
                        .builder()
                        .withName(TEST_TOPIC)
                        .build()
                )
                .withSpec(V1KafkaTopicSpec
                        .builder()
                        .withPartitions(null)
                        .withReplicas(null)
                        .withConfigs(Configs.empty())
                        .withConfigMapRefs(Set.of(TEST_CONFIG_MAP_NAME))
                        .build())
                .build();

        // When
        var result = (V1KafkaTopic) new KafkaTopicConfigMapsTransformation()
                .transform(resource, new GenericResourceListObject(List.of(TEST_CONFIG_MAP)))
                .get();

        // Then
        Assertions.assertNull(result.getSpec().getConfigMapRefs());
        Configs configs = result.getSpec().getConfigs();
        Assertions.assertEquals("v1", configs.get(TEST_CONFIG_K1).value());
    }

    @Test
    void shouldAddConfigPropsToTopicGivenValidConfigMapRefForTopicWithConfigs() {
        // Given
        var resource = V1KafkaTopic.builder()
                .withMetadata(ObjectMeta
                        .builder()
                        .withName(TEST_TOPIC)
                        .build()
                )
                .withSpec(V1KafkaTopicSpec
                        .builder()
                        .withPartitions(null)
                        .withReplicas(null)
                        .withConfigs(Configs.of(TEST_CONFIG_K2, "v2"))
                        .withConfigMapRefs(Set.of(TEST_CONFIG_MAP_NAME))
                        .build())
                .build();

        // When
        var result = (V1KafkaTopic) new KafkaTopicConfigMapsTransformation()
                .transform(resource, new GenericResourceListObject(List.of(TEST_CONFIG_MAP)))
                .get();

        // Then
        Configs configs = result.getSpec().getConfigs();
        Assertions.assertEquals(2, configs.size());
        Assertions.assertNull(result.getSpec().getConfigMapRefs());
        Assertions.assertEquals("v1", configs.get(TEST_CONFIG_K1).value());
        Assertions.assertEquals("v2", configs.get(TEST_CONFIG_K2).value());
    }

    @Test
    void shouldOverrideConfigPropsToTopicGivenValidConfigMapRefForTopicWithConfigs() {
        // Given
        var resource = V1KafkaTopic.builder()
                .withMetadata(ObjectMeta
                        .builder()
                        .withName(TEST_TOPIC)
                        .build()
                )
                .withSpec(V1KafkaTopicSpec
                        .builder()
                        .withPartitions(null)
                        .withReplicas(null)
                        .withConfigs(Configs.of(TEST_CONFIG_K1, "v2"))    // should be overridden
                        .withConfigMapRefs(Set.of(TEST_CONFIG_MAP_NAME))
                        .build())
                .build();

        // When
        var result = (V1KafkaTopic) new KafkaTopicConfigMapsTransformation()
                .transform(resource, new GenericResourceListObject(List.of(TEST_CONFIG_MAP)))
                .get();

        // Then
        Configs configs = result.getSpec().getConfigs();
        Assertions.assertEquals(1, configs.size());
        Assertions.assertNull(result.getSpec().getConfigMapRefs());
        Assertions.assertEquals("v1", configs.get(TEST_CONFIG_K1).value());
    }
}