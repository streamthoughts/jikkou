/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.extension.aiven.adapter;

import io.streamthoughts.jikkou.core.models.ConfigValue;
import io.streamthoughts.jikkou.core.models.Configs;
import io.streamthoughts.jikkou.core.models.ObjectMeta;
import io.streamthoughts.jikkou.extension.aiven.api.data.KafkaTopicInfoGet;
import io.streamthoughts.jikkou.extension.aiven.api.data.Tag;
import io.streamthoughts.jikkou.kafka.models.V1KafkaTopic;
import io.streamthoughts.jikkou.kafka.models.V1KafkaTopicSpec;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;


public final class KafkaTopicAdapter {

    public static final String TAG_AIVEN_IO_PREFIX = "tag.aiven.io/";

    public static V1KafkaTopic map(final KafkaTopicInfoGet kafka) {
        Map<String, Object> annotations = Optional.ofNullable(kafka.tags())
            .stream()
            .flatMap(Collection::stream)
            .collect(Collectors.toMap(it -> TAG_AIVEN_IO_PREFIX + it.key(), Tag::value));

        Configs topicConfigs = new Configs(kafka.configs()
            .entrySet()
            .stream()
            .map(it -> new ConfigValue(
                it.getKey().replaceAll("_", "."),
                it.getValue().value()
            )).collect(Collectors.toSet()));

        return V1KafkaTopic
            .builder()
            .withMetadata(ObjectMeta
                .builder()
                .withName(kafka.topicName())
                .withAnnotations(annotations)
                .build()
            )
            .withSpec(V1KafkaTopicSpec
                .builder()
                .withPartitions(kafka.partitions())
                .withReplicas(kafka.replication().shortValue())
                .withConfigs(topicConfigs)
                .build()
            )
            .build();
    }
}
