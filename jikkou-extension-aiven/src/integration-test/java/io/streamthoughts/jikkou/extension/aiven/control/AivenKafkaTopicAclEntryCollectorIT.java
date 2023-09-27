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

import io.streamthoughts.jikkou.api.config.Configuration;
import io.streamthoughts.jikkou.extension.aiven.AbstractAivenIntegrationTest;
import io.streamthoughts.jikkou.extension.aiven.api.data.Permission;
import io.streamthoughts.jikkou.extension.aiven.models.V1KafkaTopicAclEntry;
import java.util.List;
import okhttp3.mockwebserver.MockResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("integration")
class AivenKafkaTopicAclEntryCollectorIT extends AbstractAivenIntegrationTest {

    private static AivenKafkaTopicAclEntryCollector collector;

    @BeforeEach
    public void beforeEach() {
        collector = new AivenKafkaTopicAclEntryCollector(getAivenApiConfig());
    }

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
        List<V1KafkaTopicAclEntry> results = collector.listAll(Configuration.empty(), NO_SELECTOR);

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