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
package io.streamthoughts.jikkou.extension.aiven.reconcilier;

import io.streamthoughts.jikkou.core.ReconciliationContext;
import io.streamthoughts.jikkou.core.ReconciliationMode;
import io.streamthoughts.jikkou.core.models.CoreAnnotations;
import io.streamthoughts.jikkou.core.models.ObjectMeta;
import io.streamthoughts.jikkou.core.reconcilier.ChangeResult;
import io.streamthoughts.jikkou.core.reconcilier.ChangeType;
import io.streamthoughts.jikkou.core.reconcilier.DefaultChangeResult;
import io.streamthoughts.jikkou.core.reconcilier.Reconcilier;
import io.streamthoughts.jikkou.core.reconcilier.change.ValueChange;
import io.streamthoughts.jikkou.extension.aiven.AbstractAivenIntegrationTest;
import io.streamthoughts.jikkou.extension.aiven.api.data.Permission;
import io.streamthoughts.jikkou.extension.aiven.api.data.SchemaRegistryAclEntry;
import io.streamthoughts.jikkou.extension.aiven.models.V1SchemaRegistryAclEntry;
import io.streamthoughts.jikkou.extension.aiven.models.V1SchemaRegistryAclEntrySpec;
import java.util.List;
import okhttp3.mockwebserver.MockResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("integration")
public class AivenSchemaRegistryAclEntryControllerIT extends AbstractAivenIntegrationTest {

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

    private AivenSchemaRegistryAclEntryController controller;


    @BeforeEach
    public void beforeEach() {
        controller = new AivenSchemaRegistryAclEntryController(getAivenApiConfig());
    }


    @Test
    void shouldCreateSchemaRegistryAclEntries() {
        // Given
       enqueueResponse(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setResponseCode(200)
                .setBody(DEFAULT_AIVEN_ACL_ENTRIES)
        );
        enqueueResponse(new MockResponse()
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
        Reconcilier<V1SchemaRegistryAclEntry, ValueChange<SchemaRegistryAclEntry>> reconcilier = new Reconcilier<>(controller);
        List<ChangeResult<ValueChange<SchemaRegistryAclEntry>>> results = reconcilier
                .reconcile(List.of(entry), ReconciliationMode.CREATE, ReconciliationContext.builder().dryRun(false).build());

        // Then
        Assertions.assertNotNull(results);
        Assertions.assertEquals(1, results.size());

        ChangeResult<ValueChange<SchemaRegistryAclEntry>> result = results.get(0);
        Assertions.assertEquals(DefaultChangeResult.Status.CHANGED, result.status());
        Assertions.assertEquals(ChangeType.ADD, result.data().getChange().operation());
    }

    @Test
    void shouldDeleteSchemaRegistryAclEntries() {
        // Given
        enqueueResponse(new MockResponse()
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
        enqueueResponse(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setResponseCode(200)
                .setBody("""
                        {"acl":[],"message":"deleted"}
                        """

                ));
        // When
        V1SchemaRegistryAclEntry entry = V1SchemaRegistryAclEntry.builder()
                .withMetadata(ObjectMeta.builder()
                        .withAnnotation(CoreAnnotations.JIKKOU_IO_DELETE, true)
                        .build())
                .withSpec(V1SchemaRegistryAclEntrySpec.builder()
                        .withPermission(Permission.WRITE)
                        .withResource("Subject:*")
                        .withUsername("avnadmin")
                        .build())
                .build();

        // When
        Reconcilier<V1SchemaRegistryAclEntry, ValueChange<SchemaRegistryAclEntry>> reconcilier = new Reconcilier<>(controller);
        List<ChangeResult<ValueChange<SchemaRegistryAclEntry>>> results = reconcilier
                .reconcile(List.of(entry), ReconciliationMode.DELETE, ReconciliationContext.builder().dryRun(false).build());

        // Then
        Assertions.assertNotNull(results);
        Assertions.assertEquals(1, results.size());

        ChangeResult<ValueChange<SchemaRegistryAclEntry>> result = results.get(0);
        Assertions.assertEquals(DefaultChangeResult.Status.CHANGED, result.status());
        Assertions.assertEquals(ChangeType.DELETE, result.data().getChange().operation());
    }


    @Test
    void shouldThrowExceptionForInvalidResource() {
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
        Reconcilier<V1SchemaRegistryAclEntry, ValueChange<SchemaRegistryAclEntry>> reconcilier = new Reconcilier<>(controller);
        List<ChangeResult<ValueChange<SchemaRegistryAclEntry>>> results = reconcilier
                .reconcile(List.of(entry), ReconciliationMode.CREATE, ReconciliationContext.builder().dryRun(false).build());

        Assertions.assertEquals(1, results.size());
        Assertions.assertEquals(DefaultChangeResult.Status.FAILED, results.get(0).status());
    }
}
