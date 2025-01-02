/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
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
import io.streamthoughts.jikkou.core.models.ResourceList;
import io.streamthoughts.jikkou.core.models.change.ResourceChange;
import io.streamthoughts.jikkou.core.reconciler.ChangeExecutor;
import io.streamthoughts.jikkou.core.reconciler.ChangeHandler;
import io.streamthoughts.jikkou.core.reconciler.ChangeResult;
import io.streamthoughts.jikkou.core.reconciler.Controller;
import io.streamthoughts.jikkou.core.reconciler.annotations.ControllerConfiguration;
import io.streamthoughts.jikkou.kafka.ApiVersions;
import io.streamthoughts.jikkou.kafka.KafkaExtensionProvider;
import io.streamthoughts.jikkou.kafka.change.record.KafkaTableRecordChangeComputer;
import io.streamthoughts.jikkou.kafka.change.record.KafkaTableRecordChangeDescription;
import io.streamthoughts.jikkou.kafka.change.record.KafkaTableRecordChangeHandler;
import io.streamthoughts.jikkou.kafka.internals.admin.AdminClientFactory;
import io.streamthoughts.jikkou.kafka.internals.consumer.ConsumerFactory;
import io.streamthoughts.jikkou.kafka.internals.producer.ProducerFactory;
import io.streamthoughts.jikkou.kafka.model.DataValue;
import io.streamthoughts.jikkou.kafka.models.V1KafkaTableRecord;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.jetbrains.annotations.NotNull;

@SupportedResource(type = V1KafkaTableRecord.class)
@SupportedResource(apiVersion = ApiVersions.KAFKA_V1BETA1, kind = "KafkaTableRecordChange")
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
            producerFactory = context.<KafkaExtensionProvider>provider()
                .newProducerFactory(new ByteArraySerializer(),  new ByteArraySerializer());
        }

        if (consumerFactory == null) {
            consumerFactory = context.<KafkaExtensionProvider>provider()
                .newConsumerFactory(new ByteArrayDeserializer(),  new ByteArrayDeserializer());
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
            ResourceList<V1KafkaTableRecord> list = collector.listAll(configuration, context.selector());

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
