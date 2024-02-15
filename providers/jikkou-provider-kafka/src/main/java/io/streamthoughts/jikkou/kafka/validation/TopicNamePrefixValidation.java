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
import io.streamthoughts.jikkou.core.config.Configuration;
import io.streamthoughts.jikkou.core.exceptions.ConfigException;
import io.streamthoughts.jikkou.core.exceptions.ValidationException;
import io.streamthoughts.jikkou.core.extension.ExtensionContext;
import io.streamthoughts.jikkou.core.validation.ValidationError;
import io.streamthoughts.jikkou.core.validation.ValidationResult;
import io.streamthoughts.jikkou.kafka.models.V1KafkaTopic;
import java.util.List;
import org.jetbrains.annotations.NotNull;

@Title("TopicNamePrefixValidation allows validating that topic names start with one of the defined prefixes.")
@Example(
        title = "Validate topic names start with one of the defined visibility prefixes.",
        full = true,
        code = {"""
                validations:
                - name: "topicsMustStartWithVisibilityPrefix"
                  type: "io.streamthoughts.jikkou.kafka.validation.TopicNamePrefixValidation"
                  priority: 100
                  config:
                    topicNamePrefixes: ["public", "private", "protected"]
                """
        }
)
public final class TopicNamePrefixValidation extends TopicValidation {

    public static final ConfigProperty<List<String>> VALIDATION_TOPIC_NAME_PREFIXES_CONFIG = ConfigProperty
            .ofList("topicNamePrefixes");

    private List<String> prefixes;

    /**
     * Empty constructor used by {@link Configuration}.
     */
    public TopicNamePrefixValidation() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void init(@NotNull final ExtensionContext context) {
        super.init(context);
        prefixes = VALIDATION_TOPIC_NAME_PREFIXES_CONFIG.getOptional(context.appConfiguration())
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
    public ValidationResult validate(final @NotNull V1KafkaTopic resource) throws ValidationException {
        if (prefixes == null) {
            throw new IllegalStateException("No prefix was configured");
        }
        final boolean matched = prefixes.stream()
                .filter(prefix -> resource.getMetadata().getName().startsWith(prefix))
                .findAny()
                .isEmpty();
        if (matched) {
            String error = String.format(
                    "Name for topic '%s' does not start with one of the configured prefixes: %s",
                    resource.getMetadata().getName(),
                    prefixes
            );
            return ValidationResult.failure(new ValidationError(getName(), resource, error));
        }
        return ValidationResult.success();
    }
}
