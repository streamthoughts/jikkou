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
package io.streamthoughts.jikkou.extension.aiven.control;

import io.streamthoughts.jikkou.core.config.Configuration;
import io.streamthoughts.jikkou.extension.aiven.AbstractAivenIntegrationTest;
import io.streamthoughts.jikkou.extension.aiven.models.V1KafkaQuota;
import io.streamthoughts.jikkou.extension.aiven.models.V1KafkaQuotaSpec;
import java.util.List;
import okhttp3.mockwebserver.MockResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("integration")
class AivenKafkaQuotaCollectorIT extends AbstractAivenIntegrationTest {

    private static AivenKafkaQuotaCollector collector;

    @BeforeEach
    public void beforeEach() {
        collector = new AivenKafkaQuotaCollector(getAivenApiConfig());
    }

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
        List<V1KafkaQuota> results = collector.listAll(Configuration.empty(), NO_SELECTOR);

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