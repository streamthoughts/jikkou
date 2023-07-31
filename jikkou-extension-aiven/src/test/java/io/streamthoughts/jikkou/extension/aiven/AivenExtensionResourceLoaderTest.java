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
package io.streamthoughts.jikkou.extension.aiven;

import io.streamthoughts.jikkou.api.io.ResourceDeserializer;
import io.streamthoughts.jikkou.api.io.ResourceLoader;
import io.streamthoughts.jikkou.api.model.HasItems;
import io.streamthoughts.jikkou.extension.aiven.models.V1KafkaTopicAclEntry;
import io.streamthoughts.jikkou.extension.aiven.models.V1KafkaTopicAclEntryList;
import io.streamthoughts.jikkou.extension.aiven.models.V1SchemaRegistryAclEntry;
import io.streamthoughts.jikkou.extension.aiven.models.V1SchemaRegistryAclEntryList;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class AivenExtensionResourceLoaderTest {


    private final ResourceLoader loader = ResourceLoader.create();

    @Test
    void shouldLoadKafkaTopicAclEntry() {
        // Given
        ResourceDeserializer.registerKind(V1KafkaTopicAclEntry.class);

        // When
        HasItems resources = loader.loadFromClasspath("datasets/kafka-topic-acl-entry.yaml");

        // Then
        Assertions.assertNotNull(resources);
        Assertions.assertFalse(resources.getItems().isEmpty());

        List<V1KafkaTopicAclEntry> resource = resources.getAllByClass(V1KafkaTopicAclEntry.class);
        Assertions.assertEquals(2, resource.size());
    }

    @Test
    void shouldLoadKafkaTopicAclEntryList() {
        // Given
        ResourceDeserializer.registerKind(V1KafkaTopicAclEntryList.class);

        // When
        HasItems resources = loader.loadFromClasspath("datasets/kafka-topic-acl-entry-list.yaml");

        // Then
        Assertions.assertNotNull(resources);
        Assertions.assertFalse(resources.getItems().isEmpty());

        List<V1KafkaTopicAclEntryList> resource = resources.getAllByClass(V1KafkaTopicAclEntryList.class);
        Assertions.assertEquals(1, resource.size());
        Assertions.assertEquals(2, resource.get(0).getItems().size());
    }

    @Test
    void shouldLoadSchemaRegistryAclEntry() {
        // Given
        ResourceDeserializer.registerKind(V1SchemaRegistryAclEntry.class);

        // When
        HasItems resources = loader.loadFromClasspath("datasets/schema-registry-acl-entry.yaml");

        // Then
        Assertions.assertNotNull(resources);
        Assertions.assertFalse(resources.getItems().isEmpty());

        List<V1SchemaRegistryAclEntry> resource = resources.getAllByClass(V1SchemaRegistryAclEntry.class);
        Assertions.assertEquals(2, resource.size());
    }

    @Test
    void shouldLoadSchemaRegistryAclEntryList() {
        // Given
        ResourceDeserializer.registerKind(V1SchemaRegistryAclEntryList.class);

        // When
        HasItems resources = loader.loadFromClasspath("datasets/schema-registry-acl-entry-list.yaml");

        // Then
        Assertions.assertNotNull(resources);
        Assertions.assertFalse(resources.getItems().isEmpty());

        List<V1SchemaRegistryAclEntryList> resource = resources.getAllByClass(V1SchemaRegistryAclEntryList.class);
        Assertions.assertEquals(1, resource.size());
        Assertions.assertEquals(2, resource.get(0).getItems().size());
    }
}