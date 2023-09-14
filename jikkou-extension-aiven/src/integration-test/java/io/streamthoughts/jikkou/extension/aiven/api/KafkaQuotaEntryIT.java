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
package io.streamthoughts.jikkou.extension.aiven.api;

import io.streamthoughts.jikkou.JikkouMetadataAnnotations;
import io.streamthoughts.jikkou.api.ReconciliationContext;
import io.streamthoughts.jikkou.api.ReconciliationMode;
import io.streamthoughts.jikkou.api.change.ChangeResult;
import io.streamthoughts.jikkou.api.change.ValueChange;
import io.streamthoughts.jikkou.api.config.Configuration;
import io.streamthoughts.jikkou.api.model.ObjectMeta;
import io.streamthoughts.jikkou.api.selector.ResourceSelector;
import io.streamthoughts.jikkou.extension.aiven.adapter.KafkaQuotaAdapter;
import io.streamthoughts.jikkou.extension.aiven.api.data.KafkaQuotaEntry;
import io.streamthoughts.jikkou.extension.aiven.control.KafkaQuotaCollector;
import io.streamthoughts.jikkou.extension.aiven.control.KafkaQuotaController;
import io.streamthoughts.jikkou.extension.aiven.models.V1KafkaQuota;
import io.streamthoughts.jikkou.extension.aiven.models.V1KafkaQuotaSpec;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("integration")
class KafkaQuotaEntryIT {

    public static final List<ResourceSelector> NO_SELECTOR = Collections.emptyList();

    public static MockWebServer SERVER;

    private static KafkaQuotaController CONTROLLER;
    private static KafkaQuotaCollector COLLECTOR;

    @BeforeAll
    static void setUp() throws IOException {
        SERVER = new MockWebServer();
        SERVER.start();

        Configuration configuration = new Configuration
                .Builder()
                .with(AivenApiClientConfig.AIVEN_API_URL.key(), SERVER.url("/"))
                .with(AivenApiClientConfig.AIVEN_PROJECT.key(), "project")
                .with(AivenApiClientConfig.AIVEN_SERVICE.key(), "service")
                .with(AivenApiClientConfig.AIVEN_TOKEN_AUTH.key(), "token")
                .with(AivenApiClientConfig.AIVEN_DEBUG_LOGGING_ENABLED.key(), true)
                .build();
        COLLECTOR = new KafkaQuotaCollector(new AivenApiClientConfig(configuration));
        CONTROLLER = new KafkaQuotaController(new AivenApiClientConfig(configuration));
    }

    @AfterAll
    static void tearDown() throws IOException {
        COLLECTOR.close();
        CONTROLLER.close();
        SERVER.shutdown();
    }

    @Test
    void shouldListKafkaQuotaEntries() {
        // Given
        SERVER.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setResponseCode(200)
                .setBody("""
                        {"quotas":[{"client-id":"default","consumer_byte_rate":1048576.0,"producer_byte_rate":1048576.0,"request_percentage":25.0,"user":"default"}]}
                        """
                ));
        // When
        List<V1KafkaQuota> results = COLLECTOR.listAll(Configuration.empty(), NO_SELECTOR);

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

    @Test
    void shouldCreateKafkaQuota() {
        // Given
        SERVER.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setResponseCode(200)
                .setBody("""
                        {"quotas":[{"client-id":"default","consumer_byte_rate":1048576.0,"producer_byte_rate":1048576.0,"request_percentage":25.0,"user":"default"}]}
                        """
                ));
        SERVER.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setResponseCode(200)
                .setBody("""
                        {"message":"upsert"}
                        """
                ));

        V1KafkaQuota entry = V1KafkaQuota.builder()
                .withSpec(V1KafkaQuotaSpec
                        .builder()
                        .withUser("test")
                        .withClientId("test")
                        .withProducerByteRate(1048576.0)
                        .withConsumerByteRate(1048576.0)
                        .withRequestPercentage(25.0)
                        .build()
                )
                .build();

        // When
        List<ChangeResult<ValueChange<KafkaQuotaEntry>>> results = CONTROLLER
                .reconcile(List.of(entry), ReconciliationMode.CREATE, ReconciliationContext.with(false));

        // Then
        ValueChange<KafkaQuotaEntry> expected = ValueChange.withAfterValue(KafkaQuotaAdapter.map(entry));
        Assertions.assertEquals(expected, results.get(0).data().getChange());
    }


    @Test
    void shouldDeleteKafkaQuota() {
        // Given
        SERVER.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setResponseCode(200)
                .setBody("""
                        {"quotas":[{"client-id":"default","consumer_byte_rate":1048576.0,"producer_byte_rate":1048576.0,"request_percentage":25.0,"user":"default"}]}
                        """
                ));
        SERVER.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setResponseCode(200)
                .setBody("""
                        {"message":"Deleted quota for for (User: default, Client-id: default)"}
                        """

                ));
        // When
        V1KafkaQuota entry = V1KafkaQuota.builder()
                .withMetadata(ObjectMeta.builder()
                        .withAnnotation(JikkouMetadataAnnotations.JIKKOU_IO_DELETE, true)
                        .build())
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

        // When
        List<ChangeResult<ValueChange<KafkaQuotaEntry>>> results = CONTROLLER
                .reconcile(List.of(entry), ReconciliationMode.DELETE, ReconciliationContext.with(false));

        // Then
        ValueChange<KafkaQuotaEntry> expected = ValueChange.withBeforeValue(KafkaQuotaAdapter.map(entry));
        Assertions.assertEquals(expected, results.get(0).data().getChange());
    }
}