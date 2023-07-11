/*
 * Copyright 2022 StreamThoughts.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.streamthoughts.jikkou.schema.registry;

import io.streamthoughts.jikkou.api.io.ResourceDeserializer;
import io.streamthoughts.jikkou.api.io.ResourceLoader;
import io.streamthoughts.jikkou.api.model.HasItems;
import io.streamthoughts.jikkou.schema.registry.models.V1SchemaRegistrySubject;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class SchemaRegistryExtensionResourceLoaderTest {


    private final ResourceLoader loader = ResourceLoader.create();

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