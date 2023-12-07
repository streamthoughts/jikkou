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
package io.streamthoughts.jikkou.extension.aiven.reconciler;

import io.streamthoughts.jikkou.core.ReconciliationContext;
import io.streamthoughts.jikkou.core.ReconciliationMode;
import io.streamthoughts.jikkou.core.models.CoreAnnotations;
import io.streamthoughts.jikkou.core.models.ObjectMeta;
import io.streamthoughts.jikkou.core.models.change.GenericResourceChange;
import io.streamthoughts.jikkou.core.models.change.ResourceChange;
import io.streamthoughts.jikkou.core.models.change.ResourceChangeSpec;
import io.streamthoughts.jikkou.core.models.change.StateChange;
import io.streamthoughts.jikkou.core.reconciler.ChangeResult;
import io.streamthoughts.jikkou.core.reconciler.Operation;
import io.streamthoughts.jikkou.core.reconciler.Reconciler;
import io.streamthoughts.jikkou.extension.aiven.AbstractAivenIntegrationTest;
import io.streamthoughts.jikkou.extension.aiven.adapter.SchemaRegistryAclEntryAdapter;
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
        Reconciler<V1SchemaRegistryAclEntry, ResourceChange> reconciler = new Reconciler<>(controller);
        List<ChangeResult> results = reconciler
                .reconcile(List.of(entry), ReconciliationMode.CREATE, ReconciliationContext.builder().dryRun(false).build());

        // Then
        ChangeResult result = results.getFirst();
        ResourceChange actual = result.change();
        ResourceChange expected = GenericResourceChange
                .builder(V1SchemaRegistryAclEntry.class)
                .withSpec(ResourceChangeSpec
                        .builder()
                        .withOperation(Operation.CREATE)
                        .withChange(StateChange.create(
                                "entry",
                                new SchemaRegistryAclEntry(SchemaRegistryAclEntryAdapter.AivenPermissionMapper.map(Permission.WRITE), "Subject:*", "TestUser"))
                        )
                        .build()
                )
                .build();
        Assertions.assertEquals(expected, actual);
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
        Reconciler<V1SchemaRegistryAclEntry, ResourceChange> reconciler = new Reconciler<>(controller);
        List<ChangeResult> results = reconciler
                .reconcile(List.of(entry), ReconciliationMode.DELETE, ReconciliationContext.builder().dryRun(false).build());

        // Then
        ChangeResult result = results.getFirst();
        ResourceChange actual = result.change();
        ResourceChange expected = GenericResourceChange
                .builder(V1SchemaRegistryAclEntry.class)
                .withSpec(ResourceChangeSpec
                        .builder()
                        .withOperation(Operation.DELETE)
                        .withChange(StateChange.delete(
                                "entry",
                                new SchemaRegistryAclEntry(SchemaRegistryAclEntryAdapter.AivenPermissionMapper.map(Permission.WRITE), "Subject:*", "avnadmin"))
                        )
                        .build()
                )
                .build();
        Assertions.assertEquals(expected, actual);
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
                        .withUsername("avnadmin")
                        .build())
                .build();

        // When
        Reconciler<V1SchemaRegistryAclEntry, ResourceChange> reconciler = new Reconciler<>(controller);
        List<ChangeResult> results = reconciler
                .reconcile(List.of(entry), ReconciliationMode.CREATE, ReconciliationContext.builder().dryRun(false).build());

        ChangeResult result = results.getFirst();
        ResourceChange actual = result.change();
        ResourceChange expected = GenericResourceChange
                .builder(V1SchemaRegistryAclEntry.class)
                .withSpec(ResourceChangeSpec
                        .builder()
                        .withOperation(Operation.CREATE)
                        .withChange(StateChange.create(
                                "entry",
                                new SchemaRegistryAclEntry(SchemaRegistryAclEntryAdapter.AivenPermissionMapper.map(Permission.WRITE), "Invalid:*", "avnadmin"))
                        )
                        .build()
                )
                .build();
        Assertions.assertEquals(expected, actual);
        Assertions.assertEquals(ChangeResult.Status.FAILED, result.status());
    }
}
