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
package io.streamthoughts.kafka.specs.manager.adminclient;

import io.streamthoughts.kafka.specs.config.JikkouConfig;
import io.streamthoughts.kafka.specs.config.JikkouParams;
import io.streamthoughts.kafka.specs.error.JikkouException;
import io.streamthoughts.kafka.specs.internal.KafkaBrokersReady;
import io.streamthoughts.kafka.specs.internal.KafkaUtils;
import org.apache.kafka.clients.admin.AdminClient;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.Function;

public class AdminClientContext {

    public interface KafkaFunction<O> extends Function<AdminClient, O> { }

    private final JikkouConfig config;

    private AdminClient client;

    /**
     * Creates a new {@link AdminClientContext} instance.
     * @param config the {@link JikkouConfig}.
     */
    public AdminClientContext(final @NotNull JikkouConfig config) {
        this.config = Objects.requireNonNull(config, "'config should not be null'");
    }

    public <O> O invokeAndClose(final @NotNull KafkaFunction<O> function) {
        try (AdminClient client = newAdminClient()) {
            this.client = client;
            if (JikkouParams.KAFKA_BROKERS_WAIT_FOR_ENABLED.orElseGet(config, () -> true)) {
                final boolean isReady = KafkaUtils.waitForKafkaBrokers(
                        client,
                        KafkaBrokersReady.Options
                                .withDefaults()
                                .withMinAvailableBrokers(JikkouParams.KAFKA_BROKERS_WAIT_FOR_MIN_AVAILABLE.get(config))
                                .withRetryBackoffMs(JikkouParams.KAFKA_BROKERS_WAIT_FOR_RETRY_BACKOFF_MS.get(config))
                                .withTimeoutMs(JikkouParams.KAFKA_BROKERS_WAIT_FOR_TIMEOUT_MS.get(config))
                );
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
     * @throws  IllegalStateException if not adminClient is currently created.
     */
    public AdminClient current() {
        if (!isInitialized()) {
            throw new IllegalStateException("No current AdminClient created");
        }
        return client;
    }

    private AdminClient newAdminClient() {
        return KafkaUtils.newAdminClient(JikkouParams.ADMIN_CLIENT_CONFIG.get(config));
    }
}
