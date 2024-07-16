/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.kafka.internals.admin;

import io.streamthoughts.jikkou.core.config.ConfigProperty;
import io.streamthoughts.jikkou.core.config.Configuration;
import io.streamthoughts.jikkou.kafka.internals.KafkaBrokersReady;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

/**
 * Factory to create a {@link AdminClientContext} instance.
 */
public final class AdminClientContextFactory {

    public static final ConfigProperty<Boolean> KAFKA_BROKERS_WAIT_FOR_ENABLED = ConfigProperty
            .ofBoolean("brokers.waitForEnabled")
            .orElse(true);

    public static final ConfigProperty<Integer> KAFKA_BROKERS_WAIT_FOR_MIN_AVAILABLE = ConfigProperty
            .ofInt("brokers.waitForMinAvailable")
            .orElse(KafkaBrokersReady.DEFAULT_MIN_AVAILABLE_BROKERS);

    public static final ConfigProperty<Long> KAFKA_BROKERS_WAIT_FOR_RETRY_BACKOFF_MS = ConfigProperty
            .ofLong("brokers.waitForRetryBackoffMs")
            .orElse(KafkaBrokersReady.DEFAULT_RETRY_BACKOFF_MS);

    public static final ConfigProperty<Long> KAFKA_BROKERS_WAIT_FOR_TIMEOUT_MS = ConfigProperty
            .ofLong("brokers.waitForTimeoutMs")
            .orElse(KafkaBrokersReady.DEFAULT_TIMEOUT_MS);

    private final AdminClientFactory factory;
    private final Configuration configuration;

    /**
     * Creates a new {@link AdminClientContextFactory} instance.
     *
     * @param configuration the configuration.
     * @param factory       the AdminClient factory.
     */
    public AdminClientContextFactory(@NotNull final Configuration configuration,
                                     @NotNull final AdminClientFactory factory) {
        this.factory = Objects.requireNonNull(factory, "factory must not be null");
        this.configuration = Objects.requireNonNull(configuration, "configuration must not be null");
    }

    /**
     * Creates a new {@link AdminClientContext} instance.
     *
     * @return the instance.
     */
    @NotNull
    public AdminClientContext createAdminClientContext() {
        AdminClientContext context = new AdminClientContext(factory);
        context.enabledWaitForKafkaBrokers(KAFKA_BROKERS_WAIT_FOR_ENABLED.get(configuration));
        context.setOptions(KafkaBrokersReady.Options.withDefaults()
            .withRetryBackoffMs(KAFKA_BROKERS_WAIT_FOR_RETRY_BACKOFF_MS.get(configuration))
            .withMinAvailableBrokers(KAFKA_BROKERS_WAIT_FOR_MIN_AVAILABLE.get(configuration))
            .withTimeoutMs(KAFKA_BROKERS_WAIT_FOR_TIMEOUT_MS.get(configuration))
        );
        return context;
    }
}
