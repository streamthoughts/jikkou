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
package io.streamthoughts.jikkou.kafka.reconcilier;

import static io.streamthoughts.jikkou.core.ReconciliationMode.DELETE;

import io.streamthoughts.jikkou.core.ReconciliationContext;
import io.streamthoughts.jikkou.core.annotation.SupportedResource;
import io.streamthoughts.jikkou.core.extension.ContextualExtension;
import io.streamthoughts.jikkou.core.extension.ExtensionContext;
import io.streamthoughts.jikkou.core.models.DefaultResourceListObject;
import io.streamthoughts.jikkou.core.models.HasMetadataChange;
import io.streamthoughts.jikkou.core.models.ResourceListObject;
import io.streamthoughts.jikkou.core.reconcilier.ChangeExecutor;
import io.streamthoughts.jikkou.core.reconcilier.ChangeHandler;
import io.streamthoughts.jikkou.core.reconcilier.ChangeResult;
import io.streamthoughts.jikkou.core.reconcilier.Controller;
import io.streamthoughts.jikkou.core.reconcilier.annotations.ControllerConfiguration;
import io.streamthoughts.jikkou.core.reconcilier.change.ValueChange;
import io.streamthoughts.jikkou.kafka.change.ConsumerGroupChangeComputer;
import io.streamthoughts.jikkou.kafka.change.handlers.consumer.ConsumerGroupChangeDescription;
import io.streamthoughts.jikkou.kafka.change.handlers.consumer.DeleteConsumerGroupHandler;
import io.streamthoughts.jikkou.kafka.internals.admin.AdminClientContext;
import io.streamthoughts.jikkou.kafka.internals.admin.AdminClientContextFactory;
import io.streamthoughts.jikkou.kafka.models.V1KafkaConsumerGroup;
import io.streamthoughts.jikkou.kafka.reconcilier.service.KafkaConsumerGroupService;
import java.util.Collection;
import java.util.List;
import org.apache.kafka.clients.admin.AdminClient;
import org.jetbrains.annotations.NotNull;

@SupportedResource(type = V1KafkaConsumerGroup.class)
@ControllerConfiguration(
        supportedModes = {DELETE}
)
public final class AdminClientConsumerGroupController
        extends ContextualExtension
        implements Controller<V1KafkaConsumerGroup, ValueChange<V1KafkaConsumerGroup>> {

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
            this.adminClientContextFactory = new AdminClientContextFactory(context.appConfiguration());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ChangeResult<ValueChange<V1KafkaConsumerGroup>>> execute(@NotNull ChangeExecutor<ValueChange<V1KafkaConsumerGroup>> executor,
                                                                         @NotNull ReconciliationContext context) {

        try (AdminClientContext clientContext = adminClientContextFactory.createAdminClientContext()) {
            AdminClient adminClient = clientContext.getAdminClient();
            List<ChangeHandler<ValueChange<V1KafkaConsumerGroup>>> handlers = List.of(
                    new DeleteConsumerGroupHandler(adminClient),
                    new ChangeHandler.None<>(ConsumerGroupChangeDescription::new)
            );
            return executor.execute(handlers);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResourceListObject<? extends HasMetadataChange<ValueChange<V1KafkaConsumerGroup>>> plan(@NotNull Collection<V1KafkaConsumerGroup> resources,
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
            KafkaConsumerGroupService service = new KafkaConsumerGroupService(clientContext.getAdminClient());

            // Get the actual Consumer Groups.
            List<V1KafkaConsumerGroup> actualStates = service.listConsumerGroups(consumerGroupsIds, false)
                    .getItems()
                    .stream()
                    .filter(context.selector()::apply)
                    .map(resource -> resource.withStatus(null))
                    .toList();

            ConsumerGroupChangeComputer changeComputer = new ConsumerGroupChangeComputer();
            List<HasMetadataChange<ValueChange<V1KafkaConsumerGroup>>> changes = changeComputer
                    .computeChanges(actualStates, expectedState);

            return DefaultResourceListObject
                    .<HasMetadataChange<ValueChange<V1KafkaConsumerGroup>>>builder()
                    .withItems(changes)
                    .build();
        }
    }
}
