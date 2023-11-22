/*
 * Copyright 2023 The original authors
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

import io.streamthoughts.jikkou.core.annotation.Example;
import io.streamthoughts.jikkou.core.annotation.Title;
import io.streamthoughts.jikkou.core.config.ConfigProperty;
import io.streamthoughts.jikkou.core.config.Configuration;
import io.streamthoughts.jikkou.core.exceptions.ConfigException;
import io.streamthoughts.jikkou.core.exceptions.ValidationException;
import io.streamthoughts.jikkou.core.extension.ExtensionContext;
import io.streamthoughts.jikkou.core.validation.ValidationError;
import io.streamthoughts.jikkou.core.validation.ValidationResult;
import io.streamthoughts.jikkou.kafka.models.V1KafkaTopic;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.jetbrains.annotations.NotNull;

@Title("TopicNameRegexValidation allows validating that topic names match a defined regex.")
@Example(
        title = "Validate topic names match with one of the defined visibility prefixes.",
        full = true,
        code = {"""
                validations:
                - name: "topicMustHaveValidName"
                  type: "io.streamthoughts.jikkou.kafka.validation.TopicNameRegexValidation"
                  priority: 100
                  config:
                    topicNameRegex: "[a-zA-Z0-9\\\\._\\\\-]+"
                """
        }
)
public class TopicNameRegexValidation extends TopicValidation {

    public static final ConfigProperty<String> VALIDATION_TOPIC_NAME_REGEX_CONFIG = ConfigProperty
            .ofString("topicNameRegex");

    private Pattern pattern;

    /**
     * Empty constructor used by {@link Configuration}.
     */
    public TopicNameRegexValidation() {}

    /**
     * {@inheritDoc}
     */
    @Override
    public void init(@NotNull final ExtensionContext context) {
        super.init(context);
        final Optional<String> regex = VALIDATION_TOPIC_NAME_REGEX_CONFIG.getOptional(context.appConfiguration());
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
    public ValidationResult validate(final @NotNull V1KafkaTopic resource) throws ValidationException {
        if (!pattern.matcher(resource.getMetadata().getName()).matches()) {
            String error = String.format(
                    "Name for topic '%s' does not match the configured regex: %s",
                    resource.getMetadata().getName(),
                    pattern
            );
            return ValidationResult.failure(new ValidationError(getName(), resource, error));
        }
        return ValidationResult.success();
    }
}
