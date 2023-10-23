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
package io.streamthoughts.jikkou.kafka.control;

import static io.streamthoughts.jikkou.core.ReconciliationMode.APPLY_ALL;
import static io.streamthoughts.jikkou.core.ReconciliationMode.CREATE;
import static io.streamthoughts.jikkou.core.ReconciliationMode.UPDATE;

import io.streamthoughts.jikkou.core.ReconciliationContext;
import io.streamthoughts.jikkou.core.ReconciliationMode;
import io.streamthoughts.jikkou.core.annotation.AcceptsReconciliationModes;
import io.streamthoughts.jikkou.core.annotation.AcceptsResource;
import io.streamthoughts.jikkou.core.change.ChangeExecutor;
import io.streamthoughts.jikkou.core.change.ChangeHandler;
import io.streamthoughts.jikkou.core.change.ChangeResult;
import io.streamthoughts.jikkou.core.config.Configuration;
import io.streamthoughts.jikkou.core.exceptions.ConfigException;
import io.streamthoughts.jikkou.core.models.GenericResourceListObject;
import io.streamthoughts.jikkou.core.models.HasMetadataChange;
import io.streamthoughts.jikkou.core.models.ResourceListObject;
import io.streamthoughts.jikkou.core.resource.BaseResourceController;
import io.streamthoughts.jikkou.core.selectors.AggregateSelector;
import io.streamthoughts.jikkou.kafka.change.KafkaTableRecordChange;
import io.streamthoughts.jikkou.kafka.change.KafkaTableRecordChangeComputer;
import io.streamthoughts.jikkou.kafka.change.handlers.record.KafkaTableRecordChangeDescription;
import io.streamthoughts.jikkou.kafka.change.handlers.record.KafkaTableRecordChangeHandler;
import io.streamthoughts.jikkou.kafka.internals.admin.AdminClientFactory;
import io.streamthoughts.jikkou.kafka.internals.consumer.ConsumerFactory;
import io.streamthoughts.jikkou.kafka.internals.producer.DefaultProducerFactory;
import io.streamthoughts.jikkou.kafka.internals.producer.ProducerFactory;
import io.streamthoughts.jikkou.kafka.model.DataValue;
import io.streamthoughts.jikkou.kafka.models.V1KafkaTableRecord;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@AcceptsResource(type = V1KafkaTableRecord.class)
@AcceptsReconciliationModes({CREATE, UPDATE, APPLY_ALL})
public final class AdminClientKafkaTableController
        implements BaseResourceController<V1KafkaTableRecord, KafkaTableRecordChange> {

    private static final Logger LOG = LoggerFactory.getLogger(AdminClientKafkaTableController.class);

    private ProducerFactory<byte[], byte[]> producerFactory;

    private ConsumerFactory<byte[], byte[]> consumerFactory;

    private AdminClientFactory adminClientFactory;

    private AdminClientKafkaTableCollector collector;

    /**
     * Creates a new {@link AdminClientKafkaTableController} instance.
     */
    public AdminClientKafkaTableController() {
        super();
    }

    /**
     * Creates a new {@link AdminClientKafkaTableController} instance with the specified
     * ProducerFactory.
     *
     * @param producerFactory the Producer factory.
     */
    public AdminClientKafkaTableController(ProducerFactory<byte[], byte[]> producerFactory,
                                           ConsumerFactory<byte[], byte[]> consumerFactory,
                                           AdminClientFactory adminClientFactory) {
        this.producerFactory = producerFactory;
        this.consumerFactory = consumerFactory;
        this.adminClientFactory = adminClientFactory;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void configure(@NotNull Configuration config) throws ConfigException {
        LOG.info("Configuring");
        if (producerFactory == null) {
            producerFactory = new DefaultProducerFactory<>(
                    () -> KafkaClientConfiguration.PRODUCER_CLIENT_CONFIG.evaluate(config),
                    new ByteArraySerializer(),
                    new ByteArraySerializer()
            );
        }

        collector = new AdminClientKafkaTableCollector(consumerFactory, adminClientFactory);
        collector.configure(config);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ChangeResult<KafkaTableRecordChange>> execute(@NotNull List<HasMetadataChange<KafkaTableRecordChange>> changes,
                                                              @NotNull ReconciliationMode mode, boolean dryRun) {
        try (var producer = producerFactory.createProducer()) {
            List<ChangeHandler<KafkaTableRecordChange>> handlers = List.of(
                    new KafkaTableRecordChangeHandler(producer),
                    new ChangeHandler.None<>(KafkaTableRecordChangeDescription::new)
            );
            return new ChangeExecutor<>(handlers).execute(changes, dryRun);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResourceListObject<? extends HasMetadataChange<KafkaTableRecordChange>> computeReconciliationChanges(
            @NotNull Collection<V1KafkaTableRecord> resources,
            @NotNull ReconciliationMode mode,
            @NotNull ReconciliationContext context) {

        Map<String, List<V1KafkaTableRecord>> resourcesByTopic = resources.stream()
                .filter(AdminClientKafkaTableController::recordWithNonEmptyKey)
                .filter(new AggregateSelector(context.selectors())::apply)
                .collect(Collectors.groupingBy(it -> it.getMetadata().getName(), Collectors.toList()));

        List<HasMetadataChange<KafkaTableRecordChange>> changes = new ArrayList<>();

        for (var entry : resourcesByTopic.entrySet()) {
            String topicName = entry.getKey();
            // assuming that all the records in a theme have the same key/value format.
            V1KafkaTableRecord first = entry.getValue().get(0);
            Configuration configuration = Configuration.from(Map.of(
                    AdminClientKafkaTableCollector.Config.TOPIC_NAME_CONFIG.key(), topicName,
                    AdminClientKafkaTableCollector.Config.KEY_TYPE_CONFIG.key(), first.getSpec().getKey().type().name(),
                    AdminClientKafkaTableCollector.Config.VALUE_TYPE_CONFIG.key(), first.getSpec().getValue().type().name(),
                    AdminClientKafkaTableCollector.Config.SKIP_MESSAGE_ON_ERROR_CONFIG.key(), true
            ));
            List<V1KafkaTableRecord> actual = collector.listAll(configuration, context.selectors());

            KafkaTableRecordChangeComputer changeComputer = new KafkaTableRecordChangeComputer();
            changes.addAll(changeComputer.computeChanges(actual, entry.getValue()));
        }
        return new GenericResourceListObject<>(changes);
    }

    private static boolean recordWithNonEmptyKey(@NotNull V1KafkaTableRecord item) {
        DataValue keyValue = item.getSpec().getKey();
        return keyValue != null && !keyValue.data().isNull();
    }
}
