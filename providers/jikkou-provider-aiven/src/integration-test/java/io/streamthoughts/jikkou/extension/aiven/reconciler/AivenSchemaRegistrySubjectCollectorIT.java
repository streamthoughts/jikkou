/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.extension.aiven.reconciler;

import io.streamthoughts.jikkou.core.config.Configuration;
import io.streamthoughts.jikkou.core.models.ResourceListObject;
import io.streamthoughts.jikkou.core.selector.Selector;
import io.streamthoughts.jikkou.core.selector.Selectors;
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
        ResourceListObject<V1SchemaRegistrySubject> result = collector.listAll(Configuration.empty(), Selectors.NO_SELECTOR);

        // Then
        Assertions.assertNotNull(result);
    }
}