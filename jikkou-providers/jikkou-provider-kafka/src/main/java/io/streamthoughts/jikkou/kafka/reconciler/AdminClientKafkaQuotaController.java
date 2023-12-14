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
import org.jetbrains.annotations.VisibleForTesting;

@SupportedResource(type = V1KafkaClientQuota.class)
@ControllerConfiguration(
        supportedModes = {CREATE, DELETE, UPDATE, FULL}
)
public final class AdminClientKafkaQuotaController
        extends ContextualExtension
        implements Controller<V1KafkaClientQuota, ResourceChange> {

    public static final String LIMITS_DELETE_ORPHANS_CONFIG_NAME = "limits-delete-orphans";

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
            this.adminClientContextFactory = new AdminClientContextFactory(context.appConfiguration());
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

        boolean isLimitDeletionEnabled = isLimitDeletionEnabled(context);

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

    @VisibleForTesting
    static boolean isLimitDeletionEnabled(@NotNull ReconciliationContext context) {
        return ConfigProperty.ofBoolean(LIMITS_DELETE_ORPHANS_CONFIG_NAME)
                .orElse(true)
                .get(context.configuration());
    }
}
