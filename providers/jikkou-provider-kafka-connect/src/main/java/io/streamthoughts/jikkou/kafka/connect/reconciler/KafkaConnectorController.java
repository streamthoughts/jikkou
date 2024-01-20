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
package io.streamthoughts.jikkou.kafka.connect.reconciler;

import static io.streamthoughts.jikkou.core.ReconciliationMode.CREATE;
import static io.streamthoughts.jikkou.core.ReconciliationMode.DELETE;
import static io.streamthoughts.jikkou.core.ReconciliationMode.FULL;
import static io.streamthoughts.jikkou.core.ReconciliationMode.UPDATE;

import io.streamthoughts.jikkou.core.ReconciliationContext;
import io.streamthoughts.jikkou.core.annotation.SupportedResource;
import io.streamthoughts.jikkou.core.extension.ContextualExtension;
import io.streamthoughts.jikkou.core.extension.ExtensionContext;
import io.streamthoughts.jikkou.core.models.HasMetadata;
import io.streamthoughts.jikkou.core.models.change.ResourceChange;
import io.streamthoughts.jikkou.core.reconciler.ChangeExecutor;
import io.streamthoughts.jikkou.core.reconciler.ChangeHandler;
import io.streamthoughts.jikkou.core.reconciler.ChangeResult;
import io.streamthoughts.jikkou.core.reconciler.Controller;
import io.streamthoughts.jikkou.core.reconciler.DefaultChangeExecutor;
import io.streamthoughts.jikkou.core.reconciler.annotations.ControllerConfiguration;
import io.streamthoughts.jikkou.kafka.connect.ApiVersions;
import io.streamthoughts.jikkou.kafka.connect.KafkaConnectExtensionConfig;
import io.streamthoughts.jikkou.kafka.connect.KafkaConnectLabels;
import io.streamthoughts.jikkou.kafka.connect.api.KafkaConnectApi;
import io.streamthoughts.jikkou.kafka.connect.api.KafkaConnectApiFactory;
import io.streamthoughts.jikkou.kafka.connect.change.KafkaConnectorChangeComputer;
import io.streamthoughts.jikkou.kafka.connect.change.KafkaConnectorChangeDescription;
import io.streamthoughts.jikkou.kafka.connect.change.KafkaConnectorChangeHandler;
import io.streamthoughts.jikkou.kafka.connect.models.V1KafkaConnector;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;

@SupportedResource(type = V1KafkaConnector.class)
@SupportedResource(apiVersion = ApiVersions.KAFKA_V1BETA, kind = "KafkaConnectorChange")
@ControllerConfiguration(
        supportedModes = {CREATE, DELETE, UPDATE, FULL}
)
public final class KafkaConnectorController extends ContextualExtension implements Controller<V1KafkaConnector, ResourceChange> {

    private KafkaConnectExtensionConfig configuration;

    private KafkaConnectorCollector collector;

    /**
     * {@inheritDoc}
     **/
    @Override
    public void init(@NotNull ExtensionContext context) {
        super.init(context);
        this.configuration = new KafkaConnectExtensionConfig(context.appConfiguration());
        this.collector = new KafkaConnectorCollector();
        this.collector.init(context);
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public List<ChangeResult> execute(@NotNull final ChangeExecutor<ResourceChange> executor,
                                      @NotNull final ReconciliationContext context) {

        List<ResourceChange> changes = executor.changes();
        Map<String, List<ResourceChange>> changesByCluster = groupByKafkaConnectCluster(
                changes,
                change -> true
        );

        List<ChangeResult> results = new LinkedList<>();
        for (Map.Entry<String, List<ResourceChange>> entry : changesByCluster.entrySet()) {
            final String cluster = entry.getKey();
            try (KafkaConnectApi api = KafkaConnectApiFactory.create(configuration.getConfigForCluster(cluster).get())) {
                List<ChangeHandler<ResourceChange>> handlers = List.of(
                        new KafkaConnectorChangeHandler(api, cluster),
                        new ChangeHandler.None<>(change -> new KafkaConnectorChangeDescription(cluster, change))
                );
                DefaultChangeExecutor<ResourceChange> dedicatedExecutor = new DefaultChangeExecutor<>(
                        context,
                        entry.getValue()
                );
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

        Map<String, List<V1KafkaConnector>> resourcesByCluster = groupByKafkaConnectCluster(
                resources,
                context.selector()::apply);

        KafkaConnectorChangeComputer computer = new KafkaConnectorChangeComputer();

        List<ResourceChange> allChanges = new LinkedList<>();
        for (Map.Entry<String, List<V1KafkaConnector>> entry : resourcesByCluster.entrySet()) {
            String clusterName = entry.getKey();
            List<V1KafkaConnector> expectedStates = entry.getValue();

            List<V1KafkaConnector> actualStates = collector.listAll(clusterName, false)
                    .stream()
                    .filter(context.selector()::apply)
                    .toList();
            allChanges.addAll(computer.computeChanges(actualStates, expectedStates));
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
