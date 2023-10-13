/*
 * Copyright 2022 The original authors
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
package io.streamthoughts.jikkou.kafka.connect;

import io.streamthoughts.jikkou.api.io.Jackson;
import io.streamthoughts.jikkou.api.io.ResourceDeserializer;
import io.streamthoughts.jikkou.api.io.ResourceLoader;
import io.streamthoughts.jikkou.api.io.readers.ResourceReaderFactory;
import io.streamthoughts.jikkou.api.model.Configs;
import io.streamthoughts.jikkou.api.model.HasItems;
import io.streamthoughts.jikkou.api.model.ObjectMeta;
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
                                .withConfig(Configs.of(Map.of(
                                    "file", "/tmp/test.sink.txt",
                                    "topics", "connect-test"
                                )))
                                .build()
                        )
                        .build(),
                resource.get(0)
        );
    }
}