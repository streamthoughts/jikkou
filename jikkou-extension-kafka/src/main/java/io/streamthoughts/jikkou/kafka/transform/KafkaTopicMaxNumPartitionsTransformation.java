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

import io.streamthoughts.jikkou.core.annotation.AcceptsResource;
import io.streamthoughts.jikkou.core.annotation.Priority;
import io.streamthoughts.jikkou.core.config.ConfigProperty;
import io.streamthoughts.jikkou.core.config.Configuration;
import io.streamthoughts.jikkou.core.exceptions.ConfigException;
import io.streamthoughts.jikkou.core.models.HasItems;
import io.streamthoughts.jikkou.core.models.HasMetadata;
import io.streamthoughts.jikkou.core.models.HasPriority;
import io.streamthoughts.jikkou.core.resource.transform.ResourceTransformation;
import io.streamthoughts.jikkou.kafka.models.V1KafkaTopic;
import io.streamthoughts.jikkou.kafka.models.V1KafkaTopicSpec;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;

/**
 * This transformation can be used to enforce a maximum number of partitions for a kafka topic.
 */
@Priority(HasPriority.HIGHEST_PRECEDENCE)
@AcceptsResource(type = V1KafkaTopic.class)
public class KafkaTopicMaxNumPartitionsTransformation implements ResourceTransformation<V1KafkaTopic> {

    public static final String JIKKOU_IO_KAFKA_MAX_NUM_PARTITION = JIKKOU_IO_TRANSFORM_PREFIX + "/kafka-max-num-partition";

    public static final ConfigProperty<Integer> MAX_NUM_PARTITIONS_CONFIG = ConfigProperty
            .ofInt("maxNumPartitions");

    private Integer maxNumPartitions;

    /**
     * {@inheritDoc}
     **/
    @Override
    public void configure(@NotNull Configuration config) throws ConfigException {
        maxNumPartitions = MAX_NUM_PARTITIONS_CONFIG.getOptional(config)
                .orElseThrow(() -> new ConfigException(
                        String.format("The '%s' configuration property is required for transformation class: %s",
                                MAX_NUM_PARTITIONS_CONFIG.key(),
                                KafkaTopicMaxNumPartitionsTransformation.class.getName()
                        )
                ));
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public @NotNull Optional<V1KafkaTopic> transform(@NotNull V1KafkaTopic resource,
                                                     @NotNull HasItems resources) {
        V1KafkaTopicSpec spec = resource.getSpec();
        Integer partitions = spec.getPartitions();
        if (partitions != null && partitions > maxNumPartitions) {
            V1KafkaTopicSpec newSpec = spec.withPartitions(maxNumPartitions);
            resource = HasMetadata.addMetadataAnnotation(resource, JIKKOU_IO_KAFKA_MAX_NUM_PARTITION, maxNumPartitions);
            return Optional.of(resource.withSpec(newSpec));
        }
        return Optional.of(resource);
    }
}
