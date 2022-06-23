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

import io.streamthoughts.jikkou.api.model.Configs;
import io.streamthoughts.jikkou.kafka.internals.KafkaConstants;
import io.streamthoughts.jikkou.kafka.models.V1KafkaTopicObject;
import java.util.Objects;
import java.util.Optional;

public final class KafkaTopicObjectAdapter {

    private final V1KafkaTopicObject object;

    /**
     * Creates a new {@link KafkaTopicObjectAdapter} instance.
     *
     * @param object the resource.
     */
    public KafkaTopicObjectAdapter(final V1KafkaTopicObject object) {
        this.object = Objects.requireNonNull(object, "'object' should not be null");
    }

    /**
     * @return  the {@link Configs}.
     */
    public Configs getConfigs() {
        return Optional.ofNullable(object.getConfigs()).orElse(Configs.empty());
    }

    /**
     * @return  the name of the topic.
     */
    public String getName() {
        return object.getName();
    }

    /**
     * @return  the partition of the topic of the default ({@link KafkaConstants#NO_NUM_PARTITIONS})
     */
    public int getPartitionsOrDefault() {
        return Optional
                .ofNullable(object.getPartitions()).
                orElse(KafkaConstants.NO_NUM_PARTITIONS);
    }

    /**
     * @return  the replication of the topic of the default ({@link KafkaConstants#NO_REPLICATION_FACTOR})
     */
    public short getReplicationFactorOrDefault() {
        return Optional
                .ofNullable(object.getReplicationFactor())
                .orElse(KafkaConstants.NO_REPLICATION_FACTOR);
    }
}
