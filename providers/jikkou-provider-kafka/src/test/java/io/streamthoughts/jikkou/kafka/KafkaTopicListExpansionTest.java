/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.kafka;

import io.streamthoughts.jikkou.core.CoreExtensionProvider;
import io.streamthoughts.jikkou.core.JikkouApi;
import io.streamthoughts.jikkou.core.ReconciliationContext;
import io.streamthoughts.jikkou.core.models.HasItems;
import io.streamthoughts.jikkou.core.models.HasMetadata;
import io.streamthoughts.jikkou.core.models.ObjectMeta;
import io.streamthoughts.jikkou.core.models.ResourceList;
import io.streamthoughts.jikkou.kafka.collections.V1KafkaTopicList;
import io.streamthoughts.jikkou.kafka.models.V1KafkaTopic;
import io.streamthoughts.jikkou.kafka.models.V1KafkaTopicSpec;
import io.streamthoughts.jikkou.runtime.JikkouContext;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class KafkaTopicListExpansionTest {

    @Test
    void shouldExpandKafkaTopicListIntoIndividualTopics() {
        // Given
        JikkouApi api = JikkouContext.defaultContext()
                .newApiBuilder()
                .register(new CoreExtensionProvider())
                .register(new KafkaExtensionProvider())
                .build()
                .enableBuiltInAnnotations(false);

        V1KafkaTopicList topicList = new V1KafkaTopicList.Builder()
                .withItems(List.of(
                        V1KafkaTopic.builder()
                                .withMetadata(ObjectMeta.builder()
                                        .withName("topic-1")
                                        .build())
                                .withSpec(V1KafkaTopicSpec.builder()
                                        .withPartitions(1)
                                        .withReplicas((short) 1)
                                        .build())
                                .build(),
                        V1KafkaTopic.builder()
                                .withMetadata(ObjectMeta.builder()
                                        .withName("topic-2")
                                        .build())
                                .withSpec(V1KafkaTopicSpec.builder()
                                        .withPartitions(2)
                                        .withReplicas((short) 1)
                                        .build())
                                .build()))
                .build();

        ReconciliationContext context = ReconciliationContext.builder()
                .dryRun(true)
                .build();

        // When
        HasItems result = api.prepare(ResourceList.of(topicList), context);

        // Then
        List<? extends HasMetadata> items = result.getItems();
        Assertions.assertEquals(2, items.size());
        Assertions.assertTrue(items.stream().allMatch(item -> "KafkaTopic".equals(item.getKind())));
        Assertions.assertTrue(items.stream().noneMatch(item -> "KafkaTopicList".equals(item.getKind())));
        Assertions.assertTrue(result.findByName("topic-1").isPresent());
        Assertions.assertTrue(result.findByName("topic-2").isPresent());
    }

    @Test
    void shouldNotExpandKafkaTopicListWhenCoreProviderNotRegistered() {
        // Given
        JikkouApi api = JikkouContext.defaultContext()
                .newApiBuilder()
                .register(new KafkaExtensionProvider())
                .build()
                .enableBuiltInAnnotations(false);

        V1KafkaTopicList topicList = new V1KafkaTopicList.Builder()
                .withItems(List.of(
                        V1KafkaTopic.builder()
                                .withMetadata(ObjectMeta.builder()
                                        .withName("topic-1")
                                        .build())
                                .withSpec(V1KafkaTopicSpec.builder()
                                        .withPartitions(1)
                                        .withReplicas((short) 1)
                                        .build())
                                .build(),
                        V1KafkaTopic.builder()
                                .withMetadata(ObjectMeta.builder()
                                        .withName("topic-2")
                                        .build())
                                .withSpec(V1KafkaTopicSpec.builder()
                                        .withPartitions(2)
                                        .withReplicas((short) 1)
                                        .build())
                                .build()))
                .build();

        ReconciliationContext context = ReconciliationContext.builder()
                .dryRun(true)
                .build();

        // When
        HasItems result = api.prepare(ResourceList.of(topicList), context);

        // Then - without CoreExtensionProvider, the list is not expanded
        List<? extends HasMetadata> items = result.getItems();
        Assertions.assertEquals(1, items.size());
        Assertions.assertEquals("KafkaTopicList", items.getFirst().getKind());
    }
}
