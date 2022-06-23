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
package io.streamthoughts.jikkou.kafka.validations;

import io.streamthoughts.jikkou.api.AcceptResource;
import io.streamthoughts.jikkou.api.config.Configuration;
import io.streamthoughts.jikkou.api.error.ConfigException;
import io.streamthoughts.jikkou.api.error.ValidationException;
import io.streamthoughts.jikkou.api.model.HasMetadata;
import io.streamthoughts.jikkou.api.model.ResourceValidation;
import io.streamthoughts.jikkou.kafka.models.V1KafkaTopicList;
import io.streamthoughts.jikkou.kafka.models.V1KafkaTopicObject;
import io.vavr.control.Option;
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.NotNull;

/**
 * Validation for {@link V1KafkaTopicList}.
 */
@AcceptResource(type = V1KafkaTopicList.class)
public abstract class TopicValidation implements ResourceValidation {

    private Configuration config;

    /** {@inheritDoc} */
    @Override
    public void configure(@NotNull final Configuration config) throws ConfigException {
        this.config = config;
    }

    /** {@inheritDoc} */
    @Override
    public void validate(@NotNull final HasMetadata resource) throws ValidationException {
        V1KafkaTopicList topicList = (V1KafkaTopicList) resource;
        List<V1KafkaTopicObject> topics = topicList.getSpec().getTopics();
        if (topics == null || topics.isEmpty()) return;

        List<ValidationException> exceptions = new ArrayList<>(topics.size());
        for (V1KafkaTopicObject topic : topics) {
            try {
                validateTopic(topic);
            } catch (ValidationException e) {
                if (e.getErrors() != null && !e.getErrors().isEmpty()) {
                    exceptions.addAll(e.getErrors());
                } else {
                    exceptions.add(e);
                }
            }
        }
        if (!exceptions.isEmpty()) {
            throw new ValidationException(exceptions);
        }
    }

    public Configuration config() {
        return Option.of(config).getOrElseThrow(() -> new IllegalStateException("not configured."));
    }

    public abstract void validateTopic(@NotNull final V1KafkaTopicObject topic) throws ValidationException;
}
