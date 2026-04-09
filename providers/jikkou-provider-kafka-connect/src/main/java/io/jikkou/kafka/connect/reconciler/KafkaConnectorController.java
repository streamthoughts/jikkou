/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.jikkou.kafka.connect.reconciler;

import static io.jikkou.core.ReconciliationMode.CREATE;
import static io.jikkou.core.ReconciliationMode.DELETE;
import static io.jikkou.core.ReconciliationMode.FULL;
import static io.jikkou.core.ReconciliationMode.UPDATE;

import io.jikkou.core.ReconciliationContext;
import io.jikkou.core.annotation.Description;
import io.jikkou.core.annotation.SupportedResource;
import io.jikkou.core.annotation.Title;
import io.jikkou.core.extension.ContextualExtension;
import io.jikkou.core.extension.ExtensionContext;
import io.jikkou.core.models.HasMetadata;
import io.jikkou.core.models.change.ResourceChange;
import io.jikkou.core.reconciler.ChangeExecutor;
import io.jikkou.core.reconciler.ChangeHandler;
import io.jikkou.core.reconciler.ChangeResult;
import io.jikkou.core.reconciler.Controller;
import io.jikkou.core.reconciler.DefaultChangeExecutor;
import io.jikkou.core.reconciler.annotations.ControllerConfiguration;
import io.jikkou.core.selector.Selector;
import io.jikkou.kafka.connect.ApiVersions;
import io.jikkou.kafka.connect.KafkaConnectClusterConfigs;
import io.jikkou.kafka.connect.KafkaConnectExtensionProvider;
import io.jikkou.kafka.connect.KafkaConnectLabels;
import io.jikkou.kafka.connect.api.KafkaConnectApi;
import io.jikkou.kafka.connect.api.KafkaConnectApiFactory;
import io.jikkou.kafka.connect.api.KafkaConnectClientConfig;
import io.jikkou.kafka.connect.change.KafkaConnectorChangeComputer;
import io.jikkou.kafka.connect.change.KafkaConnectorChangeDescription;
import io.jikkou.kafka.connect.change.KafkaConnectorChangeHandler;
import io.jikkou.kafka.connect.models.V1KafkaConnector;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;

@Title("Reconcile Kafka connectors")
@Description("Reconciles Kafka Connect connector resources to ensure they match the desired state.")
@SupportedResource(type = V1KafkaConnector.class)
@SupportedResource(apiVersion = ApiVersions.KAFKA_V1BETA, kind = "KafkaConnectorChange")
@ControllerConfiguration(
    supportedModes = {CREATE, DELETE, UPDATE, FULL}
)
public final class KafkaConnectorController extends ContextualExtension implements Controller<V1KafkaConnector> {

    private KafkaConnectClusterConfigs configuration;

    private KafkaConnectorCollector collector;

    /**
     * {@inheritDoc}
     **/
    @Override
    public void init(@NotNull ExtensionContext context) {
        super.init(context);
        this.configuration = context.<KafkaConnectExtensionProvider>provider().clusterConfigs();
        this.collector = new KafkaConnectorCollector();
        this.collector.init(context);
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public List<ChangeResult> execute(@NotNull final ChangeExecutor executor,
                                      @NotNull final ReconciliationContext context) {

        List<ResourceChange> changes = executor.changes();
        Map<String, List<ResourceChange>> changesByCluster = groupByKafkaConnectCluster(
            changes,
            change -> true
        );

        List<ChangeResult> results = new LinkedList<>();
        for (Map.Entry<String, List<ResourceChange>> entry : changesByCluster.entrySet()) {
            final String cluster = entry.getKey();
            KafkaConnectClientConfig connectClientConfig = configuration.resolveClientConfigForCluster(cluster, entry.getValue());
            try (KafkaConnectApi api = KafkaConnectApiFactory.create(connectClientConfig)) {
                List<ChangeHandler> handlers = List.of(
                    new KafkaConnectorChangeHandler(api, cluster),
                    new ChangeHandler.None(change -> new KafkaConnectorChangeDescription(cluster, change))
                );
                DefaultChangeExecutor dedicatedExecutor = new DefaultChangeExecutor(context, entry.getValue());
                results.addAll(dedicatedExecutor.applyChanges(handlers));
            }
        }

        return results;
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public List<ResourceChange> plan(
        @NotNull Collection<V1KafkaConnector> resources,
        @NotNull ReconciliationContext context) {

        Selector selector = context.selector();

        Map<String, List<V1KafkaConnector>> resourcesByCluster = groupByKafkaConnectCluster(
            resources,
            t -> true);

        KafkaConnectorChangeComputer computer = new KafkaConnectorChangeComputer();

        List<ResourceChange> allChanges = new LinkedList<>();
        for (Map.Entry<String, List<V1KafkaConnector>> entry : resourcesByCluster.entrySet()) {
            List<V1KafkaConnector> allExpected = entry.getValue();
            KafkaConnectClientConfig connectClientConfig = configuration.resolveClientConfigForCluster(entry.getKey(), allExpected);
            List<V1KafkaConnector> allActual = collector.listAll(entry.getKey(), connectClientConfig, false);

            // Enrich actual connectors with labels from expected so label selectors work on both sides
            Controller.enrichLabelsFromExpected(allActual, allExpected);

            List<V1KafkaConnector> expectedConnectors = allExpected.stream().filter(selector::apply).toList();
            List<V1KafkaConnector> actualConnectors = allActual.stream().filter(selector::apply).toList();

            allChanges.addAll(computer.computeChanges(actualConnectors, expectedConnectors));
        }
        return allChanges;
    }

    @NotNull
    private <T extends HasMetadata> Map<String, List<T>> groupByKafkaConnectCluster(@NotNull Collection<T> changes,
                                                                                    @NotNull Predicate<T> predicate) {
        return changes
            .stream()
            .filter(predicate)
            .collect(Collectors.groupingBy(
                it -> it.getMetadata()
                    .getLabelByKey(KafkaConnectLabels.KAFKA_CONNECT_CLUSTER)
                    .getValue()
                    .toString(),
                Collectors.toList())
            );
    }
}
