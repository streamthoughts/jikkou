/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.kafka;

import static io.streamthoughts.jikkou.kafka.internals.admin.AdminClientContextFactory.KAFKA_BROKERS_WAIT_FOR_ENABLED;
import static io.streamthoughts.jikkou.kafka.internals.admin.AdminClientContextFactory.KAFKA_BROKERS_WAIT_FOR_MIN_AVAILABLE;
import static io.streamthoughts.jikkou.kafka.internals.admin.AdminClientContextFactory.KAFKA_BROKERS_WAIT_FOR_RETRY_BACKOFF_MS;
import static io.streamthoughts.jikkou.kafka.internals.admin.AdminClientContextFactory.KAFKA_BROKERS_WAIT_FOR_TIMEOUT_MS;

import io.streamthoughts.jikkou.core.config.Configuration;
import io.streamthoughts.jikkou.kafka.internals.admin.AdminClientContext;
import io.streamthoughts.jikkou.kafka.internals.admin.AdminClientContextFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class AdminClientContextTest {

    @Test
    void shouldOverrideOptionsForWaitFalse() {
        // Given
        Configuration configuration = KAFKA_BROKERS_WAIT_FOR_ENABLED.asConfiguration(true);
        try(AdminClientContext context = new AdminClientContextFactory(configuration).createAdminClientContext()) {
            // When
            boolean result = context.isWaitForKafkaBrokersEnabled();

            // Then
            Assertions.assertTrue(result);
        }
    }


    @Test
    void shouldOverrideOptionsForWaitMinAvailable() {
        // Given
        Configuration configuration = KAFKA_BROKERS_WAIT_FOR_MIN_AVAILABLE.asConfiguration(42);
        try(AdminClientContext context = new AdminClientContextFactory(configuration).createAdminClientContext()) {
            // When
            int result = context.getOptions().minAvailableBrokers();

            // Then
            Assertions.assertEquals(42, result);
        }
    }

    @Test
    void shouldOverrideOptionsForWaitRetryBackMs() {
        // Given
        Configuration configuration = KAFKA_BROKERS_WAIT_FOR_RETRY_BACKOFF_MS.asConfiguration(42L);
        ;
        try(AdminClientContext context = new AdminClientContextFactory(configuration).createAdminClientContext()) {
            // When
            long result = context.getOptions().retryBackoffMs();

            // Then
            Assertions.assertEquals(42L, result);
        }
    }

    @Test
    void shouldOverrideOptionsForWaitRetryTimeoutMs() {
        // Given
        Configuration configuration = KAFKA_BROKERS_WAIT_FOR_TIMEOUT_MS.asConfiguration(42L);
        try(AdminClientContext context = new AdminClientContextFactory(configuration).createAdminClientContext()) {
            // When
            long result = context.getOptions().timeoutMs();

            // Then
            Assertions.assertEquals(42L, result);
        }
    }

}