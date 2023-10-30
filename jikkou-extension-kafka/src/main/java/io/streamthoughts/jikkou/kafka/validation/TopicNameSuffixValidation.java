/*
 * Copyright 2021 The original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

import io.streamthoughts.jikkou.core.config.ConfigProperty;
import io.streamthoughts.jikkou.core.config.Configuration;
import io.streamthoughts.jikkou.core.exceptions.ConfigException;
import io.streamthoughts.jikkou.core.exceptions.ValidationException;
import io.streamthoughts.jikkou.core.validation.ValidationError;
import io.streamthoughts.jikkou.core.validation.ValidationResult;
import io.streamthoughts.jikkou.kafka.models.V1KafkaTopic;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public class TopicNameSuffixValidation extends TopicValidation {

    public static final ConfigProperty<List<String>> VALIDATION_TOPIC_NAME_SUFFIXES_CONFIG = ConfigProperty
            .ofList("topicNameSuffixes");

    private List<String> suffixes;

    /** {@inheritDoc} */
    @Override
    public void configure(@NotNull final Configuration config) throws ConfigException {
        super.configure(config);
        suffixes = VALIDATION_TOPIC_NAME_SUFFIXES_CONFIG.getOptional(config)
               .orElseThrow(() -> new ConfigException(
                       String.format("The '%s' configuration property is required for %s",
                               VALIDATION_TOPIC_NAME_SUFFIXES_CONFIG.key(),
                               TopicNameSuffixValidation.class.getSimpleName()
                       )
               ));
    }

    /** {@inheritDoc} */
    @Override
    public ValidationResult validate(final @NotNull V1KafkaTopic resource) throws ValidationException {
        final boolean matched = suffixes.stream()
                .filter(prefix -> resource.getMetadata().getName().endsWith(prefix))
                .findAny()
                .isEmpty();
        if (matched) {
            String error = String.format(
                    "Name for topic '%s' does not end with one of the configured suffixes: %s",
                    resource.getMetadata().getName(),
                    suffixes
            );
            return ValidationResult.failure(new ValidationError(getName(), resource, error));
        }
        return ValidationResult.success();
    }
}
