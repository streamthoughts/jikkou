/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.kafka.validation;

import io.streamthoughts.jikkou.core.annotation.Enabled;
import io.streamthoughts.jikkou.core.annotation.Title;
import io.streamthoughts.jikkou.core.exceptions.ValidationException;
import io.streamthoughts.jikkou.core.models.ConfigValue;
import io.streamthoughts.jikkou.core.models.Configs;
import io.streamthoughts.jikkou.core.validation.ValidationError;
import io.streamthoughts.jikkou.core.validation.ValidationResult;
import io.streamthoughts.jikkou.kafka.KafkaExtensionProvider;
import io.streamthoughts.jikkou.kafka.models.V1KafkaTopic;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.apache.kafka.common.config.TopicConfig;
import org.jetbrains.annotations.NotNull;

/**
 * A {@link TopicValidation} implementation to verify that all config-keys for a topic are valid.
 *
 * @see TopicConfig
 */
@Enabled
@Title("TopicConfigKeysValidation verifies that provided topic configuration keys are validated.")
public class TopicConfigKeysValidation extends TopicValidation {

    /**
     * {@inheritDoc}
     */
    @Override
    public ValidationResult validate(final @NotNull V1KafkaTopic resource) throws ValidationException {

        Configs configs = resource.getSpec().getConfigs();
        if (configs == null || configs.isEmpty())
            return ValidationResult.success();

        final List<String> definedStaticConfigKeys = Arrays
                .stream(TopicConfig.class.getDeclaredFields())
                .flatMap(f -> {
                    try {
                        return f.trySetAccessible() ? Optional.ofNullable(f.get(null)).map(Object::toString).stream() : Stream.empty();
                    } catch (IllegalAccessException e) {
                        return Stream.empty();
                    }
                })
                .toList();

        List<Pattern> ignoreConfigKeys = context().<KafkaExtensionProvider>provider().topicValidationIgnoreConfigKeys();
        final List<ValidationError> errors = configs.flatten().values().stream()
                .map(ConfigValue::name)
                .filter(o -> ignoreConfigKeys.stream().noneMatch(pattern -> pattern.matcher(o).matches()))
                .filter(o -> !definedStaticConfigKeys.contains(o))
                .map(o -> newValidationError(resource, o))
                .toList();

        return !errors.isEmpty() ? new ValidationResult(errors) : ValidationResult.success();
    }

    @NotNull
    private ValidationError newValidationError(final @NotNull V1KafkaTopic resource,
                                               final @NotNull String configKey) {
        var topicName = resource.getMetadata().getName();
        var message = String.format("Config key '%s' for topic '%s' is not valid", configKey, topicName);
        return new ValidationError(getName(), resource, message);
    }
}