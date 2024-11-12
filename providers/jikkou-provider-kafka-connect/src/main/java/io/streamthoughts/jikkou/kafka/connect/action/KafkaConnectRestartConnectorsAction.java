/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.kafka.connect.action;

import io.streamthoughts.jikkou.common.utils.AsyncUtils;
import io.streamthoughts.jikkou.common.utils.Either;
import io.streamthoughts.jikkou.core.action.Action;
import io.streamthoughts.jikkou.core.action.ExecutionError;
import io.streamthoughts.jikkou.core.action.ExecutionResult;
import io.streamthoughts.jikkou.core.action.ExecutionResultSet;
import io.streamthoughts.jikkou.core.action.ExecutionStatus;
import io.streamthoughts.jikkou.core.annotation.Description;
import io.streamthoughts.jikkou.core.annotation.Named;
import io.streamthoughts.jikkou.core.annotation.SupportedResource;
import io.streamthoughts.jikkou.core.annotation.Title;
import io.streamthoughts.jikkou.core.config.ConfigProperty;
import io.streamthoughts.jikkou.core.config.Configuration;
import io.streamthoughts.jikkou.core.extension.ContextualExtension;
import io.streamthoughts.jikkou.core.extension.ExtensionContext;
import io.streamthoughts.jikkou.core.models.ObjectMeta;
import io.streamthoughts.jikkou.kafka.connect.KafkaConnectClusterConfigs;
import io.streamthoughts.jikkou.kafka.connect.KafkaConnectExtensionProvider;
import io.streamthoughts.jikkou.kafka.connect.KafkaConnectLabels;
import io.streamthoughts.jikkou.kafka.connect.api.KafkaConnectApi;
import io.streamthoughts.jikkou.kafka.connect.api.KafkaConnectApiFactory;
import io.streamthoughts.jikkou.kafka.connect.api.KafkaConnectClientConfig;
import io.streamthoughts.jikkou.kafka.connect.api.data.ErrorResponse;
import io.streamthoughts.jikkou.kafka.connect.exception.KafkaConnectClusterNotFoundException;
import io.streamthoughts.jikkou.kafka.connect.models.V1KafkaConnector;
import io.streamthoughts.jikkou.kafka.connect.service.KafkaConnectClusterService;
import jakarta.ws.rs.core.Response;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Action for restarting connector instances and task instances.
 *
 * <p>
 * See Kafka Connect REST API: <a href="https://cwiki.apache.org/confluence/display/KAFKA/KIP-745%3A+Connect+API+to+restart+connector+and+tasks">KIP-745</a>
 * </p>
 */
@Named(KafkaConnectRestartConnectorsAction.NAME)
@Title("Restart Kafka Connect connector instances and task instances")
@Description("The KafkaConnectRestartConnectors action allows restarting all or just the failed Connector and Task instances for one or multiple named connectors.")
@SupportedResource(type = V1KafkaConnector.class)
public class KafkaConnectRestartConnectorsAction extends ContextualExtension implements Action<V1KafkaConnector> {

    private static final Logger LOG = LoggerFactory.getLogger(KafkaConnectRestartConnectorsAction.class);

    public static final String NAME = "KafkaConnectRestartConnectors";

    interface Config {
        ConfigProperty<List<String>> CONNECTOR_NAME = ConfigProperty
            .ofList("connector-name")
            .description("The connector's name.")
            .required(false);

        ConfigProperty<List<String>> CONNECT_CLUSTER = ConfigProperty
            .ofList("connect-cluster")
            .description("The name of the connect cluster.")
            .required(false);

        ConfigProperty<Boolean> INCLUDE_TASKS = ConfigProperty
            .ofBoolean("include-tasks")
            .description("Specifies whether to restart the connector instance and task instances (includeTasks=true) or just the connector instance (includeTasks=false)")
            .required(false);

        ConfigProperty<Boolean> ONLY_FAILED = ConfigProperty
            .ofBoolean("only-failed")
            .description("Specifies whether to restart just the instances with a FAILED status (onlyFailed=true) or all instances (onlyFailed=false)")
            .required(false);
    }


    private KafkaConnectClusterConfigs configuration;

    /**
     * {@inheritDoc}
     **/
    @Override
    public void init(@NotNull ExtensionContext context) {
        super.init(context);
        this.configuration = context.<KafkaConnectExtensionProvider>provider().clusterConfigs();
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public List<ConfigProperty<?>> configProperties() {
        return List.of(
            Config.CONNECTOR_NAME,
            Config.CONNECT_CLUSTER,
            Config.INCLUDE_TASKS,
            Config.ONLY_FAILED
        );
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public @NotNull ExecutionResultSet<V1KafkaConnector> execute(@NotNull Configuration configuration) {

        final boolean includeTasks = Config.INCLUDE_TASKS.getOptional(configuration).orElse(false);
        final boolean onlyFailed = Config.ONLY_FAILED.getOptional(configuration).orElse(false);

        // Get the list of Kafka Connect clusters
        Set<String> clusters = getConnectClusters(configuration);
        try (ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor()) {
            List<CompletableFuture<List<ExecutionResult<V1KafkaConnector>>>> futures = clusters.stream()
                .map(clusterName ->
                    CompletableFuture.supplyAsync(() ->
                            restartConnectors(
                                configuration,
                                clusterName,
                                includeTasks,
                                onlyFailed),
                        executorService)
                )
                .toList();
            executorService.shutdown();
            CompletableFuture<List<ExecutionResult<V1KafkaConnector>>> allFuture = AsyncUtils.waitForAll(futures)
                .thenApply(clusterResults ->
                    clusterResults.stream()
                        .flatMap(Collection::stream)
                        .toList()
                );
            return switch (AsyncUtils.get(allFuture)) {
                case Either.Left<List<ExecutionResult<V1KafkaConnector>>, Throwable> success ->
                    ExecutionResultSet.<V1KafkaConnector>newBuilder()
                        .results(success.left().get())
                        .build();

                case Either.Right<List<ExecutionResult<V1KafkaConnector>>, Throwable> error ->
                    ExecutionResultSet.<V1KafkaConnector>newBuilder()
                        .result(ExecutionResult.<V1KafkaConnector>newBuilder()
                            .status(ExecutionStatus.FAILED)
                            .errors(List.of(new ExecutionError(error.right().get().getLocalizedMessage())))
                            .build()
                        )
                        .build();
            };
        }
    }

    private List<ExecutionResult<V1KafkaConnector>> restartConnectors(@NotNull Configuration configuration,
                                                                      @NotNull String clusterName,
                                                                      boolean includeTasks,
                                                                      boolean onlyFailed) {
        KafkaConnectClientConfig clusterClientConfig = getKafkaConnectClientConfig(clusterName);
        KafkaConnectApi api = KafkaConnectApiFactory.create(clusterClientConfig);
        try {
            KafkaConnectClusterService service = new KafkaConnectClusterService(clusterName, api);
            // Get the list of connectors from the clusterName of from the configuration.
            return getConnectorsFromClusterOrConfig(configuration, api)
                .stream()
                .map(connectorName -> {
                    // Restart the connector.
                    return restartConnector(clusterName, connectorName, includeTasks, onlyFailed, api, service);
                })
                .collect(Collectors.toList());
        } finally {
            api.close();
        }
    }

    private static ExecutionResult<V1KafkaConnector> restartConnector(String clusterName,
                                                                      String connectorName,
                                                                      boolean includeTasks,
                                                                      boolean onlyFailed,
                                                                      KafkaConnectApi api,
                                                                      KafkaConnectClusterService service) {
        ExecutionResult<V1KafkaConnector> result;
        try {
            Response response = api.restartConnector(connectorName, includeTasks, onlyFailed);
            final int statusCode = response.getStatus();

            if (statusCode == 202 || statusCode == 204) {
                Optional<V1KafkaConnector> resource = AsyncUtils.getValue(service.getConnectorAsync(connectorName, true));
                result = ExecutionResult
                    .<V1KafkaConnector>newBuilder()
                    .status(ExecutionStatus.SUCCEEDED)
                    .data(resource.orElse(V1KafkaConnector
                        .builder()
                        .withMetadata(ObjectMeta
                            .builder()
                            .withName(connectorName)
                            .withLabel(KafkaConnectLabels.KAFKA_CONNECT_CLUSTER, clusterName)
                            .build()
                        )
                        .build())
                    )
                    .build();
            } else {
                ErrorResponse error = response.readEntity(ErrorResponse.class);
                result = ExecutionResult
                    .<V1KafkaConnector>newBuilder()
                    .status(ExecutionStatus.FAILED)
                    .errors(List.of(new ExecutionError(error.message(), error.errorCode())))
                    .build();
            }
        } catch (Exception ex) {
            if (ex instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            LOG.error("Failed to restart connectorName '{}' on connect clusterName {} (includeTasks={}, onlyFailed={}).",
                connectorName,
                clusterName,
                includeTasks,
                onlyFailed,
                ex);
            result = ExecutionResult
                .<V1KafkaConnector>newBuilder()
                .status(ExecutionStatus.FAILED)
                .errors(List.of(new ExecutionError(ex.getLocalizedMessage())))
                .build();
        }
        return result;
    }

    private KafkaConnectClientConfig getKafkaConnectClientConfig(@NotNull String clusterName) {
        return configuration
            .getConfigForCluster(clusterName)
            .orElseThrow(() -> new KafkaConnectClusterNotFoundException(
                "No connect cluster configured for name '" + clusterName + "'"));
    }

    @NotNull
    private List<String> getConnectorsFromClusterOrConfig(@NotNull Configuration configuration,
                                                          @NotNull KafkaConnectApi api) {
        return Config.CONNECTOR_NAME.getOptional(configuration).orElseGet(api::listConnectors);
    }

    @NotNull
    private Set<String> getConnectClusters(@NotNull Configuration configuration) {
        return Config.CONNECT_CLUSTER
            .getOptional(configuration)
            .map(list -> (Set<String>) new HashSet<>(list))
            .orElseGet(() -> this.configuration.getClusters());
    }
}
