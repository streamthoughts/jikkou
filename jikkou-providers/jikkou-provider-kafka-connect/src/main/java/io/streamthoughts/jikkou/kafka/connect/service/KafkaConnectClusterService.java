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
package io.streamthoughts.jikkou.kafka.connect.service;

import static io.streamthoughts.jikkou.kafka.connect.KafkaConnectConstants.CONNECTOR_CLASS_CONFIG;
import static io.streamthoughts.jikkou.kafka.connect.KafkaConnectConstants.CONNECTOR_TASKS_MAX_CONFIG;

import io.streamthoughts.jikkou.common.utils.Strings;
import io.streamthoughts.jikkou.core.models.ObjectMeta;
import io.streamthoughts.jikkou.kafka.connect.KafkaConnectLabels;
import io.streamthoughts.jikkou.kafka.connect.api.KafkaConnectApi;
import io.streamthoughts.jikkou.kafka.connect.internals.KafkaConnectUtils;
import io.streamthoughts.jikkou.kafka.connect.models.KafkaConnectorState;
import io.streamthoughts.jikkou.kafka.connect.models.V1KafkaConnector;
import io.streamthoughts.jikkou.kafka.connect.models.V1KafkaConnectorSpec;
import io.streamthoughts.jikkou.kafka.connect.models.V1KafkaConnectorStatus;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import org.jetbrains.annotations.NotNull;

/**
 * Service for managing Kafka Connect.
 */
public final class KafkaConnectClusterService {

    private static final String DEFAULT_CONNECTOR_TASKS_MAX = "1";

    private final String clusterName;
    private final KafkaConnectApi api;

    /**
     * Creates a new {@link KafkaConnectClusterService} instance.
     *
     * @param clusterName The cluster name.
     * @param api         The KafkaConnectApi.
     */
    public KafkaConnectClusterService(@NotNull final String clusterName,
                                      @NotNull final KafkaConnectApi api) {
        this.clusterName = Objects.requireNonNull(clusterName, "clusterName cannot be null");
        this.api = Objects.requireNonNull(api, "api cannot be null");
    }


    public CompletableFuture<V1KafkaConnector> getConnectorAsync(@NotNull final String connectorName,
                                                                 boolean expandStatus) {
        if (Strings.isBlank(connectorName)) {
            throw new IllegalArgumentException("connectorName is null or empty.");
        }
        return CompletableFuture
                .supplyAsync(() -> api.getConnectorConfig(connectorName))
                .thenCombine(
                        CompletableFuture.supplyAsync(() -> api.getConnectorStatus(connectorName)),
                        (config, status) -> {
                            String connectorClass = Optional.ofNullable(config.get(CONNECTOR_CLASS_CONFIG))
                                    .map(Object::toString)
                                    .orElse(null);

                            String connectorTasksMax = Optional.ofNullable(config.get(CONNECTOR_TASKS_MAX_CONFIG))
                                    .map(Object::toString)
                                    .orElse(DEFAULT_CONNECTOR_TASKS_MAX);

                            return V1KafkaConnector.
                                    builder()
                                    .withMetadata(ObjectMeta
                                            .builder()
                                            .withName(connectorName)
                                            .withLabel(KafkaConnectLabels.KAFKA_CONNECT_CLUSTER, clusterName)
                                            .build()
                                    )
                                    .withSpec(V1KafkaConnectorSpec
                                            .builder()
                                            .withConnectorClass(connectorClass)
                                            .withTasksMax(Integer.parseInt(connectorTasksMax))
                                            .withConfig(KafkaConnectUtils.removeCommonConnectorConfig(config))
                                            .withState(KafkaConnectorState.fromValue(status.connector().state()))
                                            .build()
                                    )
                                    .withStatus(expandStatus ? new V1KafkaConnectorStatus(status) : null)
                                    .build();
                        }
                );
    }
}
