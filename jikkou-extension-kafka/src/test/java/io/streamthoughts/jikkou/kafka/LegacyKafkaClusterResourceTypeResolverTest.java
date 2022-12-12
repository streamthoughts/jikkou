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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.streamthoughts.jikkou.api.model.HasMetadata;
import io.streamthoughts.jikkou.api.model.Resource;
import io.streamthoughts.jikkou.kafka.models.V1KafkaCluster;
import io.streamthoughts.jikkou.kafka.models.V1KafkaTopicList;
import java.util.Collections;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class LegacyKafkaClusterResourceTypeResolverTest {

    private final LegacyKafkaClusterResourceTypeResolver resolver = new LegacyKafkaClusterResourceTypeResolver();

    @Test
    void should_resolve_kafka_cluster_given_version_equals_to_1() {
        // Given
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.valueToTree(Map.of("apiVersion", "1"));

        // When
        Class<? extends Resource> resolvesType = resolver.resolvesType(jsonNode);

        // Then
        Assertions.assertNotNull(resolvesType);
        Assertions.assertTrue(resolvesType.isAssignableFrom(V1KafkaCluster.class));
    }

    @Test
    void should_resolve_kafka_cluster_given_no_version() {
        // Given
        ObjectMapper mapper = new ObjectMapper();
        String apiVersion = HasMetadata.getApiVersion(V1KafkaTopicList.class);
        assert apiVersion != null;

        JsonNode jsonNode = mapper.valueToTree(Map.of("apiVersion", apiVersion));

        // When
        Class<? extends Resource> resolvesType = resolver.resolvesType(jsonNode);

        // Then
        Assertions.assertNull(resolvesType);
    }

    @Test
    void should_return_null_given_version() {
        // Given
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.valueToTree(Collections.emptyMap());

        // When
        Class<? extends Resource> resolvesType = resolver.resolvesType(jsonNode);

        // Then
        Assertions.assertNotNull(resolvesType);
        Assertions.assertTrue(resolvesType.isAssignableFrom(V1KafkaCluster.class));
    }
}