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
import io.jikkou.core.config.ConfigProperty;
import io.jikkou.core.config.Configuration;
import io.jikkou.core.extension.ContextualExtension;
import io.jikkou.core.extension.ExtensionContext;
import io.jikkou.core.models.change.ResourceChange;
import io.jikkou.core.reconciler.ChangeExecutor;
import io.jikkou.core.reconciler.ChangeHandler;
import io.jikkou.core.reconciler.ChangeResult;
import io.jikkou.core.reconciler.Controller;
import io.jikkou.core.reconciler.annotations.ControllerConfiguration;
import io.jikkou.core.selector.Selectors;
import io.jikkou.kafka.ApiVersions;
import io.jikkou.kafka.KafkaExtensionProvider;
import io.jikkou.kafka.change.quota.KafkaClientQuotaChangeComputer;
import io.jikkou.kafka.change.quota.KafkaClientQuotaChangeDescription;
import io.jikkou.kafka.change.quota.KafkaClientQuotaChangeHandler;
import io.jikkou.kafka.internals.admin.AdminClientContext;
import io.jikkou.kafka.internals.admin.AdminClientContextFactory;
import io.jikkou.kafka.models.V1KafkaClientQuota;
import java.util.Collection;
import java.util.List;
import org.apache.kafka.clients.admin.AdminClient;
import org.jetbrains.annotations.NotNull;

@Title("Reconcile Kafka quotas")
@Description("Reconciles Kafka client quota resources to ensure they match the desired state.")
@SupportedResource(type = V1KafkaClientQuota.class)
@SupportedResource(apiVersion = ApiVersions.KAFKA_V1BETA2, kind = "KafkaClientQuotaChange")
@ControllerConfiguration(
        supportedModes = {CREATE, DELETE, UPDATE, FULL}
)
public final class AdminClientKafkaQuotaController
        extends ContextualExtension
        implements Controller<V1KafkaClientQuota> {

    /**
     * The extension config
     */
    public interface Config {
        ConfigProperty<Boolean> LIMITS_DELETE_ORPHANS = ConfigProperty
            .ofBoolean("limits-delete-orphans")
            .displayName("Delete Orphan Limits")
            .description("Specify whether to delete quota limits that exist on the cluster but are not defined in the resource.")
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
    public List<ChangeResult> execute(@NotNull final ChangeExecutor executor,
                                      @NotNull ReconciliationContext context) {
        try (AdminClientContext clientContext = adminClientContextFactory.createAdminClientContext()) {
            final AdminClient adminClient = clientContext.getAdminClient();
            List<ChangeHandler> handlers = List.of(
                    new KafkaClientQuotaChangeHandler(adminClient),
                    new ChangeHandler.None(KafkaClientQuotaChangeDescription::new)
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
