/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.extension.aiven.reconciler;

import io.streamthoughts.jikkou.core.config.Configuration;
import io.streamthoughts.jikkou.core.selector.Selectors;
import io.streamthoughts.jikkou.extension.aiven.AbstractAivenIntegrationTest;
import io.streamthoughts.jikkou.extension.aiven.api.data.Permission;
import io.streamthoughts.jikkou.extension.aiven.models.V1SchemaRegistryAclEntry;
import java.util.List;
import okhttp3.mockwebserver.MockResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("integration")
public class AivenSchemaRegistryAclEntryCollectorIT extends AbstractAivenIntegrationTest {

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

    private AivenSchemaRegistryAclEntryCollector collector;


    @BeforeEach
    public void beforeEach() {
        collector = new AivenSchemaRegistryAclEntryCollector(getAivenApiConfig());
    }

    @Test
    void shouldListSchemaRegistryAclEntries() {
        // Given
        enqueueResponse(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setResponseCode(200)
                .setBody(DEFAULT_AIVEN_ACL_ENTRIES)
        );
        // When
        List<V1SchemaRegistryAclEntry> results = collector.listAll(Configuration.empty(), Selectors.NO_SELECTOR)
                .getItems();

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
}
