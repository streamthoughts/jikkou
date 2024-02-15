/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.kafka.connect.transform;

import io.streamthoughts.jikkou.core.ReconciliationContext;
import io.streamthoughts.jikkou.core.annotation.Enabled;
import io.streamthoughts.jikkou.core.annotation.SupportedResource;
import io.streamthoughts.jikkou.core.models.HasItems;
import io.streamthoughts.jikkou.core.transform.Transformation;
import io.streamthoughts.jikkou.kafka.connect.internals.KafkaConnectUtils;
import io.streamthoughts.jikkou.kafka.connect.models.V1KafkaConnector;
import io.streamthoughts.jikkou.kafka.connect.models.V1KafkaConnectorSpec;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;

@Enabled
@SupportedResource(type = V1KafkaConnector.class)
public class KafkaConnectorResourceTransformation implements Transformation<V1KafkaConnector> {

    /**
     * {@inheritDoc}
     **/
    @Override
    public @NotNull Optional<V1KafkaConnector> transform(@NotNull V1KafkaConnector resource,
                                                         @NotNull HasItems resources,
                                                         @NotNull ReconciliationContext context) {
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
