/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.extension.aiven.reconciler;

import io.streamthoughts.jikkou.core.config.Configuration;
import io.streamthoughts.jikkou.core.models.ResourceList;
import io.streamthoughts.jikkou.core.selector.Selectors;
import io.streamthoughts.jikkou.extension.aiven.BaseExtensionProviderIT;
import io.streamthoughts.jikkou.extension.aiven.models.V1KafkaQuota;
import io.streamthoughts.jikkou.extension.aiven.models.V1KafkaQuotaSpec;
import java.util.List;
import okhttp3.mockwebserver.MockResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("integration")
class AivenKafkaQuotaCollectorIT extends BaseExtensionProviderIT {

    @Test
    void shouldListKafkaQuotaEntries() {
        // Given
        enqueueResponse(new MockResponse()
            .setHeader("Content-Type", "application/json")
            .setResponseCode(200)
            .setBody("""
                {"quotas":[{"client-id":"default","consumer_byte_rate":1048576.0,"producer_byte_rate":1048576.0,"request_percentage":25.0,"user":"default"}]}
                """
            ));
        // When
        ResourceList<V1KafkaQuota> resources = api.listResources(V1KafkaQuota.class, Selectors.NO_SELECTOR, Configuration.empty());
        List<V1KafkaQuota> results = resources.getItems();

        // Then
        Assertions.assertNotNull(results);
        V1KafkaQuota expected = V1KafkaQuota.builder()
            .withSpec(V1KafkaQuotaSpec
                .builder()
                .withUser("default")
                .withClientId("default")
                .withProducerByteRate(1048576.0)
                .withConsumerByteRate(1048576.0)
                .withRequestPercentage(25.0)
                .build()
            )
            .build();
        Assertions.assertEquals(List.of(expected), results);
    }
}