/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.extension.aiven.reconciler;

import io.streamthoughts.jikkou.core.ReconciliationContext;
import io.streamthoughts.jikkou.core.ReconciliationMode;
import io.streamthoughts.jikkou.core.models.CoreAnnotations;
import io.streamthoughts.jikkou.core.models.ObjectMeta;
import io.streamthoughts.jikkou.core.models.ResourceList;
import io.streamthoughts.jikkou.core.models.change.GenericResourceChange;
import io.streamthoughts.jikkou.core.models.change.ResourceChange;
import io.streamthoughts.jikkou.core.models.change.ResourceChangeSpec;
import io.streamthoughts.jikkou.core.models.change.StateChange;
import io.streamthoughts.jikkou.core.reconciler.ChangeResult;
import io.streamthoughts.jikkou.core.reconciler.Operation;
import io.streamthoughts.jikkou.extension.aiven.BaseExtensionProviderIT;
import io.streamthoughts.jikkou.extension.aiven.api.data.KafkaQuotaEntry;
import io.streamthoughts.jikkou.extension.aiven.models.V1KafkaQuota;
import io.streamthoughts.jikkou.extension.aiven.models.V1KafkaQuotaSpec;
import java.util.List;
import okhttp3.mockwebserver.MockResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("integration")
class AivenKafkaQuotaControllerIT extends BaseExtensionProviderIT {

    @Test
    void shouldCreateKafkaQuota() {
        // Given
        enqueueResponse(new MockResponse()
            .setHeader("Content-Type", "application/json")
            .setResponseCode(200)
            .setBody("""
                {"quotas":[{"client-id":"default","consumer_byte_rate":1048576.0,"producer_byte_rate":1048576.0,"request_percentage":25.0,"user":"default"}]}
                """
            ));
        enqueueResponse(new MockResponse()
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
        List<ChangeResult> results = api
            .reconcile(ResourceList.of(List.of(entry)), ReconciliationMode.CREATE, ReconciliationContext.builder().dryRun(false).build())
            .results();

        // Then
        ChangeResult result = results.getFirst();
        ResourceChange actual = result.change();
        ResourceChange expected = GenericResourceChange
            .builder(V1KafkaQuota.class)
            .withSpec(ResourceChangeSpec
                .builder()
                .withOperation(Operation.CREATE)
                .withChange(StateChange.create(
                    "entry",
                    new KafkaQuotaEntry("test", "test", 1048576.0, 1048576.0, 25.0))
                )
                .build()
            )
            .build();
        Assertions.assertEquals(expected, actual);
    }


    @Test
    void shouldDeleteKafkaQuota() {
        // Given
        enqueueResponse(new MockResponse()
            .setHeader("Content-Type", "application/json")
            .setResponseCode(200)
            .setBody("""
                {"quotas":[{"client-id":"default","consumer_byte_rate":1048576.0,"producer_byte_rate":1048576.0,"request_percentage":25.0,"user":"default"}]}
                """
            ));
        enqueueResponse(new MockResponse()
            .setHeader("Content-Type", "application/json")
            .setResponseCode(200)
            .setBody("""
                {"message":"Deleted quota for for (User: default, Client-id: default)"}
                """

            ));
        // When
        V1KafkaQuota entry = V1KafkaQuota.builder()
            .withMetadata(ObjectMeta.builder()
                .withAnnotation(CoreAnnotations.JIKKOU_IO_DELETE, true)
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
        ReconciliationContext context = ReconciliationContext.builder().dryRun(false).build();
        List<ChangeResult> results = api
            .reconcile(ResourceList.of(List.of(entry)), ReconciliationMode.DELETE, context)
            .results();

        // Then
        ChangeResult result = results.getFirst();
        ResourceChange actual = result.change();
        ResourceChange expected = GenericResourceChange
            .builder(V1KafkaQuota.class)
            .withSpec(ResourceChangeSpec
                .builder()
                .withOperation(Operation.DELETE)
                .withChange(StateChange.delete(
                    "entry",
                    new KafkaQuotaEntry("default", "default", 1048576.0, 1048576.0, 25.0))
                )
                .build()
            )
            .build();
        Assertions.assertEquals(expected, actual);
    }
}