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
package io.streamthoughts.jikkou.kafka.converters;

import io.streamthoughts.jikkou.CoreAnnotations;
import io.streamthoughts.jikkou.api.converter.ResourceConverter;
import io.streamthoughts.jikkou.api.model.ObjectMeta;
import io.streamthoughts.jikkou.kafka.models.V1KafkaBroker;
import io.streamthoughts.jikkou.kafka.models.V1KafkaBrokerList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.jetbrains.annotations.NotNull;

public class V1KafkaBrokerListConverter implements ResourceConverter<V1KafkaBrokerList, V1KafkaBroker> {

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull List<V1KafkaBroker> convertFrom(@NotNull List<V1KafkaBrokerList> resources) {
        return resources
                .stream()
                .flatMap(this::stream)
                .toList();

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull List<V1KafkaBrokerList> convertTo(@NotNull List<V1KafkaBroker> resources) {
        List<V1KafkaBroker> objects = resources
                .stream()
                .map(topic ->
                        topic.toBuilder()
                                .withKind(null)
                                .withApiVersion(null)
                                .build()
                )
                .toList();

        return List.of(V1KafkaBrokerList.builder()
                        .withMetadata(ObjectMeta.builder()
                                .withAnnotation(CoreAnnotations.JIKKOU_IO_ITEMS_COUNT, objects.size())
                                .build()
                        )
                        .withItems(objects)
                        .build()
        );
    }

    private Stream<V1KafkaBroker> stream(V1KafkaBrokerList resource) {
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

                    return new V1KafkaBroker().toBuilder()
                            .withMetadata(objectMeta)
                            .withSpec(object.getSpec())
                            .build();
                });
    }
}
