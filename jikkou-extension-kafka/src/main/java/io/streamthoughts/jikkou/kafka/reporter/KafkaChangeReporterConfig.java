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
package io.streamthoughts.jikkou.kafka.reporter;

import io.streamthoughts.jikkou.api.config.ConfigProperty;
import io.streamthoughts.jikkou.api.config.Configuration;
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
        return KAFKA_TOPIC_CONFIG.evaluate(configuration);
    }

    public Map<String, Object> clientConfig() {
        return PRODUCER_CLIENT_CONFIG.evaluate(configuration);
    }

    public String eventSource() {
        return EVENT_SOURCE_CONFIG.evaluate(configuration);
    }

    public boolean isTopicCreationEnabled() {
        return KAFKA_TOPIC_CREATION_ENABLED_CONFIG.evaluate(configuration);
    }

    public int defaultReplicationFactor() {
        return KAFKA_TOPIC_CREATION_DEFAULT_REPLICAS_CONFIG.evaluate(configuration);
    }
}
