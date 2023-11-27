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
package io.streamthoughts.jikkou.kafka.connect.validation;

import io.streamthoughts.jikkou.core.models.Configs;
import io.streamthoughts.jikkou.core.models.ObjectMeta;
import io.streamthoughts.jikkou.core.validation.ValidationError;
import io.streamthoughts.jikkou.core.validation.ValidationResult;
import io.streamthoughts.jikkou.kafka.connect.KafkaConnectLabels;
import io.streamthoughts.jikkou.kafka.connect.models.V1KafkaConnector;
import io.streamthoughts.jikkou.kafka.connect.models.V1KafkaConnectorSpec;
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
                        .withConfig(Configs.empty())
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
                        .withConfig(Configs.empty())
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