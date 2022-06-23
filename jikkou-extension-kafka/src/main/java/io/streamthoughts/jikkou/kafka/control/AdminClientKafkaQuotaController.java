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

import io.streamthoughts.jikkou.api.AcceptResource;
import io.streamthoughts.jikkou.api.ReconciliationContext;
import io.streamthoughts.jikkou.api.ResourceFilter;
import io.streamthoughts.jikkou.api.config.Configuration;
import io.streamthoughts.jikkou.api.control.ChangeExecutor;
import io.streamthoughts.jikkou.api.control.ChangeResult;
import io.streamthoughts.jikkou.api.control.ResourceController;
import io.streamthoughts.jikkou.api.error.ExecutionException;
import io.streamthoughts.jikkou.api.model.ObjectMeta;
import io.streamthoughts.jikkou.kafka.AdminClientContext;
import io.streamthoughts.jikkou.kafka.adapters.KafkaQuotaLimitsAdapter;
import io.streamthoughts.jikkou.kafka.control.change.KafkaQuotaReconciliationConfig;
import io.streamthoughts.jikkou.kafka.control.change.QuotaChange;
import io.streamthoughts.jikkou.kafka.control.change.QuotaChangeComputer;
import io.streamthoughts.jikkou.kafka.control.operation.quotas.AlterQuotasOperation;
import io.streamthoughts.jikkou.kafka.control.operation.quotas.ApplyQuotasOperation;
import io.streamthoughts.jikkou.kafka.control.operation.quotas.CreateQuotasOperation;
import io.streamthoughts.jikkou.kafka.control.operation.quotas.DeleteQuotasOperation;
import io.streamthoughts.jikkou.kafka.model.QuotaType;
import io.streamthoughts.jikkou.kafka.models.V1KafkaQuotaList;
import io.streamthoughts.jikkou.kafka.models.V1KafkaQuotaObject;
import io.streamthoughts.jikkou.kafka.models.V1KafkaQuotaSpec;
import io.streamthoughts.jikkou.kafka.models.V1QuotaEntityObject;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.DescribeClientQuotasResult;
import org.apache.kafka.common.KafkaFuture;
import org.apache.kafka.common.quota.ClientQuotaEntity;
import org.apache.kafka.common.quota.ClientQuotaFilter;
import org.jetbrains.annotations.NotNull;

@AcceptResource(type = V1KafkaQuotaList.class)
public final class AdminClientKafkaQuotaController extends AdminClientKafkaController
        implements ResourceController<V1KafkaQuotaList, QuotaChange> {

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

    /** {@inheritDoc} */
    @Override
    public V1KafkaQuotaList describe(@NotNull final Configuration configuration,
                                     @NotNull final ResourceFilter filter) {
        final List<V1KafkaQuotaObject> quotaObjects;
        if (adminClientContext.isInitialized()) {
            quotaObjects = new DescribeQuotas(adminClientContext.client()).describe();
        } else {
            quotaObjects = adminClientContext.invoke(adminClient -> new DescribeQuotas(adminClient).describe());
        }

        return new V1KafkaQuotaList().toBuilder()
                .withMetadata(ObjectMeta
                        .builder()
                        .withAnnotation("jikkou.io/kafka-cluster-id", adminClientContext.getClusterId() )
                        .build()
                )
                .withSpec(V1KafkaQuotaSpec.builder()
                        .withQuotas(quotaObjects)
                        .build())
                .build();
    }

    /** {@inheritDoc} */
    @Override
    public KafkaQuotaReconciliationConfig defaultConciliationConfig() {
        return new KafkaQuotaReconciliationConfig();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<QuotaChange> computeReconciliationChanges(@NotNull V1KafkaQuotaList resource,
                                                          @NotNull ReconciliationContext reconciliationContext) {
        // Get the list of remote resources that are candidates for this reconciliation
        final List<V1KafkaQuotaObject> expectedResources = resource
                .getSpec().getQuotas();

        // Get the list of described resource that are candidates for this reconciliation
        final List<V1KafkaQuotaObject> actualResource = describe()
                .getSpec()
                .getQuotas();

        // Compute state changes
        return new QuotaChangeComputer().computeChanges(
                actualResource,
                expectedResources,
                new KafkaQuotaReconciliationConfig(reconciliationContext.configuration())
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<ChangeResult<QuotaChange>> create(@NotNull List<QuotaChange> changes, boolean dryRun) {
        var operation = new CreateQuotasOperation(adminClientContext.client());
        return ChangeExecutor.ofSupplier(() -> changes).execute(operation, dryRun);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<ChangeResult<QuotaChange>> update(@NotNull List<QuotaChange> changes, boolean dryRun) {
        var operation = new AlterQuotasOperation(adminClientContext.client());
        return ChangeExecutor.ofSupplier(() -> changes).execute(operation, dryRun);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<ChangeResult<QuotaChange>> delete(@NotNull List<QuotaChange> changes, boolean dryRun) {
        var operation = new DeleteQuotasOperation(adminClientContext.client());
        return ChangeExecutor.ofSupplier(() -> changes).execute(operation, dryRun);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<ChangeResult<QuotaChange>> apply(@NotNull List<QuotaChange> changes, boolean dryRun) {
        var operation = new ApplyQuotasOperation(adminClientContext.client());
        return ChangeExecutor.ofSupplier(() -> changes).execute(operation, dryRun);
    }

    public static final class DescribeQuotas {

        private final AdminClient client;

        /**
         * Creates a new {@link DescribeQuotas} instance.
         *
         * @param client       the {@link AdminClient}.
         */
        public DescribeQuotas(final AdminClient client) {
            this.client = client;
        }

        public List<V1KafkaQuotaObject> describe() {
            DescribeClientQuotasResult result = client.describeClientQuotas(ClientQuotaFilter.all());
            KafkaFuture<Map<ClientQuotaEntity, Map<String, Double>>> future = result.entities();
            try {
                Map<ClientQuotaEntity, Map<String, Double>> entities = future.get();
                return entities.entrySet()
                        .stream()
                        .map(e -> {
                            Map<String, String> entries = e.getKey().entries();
                            V1QuotaEntityObject entityObject = new V1QuotaEntityObject(
                                    entries.get(ClientQuotaEntity.USER),
                                    entries.get(ClientQuotaEntity.CLIENT_ID)
                            );
                            var configsObject = new KafkaQuotaLimitsAdapter(e.getValue()).toV1QuotaLimitsObject();
                            return new V1KafkaQuotaObject(QuotaType.from(entries), entityObject, configsObject);
                        })
                        .collect(Collectors.toList());
            } catch (Exception e) {
                throw new ExecutionException(e);
            }
        }
    }
}
