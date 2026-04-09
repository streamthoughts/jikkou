/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.jikkou.kafka.validation;

import io.jikkou.core.annotation.Example;
import io.jikkou.core.annotation.Title;
import io.jikkou.core.config.ConfigProperty;
import io.jikkou.core.exceptions.ConfigException;
import io.jikkou.core.exceptions.ValidationException;
import io.jikkou.core.extension.ExtensionContext;
import io.jikkou.core.validation.ValidationError;
import io.jikkou.core.validation.ValidationResult;
import io.jikkou.kafka.models.V1KafkaTopic;
import java.util.List;
import org.jetbrains.annotations.NotNull;

@Title("TopicNameSuffixValidation allows validating that topic names end with one of the defined suffixes.")
@Example(
        title = "Validate topic names end with one of the defined format suffixes.",
        full = true,
        code = {
                """
                        validations:
                        - name: "topicNameMustEndWithFormatSuffix"
                          type: "io.jikkou.kafka.validation.TopicNameSuffixValidation"
                          priority: 100
                          config:
                            topicNamePrefixes: [".avro", ".json", ".proto"]
                        """
        }
)
public class TopicNameSuffixValidation extends TopicValidation {

    public static final ConfigProperty<List<String>> VALIDATION_TOPIC_NAME_SUFFIXES_CONFIG = ConfigProperty
            .ofList("topicNameSuffixes")
            .displayName("Topic Name Suffixes")
            .description("List of valid suffixes that topic names must end with.");

    private List<String> suffixes;

    /**
     * {@inheritDoc}
     */
    @Override
    public void init(@NotNull final ExtensionContext context) {
        super.init(context);
        suffixes = VALIDATION_TOPIC_NAME_SUFFIXES_CONFIG.getOptional(context.configuration())
                .orElseThrow(() -> new ConfigException(
                        String.format("The '%s' configuration property is required for %s",
                                VALIDATION_TOPIC_NAME_SUFFIXES_CONFIG.key(),
                                TopicNameSuffixValidation.class.getSimpleName()
                        )
                ));
    }

    /**
     * {@inheritDoc}
     */
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
