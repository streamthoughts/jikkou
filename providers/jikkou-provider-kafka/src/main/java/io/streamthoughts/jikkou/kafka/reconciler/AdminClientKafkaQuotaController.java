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
import io.streamthoughts.jikkou.core.config.ConfigProperty;
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
import io.streamthoughts.jikkou.kafka.change.quota.KafkaClientQuotaChangeComputer;
import io.streamthoughts.jikkou.kafka.change.quota.KafkaClientQuotaChangeDescription;
import io.streamthoughts.jikkou.kafka.change.quota.KafkaClientQuotaChangeHandler;
import io.streamthoughts.jikkou.kafka.internals.admin.AdminClientContext;
import io.streamthoughts.jikkou.kafka.internals.admin.AdminClientContextFactory;
import io.streamthoughts.jikkou.kafka.models.V1KafkaClientQuota;
import java.util.Collection;
import java.util.List;
import org.apache.kafka.clients.admin.AdminClient;
import org.jetbrains.annotations.NotNull;

@SupportedResource(type = V1KafkaClientQuota.class)
@SupportedResource(apiVersion = ApiVersions.KAFKA_V1BETA2, kind = "KafkaClientQuotaChange")
@ControllerConfiguration(
        supportedModes = {CREATE, DELETE, UPDATE, FULL}
)
public final class AdminClientKafkaQuotaController
        extends ContextualExtension
        implements Controller<V1KafkaClientQuota, ResourceChange> {

    /**
     * The extension config
     */
    public interface Config {
        ConfigProperty<Boolean> LIMITS_DELETE_ORPHANS = ConfigProperty
            .ofBoolean("limits-delete-orphans")
            .defaultValue(true);
    }

    private AdminClientContextFactory adminClientContextFactory;

    /**
     * Creates a new {@link AdminClientKafkaQuotaController} instance.
     */
    public AdminClientKafkaQuotaController() {
        super();
    }

    /**
     * Creates a new {@link AdminClientKafkaQuotaCollector} instance.
     *
     * @param AdminClientContextFactory the {@link AdminClientContextFactory} to use for acquiring a new {@link AdminClientContext}.
     */
    public AdminClientKafkaQuotaController(final @NotNull AdminClientContextFactory AdminClientContextFactory) {
        this.adminClientContextFactory = AdminClientContextFactory;
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
    public List<ResourceChange> plan(@NotNull Collection<V1KafkaClientQuota> resources,
                                     @NotNull ReconciliationContext context) {

        // Get the list of described resource that are candidates for this reconciliation
        List<V1KafkaClientQuota> expected = resources.stream()
                .filter(context.selector()::apply)
                .toList();

        // Get the list of described resource that are candidates for this reconciliation
        AdminClientKafkaQuotaCollector collector = new AdminClientKafkaQuotaCollector(adminClientContextFactory);
        collector.init(extensionContext().contextForExtension(AdminClientKafkaQuotaCollector.class));

        final List<V1KafkaClientQuota> actual = collector.listAll(Configuration.empty(), Selectors.NO_SELECTOR)
                .stream()
                .filter(context.selector()::apply)
                .toList();

        boolean isLimitDeletionEnabled = Config.LIMITS_DELETE_ORPHANS.get(context.configuration());

        // Compute state changes
        KafkaClientQuotaChangeComputer changeComputer = new KafkaClientQuotaChangeComputer(isLimitDeletionEnabled);
        return changeComputer.computeChanges(actual, expected);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ChangeResult> execute(@NotNull final ChangeExecutor<ResourceChange> executor,
                                      @NotNull ReconciliationContext context) {
        try (AdminClientContext clientContext = adminClientContextFactory.createAdminClientContext()) {
            final AdminClient adminClient = clientContext.getAdminClient();
            List<ChangeHandler<ResourceChange>> handlers = List.of(
                    new KafkaClientQuotaChangeHandler(adminClient),
                    new ChangeHandler.None<>(KafkaClientQuotaChangeDescription::new)
            );
            return executor.applyChanges(handlers);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ConfigProperty<?>> configProperties() {
        return List.of(Config.LIMITS_DELETE_ORPHANS);
    }
}
