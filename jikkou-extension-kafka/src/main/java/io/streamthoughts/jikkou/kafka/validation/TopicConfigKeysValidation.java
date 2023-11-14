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

import io.streamthoughts.jikkou.core.annotation.Enabled;
import io.streamthoughts.jikkou.core.annotation.Title;
import io.streamthoughts.jikkou.core.exceptions.ValidationException;
import io.streamthoughts.jikkou.core.models.Configs;
import io.streamthoughts.jikkou.core.validation.ValidationError;
import io.streamthoughts.jikkou.core.validation.ValidationResult;
import io.streamthoughts.jikkou.kafka.models.V1KafkaTopic;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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

        final Map<String, Object> topicConfigs = configs.toMap();
        final List<ValidationError> errors = topicConfigs.keySet()
                .stream()
                .filter(o -> !definedStaticConfigKeys.contains(o))
                .map(o -> newValidationError(resource, o))
                .toList();

        if (!errors.isEmpty()) {
            return new ValidationResult(errors);
        }
        return ValidationResult.success();
    }

    @NotNull
    private ValidationError newValidationError(final @NotNull V1KafkaTopic resource,
                                               final @NotNull String configKey) {
        var topicName = resource.getMetadata().getName();
        var message = String.format("Config key '%s' for topic '%s' is not valid", configKey, topicName);
        return new ValidationError(getName(), resource, message);
    }
}