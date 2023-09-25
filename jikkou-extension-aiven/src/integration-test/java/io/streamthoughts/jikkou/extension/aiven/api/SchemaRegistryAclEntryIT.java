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
import io.streamthoughts.jikkou.extension.aiven.api.data.Permission;
import io.streamthoughts.jikkou.extension.aiven.api.data.SchemaRegistryAclEntry;
import io.streamthoughts.jikkou.extension.aiven.control.SchemaRegistryAclEntryCollector;
import io.streamthoughts.jikkou.extension.aiven.control.SchemaRegistryAclEntryController;
import io.streamthoughts.jikkou.extension.aiven.models.V1SchemaRegistryAclEntry;
import io.streamthoughts.jikkou.extension.aiven.models.V1SchemaRegistryAclEntrySpec;
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
class SchemaRegistryAclEntryIT {

    public static final List<ResourceSelector> NO_SELECTOR = Collections.emptyList();
    public static final String DEFAULT_AIVEN_ACL_ENTRIES = """
             {
               "acl": [
                 {
                   "id": "default-sr-admin-config",
                   "permission": "schema_registry_write",
                   "resource": "Config:",
                   "username": "avnadmin"
                 },
                 {
                   "id": "default-sr-admin-subject",
                   "permission": "schema_registry_write",
                   "resource": "Subject:*",
                   "username": "avnadmin"
                 }
               ]
             }
            """;

    public static MockWebServer SERVER;

    private static SchemaRegistryAclEntryController CONTROLLER;
    private static SchemaRegistryAclEntryCollector COLLECTOR;

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
        COLLECTOR = new SchemaRegistryAclEntryCollector(new AivenApiClientConfig(configuration));
        CONTROLLER = new SchemaRegistryAclEntryController(new AivenApiClientConfig(configuration));
    }

    @AfterAll
    static void tearDown() throws IOException {
        SERVER.shutdown();
    }

    @Test
    void shouldListSchemaRegistryAclEntries() {
        // Given
        SERVER.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setResponseCode(200)
                .setBody(DEFAULT_AIVEN_ACL_ENTRIES)
        );
        // When
        List<V1SchemaRegistryAclEntry> results = COLLECTOR.listAll(Configuration.empty(), NO_SELECTOR);

        // Then
        Assertions.assertNotNull(results);
        Assertions.assertEquals(2, results.size());

        V1SchemaRegistryAclEntry entry1 = results.get(0);
        Assertions.assertNotNull(entry1.getKind());
        Assertions.assertNotNull(entry1.getApiVersion());
        Assertions.assertEquals(Permission.WRITE, entry1.getSpec().getPermission());
        Assertions.assertEquals("Config:", entry1.getSpec().getResource());
        Assertions.assertEquals("avnadmin", entry1.getSpec().getUsername());

        V1SchemaRegistryAclEntry entry2 = results.get(1);
        Assertions.assertEquals(Permission.WRITE, entry2.getSpec().getPermission());
        Assertions.assertEquals("Subject:*", entry2.getSpec().getResource());
        Assertions.assertEquals("avnadmin", entry2.getSpec().getUsername());
    }

    @Test
    void shouldCreateSchemaRegistryAclEntries() {
        // Given
        SERVER.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setResponseCode(200)
                .setBody(DEFAULT_AIVEN_ACL_ENTRIES)
        );
        SERVER.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setResponseCode(200)
                .setBody("""
                {
                  "acl": [
                    {
                      "id": "default-sr-admin-config",
                      "permission": "schema_registry_write",
                      "resource": "Config:",
                      "username": "avnadmin"
                    },
                    {
                      "id": "default-sr-admin-subject",
                      "permission": "schema_registry_write",
                      "resource": "Subject:*",
                      "username": "avnadmin"
                    },
                    {
                      "id": "acl44c2e14d2da",
                      "permission": "schema_registry_write",
                      "resource": "Subject:*",
                      "username": "TestUser"
                    }
                  ],
                  "message": "added"
                }
                """
                ));
        V1SchemaRegistryAclEntry entry = V1SchemaRegistryAclEntry.builder()
                .withSpec(V1SchemaRegistryAclEntrySpec.builder()
                        .withPermission(Permission.WRITE)
                        .withResource("Subject:*")
                        .withUsername("TestUser")
                        .build())
                .build();

        // When
        List<ChangeResult<ValueChange<SchemaRegistryAclEntry>>> results = CONTROLLER
                .reconcile(List.of(entry), ReconciliationMode.CREATE, ReconciliationContext.builder().dryRun(false).build());

        // Then
        Assertions.assertNotNull(results);
        Assertions.assertEquals(1, results.size());

        ChangeResult<ValueChange<SchemaRegistryAclEntry>> result = results.get(0);
        Assertions.assertEquals(ChangeResult.Status.CHANGED, result.status());
        Assertions.assertEquals(ChangeType.ADD, result.data().getChange().getChangeType());
    }

    @Test
    void shouldDeleteSchemaRegistryAclEntries() {
        // Given
        SERVER.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setResponseCode(200)
                .setBody("""
                {
                  "acl": [
                    {
                      "id": "default-sr-admin-config",
                      "permission": "schema_registry_write",
                      "resource": "Config:",
                      "username": "avnadmin"
                    },
                    {
                      "id": "default-sr-admin-subject",
                      "permission": "schema_registry_write",
                      "resource": "Subject:*",
                      "username": "avnadmin"
                    }
                  ],
                  "message": "deleted"
                }
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
        V1SchemaRegistryAclEntry entry = V1SchemaRegistryAclEntry.builder()
                .withMetadata(ObjectMeta.builder()
                        .withAnnotation(JikkouMetadataAnnotations.JIKKOU_IO_DELETE, true)
                        .build())
                .withSpec(V1SchemaRegistryAclEntrySpec.builder()
                        .withPermission(Permission.WRITE)
                        .withResource("Subject:*")
                        .withUsername("avnadmin")
                        .build())
                .build();

        // When
        List<ChangeResult<ValueChange<SchemaRegistryAclEntry>>> results = CONTROLLER
                .reconcile(List.of(entry), ReconciliationMode.DELETE, ReconciliationContext.builder().dryRun(false).build());

        // Then
        Assertions.assertNotNull(results);
        Assertions.assertEquals(1, results.size());

        ChangeResult<ValueChange<SchemaRegistryAclEntry>> result = results.get(0);
        Assertions.assertEquals(ChangeResult.Status.CHANGED, result.status());
        Assertions.assertEquals(ChangeType.DELETE, result.data().getChange().getChangeType());
    }


    @Test
    void shouldThrowExceptionForInvalidResource() {
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
                        {
                          "errors": [
                            {
                              "message": "Invalid input for resource: Config: or Subject:<subject_name> where subject_name must consist of alpha-numeric characters, underscores, dashes, dots and glob characters '*' and '?'",
                              "status": 400
                            }
                          ],
                          "message": "Invalid input for resource: Config: or Subject:<subject_name> where subject_name must consist of alpha-numeric characters, underscores, dashes, dots and glob characters '*' and '?'"
                        }
                        """
                ));

        V1SchemaRegistryAclEntry entry = V1SchemaRegistryAclEntry.builder()
                .withSpec(V1SchemaRegistryAclEntrySpec.builder()
                        .withPermission(Permission.WRITE)
                        .withResource("Invalid:*")
                        .withUsername("TestUser")
                        .build())
                .build();

        // When
        List<ChangeResult<ValueChange<SchemaRegistryAclEntry>>> results = CONTROLLER
                .reconcile(List.of(entry), ReconciliationMode.CREATE, ReconciliationContext.builder().dryRun(false).build());

        Assertions.assertEquals(1, results.size());
        Assertions.assertEquals(ChangeResult.Status.FAILED, results.get(0).status());
    }
}