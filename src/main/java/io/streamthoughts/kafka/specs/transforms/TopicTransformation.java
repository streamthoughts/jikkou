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
package io.streamthoughts.kafka.specs.transforms;

import io.streamthoughts.kafka.specs.config.JikkouConfig;
import io.streamthoughts.kafka.specs.error.ConfigException;
import io.streamthoughts.kafka.specs.model.V1SpecObject;
import io.streamthoughts.kafka.specs.model.V1TopicObject;
import io.vavr.control.Option;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Transform an input {@link V1SpecObject}.
 */
public abstract class TopicTransformation implements Transformation {

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
    public @NotNull V1SpecObject transform(@NotNull final V1SpecObject specsObject) {
        final List<V1TopicObject> topics = specsObject.topics();
        if (topics.isEmpty()) return specsObject;

        var transformed = specsObject.topics()
                .stream()
                .map(this::transformTopic)
                .collect(Collectors.toList());

        return specsObject.topics(transformed);
    }

    public JikkouConfig config() {
        return Option.of(config).getOrElseThrow(() -> new IllegalStateException("not configured."));
    }

    public abstract @NotNull V1TopicObject transformTopic(@NotNull final V1TopicObject topic);

}
