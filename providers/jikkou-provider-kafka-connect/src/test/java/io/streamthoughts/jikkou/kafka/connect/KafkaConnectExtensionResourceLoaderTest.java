/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.kafka.connect;

import io.streamthoughts.jikkou.core.io.Jackson;
import io.streamthoughts.jikkou.core.io.ResourceDeserializer;
import io.streamthoughts.jikkou.core.io.ResourceLoader;
import io.streamthoughts.jikkou.core.io.reader.ResourceReaderFactory;
import io.streamthoughts.jikkou.core.models.HasItems;
import io.streamthoughts.jikkou.core.models.ObjectMeta;
import io.streamthoughts.jikkou.kafka.connect.models.KafkaConnectorState;
import io.streamthoughts.jikkou.kafka.connect.models.V1KafkaConnector;
import io.streamthoughts.jikkou.kafka.connect.models.V1KafkaConnectorSpec;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class KafkaConnectExtensionResourceLoaderTest {

    private final ResourceLoader loader = new ResourceLoader(new ResourceReaderFactory(Jackson.YAML_OBJECT_MAPPER));

    @Test
    void shouldLoadKafkaTopicAclEntry() {
        // Given
        ResourceDeserializer.registerKind(V1KafkaConnector.class);

        // When
        HasItems resources = loader.loadFromClasspath("datasets/kafka-connector-filestream-sink.yaml");

        // Then
        Assertions.assertNotNull(resources);
        Assertions.assertFalse(resources.getItems().isEmpty());

        List<V1KafkaConnector> resource = resources.getAllByClass(V1KafkaConnector.class);
        Assertions.assertEquals(1, resource.size());
        Assertions.assertEquals(
                V1KafkaConnector
                        .builder()
                        .withMetadata(ObjectMeta
                                .builder()
                                .withName("local-file-sink")
                                .withLabel("kafka.jikkou.io/connect-cluster", "my-kconnect-cluster")
                                .build()
                        )
                        .withSpec(V1KafkaConnectorSpec
                                .builder()
                                .withConnectorClass("FileStreamSink")
                                .withTasksMax(1)
                                .withState(KafkaConnectorState.RUNNING)
                                .withConfig(Map.of(
                                    "file", "/tmp/test.sink.txt",
                                    "topics", "connect-test"
                                ))
                                .build()
                        )
                        .build(),
                resource.getFirst()
        );
    }
}