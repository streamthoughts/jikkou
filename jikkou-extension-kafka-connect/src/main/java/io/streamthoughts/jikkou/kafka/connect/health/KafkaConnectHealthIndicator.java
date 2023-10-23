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
package io.streamthoughts.jikkou.kafka.connect.health;

import io.streamthoughts.jikkou.core.annotation.Description;
import io.streamthoughts.jikkou.core.annotation.Named;
import io.streamthoughts.jikkou.core.config.Configuration;
import io.streamthoughts.jikkou.core.exceptions.ConfigException;
import io.streamthoughts.jikkou.core.health.Health;
import io.streamthoughts.jikkou.core.health.HealthAggregator;
import io.streamthoughts.jikkou.core.health.HealthIndicator;
import io.streamthoughts.jikkou.kafka.connect.KafkaConnectExtensionConfig;
import io.streamthoughts.jikkou.kafka.connect.api.KafkaConnectApi;
import io.streamthoughts.jikkou.kafka.connect.api.KafkaConnectApiFactory;
import io.streamthoughts.jikkou.kafka.connect.api.KafkaConnectClientConfig;
import io.streamthoughts.jikkou.kafka.connect.api.data.ConnectCluster;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.NotNull;

/**
 * Health indicator for Kafka Connect clusters.
 */
@Named("kafkaconnect")
@Description("Get the health of Kafka Connect clusters")
public class KafkaConnectHealthIndicator implements HealthIndicator {

    private static final String HEALTH_INDICATOR_NAME = "KafkaConnect";

    private KafkaConnectExtensionConfig configuration;

    /**
     * {@inheritDoc}
     **/
    @Override
    public void configure(@NotNull Configuration configuration) throws ConfigException {
        configure(new KafkaConnectExtensionConfig(configuration));
    }

    public void configure(@NotNull KafkaConnectExtensionConfig configuration) throws ConfigException {
        this.configuration = configuration;
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public Health getHealth(Duration timeout) {
        Map<String, KafkaConnectClientConfig> configurations = configuration.getConfigurationsByClusterName();
        if (configurations.isEmpty()) {
            return Health
                    .builder()
                    .withName(HEALTH_INDICATOR_NAME)
                    .unknown()
                    .build();
        }
        List<Health> indicators = new ArrayList<>(configurations.size());
        for (KafkaConnectClientConfig config : configurations.values()) {
            indicators.add(getHealth(timeout, config));
        }
        HealthAggregator aggregator = new HealthAggregator();
        return aggregator.aggregate(HEALTH_INDICATOR_NAME, indicators);
    }

    public Health getHealth(Duration timeout, KafkaConnectClientConfig connectClientConfig) {
        KafkaConnectApi api = KafkaConnectApiFactory.create(connectClientConfig, timeout);
        Health.Builder builder = Health
                .builder()
                .withName(connectClientConfig.getConnectClusterName());
        try {
            try {
                ConnectCluster cluster = api.getConnectCluster();
                builder = builder.up()
                        .withDetails("version", cluster.version())
                        .withDetails("commit", cluster.commit())
                        .withDetails("kafkaClusterId", cluster.kafkaClusterId());
            } catch (Exception e) {
                builder = builder.down().withException(e);
            }
        } finally {
            api.close();
        }

        builder = builder
                .withDetails("url", connectClientConfig.getConnectUrl());
        return builder.build();
    }
}
