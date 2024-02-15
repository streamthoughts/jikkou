/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.kafka.health;

import static io.streamthoughts.jikkou.kafka.health.KafkaBrokerHealthIndicator.HEALTH_NAME;

import io.streamthoughts.jikkou.core.annotation.Description;
import io.streamthoughts.jikkou.core.annotation.Named;
import io.streamthoughts.jikkou.core.annotation.Title;
import io.streamthoughts.jikkou.core.config.Configuration;
import io.streamthoughts.jikkou.core.exceptions.ConfigException;
import io.streamthoughts.jikkou.core.extension.ContextualExtension;
import io.streamthoughts.jikkou.core.extension.ExtensionContext;
import io.streamthoughts.jikkou.core.health.Health;
import io.streamthoughts.jikkou.core.health.HealthIndicator;
import io.streamthoughts.jikkou.kafka.internals.admin.AdminClientContext;
import io.streamthoughts.jikkou.kafka.internals.admin.AdminClientContextFactory;
import io.streamthoughts.jikkou.kafka.reconciler.KafkaClientConfiguration;
import java.time.Duration;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.DescribeClusterOptions;
import org.apache.kafka.clients.admin.DescribeClusterResult;
import org.apache.kafka.common.Node;
import org.jetbrains.annotations.NotNull;

@Named(HEALTH_NAME)
@Title("KafkaBrokerHealthIndicator allows checking whether the Kafka cluster is healthy.")
@Description("Get the health of kafka brokers")
public final class KafkaBrokerHealthIndicator extends ContextualExtension implements HealthIndicator {

    public static final String HEALTH_NAME = "kafka";

    private AdminClientContextFactory adminClientContextFactory;

    private KafkaClientConfiguration configuration;

    /**
     * Creates a new {@link KafkaBrokerHealthIndicator} instance.
     * Empty constructor required for CLI.
     */
    public KafkaBrokerHealthIndicator() {
    }

    /**
     * Creates a new {@link KafkaBrokerHealthIndicator} instance.
     *
     * @param adminClientContextFactory the AdminClientContext factory.
     */
    public KafkaBrokerHealthIndicator(final @NotNull AdminClientContextFactory adminClientContextFactory) {
        this.adminClientContextFactory = adminClientContextFactory;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void init(@NotNull ExtensionContext context) throws ConfigException {
        Configuration appConfiguration = context.appConfiguration();
        if (adminClientContextFactory == null) {
            this.adminClientContextFactory = new AdminClientContextFactory(appConfiguration);
        }
        this.configuration = new KafkaClientConfiguration(appConfiguration);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Health getHealth(final Duration timeout) {
        if (adminClientContextFactory == null) {
            throw new IllegalStateException("not configured");
        }
        try (AdminClientContext context = adminClientContextFactory.createAdminClientContext()) {
            return getHealth(context.getAdminClient(), timeout);
        }
    }

    private Health getHealth(@NotNull final AdminClient client,
                             @NotNull final Duration timeout) {
        try {
            DescribeClusterResult result = client.describeCluster(
                    new DescribeClusterOptions().timeoutMs((int) timeout.toMillis()));

            Collection<Node> nodes = result.nodes().get();
            var clusterId = result.clusterId().get();
            Health.Builder builder = new Health.Builder()
                    .up()
                    .name(HEALTH_NAME);

            List<Map<String, Object>> brokers = nodes
                    .stream()
                    .map(node -> {
                        Map<String, Object> details = new LinkedHashMap<>();
                        details.put("id", node.idString());
                        details.put("host", node.host());
                        details.put("port", node.port());
                        return details;
                    }).toList();
            builder
                    .details("resource", "urn:kafka:cluster:id:" + clusterId)
                    .details("brokers", brokers);

            return builder.build();
        } catch (InterruptedException | ExecutionException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            return new Health.Builder()
                    .unknown()
                    .name(HEALTH_NAME)
                    .exception(e)
                    .build();
        }
    }
}
