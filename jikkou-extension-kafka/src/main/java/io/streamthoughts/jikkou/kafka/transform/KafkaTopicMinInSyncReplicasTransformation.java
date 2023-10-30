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

import static io.streamthoughts.jikkou.core.models.CoreAnnotations.JIKKOU_IO_TRANSFORM_PREFIX;

import io.streamthoughts.jikkou.core.annotation.HandledResource;
import io.streamthoughts.jikkou.core.annotation.Priority;
import io.streamthoughts.jikkou.core.config.ConfigProperty;
import io.streamthoughts.jikkou.core.config.Configuration;
import io.streamthoughts.jikkou.core.exceptions.ConfigException;
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
 * This transformation can be used to enforce a minimum value for the `min.insync.replicas` property of a kafka topic.
 */
@Priority(HasPriority.HIGHEST_PRECEDENCE)
@HandledResource(type = V1KafkaTopic.class)
public class KafkaTopicMinInSyncReplicasTransformation implements Transformation<V1KafkaTopic> {

    public static final String JIKKOU_IO_KAFKA_MIN_INSYNC_REPLICAS = JIKKOU_IO_TRANSFORM_PREFIX + "/kafka-min-sync-replicas";

    public static final ConfigProperty<Integer> MIN_INSYNC_REPLICAS_CONFIG = ConfigProperty
            .ofInt("minInSyncReplicas");

    private int minInSyncReplicas;

    /**
     * {@inheritDoc}
     **/
    @Override
    public void configure(@NotNull Configuration config) throws ConfigException {
        minInSyncReplicas = MIN_INSYNC_REPLICAS_CONFIG.getOptional(config)
                .orElseThrow(() -> new ConfigException(
                        String.format("The '%s' configuration property is required for transformation class: %s",
                                MIN_INSYNC_REPLICAS_CONFIG.key(),
                                KafkaTopicMinInSyncReplicasTransformation.class.getName()
                        )
                ));
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public @NotNull Optional<V1KafkaTopic> transform(@NotNull V1KafkaTopic resource,
                                                     @NotNull HasItems resources) {
        Optional<Integer> inSyncReplicas = getCurrentMinInSyncReplicas(resource);
        if (inSyncReplicas.isEmpty() || inSyncReplicas.get() < minInSyncReplicas) {
            return enforceConstraint(resource);
        }
        return Optional.of(resource);
    }

    private Optional<Integer> getCurrentMinInSyncReplicas(V1KafkaTopic resource) {
        V1KafkaTopicSpec spec = resource.getSpec();
        return Optional.ofNullable(spec.getConfigs())
                .filter(Predicate.not(Configs::isEmpty))
                .flatMap(it -> Optional.ofNullable(it.get(TopicConfig.MIN_IN_SYNC_REPLICAS_CONFIG)))
                .flatMap(it -> Optional.ofNullable(it.value()))
                .map(it -> Integer.parseInt(it.toString()));
    }

    @NotNull
    private Optional<V1KafkaTopic> enforceConstraint(@NotNull V1KafkaTopic resource) {
        V1KafkaTopicSpec spec = resource.getSpec();
        Configs configs = Optional.ofNullable(spec.getConfigs()).orElse(Configs.empty());
        configs.add(new ConfigValue(TopicConfig.MIN_IN_SYNC_REPLICAS_CONFIG, this.minInSyncReplicas));
        resource = HasMetadata.addMetadataAnnotation(resource,
                JIKKOU_IO_KAFKA_MIN_INSYNC_REPLICAS,
                this.minInSyncReplicas
        );
        return Optional.of(resource.withSpec(spec.withConfigs(configs)));
    }
}
