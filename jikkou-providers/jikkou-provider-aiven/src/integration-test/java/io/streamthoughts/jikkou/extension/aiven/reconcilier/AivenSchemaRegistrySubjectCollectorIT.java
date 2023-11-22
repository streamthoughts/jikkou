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

import io.streamthoughts.jikkou.core.config.Configuration;
import io.streamthoughts.jikkou.core.models.ResourceListObject;
import io.streamthoughts.jikkou.core.selectors.Selector;
import io.streamthoughts.jikkou.extension.aiven.AbstractAivenIntegrationTest;
import io.streamthoughts.jikkou.schema.registry.models.V1SchemaRegistrySubject;
import java.util.Collections;
import java.util.List;
import okhttp3.mockwebserver.MockResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("integration")
class AivenSchemaRegistrySubjectCollectorIT extends AbstractAivenIntegrationTest {

    public static final List<Selector> NO_SELECTOR = Collections.emptyList();

    private static AivenSchemaRegistrySubjectCollector collector;

    @BeforeEach
    public void beforeEach() {
        collector = new AivenSchemaRegistrySubjectCollector(getAivenApiConfig());
    }

    @Test
    void shouldListSchemaSubjects() {
        // Given
        enqueueResponse(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setResponseCode(200)
                .setBody("""
                        {
                            "subjects": [ "test" ]
                        }
                        """)
        );
        enqueueResponse(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setResponseCode(200)
                .setBody("""
                        {
                          "version": {
                            "id": 1,
                            "schema": "{\\"fields\\":[{\\"default\\":null,\\"name\\":\\"name\\",\\"type\\":[\\"null\\",\\"string\\"]},{\\"default\\":null,\\"name\\":\\"favorite_number\\",\\"type\\":[\\"null\\",\\"int\\"]},{\\"default\\":null,\\"name\\":\\"favorite_color\\",\\"type\\":[\\"null\\",\\"string\\"]}],\\"name\\":\\"User\\",\\"namespace\\":\\"example.avro\\",\\"type\\":\\"record\\"}",
                            "subject": "User",
                            "version": 1
                          }
                        }
                        """)
        );
        enqueueResponse(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setResponseCode(200)
                .setBody("""
                        {
                          "compatibilityLevel": "BACKWARD"
                        }
                        """)
        );
        // When
        ResourceListObject<V1SchemaRegistrySubject> result = collector.listAll(Configuration.empty(), NO_SELECTOR);

        // Then
        Assertions.assertNotNull(result);
    }
}