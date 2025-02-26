/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.kafka.transform;

import static io.streamthoughts.jikkou.core.models.CoreAnnotations.JIKKOU_IO_TRANSFORM_PREFIX;
import static io.streamthoughts.jikkou.kafka.internals.KafkaTopics.NO_REPLICATION_FACTOR;

import io.streamthoughts.jikkou.core.ReconciliationContext;
import io.streamthoughts.jikkou.core.annotation.Priority;
import io.streamthoughts.jikkou.core.annotation.SupportedResource;
import io.streamthoughts.jikkou.core.config.ConfigProperty;
import io.streamthoughts.jikkou.core.exceptions.ConfigException;
import io.streamthoughts.jikkou.core.extension.ExtensionContext;
import io.streamthoughts.jikkou.core.models.HasItems;
import io.streamthoughts.jikkou.core.models.HasMetadata;
import io.streamthoughts.jikkou.core.models.HasPriority;
import io.streamthoughts.jikkou.core.transform.Transformation;
import io.streamthoughts.jikkou.kafka.models.V1KafkaTopic;
import io.streamthoughts.jikkou.kafka.models.V1KafkaTopicSpec;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * This transformation can be used to enforce a minimum number of replicas for a kafka topic.
 */
@Priority(HasPriority.HIGHEST_PRECEDENCE)
@SupportedResource(type = V1KafkaTopic.class)
public class KafkaTopicMinReplicasTransformation implements Transformation<V1KafkaTopic> {

    public static final String JIKKOU_IO_KAFKA_MIN_REPLICAS = JIKKOU_IO_TRANSFORM_PREFIX + "/kafka-min-replicas";

    public static final ConfigProperty<Integer> MIN_REPLICATION_FACTOR_CONFIG = ConfigProperty
            .ofInt("minReplicationFactor");

    private short minReplicationFactor;

    /**
     * {@inheritDoc}
     **/
    @Override
    public void init(@NotNull ExtensionContext context) {
        minReplicationFactor = MIN_REPLICATION_FACTOR_CONFIG.getOptional(context.configuration())
                .orElseThrow(() -> new ConfigException(
                        String.format("The '%s' configuration property is required for transformation class: %s",
                                MIN_REPLICATION_FACTOR_CONFIG.key(),
                                KafkaTopicMinReplicasTransformation.class.getName()
                        )
                )).shortValue();
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public @NotNull Optional<V1KafkaTopic> transform(@NotNull V1KafkaTopic resource,
                                                     @NotNull HasItems resources,
                                                     @NotNull ReconciliationContext context) {
        V1KafkaTopicSpec spec = resource.getSpec();
        Short replicas = spec.getReplicas();
        if (isLessThanMinReplicas(replicas)) {
            V1KafkaTopicSpec newSpec = spec.withReplicas(minReplicationFactor);
            resource = HasMetadata.addMetadataAnnotation(resource,
                    JIKKOU_IO_KAFKA_MIN_REPLICAS,
                    minReplicationFactor
            );
            return Optional.of(resource.withSpec(newSpec));
        }
        return Optional.of(resource);
    }

    private boolean isLessThanMinReplicas(@Nullable Short replicas) {
        return replicas != null && !replicas.equals(NO_REPLICATION_FACTOR) && replicas < minReplicationFactor;
    }
}
