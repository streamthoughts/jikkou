/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.kafka.validation;

import io.streamthoughts.jikkou.core.annotation.Example;
import io.streamthoughts.jikkou.core.annotation.Title;
import io.streamthoughts.jikkou.core.config.ConfigProperty;
import io.streamthoughts.jikkou.core.exceptions.ConfigException;
import io.streamthoughts.jikkou.core.exceptions.ValidationException;
import io.streamthoughts.jikkou.core.extension.ExtensionContext;
import io.streamthoughts.jikkou.core.validation.ValidationError;
import io.streamthoughts.jikkou.core.validation.ValidationResult;
import io.streamthoughts.jikkou.kafka.models.V1KafkaTopic;
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
                          type: "io.streamthoughts.jikkou.kafka.validation.TopicNameSuffixValidation"
                          priority: 100
                          config:
                            topicNamePrefixes: [".avro", ".json", ".proto"]
                        """
        }
)
public class TopicNameSuffixValidation extends TopicValidation {

    public static final ConfigProperty<List<String>> VALIDATION_TOPIC_NAME_SUFFIXES_CONFIG = ConfigProperty
            .ofList("topicNameSuffixes");

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
