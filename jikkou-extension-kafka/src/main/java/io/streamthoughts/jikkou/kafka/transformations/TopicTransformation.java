/*
 * Copyright 2021 StreamThoughts.
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
package io.streamthoughts.jikkou.kafka.transformations;

import io.streamthoughts.jikkou.api.AcceptResource;
import io.streamthoughts.jikkou.api.config.Configuration;
import io.streamthoughts.jikkou.api.error.ConfigException;
import io.streamthoughts.jikkou.api.model.HasMetadata;
import io.streamthoughts.jikkou.api.model.ResourceList;
import io.streamthoughts.jikkou.api.model.ResourceTransformation;
import io.streamthoughts.jikkou.kafka.models.V1KafkaTopicList;
import io.streamthoughts.jikkou.kafka.models.V1KafkaTopicObject;
import io.streamthoughts.jikkou.kafka.models.V1KafkaTopicSpec;
import io.vavr.control.Option;
import java.util.List;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;

/**
 * Transform an input {@link V1KafkaTopicList}.
 */
@AcceptResource(type = V1KafkaTopicList.class)
public abstract class TopicTransformation implements ResourceTransformation {

    private Configuration config;

    /** {@inheritDoc} */
    @Override
    public void configure(@NotNull final Configuration config) throws ConfigException {
        this.config = config;
    }

    /** {@inheritDoc} */
    @Override
    public @NotNull HasMetadata transform(
            @NotNull final HasMetadata resource,
            @NotNull final ResourceList list) {
        V1KafkaTopicList topicList = (V1KafkaTopicList) resource;

        V1KafkaTopicSpec spec = topicList.getSpec();
        List<V1KafkaTopicObject> resources = spec.getTopics();

        if (resources == null || resources.isEmpty()) {
            return resource;
        }

        List<V1KafkaTopicObject> transformed = resources.stream()
                .map(topic -> transformTopic(topic, list))
                .collect(Collectors.toList());

        return ((V1KafkaTopicList) resource).withSpec(spec.withTopics(transformed));

    }

    public Configuration config() {
        return Option.of(config).getOrElseThrow(() -> new IllegalStateException("not configured."));
    }

    public abstract @NotNull V1KafkaTopicObject transformTopic(@NotNull final V1KafkaTopicObject topic,
                                                               @NotNull final ResourceList list);

}
