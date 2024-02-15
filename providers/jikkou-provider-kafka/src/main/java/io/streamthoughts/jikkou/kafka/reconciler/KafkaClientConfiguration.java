/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.kafka.reconciler;

import io.streamthoughts.jikkou.core.config.ConfigProperty;
import io.streamthoughts.jikkou.core.config.Configuration;
import io.streamthoughts.jikkou.kafka.internals.KafkaUtils;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

public final class KafkaClientConfiguration {

    public static final String KAFKA_CLIENT_CONFIG_NAME = "kafka.client";

    public static final ConfigProperty<Map<String, Object>> PRODUCER_CLIENT_CONFIG = ConfigProperty
            .ofMap(KAFKA_CLIENT_CONFIG_NAME)
            .orElse(HashMap::new)
            .map(KafkaUtils::getProducerClientConfigs);

    public static final ConfigProperty<Map<String, Object>> CONSUMER_CLIENT_CONFIG = ConfigProperty
            .ofMap(KAFKA_CLIENT_CONFIG_NAME)
            .orElse(HashMap::new)
            .map(KafkaUtils::getConsumerClientConfigs);

    public static final ConfigProperty<Map<String, Object>> ADMIN_CLIENT_CONFIG = ConfigProperty
            .ofMap(KAFKA_CLIENT_CONFIG_NAME)
            .orElse(HashMap::new)
            .map(KafkaUtils::getAdminClientConfigs);

    public static final ConfigProperty<Duration> KAFKA_DEFAULT_TIMEOUT_CONFIG = ConfigProperty
            .ofLong(KAFKA_CLIENT_CONFIG_NAME + ".defaultTimeoutMs")
            .map(Duration::ofMillis)
            .orElse(Duration.ofSeconds(30));

    private final Configuration configuration;

    public KafkaClientConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    public Duration getClientDefaultTimeout() {
        return KAFKA_DEFAULT_TIMEOUT_CONFIG.get(configuration);
    }
}
