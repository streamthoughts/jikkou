/*
 * Copyright 2022 StreamThoughts.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
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

import io.streamthoughts.jikkou.api.ReconciliationContext;
import io.streamthoughts.jikkou.api.ReconciliationMode;
import io.streamthoughts.jikkou.api.annotations.AcceptsReconciliationModes;
import io.streamthoughts.jikkou.api.annotations.AcceptsResource;
import io.streamthoughts.jikkou.api.config.ConfigProperty;
import io.streamthoughts.jikkou.api.config.Configuration;
import io.streamthoughts.jikkou.api.control.BaseResourceController;
import io.streamthoughts.jikkou.api.control.ChangeExecutor;
import io.streamthoughts.jikkou.api.control.ChangeHandler;
import io.streamthoughts.jikkou.api.control.ChangeResult;
import io.streamthoughts.jikkou.api.error.ConfigException;
import io.streamthoughts.jikkou.kafka.AdminClientContext;
import io.streamthoughts.jikkou.kafka.control.change.QuotaChange;
import io.streamthoughts.jikkou.kafka.control.change.QuotaChangeComputer;
import io.streamthoughts.jikkou.kafka.control.handlers.quotas.AlterQuotasChangeHandlerKafka;
import io.streamthoughts.jikkou.kafka.control.handlers.quotas.CreateQuotasChangeHandlerKafka;
import io.streamthoughts.jikkou.kafka.control.handlers.quotas.DeleteQuotasChangeHandler;
import io.streamthoughts.jikkou.kafka.control.handlers.quotas.QuotaChangeDescription;
import io.streamthoughts.jikkou.kafka.converters.V1KafkaClientQuotaListConverter;
import io.streamthoughts.jikkou.kafka.models.V1KafkaClientQuota;
import io.streamthoughts.jikkou.kafka.models.V1KafkaClientQuotaChange;
import io.streamthoughts.jikkou.kafka.models.V1KafkaClientQuotaChangeList;
import io.streamthoughts.jikkou.kafka.models.V1KafkaClientQuotaList;
import java.util.Collection;
import java.util.List;
import org.apache.kafka.clients.admin.AdminClient;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.VisibleForTesting;

@AcceptsResource(type = V1KafkaClientQuota.class)
@AcceptsResource(type = V1KafkaClientQuotaList.class, converter = V1KafkaClientQuotaListConverter.class)
@AcceptsReconciliationModes(value = {CREATE, DELETE, UPDATE, APPLY_ALL})
public final class AdminClientKafkaQuotaController extends AbstractAdminClientKafkaController
        implements BaseResourceController<V1KafkaClientQuota, QuotaChange> {

    public static final String LIMITS_DELETE_ORPHANS_CONFIG_NAME = "limits-delete-orphans";

    private AdminClientKafkaQuotaCollector descriptor;

    /**
     * Creates a new {@link AdminClientKafkaQuotaController} instance.
     */
    public AdminClientKafkaQuotaController() {
        super();
    }

    /**
     * Creates a new {@link AdminClientKafkaQuotaController} instance with the specified
     * application's configuration.
     *
     * @param config the application's configuration.
     */
    public AdminClientKafkaQuotaController(final @NotNull Configuration config) {
        super(config);
    }

    /**
     * Creates a new {@link AdminClientKafkaQuotaController} instance with the specified {@link AdminClientContext}.
     *
     * @param adminClientContext the {@link AdminClientContext} to use for acquiring a new {@link AdminClient}.
     */
    public AdminClientKafkaQuotaController(final @NotNull AdminClientContext adminClientContext) {
        super(adminClientContext);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void configure(@NotNull Configuration config) throws ConfigException {
        super.configure(config);
        this.descriptor = new AdminClientKafkaQuotaCollector(this.adminClientContext);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public V1KafkaClientQuotaChangeList computeReconciliationChanges(@NotNull Collection<V1KafkaClientQuota> resources,
                                                                     @NotNull ReconciliationMode mode,
                                                                     @NotNull ReconciliationContext context) {

        // Get the list of described resource that are candidates for this reconciliation
        final List<V1KafkaClientQuota> actualResource = descriptor.listAll();

        boolean isLimitDeletionEnabled = isLimitDeletionEnabled(mode, context);

        // Compute state changes
        List<QuotaChange> changes = new QuotaChangeComputer(isLimitDeletionEnabled).computeChanges(
                actualResource,
                resources
        );

        return new V1KafkaClientQuotaChangeList()
                .withItems(changes.stream().map(c -> V1KafkaClientQuotaChange.builder().withChange(c).build()).toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ChangeResult<QuotaChange>> execute(@NotNull List<QuotaChange> changes,
                                                   @NotNull ReconciliationMode mode,
                                                   boolean dryRun) {
        AdminClient client = adminClientContext.client();
        List<ChangeHandler<QuotaChange>> handlers = List.of(
                new CreateQuotasChangeHandlerKafka(client),
                new AlterQuotasChangeHandlerKafka(client),
                new DeleteQuotasChangeHandler(client),
                new ChangeHandler.None<>(QuotaChangeDescription::new)
        );
        return new ChangeExecutor<>(handlers).execute(changes, dryRun);
    }

    @VisibleForTesting
    static boolean isLimitDeletionEnabled(@NotNull ReconciliationMode mode, @NotNull ReconciliationContext context) {
        return ConfigProperty.ofBoolean(LIMITS_DELETE_ORPHANS_CONFIG_NAME)
                .orElse(() -> List.of(APPLY_ALL, DELETE, UPDATE).contains(mode))
                .evaluate(context.configuration());
    }
}
