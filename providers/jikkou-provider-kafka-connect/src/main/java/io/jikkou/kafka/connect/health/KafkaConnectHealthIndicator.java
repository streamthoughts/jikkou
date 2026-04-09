/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.jikkou.kafka.connect.health;

import io.jikkou.core.annotation.Description;
import io.jikkou.core.annotation.Named;
import io.jikkou.core.annotation.Title;
import io.jikkou.core.extension.ExtensionContext;
import io.jikkou.core.health.Health;
import io.jikkou.core.health.HealthAggregator;
import io.jikkou.core.health.HealthIndicator;
import io.jikkou.kafka.connect.KafkaConnectClusterConfigs;
import io.jikkou.kafka.connect.KafkaConnectExtensionProvider;
import io.jikkou.kafka.connect.api.KafkaConnectApi;
import io.jikkou.kafka.connect.api.KafkaConnectApiFactory;
import io.jikkou.kafka.connect.api.KafkaConnectClientConfig;
import io.jikkou.kafka.connect.api.data.ConnectCluster;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.NotNull;

/**
 * Health indicator for Kafka Connect clusters.
 */
@Named("kafkaconnect")
@Title("AivenServiceHealthIndicator allows checking whether Kafka Connect clusters are healthy.")
@Description("Reports the health status of Kafka Connect clusters.")
public class KafkaConnectHealthIndicator implements HealthIndicator {

    private static final String HEALTH_INDICATOR_NAME = "KafkaConnect";

    private KafkaConnectClusterConfigs configuration;

    /**
     * {@inheritDoc}
     **/
    @Override
    public void init(@NotNull ExtensionContext context) {
       init(context.<KafkaConnectExtensionProvider>provider().clusterConfigs());
    }

    public void init(KafkaConnectClusterConfigs configuration) {
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
                    .name(HEALTH_INDICATOR_NAME)
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
                .name(connectClientConfig.name());
        try {
            try {
                ConnectCluster cluster = api.getConnectCluster();
                builder = builder.up()
                        .details("version", cluster.version())
                        .details("commit", cluster.commit())
                        .details("kafkaClusterId", cluster.kafkaClusterId());
            } catch (Exception e) {
                Throwable t = e;
                if (e instanceof jakarta.ws.rs.ProcessingException pe) {
                    if (pe.getCause() != null) {
                        t = e.getCause();
                    }
                }
                builder = builder.down().exception(t);
            }
        } finally {
            api.close();
        }

        builder = builder
                .details("url", connectClientConfig.url());
        return builder.build();
    }
}
