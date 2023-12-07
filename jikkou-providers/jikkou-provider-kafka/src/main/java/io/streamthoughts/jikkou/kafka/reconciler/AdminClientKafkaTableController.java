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
package io.streamthoughts.jikkou.kafka.reconciler;

import static io.streamthoughts.jikkou.core.ReconciliationMode.CREATE;
import static io.streamthoughts.jikkou.core.ReconciliationMode.DELETE;
import static io.streamthoughts.jikkou.core.ReconciliationMode.FULL;
import static io.streamthoughts.jikkou.core.ReconciliationMode.UPDATE;

import io.streamthoughts.jikkou.core.ReconciliationContext;
import io.streamthoughts.jikkou.core.annotation.SupportedResource;
import io.streamthoughts.jikkou.core.config.Configuration;
import io.streamthoughts.jikkou.core.extension.ExtensionContext;
import io.streamthoughts.jikkou.core.models.ResourceListObject;
import io.streamthoughts.jikkou.core.models.change.ResourceChange;
import io.streamthoughts.jikkou.core.reconciler.ChangeExecutor;
import io.streamthoughts.jikkou.core.reconciler.ChangeHandler;
import io.streamthoughts.jikkou.core.reconciler.ChangeResult;
import io.streamthoughts.jikkou.core.reconciler.Controller;
import io.streamthoughts.jikkou.core.reconciler.annotations.ControllerConfiguration;
import io.streamthoughts.jikkou.kafka.change.record.KafkaTableRecordChangeComputer;
import io.streamthoughts.jikkou.kafka.change.record.KafkaTableRecordChangeDescription;
import io.streamthoughts.jikkou.kafka.change.record.KafkaTableRecordChangeHandler;
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

@SupportedResource(type = V1KafkaTableRecord.class)
@ControllerConfiguration(
        supportedModes = {CREATE, DELETE, UPDATE, FULL}
)
public final class AdminClientKafkaTableController implements Controller<V1KafkaTableRecord, ResourceChange> {

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
    public void init(@NotNull ExtensionContext context) {
        if (producerFactory == null) {
            producerFactory = new DefaultProducerFactory<>(
                    () -> KafkaClientConfiguration.PRODUCER_CLIENT_CONFIG.get(context.appConfiguration()),
                    new ByteArraySerializer(),
                    new ByteArraySerializer()
            );
        }

        collector = new AdminClientKafkaTableCollector(consumerFactory, adminClientFactory);
        collector.init(context.contextForExtension(AdminClientKafkaTableCollector.class));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ChangeResult> execute(@NotNull ChangeExecutor<ResourceChange> executor,
                                      @NotNull ReconciliationContext context) {
        try (var producer = producerFactory.createProducer()) {
            List<ChangeHandler<ResourceChange>> handlers = List.of(
                    new KafkaTableRecordChangeHandler(producer),
                    new ChangeHandler.None<>(KafkaTableRecordChangeDescription::new)
            );
            return executor.applyChanges(handlers);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ResourceChange> plan(
            @NotNull Collection<V1KafkaTableRecord> resources,
            @NotNull ReconciliationContext context) {

        Map<String, List<V1KafkaTableRecord>> resourcesByTopic = resources.stream()
                .filter(AdminClientKafkaTableController::recordWithNonEmptyKey)
                .filter(context.selector()::apply)
                .collect(Collectors.groupingBy(it -> it.getSpec().getTopic(), Collectors.toList()));

        List<ResourceChange> changes = new ArrayList<>();

        for (var entry : resourcesByTopic.entrySet()) {
            String topicName = entry.getKey();
            // assuming that all the records in a theme have the same key/value format.
            V1KafkaTableRecord first = entry.getValue().get(0);
            Configuration configuration = Configuration.from(Map.of(
                    AdminClientKafkaTableCollector.TOPIC_NAME_CONFIG, topicName,
                    AdminClientKafkaTableCollector.KEY_TYPE_CONFIG, first.getSpec().getKey().type().name(),
                    AdminClientKafkaTableCollector.VALUE_TYPE_CONFIG, first.getSpec().getValue().type().name(),
                    AdminClientKafkaTableCollector.SKIP_MESSAGE_ON_ERROR_CONFIG, true
            ));
            ResourceListObject<V1KafkaTableRecord> list = collector.listAll(configuration, context.selector());

            KafkaTableRecordChangeComputer changeComputer = new KafkaTableRecordChangeComputer();
            changes.addAll(changeComputer.computeChanges(list.getItems(), entry.getValue()));
        }
        return changes;
    }

    private static boolean recordWithNonEmptyKey(@NotNull V1KafkaTableRecord item) {
        DataValue keyValue = item.getSpec().getKey();
        return keyValue != null && !keyValue.data().isNull();
    }
}
