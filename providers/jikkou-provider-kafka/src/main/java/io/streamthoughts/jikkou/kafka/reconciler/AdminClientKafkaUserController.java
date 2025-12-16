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
import io.streamthoughts.jikkou.core.extension.ContextualExtension;
import io.streamthoughts.jikkou.core.extension.ExtensionContext;
import io.streamthoughts.jikkou.core.models.change.ResourceChange;
import io.streamthoughts.jikkou.core.reconciler.ChangeExecutor;
import io.streamthoughts.jikkou.core.reconciler.ChangeHandler;
import io.streamthoughts.jikkou.core.reconciler.ChangeResult;
import io.streamthoughts.jikkou.core.reconciler.Controller;
import io.streamthoughts.jikkou.core.reconciler.annotations.ControllerConfiguration;
import io.streamthoughts.jikkou.core.selector.Selectors;
import io.streamthoughts.jikkou.kafka.ApiVersions;
import io.streamthoughts.jikkou.kafka.KafkaExtensionProvider;
import io.streamthoughts.jikkou.kafka.change.user.UserChangeComputer;
import io.streamthoughts.jikkou.kafka.change.user.UserChangeDescription;
import io.streamthoughts.jikkou.kafka.change.user.UserChangeHandler;
import io.streamthoughts.jikkou.kafka.internals.admin.AdminClientContext;
import io.streamthoughts.jikkou.kafka.internals.admin.AdminClientContextFactory;
import io.streamthoughts.jikkou.kafka.model.user.V1KafkaUser;
import java.util.Collection;
import java.util.List;
import org.apache.kafka.clients.admin.AdminClient;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SupportedResource(type = V1KafkaUser.class)
@SupportedResource(apiVersion = ApiVersions.KAFKA_V1, kind = "KafkaUserChange")
@ControllerConfiguration(
        supportedModes = {CREATE, DELETE, UPDATE, FULL}
)
public final class AdminClientKafkaUserController
        extends ContextualExtension implements Controller<V1KafkaUser> {

    private static final Logger LOG = LoggerFactory.getLogger(AdminClientKafkaUserController.class);

    private AdminClientContextFactory adminClientContextFactory;

    /**
     * Creates a new {@link AdminClientKafkaUserController} instance.
     * CLI requires any empty constructor.
     */
    public AdminClientKafkaUserController() {
        super();
    }

    /**
     * Creates a new {@link AdminClientKafkaUserController} instance with the specified {@link AdminClientContext}.
     *
     * @param adminClientContextFactory the {@link AdminClientContextFactory} to use for acquiring a new {@link AdminClientContext}.
     */
    public AdminClientKafkaUserController(final @NotNull AdminClientContextFactory adminClientContextFactory) {
        this.adminClientContextFactory = adminClientContextFactory;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void init(@NotNull ExtensionContext context) {
        super.init(context);
        if (adminClientContextFactory == null) {
            this.adminClientContextFactory = context.<KafkaExtensionProvider>provider().newAdminClientContextFactory();
        }
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public List<ChangeResult> execute(@NotNull final ChangeExecutor executor,
                                      @NotNull final ReconciliationContext context) {

        try (AdminClientContext clientContext = adminClientContextFactory.createAdminClientContext()) {
            final AdminClient adminClient = clientContext.getAdminClient();
            List<ChangeHandler> handlers = List.of(
                    new UserChangeHandler(adminClient),
                    new ChangeHandler.None(UserChangeDescription::of)
            );
            return executor.applyChanges(handlers);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ResourceChange> plan(
            @NotNull Collection<V1KafkaUser> resources,
            @NotNull ReconciliationContext context) {

        if (LOG.isDebugEnabled()) {
            LOG.debug("Computing reconciliation change for '{}' resources", resources.size());
        }

        // Get the list of described resource that are candidates for this reconciliation
        List<V1KafkaUser> expectedResources = resources.stream()
                .filter(context.selector()::apply)
                .toList();

        // Get the list of remote resources that are AdminClientKafkaTopicCollector for this reconciliation
        AdminClientKafkaUserCollector collector = new AdminClientKafkaUserCollector(adminClientContextFactory);
        collector.init(extensionContext().contextForExtension(AdminClientKafkaUserCollector.class));

        List<V1KafkaUser> actualResources = collector.listAll(Configuration.empty(), Selectors.NO_SELECTOR)
                .stream()
                .filter(context.selector()::apply)
                .toList();

        UserChangeComputer changeComputer = new UserChangeComputer();
        return changeComputer.computeChanges(actualResources, expectedResources);
    }
}
