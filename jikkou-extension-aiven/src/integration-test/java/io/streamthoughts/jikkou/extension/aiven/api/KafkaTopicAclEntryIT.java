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
import io.streamthoughts.jikkou.api.change.ChangeType;
import io.streamthoughts.jikkou.api.change.ValueChange;
import io.streamthoughts.jikkou.api.config.Configuration;
import io.streamthoughts.jikkou.api.model.ObjectMeta;
import io.streamthoughts.jikkou.api.selector.ResourceSelector;
import io.streamthoughts.jikkou.extension.aiven.api.data.KafkaAclEntry;
import io.streamthoughts.jikkou.extension.aiven.api.data.Permission;
import io.streamthoughts.jikkou.extension.aiven.control.KafkaTopicAclEntryCollector;
import io.streamthoughts.jikkou.extension.aiven.control.KafkaTopicAclEntryController;
import io.streamthoughts.jikkou.extension.aiven.models.V1KafkaTopicAclEntry;
import io.streamthoughts.jikkou.extension.aiven.models.V1KafkaTopicAclEntrySpec;
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
class KafkaTopicAclEntryIT {

    public static final List<ResourceSelector> NO_SELECTOR = Collections.emptyList();

    public static MockWebServer SERVER;

    private static KafkaTopicAclEntryController CONTROLLER;
    private static KafkaTopicAclEntryCollector COLLECTOR;

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
        COLLECTOR = new KafkaTopicAclEntryCollector(new AivenApiClientConfig(configuration));
        CONTROLLER = new KafkaTopicAclEntryController(new AivenApiClientConfig(configuration));
    }

    @AfterAll
    static void tearDown() throws IOException {
        COLLECTOR.close();
        CONTROLLER.close();
        SERVER.shutdown();
    }

    @Test
    void shouldListKafkaAclEntries() {
        // Given
        SERVER.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setResponseCode(200)
                .setBody("""
                        {"acl":[{"id":"default","permission":"admin","topic":"*","username":"avnadmin"}]}
                        """
                ));
        // When
        List<V1KafkaTopicAclEntry> results = COLLECTOR.listAll(Configuration.empty(), NO_SELECTOR);

        // Then
        Assertions.assertNotNull(results);
        Assertions.assertEquals(1, results.size());

        V1KafkaTopicAclEntry entry = results.get(0);
        Assertions.assertNotNull(entry.getKind());
        Assertions.assertNotNull(entry.getApiVersion());
        Assertions.assertEquals(Permission.ADMIN, entry.getSpec().getPermission());
        Assertions.assertEquals("*", entry.getSpec().getTopic());
        Assertions.assertEquals("avnadmin", entry.getSpec().getUsername());
    }

    @Test
    void shouldCreateKafkaAclEntries() {
        // Given
        SERVER.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setResponseCode(200)
                .setBody("""
                        {"acl":[{"id":"default","permission":"admin","topic":"*","username":"avnadmin"}]}
                        """
                ));
        SERVER.enqueue(new MockResponse()
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
        List<ChangeResult<ValueChange<KafkaAclEntry>>> results = CONTROLLER
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
        SERVER.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setResponseCode(200)
                .setBody("""
                        {"acl":[{"id":"default","permission":"admin","topic":"*","username":"avnadmin"}]}
                        """
                ));
        SERVER.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setResponseCode(200)
                .setBody("""
                        {"acl":[],"message":"deleted"}
                        """

                ));
        // When
        V1KafkaTopicAclEntry entry = V1KafkaTopicAclEntry.builder()
                .withMetadata(ObjectMeta.builder()
                        .withAnnotation(JikkouMetadataAnnotations.JIKKOU_IO_DELETE, true)
                        .build())
                .withSpec(V1KafkaTopicAclEntrySpec.builder()
                        .withPermission(Permission.ADMIN)
                        .withTopic("*")
                        .withUsername("avnadmin")
                        .build())
                .build();

        // When
        List<ChangeResult<ValueChange<KafkaAclEntry>>> results = CONTROLLER
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
        SERVER.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setResponseCode(200)
                .setBody("""
                        {"acl":[]}
                        """
                ));
        SERVER.enqueue(new MockResponse()
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
        List<ChangeResult<ValueChange<KafkaAclEntry>>> results = CONTROLLER
                .reconcile(List.of(entry), ReconciliationMode.CREATE, ReconciliationContext.builder().dryRun(false).build());


        Assertions.assertEquals(1, results.size());
        Assertions.assertEquals(ChangeResult.Status.FAILED, results.get(0).status());
    }
}