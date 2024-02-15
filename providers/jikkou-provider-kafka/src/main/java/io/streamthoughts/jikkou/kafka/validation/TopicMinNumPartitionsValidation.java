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

@Title("TopicMinNumPartitionsValidation allows validating that topics are configured with a minimal number of partitions.")
@Example(
        title = "Validate topics have a number of partitions equals or greater than '1'.",
        full = true,
        code = {"""
                validations:
                - name: "topicMustHavePartitionsEqualsOrGreaterThanOne"
                  type: "io.streamthoughts.jikkou.kafka.validation.TopicMinNumPartitionsValidation"
                  priority: 100
                  config:
                    topicMinNumPartitions: 1
                """
        }
)
public class TopicMinNumPartitionsValidation extends TopicValidation {

    public static final ConfigProperty<Integer> VALIDATION_TOPIC_MIN_NUM_PARTITIONS_CONFIG = ConfigProperty
            .ofInt("topicMinNumPartitions");

    private Integer minNumPartitions;

    /**
     * Empty constructor used by {@link Configuration}.
     */
    public TopicMinNumPartitionsValidation() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void init(@NotNull final ExtensionContext context) {
        super.init(context);
        minNumPartitions = VALIDATION_TOPIC_MIN_NUM_PARTITIONS_CONFIG.getOptional(context.appConfiguration())
                .orElseThrow(() -> new ConfigException(
                        String.format("The '%s' configuration property is required for %s",
                                VALIDATION_TOPIC_MIN_NUM_PARTITIONS_CONFIG.key(),
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

        if (!partitions.equals(KafkaTopics.NO_NUM_PARTITIONS) && partitions < minNumPartitions) {
            String error = String.format(
                    "Number of partitions for topic '%s' is less than the minimum required: %d < %d",
                    resource.getMetadata().getName(),
                    partitions,
                    minNumPartitions
            );
            return ValidationResult.failure(new ValidationError(getName(), resource, error));
        }
        return ValidationResult.success();
    }
}
