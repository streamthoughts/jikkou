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
package io.streamthoughts.jikkou.kafka.connect.reconcilier;

import static io.streamthoughts.jikkou.kafka.connect.KafkaConnectConstants.CONNECTOR_CLASS_CONFIG;
import static io.streamthoughts.jikkou.kafka.connect.KafkaConnectConstants.CONNECTOR_TASKS_MAX_CONFIG;

import io.streamthoughts.jikkou.core.annotation.SupportedResource;
import io.streamthoughts.jikkou.core.config.ConfigProperty;
import io.streamthoughts.jikkou.core.config.Configuration;
import io.streamthoughts.jikkou.core.exceptions.ConfigException;
import io.streamthoughts.jikkou.core.exceptions.JikkouRuntimeException;
import io.streamthoughts.jikkou.core.extension.annotations.ConfigPropertySpec;
import io.streamthoughts.jikkou.core.extension.annotations.ExtensionConfigProperties;
import io.streamthoughts.jikkou.core.models.ObjectMeta;
import io.streamthoughts.jikkou.core.models.ResourceListObject;
import io.streamthoughts.jikkou.core.reconcilier.Collector;
import io.streamthoughts.jikkou.core.selectors.Selector;
import io.streamthoughts.jikkou.kafka.connect.KafkaConnectExtensionConfig;
import io.streamthoughts.jikkou.kafka.connect.KafkaConnectLabels;
import io.streamthoughts.jikkou.kafka.connect.api.KafkaConnectApi;
import io.streamthoughts.jikkou.kafka.connect.api.KafkaConnectApiFactory;
import io.streamthoughts.jikkou.kafka.connect.api.KafkaConnectClientConfig;
import io.streamthoughts.jikkou.kafka.connect.collections.V1KafkaConnectorList;
import io.streamthoughts.jikkou.kafka.connect.internals.KafkaConnectUtils;
import io.streamthoughts.jikkou.kafka.connect.models.KafkaConnectorState;
import io.streamthoughts.jikkou.kafka.connect.models.V1KafkaConnector;
import io.streamthoughts.jikkou.kafka.connect.models.V1KafkaConnectorSpec;
import io.streamthoughts.jikkou.kafka.connect.models.V1KafkaConnectorStatus;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A ResourceCollector to get {@link V1KafkaConnector} resources.
 */
@SupportedResource(type = V1KafkaConnector.class)
@ExtensionConfigProperties(
        properties = {
                @ConfigPropertySpec(
                        name = KafkaConnectorCollector.Config.EXPAND_STATUS_CONFIG_NAME,
                        description = KafkaConnectorCollector.Config.EXPAND_STATUS_CONFIG_DESCRIPTION,
                        defaultValue = "false",
                        type = Boolean.class,
                        isRequired = false
                )
        }
)
public final class KafkaConnectorCollector implements Collector<V1KafkaConnector> {

    private static final Logger LOG = LoggerFactory.getLogger(KafkaConnectorCollector.class);
    private static final String DEFAULT_CONNECTOR_TASKS_MAX = "1";

    private KafkaConnectExtensionConfig configuration;

    /**
     * {@inheritDoc}
     **/
    @Override
    public void configure(@NotNull Configuration config) throws ConfigException {
        configure(new KafkaConnectExtensionConfig(config));
    }

    public void configure(@NotNull KafkaConnectExtensionConfig configuration) throws ConfigException {
        this.configuration = configuration;
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public ResourceListObject<V1KafkaConnector> listAll(@NotNull Configuration configuration,
                                                        @NotNull List<Selector> selectors) {
        boolean expandStatus = new Config(configuration).expandStatus();
        List<V1KafkaConnector> list = this.configuration
                .getClusters()
                .stream()
                .flatMap(connectCluster -> listAll(connectCluster, expandStatus).stream())
                .collect(Collectors.toList());
        return new V1KafkaConnectorList(list);
    }

    public List<V1KafkaConnector> listAll(final String cluster, final boolean expandStatus) {
        List<V1KafkaConnector> results = new LinkedList<>();
        KafkaConnectClientConfig connectClientConfig = configuration
                .getConfigForCluster(cluster)
                .orElseThrow(() -> new JikkouRuntimeException("Cannot list connectors. Unknown Kafka Connect cluster '" + cluster + "'"));
        KafkaConnectApi api = KafkaConnectApiFactory.create(connectClientConfig);
        try {
            final List<String> connectors = api.listConnectors();
            for (String connector : connectors) {
                try {
                    CompletableFuture<V1KafkaConnector> future = getConnectorAsync(cluster, connector, api, expandStatus);
                    V1KafkaConnector result = future.get();
                    results.add(result);
                } catch (Exception ex) {
                    if (ex instanceof InterruptedException) {
                        Thread.currentThread().interrupt();
                    }
                    LOG.error("Failed to get connector '{}' from connect cluster {}", connector, cluster, ex);
                }
            }
        } finally {
            api.close();
        }
        return results;
    }

    private static CompletableFuture<V1KafkaConnector> getConnectorAsync(String cluster,
                                                                         String connector,
                                                                         KafkaConnectApi api,
                                                                         boolean expandStatus) {
        return CompletableFuture
                .supplyAsync(() -> api.getConnectorConfig(connector))
                .thenCombine(
                        CompletableFuture.supplyAsync(() -> api.getConnectorStatus(connector)),
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
                                            .withName(connector)
                                            .withLabel(KafkaConnectLabels.KAFKA_CONNECT_CLUSTER, cluster)
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

    public static class Config {
        public static final String EXPAND_STATUS_CONFIG_NAME = "expand-status";
        public static final String EXPAND_STATUS_CONFIG_DESCRIPTION =
                "Retrieves additional information about the status of the connector and its tasks.";
        public ConfigProperty<Boolean> EXPEND_STATUS = ConfigProperty.ofBoolean(EXPAND_STATUS_CONFIG_NAME)
                .description(EXPAND_STATUS_CONFIG_DESCRIPTION)
                .orElse(false);

        private final Configuration configuration;

        public Config(@NotNull Configuration configuration) {
            this.configuration = Objects.requireNonNull(configuration, "configuration must not be null");
        }

        public boolean expandStatus() {
            return EXPEND_STATUS.evaluate(configuration);
        }
    }
}
