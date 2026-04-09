/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.jikkou.extension.aiven.reconciler;

import io.jikkou.core.config.Configuration;
import io.jikkou.core.models.ResourceList;
import io.jikkou.core.selector.Selectors;
import io.jikkou.extension.aiven.BaseExtensionProviderIT;
import io.jikkou.extension.aiven.api.data.Permission;
import io.jikkou.extension.aiven.models.V1KafkaTopicAclEntry;
import java.util.List;
import okhttp3.mockwebserver.MockResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("integration")
class AivenKafkaTopicAclEntryCollectorIT extends BaseExtensionProviderIT {

    @Test
    void shouldListKafkaAclEntries() {
        // Given
        enqueueResponse(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setResponseCode(200)
                .setBody("""
                        {"acl":[{"id":"default","permission":"admin","topic":"*","username":"avnadmin"}]}
                        """
                ));
        // When
        ResourceList<V1KafkaTopicAclEntry> resources = api.listResources(V1KafkaTopicAclEntry.class, Selectors.NO_SELECTOR, Configuration.empty());
        List<V1KafkaTopicAclEntry> results = resources.getItems();

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
}