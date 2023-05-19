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
package io.streamthoughts.jikkou.kafka.validation;

import io.streamthoughts.jikkou.api.annotations.ExtensionEnabled;
import io.streamthoughts.jikkou.api.config.ConfigProperty;
import io.streamthoughts.jikkou.api.config.Configuration;
import io.streamthoughts.jikkou.api.error.ConfigException;
import io.streamthoughts.jikkou.api.error.ValidationException;
import io.streamthoughts.jikkou.kafka.models.V1KafkaTopic;
import java.util.List;
import org.jetbrains.annotations.NotNull;

@ExtensionEnabled(value = false)
public class TopicNamePrefixValidation extends TopicValidation {

    public static final ConfigProperty<List<String>> VALIDATION_TOPIC_NAME_PREFIXES_CONFIG = ConfigProperty
            .ofList("topicNamePrefixes");

    private List<String> prefixes;

    /**
     * Empty constructor used by {@link Configuration}.
     */
    public TopicNamePrefixValidation() {}

    /**
     * Creates a new {@link TopicNamePrefixValidation}.
     *
     * @param prefixes the list of prefixes.
     */
    public TopicNamePrefixValidation(final List<String> prefixes) {
        configure(VALIDATION_TOPIC_NAME_PREFIXES_CONFIG.asConfiguration(prefixes));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void configure(@NotNull final Configuration config) throws ConfigException {
        super.configure(config);
        prefixes = VALIDATION_TOPIC_NAME_PREFIXES_CONFIG.getOptional(config)
                .orElseThrow(() -> new ConfigException(
                        String.format("The '%s' configuration property is required for %s",
                                VALIDATION_TOPIC_NAME_PREFIXES_CONFIG.key(),
                                TopicNamePrefixValidation.class.getSimpleName()
                        )
                ));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void validate(final @NotNull V1KafkaTopic resource) throws ValidationException {
        if (prefixes == null) {
            throw new IllegalStateException("No prefix was configured");
        }
        final boolean matched = prefixes.stream()
                .filter(prefix -> resource.getMetadata().getName().startsWith(prefix))
                .findAny()
                .isEmpty();
        if (matched) {
            throw new ValidationException(String.format(
                    "Name for topic '%s' does not start with one of the configured prefixes: %s",
                    resource.getMetadata().getName(),
                    prefixes
            ), this);
        }
    }
}
