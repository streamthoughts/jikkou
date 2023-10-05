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

import io.streamthoughts.jikkou.CoreAnnotations;
import io.streamthoughts.jikkou.api.ReconciliationContext;
import io.streamthoughts.jikkou.api.ReconciliationMode;
import io.streamthoughts.jikkou.api.change.ChangeResult;
import io.streamthoughts.jikkou.api.change.ChangeType;
import io.streamthoughts.jikkou.api.change.ValueChange;
import io.streamthoughts.jikkou.api.model.ObjectMeta;
import io.streamthoughts.jikkou.extension.aiven.AbstractAivenIntegrationTest;
import io.streamthoughts.jikkou.extension.aiven.api.data.KafkaAclEntry;
import io.streamthoughts.jikkou.extension.aiven.api.data.Permission;
import io.streamthoughts.jikkou.extension.aiven.models.V1KafkaTopicAclEntry;
import io.streamthoughts.jikkou.extension.aiven.models.V1KafkaTopicAclEntrySpec;
import java.util.List;
import okhttp3.mockwebserver.MockResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("integration")
class AivenKafkaTopicAclEntryControllerIT extends AbstractAivenIntegrationTest {

    private static AivenKafkaTopicAclEntryController controller;

    @BeforeEach
    public void beforeEach() {
        controller = new AivenKafkaTopicAclEntryController(getAivenApiConfig());
    }

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
        List<ChangeResult<ValueChange<KafkaAclEntry>>> results = controller
                .reconcile(List.of(entry), ReconciliationMode.CREATE, ReconciliationContext.builder().dryRun(false).build());

        // Then
        Assertions.assertNotNull(results);
        Assertions.assertEquals(1, results.size());

        ChangeResult<ValueChange<KafkaAclEntry>> change = results.get(0);
        Assertions.assertEquals(ChangeResult.Status.CHANGED, change.status());
        Assertions.assertEquals(ChangeType.ADD, change.data().getChange().getChangeType());
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
        List<ChangeResult<ValueChange<KafkaAclEntry>>> results = controller
                .reconcile(List.of(entry), ReconciliationMode.DELETE, ReconciliationContext.builder().dryRun(false).build());

        // Then
        Assertions.assertNotNull(results);
        Assertions.assertEquals(1, results.size());

        ChangeResult<ValueChange<KafkaAclEntry>> change = results.get(0);
        Assertions.assertEquals(ChangeResult.Status.CHANGED, change.status());
        Assertions.assertEquals(ChangeType.DELETE, change.data().getChange().getChangeType());
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
        List<ChangeResult<ValueChange<KafkaAclEntry>>> results = controller
                .reconcile(List.of(entry), ReconciliationMode.CREATE, ReconciliationContext.builder().dryRun(false).build());


        Assertions.assertEquals(1, results.size());
        Assertions.assertEquals(ChangeResult.Status.FAILED, results.get(0).status());
    }
}