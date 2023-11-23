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

import static io.streamthoughts.jikkou.core.ReconciliationMode.CREATE;
import static io.streamthoughts.jikkou.core.ReconciliationMode.DELETE;
import static io.streamthoughts.jikkou.core.ReconciliationMode.FULL;
import static io.streamthoughts.jikkou.core.ReconciliationMode.UPDATE;

import io.streamthoughts.jikkou.core.ReconciliationContext;
import io.streamthoughts.jikkou.core.annotation.SupportedResource;
import io.streamthoughts.jikkou.core.config.ConfigProperty;
import io.streamthoughts.jikkou.core.config.Configuration;
import io.streamthoughts.jikkou.core.exceptions.ConfigException;
import io.streamthoughts.jikkou.core.reconcilier.ChangeExecutor;
import io.streamthoughts.jikkou.core.reconcilier.ChangeHandler;
import io.streamthoughts.jikkou.core.reconcilier.ChangeResult;
import io.streamthoughts.jikkou.core.reconcilier.Controller;
import io.streamthoughts.jikkou.core.reconcilier.annotations.ControllerConfiguration;
import io.streamthoughts.jikkou.core.selector.Selectors;
import io.streamthoughts.jikkou.kafka.change.QuotaChange;
import io.streamthoughts.jikkou.kafka.change.QuotaChangeComputer;
import io.streamthoughts.jikkou.kafka.change.handlers.quotas.CreateQuotasChangeHandlerKafka;
import io.streamthoughts.jikkou.kafka.change.handlers.quotas.DeleteQuotasChangeHandler;
import io.streamthoughts.jikkou.kafka.change.handlers.quotas.QuotaChangeDescription;
import io.streamthoughts.jikkou.kafka.change.handlers.quotas.UpdateQuotasChangeHandlerKafka;
import io.streamthoughts.jikkou.kafka.internals.admin.AdminClientContext;
import io.streamthoughts.jikkou.kafka.internals.admin.AdminClientContextFactory;
import io.streamthoughts.jikkou.kafka.models.V1KafkaClientQuota;
import io.streamthoughts.jikkou.kafka.models.V1KafkaClientQuotaChange;
import io.streamthoughts.jikkou.kafka.models.V1KafkaClientQuotaChangeList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.kafka.clients.admin.AdminClient;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.VisibleForTesting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SupportedResource(type = V1KafkaClientQuota.class)
@ControllerConfiguration(
        supportedModes = {CREATE, DELETE, UPDATE, FULL}
)
public final class AdminClientKafkaQuotaController
        implements Controller<V1KafkaClientQuota, QuotaChange> {

    private static final Logger LOG = LoggerFactory.getLogger(AdminClientKafkaQuotaController.class);

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
    public void configure(@NotNull Configuration configuration) throws ConfigException {
        LOG.info("Configuring");
        if (adminClientContextFactory == null) {
            this.adminClientContextFactory = new AdminClientContextFactory(configuration);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public V1KafkaClientQuotaChangeList plan(@NotNull Collection<V1KafkaClientQuota> resources,
                                             @NotNull ReconciliationContext context) {

        // Get the list of described resource that are candidates for this reconciliation
        List<V1KafkaClientQuota> expected = resources.stream()
                .filter(context.selector()::apply)
                .toList();

        // Get the list of described resource that are candidates for this reconciliation
        AdminClientKafkaQuotaCollector collector = new AdminClientKafkaQuotaCollector(adminClientContextFactory);

        final List<V1KafkaClientQuota> actual = collector.listAll(Selectors.NO_SELECTOR)
                .stream()
                .filter(context.selector()::apply)
                .toList();

        boolean isLimitDeletionEnabled = isLimitDeletionEnabled(context);

        // Compute state changes
        QuotaChangeComputer changeComputer = new QuotaChangeComputer(isLimitDeletionEnabled);
        List<V1KafkaClientQuotaChange> changes = changeComputer.computeChanges(actual, expected).stream()
                .map(it -> V1KafkaClientQuotaChange.builder().withChange(it.getChange()).build())
                .collect(Collectors.toList());

        return V1KafkaClientQuotaChangeList.builder().withItems(changes).build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ChangeResult<QuotaChange>> execute(@NotNull final ChangeExecutor<QuotaChange> executor,
                                                   @NotNull ReconciliationContext context) {
        try (AdminClientContext clientContext = adminClientContextFactory.createAdminClientContext()) {
            final AdminClient adminClient = clientContext.getAdminClient();
            List<ChangeHandler<QuotaChange>> handlers = List.of(
                    new CreateQuotasChangeHandlerKafka(adminClient),
                    new UpdateQuotasChangeHandlerKafka(adminClient),
                    new DeleteQuotasChangeHandler(adminClient),
                    new ChangeHandler.None<>(QuotaChangeDescription::new)
            );
            return executor.execute(handlers);
        }
    }

    @VisibleForTesting
    static boolean isLimitDeletionEnabled(@NotNull ReconciliationContext context) {
        return ConfigProperty.ofBoolean(LIMITS_DELETE_ORPHANS_CONFIG_NAME)
                .orElse(true)
                .evaluate(context.configuration());
    }
}
