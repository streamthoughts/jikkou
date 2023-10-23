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
package io.streamthoughts.jikkou.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import io.streamthoughts.jikkou.core.io.Jackson;
import io.streamthoughts.jikkou.core.io.ResourceDeserializer;
import io.streamthoughts.jikkou.core.io.ResourceLoader;
import io.streamthoughts.jikkou.core.io.reader.ResourceReaderFactory;
import io.streamthoughts.jikkou.core.models.ConfigMap;
import io.streamthoughts.jikkou.core.models.Configs;
import io.streamthoughts.jikkou.core.models.HasItems;
import io.streamthoughts.jikkou.core.models.HasMetadata;
import io.streamthoughts.jikkou.core.models.ObjectMeta;
import io.streamthoughts.jikkou.kafka.model.DataHandle;
import io.streamthoughts.jikkou.kafka.model.DataType;
import io.streamthoughts.jikkou.kafka.model.DataValue;
import io.streamthoughts.jikkou.kafka.model.KafkaRecordHeader;
import io.streamthoughts.jikkou.kafka.models.V1KafkaPrincipalAuthorization;
import io.streamthoughts.jikkou.kafka.models.V1KafkaPrincipalRole;
import io.streamthoughts.jikkou.kafka.models.V1KafkaTableRecord;
import io.streamthoughts.jikkou.kafka.models.V1KafkaTableRecordSpec;
import io.streamthoughts.jikkou.kafka.models.V1KafkaTopic;
import io.streamthoughts.jikkou.kafka.models.V1KafkaTopicList;
import io.streamthoughts.jikkou.kafka.models.V1KafkaTopicSpec;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class KafkaExtensionResourceLoaderTest {

    private final ResourceLoader loader = new ResourceLoader(new ResourceReaderFactory(Jackson.YAML_OBJECT_MAPPER));

    @Test
    void shouldLoadResourcesForKafkaPrincipalRoles() {
        ResourceDeserializer.registerKind(V1KafkaPrincipalRole.class);
        HasItems resources = loader.loadFromClasspath("datasets/resource-kafka-principal-role.yaml");
        Assertions.assertNotNull(resources);
        Assertions.assertFalse(resources.getItems().isEmpty());

        List<V1KafkaPrincipalRole> clusters = resources.getAllByClass(V1KafkaPrincipalRole.class);
        Assertions.assertEquals(2, clusters.size());
    }

    @Test
    void shouldLoadResourcesForKafkaPrincipalAcls() {
        ResourceDeserializer.registerKind(V1KafkaPrincipalAuthorization.class);
        HasItems resources = loader.loadFromClasspath("datasets/resource-kafka-principal-acl.yaml");
        Assertions.assertNotNull(resources);
        Assertions.assertFalse(resources.getItems().isEmpty());

        List<V1KafkaPrincipalAuthorization> clusters = resources.getAllByClass(V1KafkaPrincipalAuthorization.class);
        Assertions.assertEquals(2, clusters.size());
    }

    @Test
    void shouldLoadResourcesForConfigMap() {
        ResourceDeserializer.registerKind(ConfigMap.class);

        HasItems resources = loader
                .loadFromClasspath("datasets/resource-configmap.yaml");
        Assertions.assertNotNull(resources);
        Assertions.assertFalse(resources.getItems().isEmpty());

        List<ConfigMap> configMaps = resources.getAllByClass(ConfigMap.class);
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
    void shouldLoadResourcesForKafkaTopicList() {
        ResourceDeserializer.registerKind(V1KafkaTopicList.class);

        HasItems resources = loader.loadFromClasspath("datasets/resource-kafka-topic-list.yaml");
        Assertions.assertNotNull(resources);
        Assertions.assertFalse(resources.getItems().isEmpty());

        List<V1KafkaTopicList> kafkaTopicLists = resources.getAllByClass(V1KafkaTopicList.class);
        Assertions.assertEquals(1, kafkaTopicLists.size());

        List<V1KafkaTopic> topicObjects = HasMetadata.sortByName(kafkaTopicLists.get(0).getItems());
        Assertions.assertEquals(3, topicObjects.size());

        Assertions.assertEquals(V1KafkaTopic.builder()
                .withMetadata(ObjectMeta
                        .builder()
                        .withName("my-topic-p1")
                        .build()
                )
                .withSpec(V1KafkaTopicSpec.
                        builder()
                        .withPartitions(1)
                        .withReplicas((short) 1)
                        .build()
                ).build(), topicObjects.get(0)
        );

        Assertions.assertEquals(V1KafkaTopic.builder()
                .withMetadata(ObjectMeta
                        .builder()
                        .withName("my-topic-p2")
                        .build()
                )
                .withSpec(V1KafkaTopicSpec.
                        builder()
                        .withPartitions(2)
                        .withReplicas((short) 2)
                        .build()
                ).build(), topicObjects.get(1)
        );

        Assertions.assertEquals(V1KafkaTopic.builder()
                .withMetadata(ObjectMeta
                        .builder()
                        .withName("my-topic-p3")
                        .build()
                )
                .withSpec(V1KafkaTopicSpec.
                        builder()
                        .withConfigs(Configs.of("retention.ms", 10000))
                        .build()
                ).build(), topicObjects.get(2)
        );
    }

    @Test
    void shouldLoadResourcesForKafkaRecordHavingStringValue() throws JsonProcessingException {
        ResourceDeserializer.registerKind(V1KafkaTableRecord.class);

        HasItems resources = loader
                .loadFromClasspath("datasets/resource-kafka-record-string-value.yaml");
        Assertions.assertNotNull(resources);
        Assertions.assertFalse(resources.getItems().isEmpty());

        List<V1KafkaTableRecord> results = resources.getAllByClass(V1KafkaTableRecord.class);

        JsonNode value = Jackson.JSON_OBJECT_MAPPER.readTree("""
                {
                  "favorite_color": "red"
                }
                """);
        V1KafkaTableRecord expected = V1KafkaTableRecord
                .builder()
                .withMetadata(new ObjectMeta("topic-compacted"))
                .withSpec(V1KafkaTableRecordSpec
                        .builder()
                        .withHeader(new KafkaRecordHeader("content-type", "application/json"))
                        .withKey(new DataValue(DataType.STRING, DataHandle.ofString("test")))
                        .withValue(new DataValue(DataType.JSON, new DataHandle(value)))
                        .build()
                )
                .build();
        Assertions.assertEquals(List.of(expected), results);
    }

    @Test
    void shouldLoadResourcesForKafkaRecordHavingRefValue() throws JsonProcessingException {
        ResourceDeserializer.registerKind(V1KafkaTableRecord.class);

        HasItems resources = loader
                .loadFromClasspath("datasets/resource-kafka-record-ref-value.yaml");
        Assertions.assertNotNull(resources);
        Assertions.assertFalse(resources.getItems().isEmpty());

        List<V1KafkaTableRecord> results = resources.getAllByClass(V1KafkaTableRecord.class);

        JsonNode value = Jackson.JSON_OBJECT_MAPPER.readTree("""
                {
                  "favorite_color": "red"
                }
                """);
        V1KafkaTableRecord expected = V1KafkaTableRecord
                .builder()
                .withMetadata(new ObjectMeta("topic-compacted"))
                .withSpec(V1KafkaTableRecordSpec
                        .builder()
                        .withHeader(new KafkaRecordHeader("content-type", "application/json"))
                        .withKey(new DataValue(DataType.STRING, DataHandle.ofString("test")))
                        .withValue(new DataValue(DataType.JSON, new DataHandle(value)))
                        .build()
                )
                .build();
        Assertions.assertEquals(List.of(expected), results);
    }
}