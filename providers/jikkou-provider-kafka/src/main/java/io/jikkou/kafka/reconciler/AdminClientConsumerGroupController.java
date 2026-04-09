/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.jikkou.kafka.reconciler;

import static io.jikkou.core.ReconciliationMode.DELETE;

import io.jikkou.core.ReconciliationContext;
import io.jikkou.core.annotation.Description;
import io.jikkou.core.annotation.SupportedResource;
import io.jikkou.core.annotation.Title;
import io.jikkou.core.extension.ContextualExtension;
import io.jikkou.core.extension.ExtensionContext;
import io.jikkou.core.models.change.ResourceChange;
import io.jikkou.core.reconciler.ChangeExecutor;
import io.jikkou.core.reconciler.ChangeHandler;
import io.jikkou.core.reconciler.ChangeResult;
import io.jikkou.core.reconciler.Controller;
import io.jikkou.core.reconciler.annotations.ControllerConfiguration;
import io.jikkou.kafka.ApiVersions;
import io.jikkou.kafka.KafkaExtensionProvider;
import io.jikkou.kafka.change.consumer.ConsumerGroupChangeComputer;
import io.jikkou.kafka.change.consumer.ConsumerGroupChangeDescription;
import io.jikkou.kafka.change.consumer.DeleteConsumerGroupHandler;
import io.jikkou.kafka.internals.admin.AdminClientContext;
import io.jikkou.kafka.internals.admin.AdminClientContextFactory;
import io.jikkou.kafka.models.V1KafkaConsumerGroup;
import io.jikkou.kafka.reconciler.service.KafkaAdminService;
import java.util.Collection;
import java.util.List;
import org.apache.kafka.clients.admin.AdminClient;
import org.jetbrains.annotations.NotNull;

@Title("Reconcile Kafka consumer groups")
@Description("Reconciles Kafka consumer group resources to ensure they match the desired state.")
@SupportedResource(type = V1KafkaConsumerGroup.class)
@SupportedResource(apiVersion = ApiVersions.KAFKA_V1BETA1, kind = "KafkaConsumerGroupChange")
@ControllerConfiguration(
        supportedModes = {DELETE}
)
public final class AdminClientConsumerGroupController
        extends ContextualExtension
        implements Controller<V1KafkaConsumerGroup> {

    private AdminClientContextFactory adminClientContextFactory;

    /**
     * Creates a new {@link AdminClientConsumerGroupController} instance.
     * CLI requires any empty constructor.
     */
    public AdminClientConsumerGroupController() {
        super();
    }

    /**
     * Creates a new {@link AdminClientConsumerGroupController} instance with the specified {@link AdminClientContext}.
     *
     * @param adminClientContextFactory the {@link AdminClientContextFactory} to use for acquiring a new {@link AdminClientContext}.
     */
    public AdminClientConsumerGroupController(final @NotNull AdminClientContextFactory adminClientContextFactory) {
        this.adminClientContextFactory = adminClientContextFactory;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void init(@NotNull ExtensionContext context) {
        super.init(context);
        if (adminClientContextFactory == null) {
            adminClientContextFactory = context.<KafkaExtensionProvider>provider().newAdminClientContextFactory();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ChangeResult> execute(@NotNull ChangeExecutor executor,
                                      @NotNull ReconciliationContext context) {

        try (AdminClientContext clientContext = adminClientContextFactory.createAdminClientContext()) {
            AdminClient adminClient = clientContext.getAdminClient();
            List<ChangeHandler> handlers = List.of(
                    new DeleteConsumerGroupHandler(adminClient),
                    new ChangeHandler.None(ConsumerGroupChangeDescription::new)
            );
            return executor.applyChanges(handlers);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ResourceChange> plan(@NotNull Collection<V1KafkaConsumerGroup> resources,
                                     @NotNull ReconciliationContext context) {

        // Get the expected Consumer Groups.
        List<V1KafkaConsumerGroup> expectedState = resources
                .stream()
                .filter(context.selector()::apply)
                .map(resource -> resource.withStatus(null))
                .toList();

        // Get the Consumer Group ID.
        List<String> consumerGroupsIds = expectedState.stream()
                .map(resource -> resource.getMetadata().getName())
                .distinct()
                .toList();

        try (AdminClientContext clientContext = adminClientContextFactory.createAdminClientContext()) {
            KafkaAdminService service = new KafkaAdminService(clientContext.getAdminClient());

            // Get the actual Consumer Groups.
            List<V1KafkaConsumerGroup> actualStates = service.listConsumerGroups(consumerGroupsIds, false)
                    .getItems()
                    .stream()
                    .filter(context.selector()::apply)
                    .map(resource -> resource.withStatus(null))
                    .toList();

            ConsumerGroupChangeComputer changeComputer = new ConsumerGroupChangeComputer();
            return changeComputer.computeChanges(actualStates, expectedState);
        }
    }
}
