/*
 * Copyright 2021 StreamThoughts.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.streamthoughts.jikkou.kafka.validations;

import io.streamthoughts.jikkou.api.annotations.ExtensionEnabled;
import io.streamthoughts.jikkou.api.config.ConfigProperty;
import io.streamthoughts.jikkou.api.config.Configuration;
import io.streamthoughts.jikkou.api.error.ConfigException;
import io.streamthoughts.jikkou.api.error.ValidationException;
import io.streamthoughts.jikkou.kafka.internals.KafkaTopics;
import io.streamthoughts.jikkou.kafka.models.V1KafkaTopic;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;

@ExtensionEnabled(value = false)
public class TopicMinNumPartitionsValidation extends TopicValidation {

    public static final ConfigProperty<Integer> VALIDATION_TOPIC_MIN_NUM_PARTITIONS_CONFIG = ConfigProperty
            .ofInt("topic-min-num-partitions");

    private Integer minNumPartitions;

    /**
     * Empty constructor used by {@link Configuration}.
     */
    public TopicMinNumPartitionsValidation(){}

    /**
     * Creates a new {@link TopicMinNumPartitionsValidation}
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
    public void validate(final @NotNull V1KafkaTopic resource) throws ValidationException {
        Optional.ofNullable(resource.getSpec().getPartitions()).ifPresent(p -> {
            if (!p.equals(KafkaTopics.NO_NUM_PARTITIONS) && p < minNumPartitions) {
                throw new ValidationException(String.format(
                        "Number of partitions for topic '%s' is less than the minimum required: %d < %d",
                        resource.getMetadata().getName(),
                        p,
                        minNumPartitions
                ), this);
            }
        });
    }
}
