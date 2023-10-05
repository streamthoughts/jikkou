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
package io.streamthoughts.jikkou.kafka.transform;

import static io.streamthoughts.jikkou.CoreAnnotations.JIKKOU_IO_TRANSFORM_PREFIX;

import io.streamthoughts.jikkou.annotation.AcceptsResource;
import io.streamthoughts.jikkou.annotation.ExtensionEnabled;
import io.streamthoughts.jikkou.annotation.Priority;
import io.streamthoughts.jikkou.api.config.ConfigProperty;
import io.streamthoughts.jikkou.api.config.Configuration;
import io.streamthoughts.jikkou.api.error.ConfigException;
import io.streamthoughts.jikkou.api.model.HasItems;
import io.streamthoughts.jikkou.api.model.HasMetadata;
import io.streamthoughts.jikkou.api.model.HasPriority;
import io.streamthoughts.jikkou.api.transform.ResourceTransformation;
import io.streamthoughts.jikkou.kafka.models.V1KafkaTopic;
import io.streamthoughts.jikkou.kafka.models.V1KafkaTopicSpec;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;

/**
 * This transformation can be used to enforce a minimum number of replicas for a kafka topic.
 */
@Priority(HasPriority.HIGHEST_PRECEDENCE)
@AcceptsResource(type = V1KafkaTopic.class)
@ExtensionEnabled(value = false)
public class KafkaTopicMaxReplicasTransformation implements ResourceTransformation<V1KafkaTopic> {

    public static final String JIKKOU_IO_KAFKA_MAX_REPLICAS = JIKKOU_IO_TRANSFORM_PREFIX +  "/kafka-max-replicas";

    public static final ConfigProperty<Integer> MAX_REPLICATION_FACTOR_CONFIG = ConfigProperty
            .ofInt("maxReplicationFactor");

    private short maxReplicationFactor;

    /**
     * {@inheritDoc}
     **/
    @Override
    public void configure(@NotNull Configuration config) throws ConfigException {
        maxReplicationFactor = MAX_REPLICATION_FACTOR_CONFIG.getOptional(config)
                .orElseThrow(() -> new ConfigException(
                        String.format("The '%s' configuration property is required for transformation class: %s",
                                MAX_REPLICATION_FACTOR_CONFIG.key(),
                                KafkaTopicMaxReplicasTransformation.class.getName()
                        )
                )).shortValue();
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public @NotNull Optional<V1KafkaTopic> transform(@NotNull V1KafkaTopic resource,
                                                     @NotNull HasItems resources) {
        V1KafkaTopicSpec spec = resource.getSpec();
        Short replicas = spec.getReplicas();
        if (isGreaterThanMaxReplicas(replicas)) {
            resource = HasMetadata.addMetadataAnnotation(resource,
                    JIKKOU_IO_KAFKA_MAX_REPLICAS,
                    maxReplicationFactor
            );
            V1KafkaTopicSpec newSpec = spec.withReplicas(maxReplicationFactor);
            return Optional.of(resource.withSpec(newSpec));
        }
        return Optional.of(resource);
    }

    private boolean isGreaterThanMaxReplicas(Short replicas) {
        return replicas != null && replicas > maxReplicationFactor;
    }
}
