/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.extension.aiven.adapter;

import io.streamthoughts.jikkou.extension.aiven.api.data.KafkaQuotaEntry;
import io.streamthoughts.jikkou.extension.aiven.models.V1KafkaQuota;
import io.streamthoughts.jikkou.extension.aiven.models.V1KafkaQuotaSpec;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class KafkaQuotaAdapterTest {

    @Test
    void shouldMapToKafkaQuota() {
        // Given
        V1KafkaQuota object = V1KafkaQuota.builder()
                .withSpec(V1KafkaQuotaSpec
                        .builder()
                        .withUser("user")
                        .withClientId("client")
                        .withConsumerByteRate(1024.0)
                        .withProducerByteRate(2048.0)
                        .withRequestPercentage(25.0)
                        .build()
                )
                .build();
        // When
        KafkaQuotaEntry result = KafkaQuotaAdapter.map(object);

        // Then
        Assertions.assertEquals(result, new KafkaQuotaEntry(
                "client",
                "user",
                1024.0,
                2048.0,
                25.0)
        );
    }

    @Test
    void shouldMapToKafkaQuotaGivenNoClientAndUser() {
        // Given
        V1KafkaQuota object = V1KafkaQuota.builder()
                .withSpec(V1KafkaQuotaSpec
                        .builder()
                        .withConsumerByteRate(1024.0)
                        .withProducerByteRate(2048.0)
                        .withRequestPercentage(25.0)
                        .build()
                )
                .build();
        // When
        KafkaQuotaEntry result = KafkaQuotaAdapter.map(object);

        // Then
        Assertions.assertEquals(result, new KafkaQuotaEntry(
                "default",
                "default",
                1024.0,
                2048.0,
                25.0)
        );
    }

    @Test
    void shouldMapToKafkaQuotaGivenConsumerByteRateOnly() {
        // Given
        V1KafkaQuota object = V1KafkaQuota.builder()
                .withSpec(V1KafkaQuotaSpec
                        .builder()
                        .withConsumerByteRate(1024.0)
                        .build()
                )
                .build();
        // When
        KafkaQuotaEntry result = KafkaQuotaAdapter.map(object);

        // Then
        Assertions.assertEquals(result, new KafkaQuotaEntry(
                "default",
                "default",
                1024.0,
                null,
                null)
        );
    }

    @Test
    void shouldMapToKafkaQuotaGivenProducerByteRateOnly() {
        // Given
        V1KafkaQuota object = V1KafkaQuota.builder()
                .withSpec(V1KafkaQuotaSpec
                        .builder()
                        .withProducerByteRate(2048.0)
                        .build()
                )
                .build();
        // When
        KafkaQuotaEntry result = KafkaQuotaAdapter.map(object);

        // Then
        Assertions.assertEquals(result, new KafkaQuotaEntry(
                "default",
                "default",
                null,
                2048.0,
                null)
        );
    }
}