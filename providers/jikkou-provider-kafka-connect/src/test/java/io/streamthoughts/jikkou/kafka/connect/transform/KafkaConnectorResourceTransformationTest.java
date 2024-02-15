/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.kafka.connect.transform;

import io.streamthoughts.jikkou.core.ReconciliationContext;
import io.streamthoughts.jikkou.core.models.ResourceListObject;
import io.streamthoughts.jikkou.kafka.connect.models.V1KafkaConnector;
import io.streamthoughts.jikkou.kafka.connect.models.V1KafkaConnectorSpec;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class KafkaConnectorResourceTransformationTest {

    @Test
    void shouldRemoveConnectorCommonConfig() {
        // Given
        V1KafkaConnector resource = V1KafkaConnector
                .builder()
                .withSpec(V1KafkaConnectorSpec
                        .builder()
                        .withConfig(Map.of(
                                "connector.class", "???",
                                "tasks.max", "???",
                                "name", "???"
                        ))
                        .build()
                )
                .build();
        // When
        KafkaConnectorResourceTransformation transformation = new KafkaConnectorResourceTransformation();
        Optional<V1KafkaConnector> result = transformation.transform(
                resource,
                ResourceListObject.empty(),
                ReconciliationContext.Default.EMPTY
        );

        // Then
        V1KafkaConnector transformed = result.get();
        Assertions.assertTrue(transformed.getSpec().getConfig().isEmpty());
    }
}