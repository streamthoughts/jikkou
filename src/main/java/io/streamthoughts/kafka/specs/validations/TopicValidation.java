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
package io.streamthoughts.kafka.specs.validations;

import io.streamthoughts.kafka.specs.config.JikkouConfig;
import io.streamthoughts.kafka.specs.error.ConfigException;
import io.streamthoughts.kafka.specs.model.V1SpecsObject;
import io.streamthoughts.kafka.specs.model.V1TopicObject;
import io.vavr.control.Option;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Validation for {@link V1SpecsObject}.
 */
public abstract class TopicValidation implements Validation {

    private JikkouConfig config;

    /**
     * {@inheritDoc}
     */
    @Override
    public void configure(@NotNull final JikkouConfig config) throws ConfigException {
        this.config = config;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void validate(@NotNull final V1SpecsObject specsObject) throws ValidationException {
        final List<V1TopicObject> topics = specsObject.topics();
        if (topics.isEmpty()) return;

        List<ValidationException> exceptions = new ArrayList<>(topics.size());
        for (V1TopicObject topic : topics) {
            try {
                validateTopic(topic);
            } catch (ValidationException e) {
                exceptions.add(e);
            }
        }
        if (!exceptions.isEmpty()) {
            throw new ValidationException(exceptions);
        }
    }

    public JikkouConfig config() {
        return Option.of(config).getOrElseThrow(() -> new IllegalStateException("not configured."));
    }

    public abstract void validateTopic(@NotNull final V1TopicObject topic) throws ValidationException;
}
