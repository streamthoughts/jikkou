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

import io.streamthoughts.jikkou.kafka.config.JikkouConfig;
import io.streamthoughts.jikkou.kafka.config.JikkouParams;
import io.streamthoughts.jikkou.kafka.model.V1TopicObject;
import io.streamthoughts.jikkou.kafka.error.ConfigException;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class TopicNameSuffixValidation extends TopicValidation {

    private List<String> suffixes;

    /**
     * {@inheritDoc}
     */
    @Override
    public void configure(@NotNull final JikkouConfig config) throws ConfigException {
        super.configure(config);
        suffixes = JikkouParams.VALIDATION_TOPIC_NAME_SUFFIXES_CONFIG.getOption(config)
               .getOrElseThrow(() -> {
                   throw new ConfigException(
                           String.format("The '%s' configuration property is required for %s",
                                   JikkouParams.VALIDATION_TOPIC_NAME_SUFFIXES_CONFIG.path(),
                                   TopicNameSuffixValidation.class.getSimpleName()
                           )
                   );
               });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void validateTopic(final @NotNull V1TopicObject topic) throws ValidationException {
        final boolean matched = suffixes.stream()
                .filter(prefix -> topic.name().endsWith(prefix))
                .findAny()
                .isEmpty();
        if (matched) {
            throw new ValidationException(String.format(
                    "Name for topic '%s' does not end with one of the configured suffixes: %s",
                    topic.name(),
                    suffixes
            ), this);
        }
    }
}
