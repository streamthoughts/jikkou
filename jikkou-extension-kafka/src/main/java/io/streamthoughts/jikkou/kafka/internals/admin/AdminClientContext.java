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
package io.streamthoughts.jikkou.kafka.internals.admin;

import io.streamthoughts.jikkou.api.error.JikkouRuntimeException;
import io.streamthoughts.jikkou.kafka.internals.KafkaBrokersReady;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.ConfigEntry;
import org.apache.kafka.clients.admin.DescribeConfigsResult;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.config.ConfigResource;
import org.apache.kafka.common.config.TopicConfig;
import org.apache.kafka.common.errors.TopicExistsException;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Facade for the {@link org.apache.kafka.clients.admin.AdminClient}.
 */
public class AdminClientContext implements AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(AdminClientContext.class);

    private final AdminClientFactory adminClientFactory;

    private String clusterId;

    private AdminClient adminClient;

    private KafkaBrokersReady.Options options = KafkaBrokersReady.Options.withDefaults();

    private boolean isWaitForKafkaBrokersEnabled = false;

    private final ReentrantLock stateLock = new ReentrantLock();

    protected enum State {CREATED, CLOSED}

    private final AtomicReference<State> state = new AtomicReference<>(State.CLOSED);

    /**
     * Creates a new {@link AdminClientContext} instance with the specified {@link AdminClientFactory}.
     *
     * @param adminClient the AdminClient.
     */
    public AdminClientContext(final @NotNull AdminClient adminClient) {
        this.adminClientFactory = () -> adminClient;
    }

    /**
     * Creates a new {@link AdminClientContext} instance with the specified {@link AdminClientFactory}.
     *
     * @param adminClientFactory the AdminClient factory.
     */
    public AdminClientContext(final @NotNull AdminClientFactory adminClientFactory) {
        this.adminClientFactory = Objects.requireNonNull(adminClientFactory, "adminClientFactory must not be null");
    }

    public void createTopic(@NotNull final String topic,
                            final int numPartitions,
                            final short replicas) {
        LOG.info("Creating reporting topic: {}", topic);
        try {
            AdminClient adminClient = getAdminClient();
            adminClient.createTopics(List.of(new NewTopic(topic, numPartitions, replicas))).all().get();
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause != null & cause instanceof TopicExistsException) {
                LOG.info("Cannot auto create topic {} - topics already exists. Error can be ignored.", topic);
            } else {
                LOG.error("Cannot auto create topic {}", topic, e);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            // ignore and attempts to proceed anyway;
        }
    }

    public boolean isTopicCleanupPolicyCompact(@NotNull final String topic,
                                               final boolean defaultValue) {
        ConfigResource resource = new ConfigResource(ConfigResource.Type.TOPIC, topic);
        Collection<ConfigResource> cr = Collections.singleton(resource);
        DescribeConfigsResult ConfigsResult = getAdminClient().describeConfigs(cr);
        try {
            org.apache.kafka.clients.admin.Config config = ConfigsResult.all().get().get(resource);
            ConfigEntry configEntry = config.get(TopicConfig.CLEANUP_POLICY_CONFIG);
            if (configEntry != null) {
                return configEntry.value().contains(TopicConfig.CLEANUP_POLICY_COMPACT);
            }
        } catch (InterruptedException e) {
            LOG.debug("Interrupted while checking if topic '{}' is configured with {}={}",
                    topic,
                    TopicConfig.CLEANUP_POLICY_CONFIG,
                    TopicConfig.CLEANUP_POLICY_COMPACT
            );
            Thread.currentThread().interrupt();
        } catch (ExecutionException e) {
            LOG.debug("Failed to check if topic '{}' is configured with {}={}",
                    topic,
                    TopicConfig.CLEANUP_POLICY_CONFIG,
                    TopicConfig.CLEANUP_POLICY_COMPACT
            );
        }
        return defaultValue;
    }

    /**
     * Gets the identifier of the Kafka Cluster.
     *
     * @return the cluster id string.
     */
    public @NotNull String getClusterId() {
        if (clusterId == null) {
            Optional<String> result;
            try {
                final AdminClient adminClient = getAdminClient();

                String value = adminClient.describeCluster().clusterId().get();
                result = Optional.ofNullable(value);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                LOG.error("Failed to describe Kafka ClusterID, thread was interrupted while waiting response");
                result = Optional.empty();
            } catch (ExecutionException e) {
                LOG.error("Failed to describe Kafka ClusterID due to an unexpected error", e);
                result = Optional.empty();
            }
            clusterId = result.orElse("unknown");
        }
        return clusterId;
    }

    /**
     * Gets the current {@link AdminClient} instance.
     * <p>
     * If the {@link AdminClient} does not yet exist, it will be created using the
     * {@link AdminClientFactory} passed through the constructor.
     *
     * @return the {@link AdminClient}.
     * @throws JikkouRuntimeException if no broker a available.
     */
    public @NotNull AdminClient getAdminClient() {
        stateLock.lock();
        try {
            if (!state.compareAndSet(State.CLOSED, State.CREATED)) {
                LOG.debug("AdminClient has already been created");
                return adminClient;
            }
            LOG.info("Retrieving Kafka AdminClient instance.");
            adminClient = adminClientFactory.createAdminClient();
            if (isWaitForKafkaBrokersEnabled) {
                final boolean isReady = new KafkaBrokersReady(options).waitForBrokers(adminClient);
                if (!isReady) {
                    throw new JikkouRuntimeException(
                            "Timeout expired. The timeout period elapsed prior to " +
                                    "the requested number of kafka brokers is available."
                    );
                }
            }
            return adminClient;
        } finally {
            stateLock.unlock();
        }
    }

    public void enabledWaitForKafkaBrokers(final boolean isWaitForKafkaBrokersEnabled) {
        this.isWaitForKafkaBrokersEnabled = isWaitForKafkaBrokersEnabled;
    }

    /**
     * Sets the options for the WAIT_FOR_KAFKA_BROKERS_READY feature.
     */
    public void setOptions(final @NotNull KafkaBrokersReady.Options options) {
        this.options = Objects.requireNonNull(options, "options must not be null");
    }

    /**
     * Gets the options for the WAIT_FOR_KAFKA_BROKERS_READY feature.
     *
     * @return
     */
    public KafkaBrokersReady.Options getOptions() {
        return options;
    }

    /**
     * Check whether the WAIT_FOR_KAFKA_BROKERS_READY feature is enabled.
     *
     * @return {@code true} if the feature is enabled, {@code false} otherwise.
     */
    public boolean isWaitForKafkaBrokersEnabled() {
        return isWaitForKafkaBrokersEnabled;
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public void close() {
        stateLock.lock();
        try {
            if (!state.compareAndSet(State.CREATED, State.CLOSED)) {
                LOG.info("Kafka AdminClient has already been closed");
                return;
            }
            LOG.info("Closing context for Kafka AdminClient");
            if (adminClient != null) {
                adminClient.close();
                adminClient = null;
            }
        } finally {
            stateLock.unlock();
        }
    }
}
