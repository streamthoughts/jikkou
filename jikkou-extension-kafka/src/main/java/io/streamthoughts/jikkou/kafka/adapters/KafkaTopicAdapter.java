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
package io.streamthoughts.jikkou.kafka.adapters;

import io.streamthoughts.jikkou.JikkouMetadataAnnotations;
import io.streamthoughts.jikkou.api.model.Configs;
import io.streamthoughts.jikkou.api.model.Nameable;
import io.streamthoughts.jikkou.kafka.internals.KafkaTopics;
import io.streamthoughts.jikkou.kafka.models.V1KafkaTopic;
import java.util.Objects;
import java.util.Optional;

public final class KafkaTopicAdapter implements Nameable {

    private final V1KafkaTopic resource;

    /**
     * Creates a new {@link KafkaTopicAdapter} instance.
     *
     * @param resource the resource.
     */
    public KafkaTopicAdapter(final V1KafkaTopic resource) {
        this.resource = Objects.requireNonNull(resource, "'object' should not be null");
    }

    /**
     * @return the {@link Configs}.
     */
    public Configs getConfigs() {
        return Optional
                .ofNullable(resource.getSpec())
                .flatMap(spec -> Optional.ofNullable(spec.getConfigs()))
                .orElse(Configs.empty());
    }

    /**
     * @return the name of the topic.
     */
    @Override
    public String getName() {
        return resource.getMetadata().getName();
    }

    /**
     * @return the partition of the topic of the default ({@link KafkaTopics#NO_NUM_PARTITIONS})
     */
    public int getPartitionsOrDefault() {
        return Optional
                .ofNullable(resource.getSpec())
                .flatMap(spec -> Optional.ofNullable(spec.getPartitions()))
                .orElse(KafkaTopics.NO_NUM_PARTITIONS);
    }

    /**
     * @return the replication of the topic of the default ({@link KafkaTopics#NO_REPLICATION_FACTOR})
     */
    public short getReplicationFactorOrDefault() {
        return Optional
                .ofNullable(resource.getSpec())
                .flatMap(spec -> Optional.ofNullable(spec.getReplicas()))
                .orElse(KafkaTopics.NO_REPLICATION_FACTOR);
    }

    public boolean isDelete() {
        return JikkouMetadataAnnotations.isAnnotatedWithDelete(resource);
    }
}
