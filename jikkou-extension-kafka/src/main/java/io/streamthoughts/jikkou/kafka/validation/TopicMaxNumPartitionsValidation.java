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

import io.streamthoughts.jikkou.core.config.ConfigProperty;
import io.streamthoughts.jikkou.core.config.Configuration;
import io.streamthoughts.jikkou.core.exceptions.ConfigException;
import io.streamthoughts.jikkou.core.exceptions.ValidationException;
import io.streamthoughts.jikkou.core.resource.validation.ValidationError;
import io.streamthoughts.jikkou.core.resource.validation.ValidationResult;
import io.streamthoughts.jikkou.kafka.internals.KafkaTopics;
import io.streamthoughts.jikkou.kafka.models.V1KafkaTopic;
import org.jetbrains.annotations.NotNull;

public class TopicMaxNumPartitionsValidation extends TopicValidation {

    public static final ConfigProperty<Integer> VALIDATION_TOPIC_MAX_NUM_PARTITIONS_CONFIG = ConfigProperty
            .ofInt("topicMaxNumPartitions");

    private Integer maxNumPartitions;

    /**
     * Empty constructor used by {@link Configuration}.
     */
    public TopicMaxNumPartitionsValidation() {
    }

    /**
     * Creates a new {@link TopicMaxNumPartitionsValidation}
     *
     * @param maxNumPartitions the min number of partitions.
     */
    public TopicMaxNumPartitionsValidation(final int maxNumPartitions) {
        configure(VALIDATION_TOPIC_MAX_NUM_PARTITIONS_CONFIG.asConfiguration(maxNumPartitions));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void configure(@NotNull final Configuration config) throws ConfigException {
        super.configure(config);
        maxNumPartitions = VALIDATION_TOPIC_MAX_NUM_PARTITIONS_CONFIG.getOptional(config)
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
