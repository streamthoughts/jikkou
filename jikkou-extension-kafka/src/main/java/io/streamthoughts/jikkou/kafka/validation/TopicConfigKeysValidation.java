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

import io.streamthoughts.jikkou.api.error.ValidationException;
import io.streamthoughts.jikkou.api.model.Configs;
import io.streamthoughts.jikkou.kafka.models.V1KafkaTopic;
import io.vavr.collection.Array;
import io.vavr.collection.List;
import io.vavr.control.Option;
import io.vavr.control.Try;
import java.util.Map;
import org.apache.kafka.common.config.TopicConfig;
import org.jetbrains.annotations.NotNull;

/**
 * A {@link TopicValidation} implementation to verify that all config-keys for a topic are valid.
 *
 * @see TopicConfig
 */
public class TopicConfigKeysValidation extends TopicValidation {

    /** {@inheritDoc} */
    @Override
    public void validate(final @NotNull V1KafkaTopic resource) throws ValidationException {

        Configs configs = resource.getSpec().getConfigs();
        if (configs == null || configs.isEmpty())
            return;

        final Array<String> definedStaticConfigKeys = Array
                .of(TopicConfig.class.getDeclaredFields())
                .flatMap(f -> f.trySetAccessible() ? Try.of(() -> f.get(null).toString()).toOption() : Option.none());

        final Map<String, Object> topicConfigs = configs.toMap();
        final List<ValidationException> errors = List
                .ofAll(topicConfigs.entrySet())
                .flatMap(e -> definedStaticConfigKeys.contains(e.getKey()) ?
                        Option.none() :
                        Option.of(newValidationError(resource.getMetadata().getName(), e.getKey()))
                );

        if (!errors.isEmpty()) {
            throw new ValidationException(errors.toJavaList());
        }
    }

    @NotNull
    private ValidationException newValidationError(final @NotNull String topicName,
                                                   final @NotNull String configKey) {
        var message = String.format("Config key '%s' for topic '%s' is not valid", configKey, topicName);
        return new ValidationException(message, this);
    }
}