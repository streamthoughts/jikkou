/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.kafka.connect.reconciler;

import io.streamthoughts.jikkou.core.annotation.SupportedResource;
import io.streamthoughts.jikkou.core.config.Configuration;
import io.streamthoughts.jikkou.core.exceptions.ConfigException;
import io.streamthoughts.jikkou.core.extension.ContextualExtension;
import io.streamthoughts.jikkou.core.extension.ExtensionContext;
import io.streamthoughts.jikkou.core.extension.annotations.ExtensionOptionSpec;
import io.streamthoughts.jikkou.core.extension.annotations.ExtensionSpec;
import io.streamthoughts.jikkou.core.models.ResourceList;
import io.streamthoughts.jikkou.core.reconciler.Collector;
import io.streamthoughts.jikkou.core.selector.Selector;
import io.streamthoughts.jikkou.kafka.connect.KafkaConnectClusterConfigs;
import io.streamthoughts.jikkou.kafka.connect.KafkaConnectExtensionProvider;
import io.streamthoughts.jikkou.kafka.connect.api.KafkaConnectApi;
import io.streamthoughts.jikkou.kafka.connect.api.KafkaConnectApiFactory;
import io.streamthoughts.jikkou.kafka.connect.api.KafkaConnectClientConfig;
import io.streamthoughts.jikkou.kafka.connect.collections.V1KafkaConnectorList;
import io.streamthoughts.jikkou.kafka.connect.exception.KafkaConnectClusterNotFoundException;
import io.streamthoughts.jikkou.kafka.connect.models.V1KafkaConnector;
import io.streamthoughts.jikkou.kafka.connect.service.KafkaConnectClusterService;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A ResourceCollector to get {@link V1KafkaConnector} resources.
 */
@SupportedResource(type = V1KafkaConnector.class)
@ExtensionSpec(
    options = {
        @ExtensionOptionSpec(
            name = KafkaConnectorCollector.EXPAND_STATUS_CONFIG,
            description = "Retrieves additional information about the status of the connector and its tasks.",
            type = Boolean.class,
            defaultValue = "false"
        ),
        @ExtensionOptionSpec(
            name = KafkaConnectorCollector.CONNECT_CLUSTER_CONFIG,
            description = "List of Kafka Connect cluster from which to list connectors.",
            type = List.class
        )
    }
)
public final class KafkaConnectorCollector extends ContextualExtension implements Collector<V1KafkaConnector> {

    private static final Logger LOG = LoggerFactory.getLogger(KafkaConnectorCollector.class);

    public static final String EXPAND_STATUS_CONFIG = "expand-status";
    public static final String CONNECT_CLUSTER_CONFIG = "connect-cluster";

    private KafkaConnectClusterConfigs configuration;

    /**
     * {@inheritDoc}
     **/
    @Override
    public void init(@NotNull ExtensionContext context) throws ConfigException {
        super.init(context);
        this.configuration = context.<KafkaConnectExtensionProvider>provider().clusterConfigs();
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public ResourceList<V1KafkaConnector> listAll(final @NotNull Configuration configuration,
                                                  final @NotNull Selector selector) {

        Boolean expandStatus = extensionContext()
            .<Boolean>configProperty(EXPAND_STATUS_CONFIG).get(configuration);

        Set<String> clusters = extensionContext()
            .<List<String>>configProperty(CONNECT_CLUSTER_CONFIG)
            .getOptional(configuration)
            .map(list -> (Set<String>) new HashSet<>(list))
            .orElseGet(() -> this.configuration.getClusters());

        List<V1KafkaConnector> list = clusters
            .stream()
            .flatMap(connectCluster -> listAll(connectCluster, expandStatus).stream())
            .collect(Collectors.toList());
        return new V1KafkaConnectorList.Builder().withItems(list).build();
    }

    public List<V1KafkaConnector> listAll(final String connectClusterName,
                                          final boolean expandStatus) {
        KafkaConnectClientConfig connectClientConfig = configuration
            .getConfigForCluster(connectClusterName)
            .orElseThrow(() -> new KafkaConnectClusterNotFoundException(String.format(
                "Failed to list connectors for cluster %s. No configuration was found.", connectClusterName
            )));

        return listAll(connectClusterName, connectClientConfig, expandStatus);
    }

    public List<V1KafkaConnector> listAll(final String connectClusterName,
                                          final KafkaConnectClientConfig connectClientConfig,
                                          final boolean expandStatus) {
        List<V1KafkaConnector> results = new LinkedList<>();
        KafkaConnectApi api = KafkaConnectApiFactory.create(connectClientConfig);
        try {
            final List<String> connectors = api.listConnectors();
            for (String connector : connectors) {
                try {
                    KafkaConnectClusterService service = new KafkaConnectClusterService(connectClusterName, api);
                    CompletableFuture<V1KafkaConnector> future = service.getConnectorAsync(connector, expandStatus);
                    V1KafkaConnector result = future.get();
                    results.add(result);
                } catch (Exception ex) {
                    if (ex instanceof InterruptedException) {
                        Thread.currentThread().interrupt();
                    }
                    LOG.error("Failed to get connector '{}' from connect cluster: {}",
                        connector,
                        connectClientConfig.url(),
                        ex
                    );
                }
            }
        } finally {
            api.close();
        }
        return results;
    }
}
