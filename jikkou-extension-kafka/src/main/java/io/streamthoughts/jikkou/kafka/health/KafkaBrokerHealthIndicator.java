/*
 * Copyright 2022 The original authors
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
package io.streamthoughts.jikkou.kafka.health;

import static io.streamthoughts.jikkou.kafka.health.KafkaBrokerHealthIndicator.HEALTH_NAME;

import io.streamthoughts.jikkou.core.annotation.Description;
import io.streamthoughts.jikkou.core.annotation.Named;
import io.streamthoughts.jikkou.core.config.Configurable;
import io.streamthoughts.jikkou.core.config.Configuration;
import io.streamthoughts.jikkou.core.exceptions.ConfigException;
import io.streamthoughts.jikkou.core.health.Health;
import io.streamthoughts.jikkou.core.health.HealthIndicator;
import io.streamthoughts.jikkou.kafka.internals.admin.AdminClientContext;
import io.streamthoughts.jikkou.kafka.internals.admin.AdminClientContextFactory;
import io.streamthoughts.jikkou.kafka.reconcilier.KafkaClientConfiguration;
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
@Description("Get the health of kafka brokers")
public final class KafkaBrokerHealthIndicator implements HealthIndicator, Configurable {

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
    public void configure(@NotNull Configuration configuration) throws ConfigException {
        if (adminClientContextFactory == null) {
            this.adminClientContextFactory = new AdminClientContextFactory(configuration);
        }
        this.configuration = new KafkaClientConfiguration(configuration);
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
