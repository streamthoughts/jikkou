/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.schema.registry;

import io.streamthoughts.jikkou.core.io.Jackson;
import io.streamthoughts.jikkou.core.io.ResourceLoader;
import io.streamthoughts.jikkou.core.io.reader.ResourceReaderFactory;
import io.streamthoughts.jikkou.core.models.HasItems;
import io.streamthoughts.jikkou.core.resource.ResourceDeserializer;
import io.streamthoughts.jikkou.schema.registry.models.V1SchemaRegistrySubject;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class SchemaRegistryExtensionResourceLoaderTest {
    
    private final ResourceLoader loader = new ResourceLoader(new ResourceReaderFactory(Jackson.YAML_OBJECT_MAPPER));

    @Test
    void shouldLoadResourcesForSchemaRegistrySubject() {
        // Given
        ResourceDeserializer.registerKind(V1SchemaRegistrySubject.class);

        // When
        HasItems resources = loader.loadFromClasspath("datasets/resource-subject-test.yaml");

        // Then
        Assertions.assertNotNull(resources);
        Assertions.assertFalse(resources.getItems().isEmpty());

        List<V1SchemaRegistrySubject> subjects = resources.getAllByClass(V1SchemaRegistrySubject.class);
        Assertions.assertEquals(1, subjects.size());
    }
}