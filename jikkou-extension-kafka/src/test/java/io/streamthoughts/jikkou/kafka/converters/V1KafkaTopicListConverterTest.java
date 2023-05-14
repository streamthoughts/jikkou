/*
 * Copyright 2023 StreamThoughts.
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
package io.streamthoughts.jikkou.kafka.converters;

import io.streamthoughts.jikkou.JikkouMetadataAnnotations;
import io.streamthoughts.jikkou.api.model.HasMetadata;
import io.streamthoughts.jikkou.api.model.ObjectMeta;
import io.streamthoughts.jikkou.kafka.models.V1KafkaTopic;
import io.streamthoughts.jikkou.kafka.models.V1KafkaTopicList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class V1KafkaTopicListConverterTest {

    private static final String TOPIC_A = "TOPICA";
    private static final String TOPIC_B = "TOPICB";

    @Test
    void shouldConvertFromV1KafkaTopicList() {
        // Given
        V1KafkaTopic topicA = new V1KafkaTopic()
                .toBuilder()
                .withMetadata(ObjectMeta
                        .builder()
                        .withName(TOPIC_A)
                        .build())
                .build();

        V1KafkaTopic topicB = new V1KafkaTopic()
                .toBuilder()
                .withMetadata(ObjectMeta
                        .builder()
                        .withName(TOPIC_B)
                        .build())
                .build();

        V1KafkaTopicList kafkaTopicList = V1KafkaTopicList
                .builder()
                .withItem(topicA)
                .withItem(topicB)
                .build();
        // When
        V1KafkaTopicListConverter converter = new V1KafkaTopicListConverter();
        List<V1KafkaTopic> result = HasMetadata.sortByName(converter.convertFrom(List.of(kafkaTopicList)));

        // Then
        Assertions.assertEquals(2, result.size());
        Assertions.assertEquals(topicA, result.get(0));
        Assertions.assertEquals(topicB, result.get(1));
    }

    @Test
    void shouldConvertFromV1KafkaTopicListWithLabels() {
        // Given
        V1KafkaTopic topicA = new V1KafkaTopic()
                .toBuilder()
                .withMetadata(ObjectMeta
                        .builder()
                        .withName(TOPIC_A)
                        .build())
                .build();

        V1KafkaTopicList kafkaTopicList = V1KafkaTopicList
                .builder()
                .withMetadata(ObjectMeta
                        .builder()
                        .withLabel("keyA", "valueA")
                        .withLabel("keyB", "valueB")
                        .build()
                )
                .withItem(topicA)
                .build();
        // When
        V1KafkaTopicListConverter converter = new V1KafkaTopicListConverter();
        List<V1KafkaTopic> result = HasMetadata.sortByName(converter.convertFrom(List.of(kafkaTopicList)));

        // Then
        V1KafkaTopic topic = result.get(0);
        Map<String, Object> labels = topic.getMetadata().getLabels();
        Assertions.assertEquals(2, labels.size());
        Assertions.assertEquals("valueA", labels.get("keyA"));
        Assertions.assertEquals("valueB", labels.get("keyB"));
    }

    @Test
    void shouldConvertFromV1KafkaTopicListWithAnnotations() {
        // Given
        V1KafkaTopic topicA = new V1KafkaTopic()
                .toBuilder()
                .withMetadata(ObjectMeta
                        .builder()
                        .withName(TOPIC_A)
                        .build())
                .build();

        V1KafkaTopicList kafkaTopicList = V1KafkaTopicList
                .builder()
                .withMetadata(ObjectMeta
                        .builder()
                        .withAnnotation("keyA", "valueA")
                        .withAnnotation("keyB", "valueB")
                        .build()
                )
                .withItem(topicA)
                .build();
        // When
        V1KafkaTopicListConverter converter = new V1KafkaTopicListConverter();
        List<V1KafkaTopic> result = HasMetadata.sortByName(converter.convertFrom(List.of(kafkaTopicList)));

        // Then
        V1KafkaTopic topic = result.get(0);
        Map<String, Object> annotations = topic.getMetadata().getAnnotations();
        Assertions.assertEquals(2, annotations.size());
        Assertions.assertEquals("valueA", annotations.get("keyA"));
        Assertions.assertEquals("valueB", annotations.get("keyB"));
    }

    @Test
    void shouldConvertToV1KafkaTopicList() {
        // Given
        V1KafkaTopic topicA = new V1KafkaTopic()
                .toBuilder()
                .withMetadata(ObjectMeta
                        .builder()
                        .withName(TOPIC_A)
                        .build())
                .build();

        V1KafkaTopic topicB = new V1KafkaTopic()
                .toBuilder()
                .withMetadata(ObjectMeta
                        .builder()
                        .withName(TOPIC_B)
                        .build())
                .build();

        // When
        V1KafkaTopicListConverter converter = new V1KafkaTopicListConverter();
        List<V1KafkaTopicList> result = converter.convertTo(List.of(topicA, topicB));

        // Then
        V1KafkaTopicList kafkaTopicList = result.get(0);

        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals(2, kafkaTopicList.getItems().size());

        Assertions.assertEquals(2, kafkaTopicList
                .getMetadata()
                .getAnnotations()
                .get(JikkouMetadataAnnotations.JIKKOU_IO_ITEMS_COUNT));
    }
}
