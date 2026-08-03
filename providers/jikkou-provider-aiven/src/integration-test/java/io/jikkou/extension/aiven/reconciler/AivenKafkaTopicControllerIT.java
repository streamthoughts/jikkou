/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.jikkou.extension.aiven.reconciler;

import io.jikkou.core.ReconciliationContext;
import io.jikkou.core.ReconciliationMode;
import io.jikkou.core.config.Configuration;
import io.jikkou.core.models.ObjectMeta;
import io.jikkou.core.models.ResourceList;
import io.jikkou.core.models.change.ResourceChange;
import io.jikkou.core.reconciler.Operation;
import io.jikkou.extension.aiven.ApiVersions;
import io.jikkou.extension.aiven.BaseExtensionProviderIT;
import io.jikkou.kafka.models.V1KafkaTopic;
import io.jikkou.kafka.models.V1KafkaTopicSpec;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import okhttp3.mockwebserver.MockResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("integration")
public class AivenKafkaTopicControllerIT extends BaseExtensionProviderIT {

    public static final String TEST_TOPIC = "test";
    public static final String UNMANAGED_TOPIC = "unmanaged";

    private static MockResponse topicInfoResponse(final String topicName) {
        return new MockResponse()
            .setHeader("Content-Type", "application/json")
            .setResponseCode(200)
            .setBody("""
                {
                  "topic": {
                    "topic_name": "%s",
                    "partitions": [ { "partition": 0 } ],
                    "replication": 3,
                    "config": {
                      "retention_ms": { "source": "topic_config", "value": 60000 }
                    }
                  }
                }
                """.formatted(topicName));
    }

    private static MockResponse topicListResponse(final String... topicNames) {
        String topics = String.join(",", List.of(topicNames).stream()
            .map(name -> """
                { "topic_name": "%s", "partitions": 1, "replication": 3 }
                """.formatted(name))
            .toList());
        return new MockResponse()
            .setHeader("Content-Type", "application/json")
            .setResponseCode(200)
            .setBody("""
                {
                  "topics": [ %s ]
                }
                """.formatted(topics));
    }

    private static MockResponse topicNotFoundResponse() {
        return new MockResponse()
            .setHeader("Content-Type", "application/json")
            .setResponseCode(404)
            .setBody("""
                {
                    "errors": [ { "message": "Topic 'test' does not exist", "status": 404 } ],
                    "message": "Topic 'test' does not exist"
                }
                """);
    }

    private static V1KafkaTopic testTopic() {
        return V1KafkaTopic.builder()
            .withApiVersion(ApiVersions.KAFKA_AIVEN_V1BETA2)
            .withMetadata(ObjectMeta.builder().withName(TEST_TOPIC).build())
            .withSpec(V1KafkaTopicSpec
                .builder()
                .withPartitions(1)
                .withReplicas((short) 3)
                .build())
            .build();
    }

    @Test
    void shouldOnlyDescribeExpectedTopicsAndNotTheWholeService() throws InterruptedException {
        // Given only the expected topic 'test' is served. No `list all topics` response is enqueued:
        // the plan must not enumerate the service.
        enqueueResponse(topicInfoResponse(TEST_TOPIC));

        // When
        api.reconcile(
            ResourceList.of(List.of(testTopic())),
            ReconciliationMode.FULL,
            ReconciliationContext.builder().dryRun(true).build()
        );

        // Then the managed topic is described directly, and the service is never enumerated.
        Assertions.assertEquals(1, getRequestCount());
        String path = takeRequest().getPath();
        Assertions.assertTrue(
            path.endsWith("/topic/" + TEST_TOPIC),
            () -> "Expected only the managed topic to be described, but got: " + path
        );
    }

    @Test
    void shouldDescribeWholeServiceWhenDeleteOrphansIsEnabled() throws InterruptedException {
        // Given delete-orphans needs every topic of the service to spot orphans.
        enqueueResponse(topicListResponse(TEST_TOPIC, UNMANAGED_TOPIC));
        enqueueResponse(topicInfoResponse(TEST_TOPIC));
        enqueueResponse(topicInfoResponse(UNMANAGED_TOPIC));

        // When
        api.reconcile(
            ResourceList.of(List.of(testTopic())),
            ReconciliationMode.FULL,
            ReconciliationContext.builder()
                .dryRun(true)
                .configuration(Configuration.from(Map.of(
                    AivenKafkaTopicController.Config.IS_DELETE_ORPHANS_ENABLED.key(), true
                )))
                .build()
        );

        // Then the service is enumerated and every topic on it is described, including the
        // unmanaged one — otherwise orphans could never be detected.
        Assertions.assertEquals(3, getRequestCount());
        List<String> paths = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            paths.add(takeRequest().getPath());
        }
        Assertions.assertTrue(
            paths.stream().anyMatch(path -> path.endsWith("/topic")),
            () -> "Expected the service to be enumerated, but got: " + paths
        );
        Assertions.assertTrue(
            paths.stream().anyMatch(path -> path.endsWith("/topic/" + UNMANAGED_TOPIC)),
            () -> "Expected the unmanaged topic to be described, but got: " + paths
        );
    }

    @Test
    void shouldPlanCreateWhenExpectedTopicDoesNotExist() {
        // Given the expected topic does not exist yet: describing it returns 404, which is not an
        // error — it is simply absent from the actual state.
        enqueueResponse(topicNotFoundResponse());

        // When
        List<ResourceChange> changes = api.getDiff(
            ResourceList.of(List.of(testTopic())),
            ReconciliationContext.builder().dryRun(true).build()
        ).getItems();

        // Then
        Assertions.assertEquals(1, changes.size());
        Assertions.assertEquals(Operation.CREATE, changes.getFirst().getSpec().getOp());
    }
}
