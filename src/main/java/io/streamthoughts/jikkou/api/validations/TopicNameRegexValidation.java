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
package io.streamthoughts.jikkou.api.validations;

import io.streamthoughts.jikkou.api.config.JikkouConfig;
import io.streamthoughts.jikkou.api.config.JikkouParams;
import io.streamthoughts.jikkou.api.model.V1TopicObject;
import io.streamthoughts.jikkou.api.error.ConfigException;
import io.vavr.control.Option;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class TopicNameRegexValidation extends TopicValidation {

    private Pattern pattern;

    /**
     * Empty constructor used by {@link JikkouConfig}.
     */
    public TopicNameRegexValidation() {}

    /**
     * Creates a new {@link TopicNameRegexValidation} instance.
     * @param regex the regex.
     */
    public TopicNameRegexValidation(final String regex) {
        configure(JikkouParams.VALIDATION_TOPIC_NAME_REGEX_CONFIG.toConfig(regex));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void configure(@NotNull final JikkouConfig config) throws ConfigException {
        super.configure(config);
        final Option<String> regex = JikkouParams.VALIDATION_TOPIC_NAME_REGEX_CONFIG.getOption(config);
        try {
            pattern = regex
                    .peek(pattern -> {
                        if (pattern.isEmpty()) {
                            throw new ConfigException(
                                    String.format("The '%s' configuration property is set with an empty regexp",
                                            JikkouParams.VALIDATION_TOPIC_NAME_REGEX_CONFIG.path()
                                    )
                            );
                        }
                    })
                    .map(Pattern::compile).getOrElseThrow(() -> {
                        throw new ConfigException(
                                String.format("The '%s' configuration property is required for %s",
                                        JikkouParams.VALIDATION_TOPIC_NAME_REGEX_CONFIG.path(),
                                        TopicNameRegexValidation.class.getSimpleName()
                                )
                        );
                    });
        } catch (PatternSyntaxException e) {
            throw new ConfigException(
                    String.format("The '%s' configuration property is set with an invalid regexp '%s'",
                            JikkouParams.VALIDATION_TOPIC_NAME_REGEX_CONFIG.path(),
                            regex.get()
                    )
            );
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void validateTopic(final @NotNull V1TopicObject topic) throws ValidationException {
        if (!pattern.matcher(topic.name()).matches()) {
            throw new ValidationException(String.format(
                    "Name for topic '%s' does not match the configured regex: %s",
                    topic.name(),
                    pattern
            ), this);
        }
    }
}
