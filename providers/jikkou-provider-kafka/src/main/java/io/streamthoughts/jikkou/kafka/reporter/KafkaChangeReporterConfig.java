/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.kafka.reporter;

import io.streamthoughts.jikkou.core.config.ConfigProperty;
import io.streamthoughts.jikkou.core.config.Configuration;
import io.streamthoughts.jikkou.kafka.internals.KafkaUtils;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

/**
 * This reporter can be used to send change results into a kafka topic as Cloud Event.
 */
public class KafkaChangeReporterConfig {

    public static final ConfigProperty<String> EVENT_SOURCE_CONFIG = ConfigProperty
            .ofString("event.source")
            .description("The 'source' of the event that will be generated");

    public static final ConfigProperty<String> KAFKA_TOPIC_CONFIG = ConfigProperty
            .ofString("kafka.topic.name")
            .description("The name of the topic the events will be sent");

    public static final ConfigProperty<Boolean> KAFKA_TOPIC_CREATION_ENABLED_CONFIG = ConfigProperty
            .ofBoolean("kafka.topic.creation.enabled")
            .orElse(true)
            .description("");

    public static final ConfigProperty<Integer> KAFKA_TOPIC_CREATION_DEFAULT_REPLICAS_CONFIG = ConfigProperty
            .ofInt("kafka.topic.creation.defaultReplicationFactor")
            .orElse(1)
            .description("The default replication factor used for creating topic");

    public static final ConfigProperty<Map<String, Object>> PRODUCER_CLIENT_CONFIG = ConfigProperty
            .ofMap("kafka.client")
            .orElse(HashMap::new)
            .map(KafkaUtils::getProducerClientConfigs);

    public static final ConfigProperty<Map<String, Object>> ADMIN_CLIENT_CONFIG = ConfigProperty
            .ofMap("kafka.client")
            .orElse(HashMap::new)
            .map(KafkaUtils::getAdminClientConfigs);

    private final Configuration configuration;

    /**
     * Creates a new {@link KafkaChangeReporterConfig} instance.
     *
     * @param configuration the configuration.
     */
    public KafkaChangeReporterConfig(@NotNull final Configuration configuration) {
        this.configuration = Objects.requireNonNull(configuration, "configuration must not be null");
    }

    public String topicName() {
        return KAFKA_TOPIC_CONFIG.get(configuration);
    }

    public Map<String, Object> producerConfig() {
        return PRODUCER_CLIENT_CONFIG.get(configuration);
    }

    public Map<String, Object> adminClientConfig() {
        return ADMIN_CLIENT_CONFIG.get(configuration);
    }

    public String eventSource() {
        return EVENT_SOURCE_CONFIG.get(configuration);
    }

    public boolean isTopicCreationEnabled() {
        return KAFKA_TOPIC_CREATION_ENABLED_CONFIG.get(configuration);
    }

    public int defaultReplicationFactor() {
        return KAFKA_TOPIC_CREATION_DEFAULT_REPLICAS_CONFIG.get(configuration);
    }
}
