/*
 * Copyright 2022 The original authors
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
package io.streamthoughts.jikkou.kafka.control;

import static io.streamthoughts.jikkou.api.ReconciliationMode.APPLY_ALL;
import static io.streamthoughts.jikkou.api.ReconciliationMode.CREATE;
import static io.streamthoughts.jikkou.api.ReconciliationMode.DELETE;
import static io.streamthoughts.jikkou.api.ReconciliationMode.UPDATE;

import io.streamthoughts.jikkou.annotation.AcceptsReconciliationModes;
import io.streamthoughts.jikkou.annotation.AcceptsResource;
import io.streamthoughts.jikkou.api.ReconciliationContext;
import io.streamthoughts.jikkou.api.ReconciliationMode;
import io.streamthoughts.jikkou.api.change.ChangeExecutor;
import io.streamthoughts.jikkou.api.change.ChangeHandler;
import io.streamthoughts.jikkou.api.change.ChangeResult;
import io.streamthoughts.jikkou.api.config.ConfigProperty;
import io.streamthoughts.jikkou.api.config.Configuration;
import io.streamthoughts.jikkou.api.control.BaseResourceController;
import io.streamthoughts.jikkou.api.error.ConfigException;
import io.streamthoughts.jikkou.api.model.HasMetadataChange;
import io.streamthoughts.jikkou.api.selector.AggregateSelector;
import io.streamthoughts.jikkou.kafka.change.QuotaChange;
import io.streamthoughts.jikkou.kafka.change.QuotaChangeComputer;
import io.streamthoughts.jikkou.kafka.change.handlers.quotas.CreateQuotasChangeHandlerKafka;
import io.streamthoughts.jikkou.kafka.change.handlers.quotas.DeleteQuotasChangeHandler;
import io.streamthoughts.jikkou.kafka.change.handlers.quotas.QuotaChangeDescription;
import io.streamthoughts.jikkou.kafka.change.handlers.quotas.UpdateQuotasChangeHandlerKafka;
import io.streamthoughts.jikkou.kafka.converters.V1KafkaClientQuotaListConverter;
import io.streamthoughts.jikkou.kafka.internals.admin.AdminClientContext;
import io.streamthoughts.jikkou.kafka.internals.admin.AdminClientContextFactory;
import io.streamthoughts.jikkou.kafka.models.V1KafkaClientQuota;
import io.streamthoughts.jikkou.kafka.models.V1KafkaClientQuotaChange;
import io.streamthoughts.jikkou.kafka.models.V1KafkaClientQuotaChangeList;
import io.streamthoughts.jikkou.kafka.models.V1KafkaClientQuotaList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.kafka.clients.admin.AdminClient;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.VisibleForTesting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@AcceptsResource(type = V1KafkaClientQuota.class)
@AcceptsResource(type = V1KafkaClientQuotaList.class, converter = V1KafkaClientQuotaListConverter.class)
@AcceptsReconciliationModes(value = {CREATE, DELETE, UPDATE, APPLY_ALL})
public final class AdminClientKafkaQuotaController
        implements BaseResourceController<V1KafkaClientQuota, QuotaChange> {

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
    public V1KafkaClientQuotaChangeList computeReconciliationChanges(@NotNull Collection<V1KafkaClientQuota> resources,
                                                                     @NotNull ReconciliationMode mode,
                                                                     @NotNull ReconciliationContext context) {

        // Get the list of described resource that are candidates for this reconciliation
        List<V1KafkaClientQuota> expected = resources.stream()
                .filter(new AggregateSelector(context.selectors())::apply)
                .toList();

        // Get the list of described resource that are candidates for this reconciliation
        AdminClientKafkaQuotaCollector collector = new AdminClientKafkaQuotaCollector(adminClientContextFactory);

        final List<V1KafkaClientQuota> actual = collector.listAll()
                .stream()
                .filter(new AggregateSelector(context.selectors())::apply)
                .toList();

        boolean isLimitDeletionEnabled = isLimitDeletionEnabled(mode, context);

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
    public List<ChangeResult<QuotaChange>> execute(@NotNull List<HasMetadataChange<QuotaChange>> changes,
                                                   @NotNull ReconciliationMode mode,
                                                   boolean dryRun) {
        try (AdminClientContext context = adminClientContextFactory.createAdminClientContext()) {
            final AdminClient adminClient = context.getAdminClient();
            List<ChangeHandler<QuotaChange>> handlers = List.of(
                    new CreateQuotasChangeHandlerKafka(adminClient),
                    new UpdateQuotasChangeHandlerKafka(adminClient),
                    new DeleteQuotasChangeHandler(adminClient),
                    new ChangeHandler.None<>(QuotaChangeDescription::new)
            );
            return new ChangeExecutor<>(handlers).execute(changes, dryRun);
        }
    }

    @VisibleForTesting
    static boolean isLimitDeletionEnabled(@NotNull ReconciliationMode mode, @NotNull ReconciliationContext context) {
        return ConfigProperty.ofBoolean(LIMITS_DELETE_ORPHANS_CONFIG_NAME)
                .orElse(() -> List.of(APPLY_ALL, DELETE, UPDATE).contains(mode))
                .evaluate(context.configuration());
    }
}
