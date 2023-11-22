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
     * Creates a new {@link TopicMinNumPartitionsValidation}
     *
     * @param minNumPartitions the min number of partitions.
     */
    public TopicMinNumPartitionsValidation(final int minNumPartitions) {
        configure(VALIDATION_TOPIC_MIN_NUM_PARTITIONS_CONFIG.asConfiguration(minNumPartitions));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void configure(@NotNull final Configuration config) throws ConfigException {
        super.configure(config);
        minNumPartitions = VALIDATION_TOPIC_MIN_NUM_PARTITIONS_CONFIG.getOptional(config)
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