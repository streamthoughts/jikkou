/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.kafka.transform;

import static io.streamthoughts.jikkou.core.models.CoreAnnotations.JIKKOU_IO_TRANSFORM_PREFIX;

import io.streamthoughts.jikkou.core.ReconciliationContext;
import io.streamthoughts.jikkou.core.annotation.Priority;
import io.streamthoughts.jikkou.core.annotation.SupportedResource;
import io.streamthoughts.jikkou.core.config.ConfigProperty;
import io.streamthoughts.jikkou.core.exceptions.ConfigException;
import io.streamthoughts.jikkou.core.extension.ExtensionContext;
import io.streamthoughts.jikkou.core.models.ConfigValue;
import io.streamthoughts.jikkou.core.models.Configs;
import io.streamthoughts.jikkou.core.models.HasItems;
import io.streamthoughts.jikkou.core.models.HasMetadata;
import io.streamthoughts.jikkou.core.models.HasPriority;
import io.streamthoughts.jikkou.core.transform.Transformation;
import io.streamthoughts.jikkou.kafka.models.V1KafkaTopic;
import io.streamthoughts.jikkou.kafka.models.V1KafkaTopicSpec;
import java.util.Optional;
import java.util.function.Predicate;
import org.apache.kafka.common.config.TopicConfig;
import org.jetbrains.annotations.NotNull;

/**
 * This transformation can be used to enforce a maximum number of replicas for a kafka topic.
 */
@Priority(HasPriority.HIGHEST_PRECEDENCE)
@SupportedResource(type = V1KafkaTopic.class)
public class KafkaTopicMaxRetentionMsTransformation implements Transformation<V1KafkaTopic> {

    public static final String JIKKOU_IO_KAFKA_MAX_RETENTION_MS = JIKKOU_IO_TRANSFORM_PREFIX + "/kafka-max-retention-ms";

    public static final ConfigProperty<Long> MAX_RETENTIONS_MS_CONFIG = ConfigProperty
            .ofLong("maxRetentionMs");

    private Long maxRetentionMs;

    /**
     * {@inheritDoc}
     **/
    @Override
    public void init(@NotNull ExtensionContext context) {
        maxRetentionMs = MAX_RETENTIONS_MS_CONFIG.getOptional(context.appConfiguration())
                .orElseThrow(() -> new ConfigException(
                        String.format("The '%s' configuration property is required for transformation class: %s",
                                MAX_RETENTIONS_MS_CONFIG.key(),
                                KafkaTopicMaxRetentionMsTransformation.class.getName()
                        )
                ));
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public @NotNull Optional<V1KafkaTopic> transform(@NotNull V1KafkaTopic resource,
                                                     @NotNull HasItems resources,
                                                     @NotNull ReconciliationContext context) {
        Optional<Long> retentionMs = getCurrentRetentionMs(resource);
        if (retentionMs.isEmpty() || retentionMs.get() > maxRetentionMs) {
            return enforceConstraint(resource);
        }
        return Optional.of(resource);
    }

    private Optional<Long> getCurrentRetentionMs(V1KafkaTopic resource) {
        V1KafkaTopicSpec spec = resource.getSpec();
        return Optional.ofNullable(spec.getConfigs())
                .filter(Predicate.not(Configs::isEmpty))
                .flatMap(it -> Optional.ofNullable(it.get(TopicConfig.RETENTION_MS_CONFIG)))
                .flatMap(it -> Optional.ofNullable(it.value()))
                .map(it -> Long.parseLong(it.toString()));
    }


    @NotNull
    private Optional<V1KafkaTopic> enforceConstraint(@NotNull V1KafkaTopic resource) {
        V1KafkaTopicSpec spec = resource.getSpec();
        Configs configs = Optional.ofNullable(spec.getConfigs()).orElse(Configs.empty());
        configs.add(new ConfigValue(TopicConfig.RETENTION_MS_CONFIG, maxRetentionMs));
        resource = HasMetadata.addMetadataAnnotation(resource,
                JIKKOU_IO_KAFKA_MAX_RETENTION_MS,
                maxRetentionMs
        );
        return Optional.of(resource.withSpec(spec.withConfigs(configs)));
    }
}
