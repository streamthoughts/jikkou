/*
 * Copyright 2022 StreamThoughts.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.streamthoughts.jikkou.kafka.manager.adminclient;

import io.streamthoughts.jikkou.kafka.config.JikkouConfig;
import io.streamthoughts.jikkou.kafka.config.JikkouParams;
import io.streamthoughts.jikkou.kafka.error.JikkouException;
import io.streamthoughts.jikkou.kafka.internal.KafkaBrokersReady;
import io.streamthoughts.jikkou.kafka.internal.KafkaUtils;
import org.apache.kafka.clients.admin.AdminClient;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Properties;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Class for executing operation using an ephemeral {@link AdminClient} instance.
 */
public class AdminClientContext {

    private static final int DEFAULT_MIN_AVAILABLE_BROKERS = 1;
    private static final long DEFAULT_TIMEOUT_MS = 60000L;
    private static final long DEFAULT_RETRY_BACKOFF_MS = 1000L;

    public interface KafkaFunction<O> extends Function<AdminClient, O> { }

    private final Supplier<AdminClient> adminClientSupplier;

    private AdminClient client;

    private KafkaBrokersReady.Options options = KafkaBrokersReady.Options.withDefaults()
            .withTimeoutMs(DEFAULT_TIMEOUT_MS)
            .withMinAvailableBrokers(DEFAULT_MIN_AVAILABLE_BROKERS)
            .withRetryBackoffMs(DEFAULT_RETRY_BACKOFF_MS);

    private boolean isWaitForKafkaBrokersEnabled = true;

    /**
     * Creates a new {@link AdminClientContext} instance with the specified {@link AdminClient} configuration.
     *
     * @param adminClientConfig   the {@link AdminClient} supplier.
     */
    public AdminClientContext(final @NotNull Properties adminClientConfig) {
        this(() -> KafkaUtils.newAdminClient(adminClientConfig));
    }

    /**
     * Creates a new {@link AdminClientContext} instance with the specified {@link AdminClient} supplier.
     *
     * @param adminClientSupplier   the {@link AdminClient} supplier.
     */
    public AdminClientContext(final @NotNull Supplier<AdminClient> adminClientSupplier) {
        this.adminClientSupplier = Objects.requireNonNull(
                adminClientSupplier,
                "'adminClientSupplier' should not be null"
        );
    }

    /**
     * Creates a new {@link AdminClientContext} instance with the specified application's configuration.
     *
     * @param config the {@link JikkouConfig}.
     */
    public AdminClientContext(final @NotNull JikkouConfig config) {
        this(JikkouParams.ADMIN_CLIENT_CONFIG.get(config));
        withWaitForKafkaBrokersEnabled(JikkouParams.KAFKA_BROKERS_WAIT_FOR_ENABLED.orElseGet(config, () -> true));
        if (isWaitForKafkaBrokersEnabled) {
            withWaitForRetryBackoff(JikkouParams.KAFKA_BROKERS_WAIT_FOR_RETRY_BACKOFF_MS.orElseGet(
                    config,
                    () -> DEFAULT_RETRY_BACKOFF_MS)
            );
            withWaitForMinAvailableBrokers(JikkouParams.KAFKA_BROKERS_WAIT_FOR_MIN_AVAILABLE.orElseGet(
                    config,
                    () -> DEFAULT_MIN_AVAILABLE_BROKERS)
            );
            withWaitTimeoutMs(JikkouParams.KAFKA_BROKERS_WAIT_FOR_TIMEOUT_MS.orElseGet(
                    config,
                    () -> DEFAULT_TIMEOUT_MS)
            );
        }
    }

    public AdminClientContext withWaitForKafkaBrokersEnabled(final boolean isWaitForKafkaBrokersEnabled) {
        this.isWaitForKafkaBrokersEnabled = isWaitForKafkaBrokersEnabled;
        return this;
    }

    public AdminClientContext withWaitForMinAvailableBrokers(final int waitForMinAvailableBrokers) {
        this.options = options.withMinAvailableBrokers(waitForMinAvailableBrokers);
        return this;
    }

    public AdminClientContext withWaitForRetryBackoff(final long waitForRetryBackoff) {
        this.options = options.withRetryBackoffMs(waitForRetryBackoff);
        return this;
    }

    public AdminClientContext withWaitTimeoutMs(final long waitTimeoutMs) {
        this.options = options.withTimeoutMs(waitTimeoutMs);
        return this;
    }

    public <O> O invokeAndClose(final @NotNull KafkaFunction<O> function) {
        try (AdminClient client = adminClientSupplier.get()) {
            this.client = client;
            if (isWaitForKafkaBrokersEnabled) {
                final boolean isReady = KafkaUtils.waitForKafkaBrokers(client, options);
                if (!isReady) {
                    throw new JikkouException(
                            "Timeout expired. The timeout period elapsed prior to " +
                            "the requested number of kafka brokers is available."
                    );
                }

            }
            return function.apply(client);
        } finally {
            client = null;
        }
    }

    public boolean isInitialized() {
        return client != null;
    }

    /**
     * Retrieve the current {@link AdminClient} instance.
     *
     * @return  the {@link AdminClient}.
     * @throws  IllegalStateException if no AdminClient is currently created.
     */
    public AdminClient current() {
        if (!isInitialized()) {
            throw new IllegalStateException("No current AdminClient created");
        }
        return client;
    }
}
