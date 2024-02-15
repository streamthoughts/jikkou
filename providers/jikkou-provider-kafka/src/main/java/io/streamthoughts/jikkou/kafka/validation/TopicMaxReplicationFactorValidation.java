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

@Title("TopicMaxReplicationFactorValidation allows validating that topics are configured with a maximal replication factor.")
@Example(
        title = "Validate topics have a replication factor equals or less than '3'.",
        full = true,
        code = {"""
                validations:
                - name: "topicMustHaveReplicasEqualsOrLessThanThree"
                  type: "io.streamthoughts.jikkou.kafka.validation.TopicMaxReplicationFactorValidation"
                  priority: 100
                  config:
                    topicMaxReplicationFactor: 3
                """
        }
)
public final class TopicMaxReplicationFactorValidation extends TopicValidation {

    public static final ConfigProperty<Integer> VALIDATION_TOPIC_MAX_REPLICATION_FACTOR_CONFIG = ConfigProperty
            .ofInt("topicMaxReplicationFactor");

    private Integer maxReplicationFactor;

    /**
     * Empty constructor used by {@link Configuration}.
     */
    public TopicMaxReplicationFactorValidation() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void init(@NotNull final ExtensionContext context) {
        super.init(context);
        maxReplicationFactor = VALIDATION_TOPIC_MAX_REPLICATION_FACTOR_CONFIG.getOptional(context.appConfiguration())
                .orElseThrow(() -> new ConfigException(
                        String.format("The '%s' configuration property is required for %s",
                                VALIDATION_TOPIC_MAX_REPLICATION_FACTOR_CONFIG.key(),
                                TopicNameSuffixValidation.class.getSimpleName()
                        )
                ));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ValidationResult validate(final @NotNull V1KafkaTopic resource) throws ValidationException {
        Short replicas = resource.getSpec().getReplicas();
        if (replicas == null)
            return ValidationResult.success();

        if (!replicas.equals(KafkaTopics.NO_REPLICATION_FACTOR) && replicas > maxReplicationFactor) {
            String error = String.format(
                    "Replication factor for topic '%s' is greater than the maximum required: %d > %d",
                    resource.getMetadata().getName(),
                    replicas,
                    maxReplicationFactor
            );
            return ValidationResult.failure(new ValidationError(getName(), resource, error));
        }
        return ValidationResult.success();
    }
}
