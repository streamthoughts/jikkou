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

import io.streamthoughts.jikkou.api.io.ResourceDeserializer;
import io.streamthoughts.jikkou.api.io.ResourceLoader;
import io.streamthoughts.jikkou.api.model.Configs;
import io.streamthoughts.jikkou.api.model.HasMetadata;
import io.streamthoughts.jikkou.api.model.Nameable;
import io.streamthoughts.jikkou.api.model.ObjectMeta;
import io.streamthoughts.jikkou.api.model.ResourceList;
import io.streamthoughts.jikkou.api.models.ConfigMap;
import io.streamthoughts.jikkou.kafka.models.V1KafkaAccessRoleObject;
import io.streamthoughts.jikkou.kafka.models.V1KafkaAccessUserObject;
import io.streamthoughts.jikkou.kafka.models.V1KafkaCluster;
import io.streamthoughts.jikkou.kafka.models.V1KafkaTopicList;
import io.streamthoughts.jikkou.kafka.models.V1KafkaTopicObject;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class KafkaExtensionResourceLoaderTest {


    private final ResourceLoader loader = ResourceLoader.create();

    @Test
    void should_load_resource_given_security_role() {
        ResourceDeserializer.registerKind(V1KafkaCluster.class);
        ResourceList resources = loader.loadFromClasspath("datasets/resource-kafka-cluster-with-security-roles.yaml");
        Assertions.assertNotNull(resources);
        Assertions.assertFalse(resources.isEmpty());

        List<V1KafkaCluster> clusters = resources.getAllResourcesForClass(V1KafkaCluster.class);
        Assertions.assertEquals(1, clusters.size());

        V1KafkaCluster cluster = clusters.get(0);
        List<V1KafkaAccessRoleObject> roles = cluster.getSpec()
                .getSecurity().getRoles();

        Assertions.assertEquals(2, roles.size());
    }

    @Test
    void should_load_resource_given_security_users() {
        ResourceDeserializer.registerKind(V1KafkaCluster.class);
        ResourceList resources = loader.loadFromClasspath("datasets/resource-kafka-cluster-with-security-users.yaml");
        Assertions.assertNotNull(resources);
        Assertions.assertFalse(resources.isEmpty());

        List<V1KafkaCluster> clusters = resources.getAllResourcesForClass(V1KafkaCluster.class);
        Assertions.assertEquals(1, clusters.size());

        V1KafkaCluster cluster = clusters.get(0);
        List<V1KafkaAccessUserObject> users = cluster.getSpec()
                .getSecurity().getUsers();

        Assertions.assertEquals(2, users.size());
    }

    @Test
    void should_load_resource_given_configmap() {
        ResourceDeserializer.registerKind(ConfigMap.class);

        ResourceList resources = loader
                .loadFromClasspath("datasets/resource-configmap.yaml");
        Assertions.assertNotNull(resources);
        Assertions.assertFalse(resources.isEmpty());

        List<ConfigMap> configMaps = resources.getAllResourcesForClass(ConfigMap.class);
        Assertions.assertEquals(1, configMaps.size());

        ConfigMap configMap = configMaps.get(0);
        Assertions.assertEquals(
                ConfigMap.builder()
                    .withApiVersion(HasMetadata.getApiVersion(ConfigMap.class))
                    .withKind(HasMetadata.getKind(ConfigMap.class))
                    .withMetadata(ObjectMeta.builder().withName("test").build())
                    .withData(Configs.of("retention.ms", 10000))
                    .build(),
                configMap
        );
    }

    @Test
    void should_load_resource_given_topic() {
        ResourceDeserializer.registerKind(V1KafkaTopicList.class);

        ResourceList resources = loader.loadFromClasspath("datasets/resource-kafka-topic-list.yaml");
        Assertions.assertNotNull(resources);
        Assertions.assertFalse(resources.isEmpty());

        List<V1KafkaTopicList> kafkaTopicLists = resources.getAllResourcesForClass(V1KafkaTopicList.class);
        Assertions.assertEquals(1, kafkaTopicLists.size());

        List<V1KafkaTopicObject> topicObjects = Nameable.sortByName(kafkaTopicLists.get(0).getSpec().getTopics());
        Assertions.assertEquals(3, topicObjects.size());

        Assertions.assertEquals(V1KafkaTopicObject.builder()
                .withName("my-topic-p1")
                .withPartitions(1)
                .withReplicationFactor((short)1)
                .build(), topicObjects.get(0)
        );

        Assertions.assertEquals(V1KafkaTopicObject.builder()
                .withName("my-topic-p2")
                .withPartitions(2)
                .withReplicationFactor((short)2)
                .build(), topicObjects.get(1)
        );

        Assertions.assertEquals(V1KafkaTopicObject.builder()
                .withName("my-topic-p3")
                .withConfigs(Configs.of("retention.ms", 10000))
                .build(), topicObjects.get(2)
        );
    }
}