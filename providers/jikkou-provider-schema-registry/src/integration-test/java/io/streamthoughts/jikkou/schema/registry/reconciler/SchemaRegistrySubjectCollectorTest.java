/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.schema.registry.reconciler;

import io.streamthoughts.jikkou.core.config.Configuration;
import io.streamthoughts.jikkou.core.selector.Selectors;
import io.streamthoughts.jikkou.schema.registry.AbstractIntegrationTest;
import io.streamthoughts.jikkou.schema.registry.api.AsyncSchemaRegistryApi;
import io.streamthoughts.jikkou.schema.registry.api.data.SubjectSchemaRegistration;
import io.streamthoughts.jikkou.schema.registry.model.CompatibilityLevels;
import io.streamthoughts.jikkou.schema.registry.model.Modes;
import io.streamthoughts.jikkou.schema.registry.model.SchemaType;
import io.streamthoughts.jikkou.schema.registry.models.V1SchemaRegistrySubject;
import java.util.List;
import java.util.concurrent.ExecutionException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SchemaRegistrySubjectCollectorTest extends AbstractIntegrationTest {

    private SchemaRegistrySubjectCollector collector;

    @BeforeEach
    public void beforeEach() throws ExecutionException, InterruptedException {
        collector = new SchemaRegistrySubjectCollector(getSchemaRegistryClientConfiguration());
        collector.prettyPrintSchema(false);

        AsyncSchemaRegistryApi api = getAsyncSchemaRegistryApi();
        api.registerSubjectVersion(
                TEST_SUBJECT,
                new SubjectSchemaRegistration(null, null, AVRO_SCHEMA, SchemaType.AVRO),
                false
        ).get();
    }

    @Test
    public void shouldGetAllSchemasWithGlobalCompatibilityLevelAndGlobalModeTrue() {
        // Given
        collector.defaultToGlobalCompatibilityLevel(true);
        collector.defaultToGlobalMode(true);

        // When
        List<V1SchemaRegistrySubject> resources = collector.listAll(Configuration.empty(), Selectors.NO_SELECTOR)
                .getItems();

        // Then
        Assertions.assertNotNull(resources);
        Assertions.assertEquals(1, resources.size());

        V1SchemaRegistrySubject subject = resources.get(0);
        Assertions.assertEquals(TEST_SUBJECT, subject.getMetadata().getName());
        Assertions.assertEquals(SchemaType.AVRO, subject.getSpec().getSchemaType());
        Assertions.assertEquals(CompatibilityLevels.BACKWARD, subject.getSpec().getCompatibilityLevel());
        Assertions.assertEquals(Modes.READWRITE, subject.getSpec().getMode());
    }

    @Test
    public void shouldGetAllSchemasWithGlobalCompatibilityLevelAndGlobalModeFalse() {
        // Given
        collector.defaultToGlobalCompatibilityLevel(false);
        collector.defaultToGlobalMode(false);

        // When
        List<V1SchemaRegistrySubject> resources = collector.listAll(Configuration.empty(), Selectors.NO_SELECTOR)
                .getItems();

        // Then
        Assertions.assertNotNull(resources);
        Assertions.assertEquals(1, resources.size());

        V1SchemaRegistrySubject subject = resources.get(0);
        Assertions.assertEquals(TEST_SUBJECT, subject.getMetadata().getName());
        Assertions.assertEquals(SchemaType.AVRO, subject.getSpec().getSchemaType());
        Assertions.assertNull(subject.getSpec().getCompatibilityLevel());
        Assertions.assertNull(subject.getSpec().getMode());
    }
}
