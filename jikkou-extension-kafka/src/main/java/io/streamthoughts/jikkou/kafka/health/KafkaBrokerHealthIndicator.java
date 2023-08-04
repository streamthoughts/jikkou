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

import io.streamthoughts.jikkou.annotation.ExtensionDescription;
import io.streamthoughts.jikkou.annotation.ExtensionName;
import io.streamthoughts.jikkou.api.config.Configurable;
import io.streamthoughts.jikkou.api.config.Configuration;
import io.streamthoughts.jikkou.api.error.ConfigException;
import io.streamthoughts.jikkou.api.health.Health;
import io.streamthoughts.jikkou.api.health.HealthIndicator;
import io.streamthoughts.jikkou.kafka.AdminClientContext;
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

@ExtensionName("kafkabroker")
@ExtensionDescription("Get the health of kafka brokers")
public final class KafkaBrokerHealthIndicator implements HealthIndicator, Configurable {

    private static final String HEALTH_NAME = "kafka";

    private AdminClientContext adminClientContext;

    private Duration timeout;

    /**
     * Creates a new {@link KafkaBrokerHealthIndicator} instance.
     * Empty constructor required for CLI.
     */
    public KafkaBrokerHealthIndicator() {}

    /**
     * Creates a new {@link KafkaBrokerHealthIndicator} instance.
     * @param adminClientContext the context used to get an AdminClient instance.
     */
    public KafkaBrokerHealthIndicator(@NotNull AdminClientContext adminClientContext) {
        this.adminClientContext = adminClientContext;
    }

    /**
     * Creates a new {@link KafkaBrokerHealthIndicator} instance.
     *
     * @param configuration the context configuration.
     */
    public KafkaBrokerHealthIndicator(@NotNull Configuration configuration) {
        configure(configuration);
    }

    /** {@inheritDoc} */
    @Override
    public void configure(@NotNull Configuration config) throws ConfigException {
        adminClientContext = new AdminClientContext(config);
    }

    /** {@inheritDoc} */
    @Override
    public Health getHealth(final Duration timeout) {
        if (adminClientContext == null) {
            throw new IllegalStateException("must be configured!");
        }
        this.timeout = timeout;
        return adminClientContext
                .withWaitForKafkaBrokersEnabled(false)
                .invokeAndClose(this::getHealth);
    }

    private Health getHealth(final AdminClient client) {
        try {
            DescribeClusterResult result = client.describeCluster(
                    new DescribeClusterOptions().timeoutMs((int)timeout.toMillis()));

            Collection<Node> nodes = result.nodes().get();
            var clusterId = result.clusterId().get();
            Health.Builder builder = new Health.Builder()
                    .up()
                    .withName(HEALTH_NAME);

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
                .withDetails("resource", "urn:kafka:cluster:id:" + clusterId )
                .withDetails("brokers", brokers);

            return builder.build();
        } catch (InterruptedException | ExecutionException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            return new Health.Builder()
                    .unknown()
                    .withName(HEALTH_NAME)
                    .withException(e)
                    .build();
        }
    }
}
