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

import static io.streamthoughts.jikkou.kafka.connect.KafkaConnectLabels.KAFKA_CONNECT_CLUSTER;

import io.streamthoughts.jikkou.common.utils.Strings;
import io.streamthoughts.jikkou.core.annotation.Enabled;
import io.streamthoughts.jikkou.core.annotation.SupportedResource;
import io.streamthoughts.jikkou.core.models.ObjectMeta;
import io.streamthoughts.jikkou.core.validation.Validation;
import io.streamthoughts.jikkou.core.validation.ValidationError;
import io.streamthoughts.jikkou.core.validation.ValidationResult;
import io.streamthoughts.jikkou.kafka.connect.models.V1KafkaConnector;
import io.streamthoughts.jikkou.kafka.connect.models.V1KafkaConnectorSpec;
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.NotNull;

@Enabled
@SupportedResource(type = V1KafkaConnector.class)
public class KafkaConnectorResourceValidation implements Validation<V1KafkaConnector> {

    /**
     * {@inheritDoc}
     **/
    @Override
    public ValidationResult validate(@NotNull V1KafkaConnector resource) {
        List<ValidationError> errors = new ArrayList<>();
        ObjectMeta metadata = resource.getMetadata();
        if (metadata == null) {
            errors.add(newError(resource, "Missing or empty field: 'metadata'"));
            return new ValidationResult(errors);
        }

        String name = metadata.getName();
        if (Strings.isBlank(name)) {
            errors.add(newError(resource, "Missing or empty field: 'metadata.name'."));
        }

        if (!metadata.hasLabel(KAFKA_CONNECT_CLUSTER)) {
            errors.add(newError(resource, "Missing or empty field: 'metadata.labels." + KAFKA_CONNECT_CLUSTER + "'."));
        }

        V1KafkaConnectorSpec spec = resource.getSpec();
        if (Strings.isBlank(spec.getConnectorClass())) {
            errors.add(newError(resource, "Missing or empty field: 'spec.connectorClass'."));
        }

        if (spec.getTasksMax() == null) {
            errors.add(newError(resource, "Missing or empty field: 'spec.tasksMax'."));
        }

        return new ValidationResult(errors);
    }

    @NotNull
    private ValidationError newError(@NotNull V1KafkaConnector resource, String message) {
        return new ValidationError(
                getName(),
                resource,
                message
        );
    }
}
