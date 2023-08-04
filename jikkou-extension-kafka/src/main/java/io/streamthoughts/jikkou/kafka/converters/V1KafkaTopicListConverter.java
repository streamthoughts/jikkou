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

import io.streamthoughts.jikkou.JikkouMetadataAnnotations;
import io.streamthoughts.jikkou.api.converter.ResourceConverter;
import io.streamthoughts.jikkou.api.model.ObjectMeta;
import io.streamthoughts.jikkou.kafka.adapters.V1KafkaTopicSupport;
import io.streamthoughts.jikkou.kafka.models.V1KafkaTopic;
import io.streamthoughts.jikkou.kafka.models.V1KafkaTopicList;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public class V1KafkaTopicListConverter implements ResourceConverter<V1KafkaTopicList, V1KafkaTopic> {

    /** {@inheritDoc} */
    @Override
    public @NotNull List<V1KafkaTopic> convertFrom(@NotNull List<V1KafkaTopicList> resources) {
        return resources
                .stream()
                .flatMap(V1KafkaTopicSupport::stream)
                .toList();

    }

    /** {@inheritDoc} */
    @Override
    public @NotNull List<V1KafkaTopicList> convertTo(@NotNull List<V1KafkaTopic> resources) {
        List<V1KafkaTopic> objects = resources
                .stream()
                .map(topic ->
                        topic.toBuilder()
                                .withKind(null)
                                .withApiVersion(null)
                                .build()
                )
                .toList();

        return List.of(
                V1KafkaTopicList.builder()
                        .withMetadata(ObjectMeta.builder()
                                .withAnnotation(JikkouMetadataAnnotations.JIKKOU_IO_ITEMS_COUNT, objects.size())
                                .build()
                        )
                        .withItems(objects)
                        .build()
        );
    }
}
