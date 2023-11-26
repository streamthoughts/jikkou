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
import io.streamthoughts.jikkou.core.extension.ExtensionContext;
import io.streamthoughts.jikkou.core.validation.ValidationError;
import io.streamthoughts.jikkou.core.validation.ValidationResult;
import io.streamthoughts.jikkou.kafka.internals.KafkaTopics;
import io.streamthoughts.jikkou.kafka.models.V1KafkaTopic;
import org.jetbrains.annotations.NotNull;

@Title("TopicMinReplicationFactorValidation allows validating that topics are configured with a minimal replication factor.")
@Example(
        title = "Validate topics have a replication factor equals or greater than '1'.",
        full = true,
        code = {"""
            validations:
            - name: "topicMustHaveReplicasEqualsOrGreaterThanOne"
              type: "io.streamthoughts.jikkou.kafka.validation.TopicMinReplicationFactorValidation"
              priority: 100
              config:
                topicMinReplicationFactor: 1
            """
        }
)
public class TopicMinReplicationFactorValidation extends TopicValidation {

    public static final ConfigProperty<Integer> VALIDATION_TOPIC_MIN_REPLICATION_FACTOR_CONFIG = ConfigProperty
            .ofInt("topicMinReplicationFactor");


    private Integer minReplicationFactor;

    /**
     * Empty constructor used by {@link Configuration}.
     */
    public TopicMinReplicationFactorValidation() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void init(@NotNull final ExtensionContext context) {
        super.init(context);
        minReplicationFactor = VALIDATION_TOPIC_MIN_REPLICATION_FACTOR_CONFIG.getOptional(context.appConfiguration())
                .orElseThrow(() -> new ConfigException(
                        String.format("The '%s' configuration property is required for %s",
                                VALIDATION_TOPIC_MIN_REPLICATION_FACTOR_CONFIG.key(),
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

        if (!replicas.equals(KafkaTopics.NO_REPLICATION_FACTOR) && replicas < minReplicationFactor) {
            String error = String.format(
                    "Replication factor for topic '%s' is less than the minimum required: %d < %d",
                    resource.getMetadata().getName(),
                    replicas,
                    minReplicationFactor
            );
            return ValidationResult.failure(new ValidationError(getName(), resource, error));
        }

        return ValidationResult.success();
    }
}
