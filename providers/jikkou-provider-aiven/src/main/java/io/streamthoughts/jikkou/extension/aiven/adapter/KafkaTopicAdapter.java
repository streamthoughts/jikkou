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
import io.streamthoughts.jikkou.extension.aiven.ApiVersions;
import io.streamthoughts.jikkou.extension.aiven.api.data.KafkaTopicConfigInfo;
import io.streamthoughts.jikkou.extension.aiven.api.data.KafkaTopicInfo;
import io.streamthoughts.jikkou.extension.aiven.api.data.Tag;
import io.streamthoughts.jikkou.kafka.models.V1KafkaTopic;
import io.streamthoughts.jikkou.kafka.models.V1KafkaTopicSpec;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import javax.validation.constraints.NotNull;


public final class KafkaTopicAdapter {

    public static final String TAG_AIVEN_IO_PREFIX = "tag.aiven.io/";

    public static V1KafkaTopic map(@NotNull final KafkaTopicInfo kafka,
                                   @NotNull final Predicate<KafkaTopicConfigInfo> filter) {
        Map<String, Object> labels = Optional.ofNullable(kafka.tags())
            .stream()
            .flatMap(Collection::stream)
            .collect(Collectors.toMap(it -> TAG_AIVEN_IO_PREFIX + it.key(), Tag::value));

        Configs topicConfigs = new Configs(kafka.config()
            .entrySet()
            .stream()
            .filter(it -> filter.test(it.getValue()))
            .map(it -> {
                KafkaTopicConfigInfo configInfo = it.getValue();
                return new ConfigValue(
                    configKeyFromAiven(it.getKey()),
                    configInfo.value(),
                    configInfo.source().equals(KafkaTopicConfigInfo.Source.DEFAULT_CONFIG),
                    configInfo.source().equals(KafkaTopicConfigInfo.Source.TOPIC_CONFIG)
                );
            }).collect(Collectors.toSet()));

        return V1KafkaTopic
            .builder()
            .withApiVersion(ApiVersions.KAFKA_AIVEN_V1BETA2)
            .withMetadata(ObjectMeta
                .builder()
                .withName(kafka.topicName())
                .withLabels(labels)
                .build()
            )
            .withSpec(V1KafkaTopicSpec
                .builder()
                .withPartitions(kafka.partitions().size())
                .withReplicas(kafka.replication().shortValue())
                .withConfigs(topicConfigs)
                .build()
            )
            .build();
    }

    public static  String configKeyToAiven(final String key) {
        return key.replaceAll("\\.", "_");
    }

    public static  String configKeyFromAiven(final String key) {
        return key.replaceAll("_", ".");
    }


}
