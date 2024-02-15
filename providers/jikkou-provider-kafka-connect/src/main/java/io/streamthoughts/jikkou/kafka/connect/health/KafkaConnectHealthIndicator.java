/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.kafka.connect.health;

import io.streamthoughts.jikkou.core.annotation.Named;
import io.streamthoughts.jikkou.core.annotation.Title;
import io.streamthoughts.jikkou.core.exceptions.ConfigException;
import io.streamthoughts.jikkou.core.extension.ExtensionContext;
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
@Title("AivenServiceHealthIndicator allows checking whether Kafka Connect clusters are healthy.")
public class KafkaConnectHealthIndicator implements HealthIndicator {

    private static final String HEALTH_INDICATOR_NAME = "KafkaConnect";

    private KafkaConnectExtensionConfig configuration;

    /**
     * {@inheritDoc}
     **/
    @Override
    public void init(@NotNull ExtensionContext context) {
        configure(new KafkaConnectExtensionConfig(context.appConfiguration()));
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
                .name(connectClientConfig.getConnectClusterName());
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
                .details("url", connectClientConfig.getConnectUrl());
        return builder.build();
    }
}
