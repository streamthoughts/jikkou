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
import io.streamthoughts.jikkou.kafka.internals.KafkaTopics;
import io.streamthoughts.jikkou.kafka.models.V1KafkaTopic;
import org.jetbrains.annotations.NotNull;

@Title("TopicMaxNumPartitionsValidation allows validating that topics are configured with a maximal number of partitions.")
@Example(
        title = "Validate topics have a number of partitions equals or less than '50'.",
        full = true,
        code = {"""
                validations:
                - name: "topicMustHavePartitionsEqualsOrLessThanFifty"
                  type: "io.streamthoughts.jikkou.kafka.validation.TopicMaxNumPartitionsValidation"
                  priority: 100
                  config:
                    topicMaxNumPartitions: 50
                """
        }
)
public final class TopicMaxNumPartitionsValidation extends TopicValidation {

    public static final ConfigProperty<Integer> VALIDATION_TOPIC_MAX_NUM_PARTITIONS_CONFIG = ConfigProperty
            .ofInt("topicMaxNumPartitions");

    private Integer maxNumPartitions;

    /**
     * Empty constructor used by {@link Configuration}.
     */
    public TopicMaxNumPartitionsValidation() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void init(@NotNull final ExtensionContext context) {
        super.init(context);
        maxNumPartitions = VALIDATION_TOPIC_MAX_NUM_PARTITIONS_CONFIG.getOptional(context.appConfiguration())
                .orElseThrow(() -> new ConfigException(
                        String.format("The '%s' configuration property is required for %s",
                                VALIDATION_TOPIC_MAX_NUM_PARTITIONS_CONFIG.key(),
                                TopicNameSuffixValidation.class.getSimpleName()
                        )
                ));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ValidationResult validate(final @NotNull V1KafkaTopic resource) throws ValidationException {
        Integer partitions = resource.getSpec().getPartitions();
        if (partitions == null)
            return ValidationResult.success();

        if (!partitions.equals(KafkaTopics.NO_NUM_PARTITIONS) && partitions > maxNumPartitions) {
            String error = String.format(
                    "Number of partitions for topic '%s' is greater than the maximum required: %d > %d",
                    resource.getMetadata().getName(),
                    partitions,
                    maxNumPartitions
            );
            return ValidationResult.failure(new ValidationError(getName(), resource, error));
        }
        return ValidationResult.success();
    }
}
