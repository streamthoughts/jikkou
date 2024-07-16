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
import io.streamthoughts.jikkou.extension.aiven.api.data.KafkaAclEntry;
import io.streamthoughts.jikkou.extension.aiven.api.data.Permission;
import io.streamthoughts.jikkou.extension.aiven.models.V1KafkaTopicAclEntry;
import io.streamthoughts.jikkou.extension.aiven.models.V1KafkaTopicAclEntrySpec;
import java.util.List;
import okhttp3.mockwebserver.MockResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("integration")
class AivenKafkaTopicAclEntryControllerIT extends BaseExtensionProviderIT {

    @Test
    void shouldCreateKafkaAclEntries() {
        // Given
        enqueueResponse(new MockResponse()
            .setHeader("Content-Type", "application/json")
            .setResponseCode(200)
            .setBody("""
                {"acl":[{"id":"default","permission":"admin","topic":"*","username":"avnadmin"}]}
                """
            ));
        enqueueResponse(new MockResponse()
            .setHeader("Content-Type", "application/json")
            .setResponseCode(200)
            .setBody("""
                {
                  "acl": [
                    {
                      "id": "default",
                      "permission": "admin",
                      "topic": "*",
                      "username": "avnadmin"
                    },
                    {
                      "id": "acl44b79574e2a",
                      "permission": "write",
                      "topic": "topic-test",
                      "username": "user-test"
                    }
                  ],
                  "message": "added"
                }
                """

            ));
        V1KafkaTopicAclEntry entry = V1KafkaTopicAclEntry.builder()
            .withSpec(V1KafkaTopicAclEntrySpec.builder()
                .withPermission(Permission.WRITE)
                .withTopic("topic-test")
                .withUsername("user-test")
                .build())
            .build();

        // When
        ReconciliationContext context = ReconciliationContext.builder().dryRun(false).build();
        List<ChangeResult> results = api
            .reconcile(ResourceList.of(List.of(entry)), ReconciliationMode.CREATE, context)
            .results();

        // Then
        ResourceChange actual = results.getFirst().change();
        ResourceChange expected = GenericResourceChange
            .builder(V1KafkaTopicAclEntry.class)
            .withSpec(ResourceChangeSpec
                .builder()
                .withOperation(Operation.CREATE)
                .withChange(StateChange.create(
                    "entry",
                    new KafkaAclEntry(Permission.WRITE.val(), "topic-test", "user-test"))
                )
                .build()
            )
            .build();
        Assertions.assertEquals(expected, actual);
    }

    @Test
    void shouldDeleteKafkaAclEntries() {
        // Given
        enqueueResponse(new MockResponse()
            .setHeader("Content-Type", "application/json")
            .setResponseCode(200)
            .setBody("""
                {"acl":[{"id":"default","permission":"admin","topic":"*","username":"avnadmin"}]}
                """
            ));
        enqueueResponse(new MockResponse()
            .setHeader("Content-Type", "application/json")
            .setResponseCode(200)
            .setBody("""
                {"acl":[],"message":"deleted"}
                """

            ));
        // When
        V1KafkaTopicAclEntry entry = V1KafkaTopicAclEntry.builder()
            .withMetadata(ObjectMeta.builder()
                .withAnnotation(CoreAnnotations.JIKKOU_IO_DELETE, true)
                .build())
            .withSpec(V1KafkaTopicAclEntrySpec.builder()
                .withPermission(Permission.ADMIN)
                .withTopic("*")
                .withUsername("avnadmin")
                .build())
            .build();

        // When
        ReconciliationContext context = ReconciliationContext.builder().dryRun(false).build();
        List<ChangeResult> results = api
            .reconcile(ResourceList.of(List.of(entry)), ReconciliationMode.DELETE, context)
            .results();

        // Then
        ResourceChange actual = results.getFirst().change();
        ResourceChange expected = GenericResourceChange
            .builder(V1KafkaTopicAclEntry.class)
            .withSpec(ResourceChangeSpec
                .builder()
                .withOperation(Operation.DELETE)
                .withChange(StateChange.delete(
                    "entry",
                    new KafkaAclEntry(Permission.ADMIN.val(), "*", "avnadmin"))
                )
                .build()
            )
            .build();
        Assertions.assertEquals(expected, actual);
    }


    @Test
    void shouldHandleError() {
        // Given
        enqueueResponse(new MockResponse()
            .setHeader("Content-Type", "application/json")
            .setResponseCode(200)
            .setBody("""
                {"acl":[]}
                """
            ));
        enqueueResponse(new MockResponse()
            .setHeader("Content-Type", "application/json")
            .setResponseCode(409)
            .setBody("""
                {"errors":[{"message":"Identical ACL entry already exists","status":409}],"message":"Identical ACL entry already exists"}
                """
            ));

        V1KafkaTopicAclEntry entry = V1KafkaTopicAclEntry.builder()
            .withSpec(V1KafkaTopicAclEntrySpec.builder()
                .withPermission(Permission.ADMIN)
                .withTopic("*")
                .withUsername("avnadmin")
                .build())
            .build();

        // When
        ReconciliationContext context = ReconciliationContext.builder().dryRun(false).build();
        List<ChangeResult> results = api
            .reconcile(ResourceList.of(List.of(entry)), ReconciliationMode.CREATE, context)
            .results();

        // Then
        ChangeResult result = results.getFirst();
        ResourceChange actual = result.change();
        ResourceChange expected = GenericResourceChange
            .builder(V1KafkaTopicAclEntry.class)
            .withSpec(ResourceChangeSpec
                .builder()
                .withOperation(Operation.CREATE)
                .withChange(StateChange.create(
                    "entry",
                    new KafkaAclEntry(Permission.ADMIN.val(), "*", "avnadmin"))
                )
                .build()
            )
            .build();
        Assertions.assertEquals(expected, actual);
        Assertions.assertEquals(ChangeResult.Status.FAILED, result.status());
    }
}