/*
 * Copyright 2023 The original authors
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