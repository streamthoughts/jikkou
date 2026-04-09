/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.jikkou.kafka.reconciler;

import static io.jikkou.core.ReconciliationMode.CREATE;
import static io.jikkou.core.ReconciliationMode.DELETE;
import static io.jikkou.core.ReconciliationMode.FULL;
import static io.jikkou.core.ReconciliationMode.UPDATE;

import io.jikkou.core.ReconciliationContext;
import io.jikkou.core.annotation.Description;
import io.jikkou.core.annotation.SupportedResource;
import io.jikkou.core.annotation.Title;
import io.jikkou.core.config.Configuration;
import io.jikkou.core.extension.ExtensionContext;
import io.jikkou.core.models.ResourceList;
import io.jikkou.core.models.change.ResourceChange;
import io.jikkou.core.reconciler.ChangeExecutor;
import io.jikkou.core.reconciler.ChangeHandler;
import io.jikkou.core.reconciler.ChangeResult;
import io.jikkou.core.reconciler.Controller;
import io.jikkou.core.reconciler.annotations.ControllerConfiguration;
import io.jikkou.kafka.ApiVersions;
import io.jikkou.kafka.KafkaExtensionProvider;
import io.jikkou.kafka.change.record.KafkaTableRecordChangeComputer;
import io.jikkou.kafka.change.record.KafkaTableRecordChangeDescription;
import io.jikkou.kafka.change.record.KafkaTableRecordChangeHandler;
import io.jikkou.kafka.internals.admin.AdminClientFactory;
import io.jikkou.kafka.internals.consumer.ConsumerFactory;
import io.jikkou.kafka.internals.producer.ProducerFactory;
import io.jikkou.kafka.model.DataValue;
import io.jikkou.kafka.models.V1KafkaTableRecord;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.jetbrains.annotations.NotNull;

@Title("Reconcile Kafka tables")
@Description("Reconciles Kafka table resources to ensure they match the desired state.")
@SupportedResource(type = V1KafkaTableRecord.class)
@SupportedResource(apiVersion = ApiVersions.KAFKA_V1BETA1, kind = "KafkaTableRecordChange")
@ControllerConfiguration(
        supportedModes = {CREATE, DELETE, UPDATE, FULL}
)
public final class AdminClientKafkaTableController implements Controller<V1KafkaTableRecord> {

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
    public List<ChangeResult> execute(@NotNull ChangeExecutor executor,
                                      @NotNull ReconciliationContext context) {
        try (var producer = producerFactory.createProducer()) {
            List<ChangeHandler> handlers = List.of(
                    new KafkaTableRecordChangeHandler(producer),
                    new ChangeHandler.None(KafkaTableRecordChangeDescription::new)
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
            V1KafkaTableRecord first = entry.getValue().getFirst();
            Configuration configuration = Configuration.from(Map.of(
                TopicConfig.TOPIC_NAME.key(), topicName,
                TopicConfig.KEY_TYPE.key(), first.getSpec().getKey().type().name(),
                TopicConfig.VALUE_TYPE.key(), first.getSpec().getValue().type().name(),
                TopicConfig.SKIP_MESSAGE_ON_ERROR.key(), true
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
