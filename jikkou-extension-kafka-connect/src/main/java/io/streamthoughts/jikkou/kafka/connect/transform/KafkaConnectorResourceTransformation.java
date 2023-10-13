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
package io.streamthoughts.jikkou.kafka.connect.transform;

import io.streamthoughts.jikkou.annotation.AcceptsResource;
import io.streamthoughts.jikkou.annotation.ExtensionEnabled;
import io.streamthoughts.jikkou.api.model.HasItems;
import io.streamthoughts.jikkou.api.transform.ResourceTransformation;
import io.streamthoughts.jikkou.kafka.connect.internals.KafkaConnectUtils;
import io.streamthoughts.jikkou.kafka.connect.models.V1KafkaConnector;
import io.streamthoughts.jikkou.kafka.connect.models.V1KafkaConnectorSpec;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;

@ExtensionEnabled
@AcceptsResource(type = V1KafkaConnector.class)
public class KafkaConnectorResourceTransformation implements ResourceTransformation<V1KafkaConnector> {

    /**
     * {@inheritDoc}
     **/
    @Override
    public @NotNull Optional<V1KafkaConnector> transform(@NotNull V1KafkaConnector resource,
                                                         @NotNull HasItems resources) {
        V1KafkaConnectorSpec spec = resource.getSpec();
        if (spec != null && spec.getConfig() != null) {
            V1KafkaConnectorSpec newSpec = spec.toBuilder()
                    .withConfig(KafkaConnectUtils.removeCommonConnectorConfig(spec.getConfig()))
                    .build();
            return Optional.of(resource.withSpec(newSpec));
        }
        return Optional.of(resource);
    }
}
