/*
 * Copyright 2023 The original authors
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
package io.streamthoughts.jikkou.kafka.adapters;

import io.streamthoughts.jikkou.core.models.ObjectMeta;
import io.streamthoughts.jikkou.kafka.models.V1KafkaTopic;
import io.streamthoughts.jikkou.kafka.models.V1KafkaTopicList;
import io.streamthoughts.jikkou.kafka.models.V1KafkaTopicSpec;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public final class V1KafkaTopicSupport {

    public static Stream<V1KafkaTopic> stream(V1KafkaTopicList resource) {
        if (resource == null || resource.getItems() == null) {
            return Stream.empty();
        }

        ObjectMeta metadata = resource.getMetadata();
        Map<String, Object> listLabels = metadata != null ? metadata.getLabels() : Collections.emptyMap();
        Map<String, Object> listAnnotations = metadata != null ? metadata.getAnnotations() : Collections.emptyMap();

        return resource
                .getItems()
                .stream()
                .map(object -> {
                    var labels = new HashMap<>(listLabels);
                    var annotations = new HashMap<>(listAnnotations);

                    ObjectMeta itemObjectMeta = object.getMetadata();
                    if (itemObjectMeta != null) {
                        labels.putAll(itemObjectMeta.getLabels());
                        annotations.putAll(itemObjectMeta.getAnnotations());
                    } else {
                        itemObjectMeta = new ObjectMeta();
                    }

                    ObjectMeta objectMeta = itemObjectMeta
                            .toBuilder()
                            .withLabels(labels)
                            .withAnnotations(annotations)
                            .build();

                    return V1KafkaTopic.builder()
                            .withMetadata(objectMeta)
                            .withSpec(object.getSpec())
                            .build();
                });
    }

    public static Stream<V1KafkaTopic> stream(final Iterable<String> topicNames) {
        return StreamSupport.stream(topicNames.spliterator(), false)
                .map(topicName -> V1KafkaTopic
                        .builder()
                        .withMetadata(ObjectMeta.builder().withName(topicName).build())
                        .withSpec(V1KafkaTopicSpec.builder().build())
                        .build()
                );
    }
}
