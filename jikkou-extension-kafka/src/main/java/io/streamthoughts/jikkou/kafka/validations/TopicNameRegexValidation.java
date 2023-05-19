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

import io.streamthoughts.jikkou.api.annotations.ExtensionEnabled;
import io.streamthoughts.jikkou.api.config.ConfigProperty;
import io.streamthoughts.jikkou.api.config.Configuration;
import io.streamthoughts.jikkou.api.error.ConfigException;
import io.streamthoughts.jikkou.api.error.ValidationException;
import io.streamthoughts.jikkou.kafka.models.V1KafkaTopic;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.jetbrains.annotations.NotNull;

@ExtensionEnabled(value = false)
public class TopicNameRegexValidation extends TopicValidation {

    public static final ConfigProperty<String> VALIDATION_TOPIC_NAME_REGEX_CONFIG = ConfigProperty
            .ofString("topicNameRegex");

    private Pattern pattern;

    /**
     * Empty constructor used by {@link Configuration}.
     */
    public TopicNameRegexValidation() {
    }

    /**
     * Creates a new {@link TopicNameRegexValidation} instance.
     *
     * @param regex the regex.
     */
    public TopicNameRegexValidation(final String regex) {
        configure(VALIDATION_TOPIC_NAME_REGEX_CONFIG.asConfiguration(regex));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void configure(@NotNull final Configuration config) throws ConfigException {
        super.configure(config);
        final Optional<String> regex = VALIDATION_TOPIC_NAME_REGEX_CONFIG.getOptional(config);
        pattern = regex
                .map(pattern -> {
                    if (pattern.isEmpty()) {
                        throw new ConfigException(
                                String.format("The '%s' configuration property is set with an empty regexp",
                                        VALIDATION_TOPIC_NAME_REGEX_CONFIG.key()
                                )
                        );
                    }
                    return pattern;
                })
                .map(this::compile)
                .orElseThrow(() -> new ConfigException(
                        String.format("The '%s' configuration property is required for %s",
                                VALIDATION_TOPIC_NAME_REGEX_CONFIG.key(),
                                TopicNameRegexValidation.class.getSimpleName()
                        )
                ));

    }

    private Pattern compile(final String regex) {
        try {
            return Pattern.compile(regex);
        } catch (PatternSyntaxException e) {
            throw new ConfigException(
                    String.format("The '%s' configuration property is set with an invalid regexp '%s'",
                            VALIDATION_TOPIC_NAME_REGEX_CONFIG.key(),
                            regex
                    )
            );
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void validate(final @NotNull V1KafkaTopic resource) throws ValidationException {
        if (!pattern.matcher(resource.getMetadata().getName()).matches()) {
            throw new ValidationException(String.format(
                    "Name for topic '%s' does not match the configured regex: %s",
                    resource.getMetadata().getName(),
                    pattern
            ), this);
        }
    }
}
