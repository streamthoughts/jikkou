/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.kafka.connect.validation;

import io.streamthoughts.jikkou.core.models.ObjectMeta;
import io.streamthoughts.jikkou.core.validation.ValidationError;
import io.streamthoughts.jikkou.core.validation.ValidationResult;
import io.streamthoughts.jikkou.kafka.connect.KafkaConnectLabels;
import io.streamthoughts.jikkou.kafka.connect.models.V1KafkaConnector;
import io.streamthoughts.jikkou.kafka.connect.models.V1KafkaConnectorSpec;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class KafkaConnectorResourceValidationTest {

    @Test
    void shouldReturnErrorForResourceWithClusterLabel() {
        // Given
        V1KafkaConnector connector = V1KafkaConnector
                .builder()
                .withMetadata(new ObjectMeta("name"))
                .withSpec(V1KafkaConnectorSpec
                        .builder()
                        .withConnectorClass("connectClass")
                        .withConfig(Collections.emptyMap())
                        .withTasksMax(1)
                        .build())
                .build();

        // When
        KafkaConnectorResourceValidation validation = new KafkaConnectorResourceValidation();
        ValidationResult result = validation.validate(connector);

        // Then
        List<ValidationError> errors = result.errors();
        Assertions.assertEquals(List.of(new ValidationError(
                "KafkaConnectorResourceValidation",
                connector,
                "Missing or empty field: 'metadata.labels.kafka.jikkou.io/connect-cluster'.",
                new HashMap<>()
        )), errors);
    }

    @Test
    void shouldReturnErrorForResourceWithNoName() {
        // Given
        V1KafkaConnector connector = V1KafkaConnector
                .builder()
                .withMetadata(ObjectMeta
                        .builder()
                        .withLabel(KafkaConnectLabels.KAFKA_CONNECT_CLUSTER, "test")
                        .build())
                .withSpec(V1KafkaConnectorSpec
                        .builder()
                        .withConnectorClass("connectClass")
                        .withConfig(Collections.emptyMap())
                        .withTasksMax(1)
                        .build())
                .build();

        // When
        KafkaConnectorResourceValidation validation = new KafkaConnectorResourceValidation();
        ValidationResult result = validation.validate(connector);

        // Then
        List<ValidationError> errors = result.errors();
        Assertions.assertEquals(List.of(new ValidationError(
                "KafkaConnectorResourceValidation",
                connector,
                "Missing or empty field: 'metadata.name'.",
                new HashMap<>()
        )), errors);
    }
}