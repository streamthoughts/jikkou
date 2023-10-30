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

import static io.streamthoughts.jikkou.core.ReconciliationMode.CREATE;
import static io.streamthoughts.jikkou.core.ReconciliationMode.DELETE;
import static io.streamthoughts.jikkou.core.ReconciliationMode.FULL;
import static io.streamthoughts.jikkou.core.ReconciliationMode.UPDATE;

import io.streamthoughts.jikkou.core.ReconciliationContext;
import io.streamthoughts.jikkou.core.annotation.HandledResource;
import io.streamthoughts.jikkou.core.config.Configuration;
import io.streamthoughts.jikkou.core.exceptions.ConfigException;
import io.streamthoughts.jikkou.core.models.DefaultResourceListObject;
import io.streamthoughts.jikkou.core.models.HasMetadata;
import io.streamthoughts.jikkou.core.models.HasMetadataChange;
import io.streamthoughts.jikkou.core.models.ResourceListObject;
import io.streamthoughts.jikkou.core.reconcilier.ChangeExecutor;
import io.streamthoughts.jikkou.core.reconcilier.ChangeHandler;
import io.streamthoughts.jikkou.core.reconcilier.ChangeResult;
import io.streamthoughts.jikkou.core.reconcilier.Controller;
import io.streamthoughts.jikkou.core.reconcilier.DefaultChangeExecutor;
import io.streamthoughts.jikkou.core.reconcilier.annotations.ControllerConfiguration;
import io.streamthoughts.jikkou.core.selectors.AggregateSelector;
import io.streamthoughts.jikkou.kafka.connect.KafkaConnectExtensionConfig;
import io.streamthoughts.jikkou.kafka.connect.KafkaConnectLabels;
import io.streamthoughts.jikkou.kafka.connect.api.KafkaConnectApi;
import io.streamthoughts.jikkou.kafka.connect.api.KafkaConnectApiFactory;
import io.streamthoughts.jikkou.kafka.connect.change.KafkaConnectorChange;
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

@HandledResource(type = V1KafkaConnector.class)
@ControllerConfiguration(
        supportedModes = {CREATE, DELETE, UPDATE, FULL}
)
public final class KafkaConnectorController implements Controller<V1KafkaConnector, KafkaConnectorChange> {

    private KafkaConnectExtensionConfig configuration;

    private KafkaConnectorCollector collector;

    /**
     * {@inheritDoc}
     **/
    @Override
    public void configure(@NotNull Configuration config) throws ConfigException {
        this.configuration = new KafkaConnectExtensionConfig(config);
        this.collector = new KafkaConnectorCollector();
        this.collector.configure(config);
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public List<ChangeResult<KafkaConnectorChange>> execute(@NotNull final ChangeExecutor<KafkaConnectorChange> executor,
                                                            @NotNull final ReconciliationContext context) {

        List<HasMetadataChange<KafkaConnectorChange>> changes = executor.changes();
        Map<String, List<HasMetadataChange<KafkaConnectorChange>>> changesByCluster = groupByKafkaConnectCluster(
                changes,
                change -> true
        );

        List<ChangeResult<KafkaConnectorChange>> results = new LinkedList<>();
        for (Map.Entry<String, List<HasMetadataChange<KafkaConnectorChange>>> entry : changesByCluster.entrySet()) {
            final String cluster = entry.getKey();
            try (KafkaConnectApi api = KafkaConnectApiFactory.create(configuration.getConfigForCluster(cluster).get())) {
                List<ChangeHandler<KafkaConnectorChange>> handlers = List.of(
                        new KafkaConnectorChangeHandler(api, cluster),
                        new ChangeHandler.None<>(item -> new KafkaConnectorChangeDescription(cluster, item.getChange()))
                );
                DefaultChangeExecutor<KafkaConnectorChange> dedicatedExecutor = new DefaultChangeExecutor<>(
                        context,
                        entry.getValue()
                );
                results.addAll(dedicatedExecutor.execute(handlers));
            }
        }

        return results;
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public ResourceListObject<HasMetadataChange<KafkaConnectorChange>> plan(
            @NotNull Collection<V1KafkaConnector> resources,
            @NotNull ReconciliationContext context) {

        Map<String, List<V1KafkaConnector>> resourcesByCluster = groupByKafkaConnectCluster(
                resources,
                new AggregateSelector(context.selectors())::apply);

        KafkaConnectorChangeComputer computer = new KafkaConnectorChangeComputer();

        List<HasMetadataChange<KafkaConnectorChange>> allChanges = new LinkedList<>();
        for (Map.Entry<String, List<V1KafkaConnector>> entry : resourcesByCluster.entrySet()) {
            String clusterName = entry.getKey();
            List<V1KafkaConnector> expectedStates = entry.getValue();

            List<V1KafkaConnector> actualStates = collector.listAll(clusterName, false)
                    .stream()
                    .filter(new AggregateSelector(context.selectors())::apply)
                    .toList();
            allChanges.addAll(computer.computeChanges(actualStates, expectedStates));
        }
        return DefaultResourceListObject
                .<HasMetadataChange<KafkaConnectorChange>>builder()
                .withItems(allChanges)
                .build();
    }

    @NotNull
    private <T extends HasMetadata> Map<String, List<T>> groupByKafkaConnectCluster(@NotNull Collection<T> changes,
                                                                                    @NotNull Predicate<T> predicate) {
        return changes
                .stream()
                .filter(predicate)
                .collect(Collectors.groupingBy(
                        it -> it.getMetadata().getLabelByKey(KafkaConnectLabels.KAFKA_CONNECT_CLUSTER).toString(),
                        Collectors.toList())
                );
    }
}
