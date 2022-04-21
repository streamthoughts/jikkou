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
package io.streamthoughts.kafka.specs.manager.adminclient;

import io.streamthoughts.kafka.specs.change.ChangeResult;
import io.streamthoughts.kafka.specs.change.QuotaChange;
import io.streamthoughts.kafka.specs.change.QuotaChangeOptions;
import io.streamthoughts.kafka.specs.config.JikkouConfig;
import io.streamthoughts.kafka.specs.error.ConfigException;
import io.streamthoughts.kafka.specs.error.ExecutionException;
import io.streamthoughts.kafka.specs.manager.AbstractKafkaQuotaManager;
import io.streamthoughts.kafka.specs.manager.DescribeOptions;
import io.streamthoughts.kafka.specs.manager.KafkaResourceUpdateContext;
import io.streamthoughts.kafka.specs.model.V1QuotaEntityObject;
import io.streamthoughts.kafka.specs.model.V1QuotaLimitsObject;
import io.streamthoughts.kafka.specs.model.V1QuotaObject;
import io.streamthoughts.kafka.specs.model.V1QuotaType;
import io.streamthoughts.kafka.specs.model.V1SpecObject;
import io.streamthoughts.kafka.specs.operation.quotas.AlterQuotasOperation;
import io.streamthoughts.kafka.specs.operation.quotas.ApplyQuotasOperation;
import io.streamthoughts.kafka.specs.operation.quotas.CreateQuotasOperation;
import io.streamthoughts.kafka.specs.operation.quotas.DeleteQuotasOperation;
import io.streamthoughts.kafka.specs.operation.quotas.QuotaOperation;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.DescribeClientQuotasResult;
import org.apache.kafka.common.KafkaFuture;
import org.apache.kafka.common.quota.ClientQuotaEntity;
import org.apache.kafka.common.quota.ClientQuotaFilter;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class AdminClientKafkaQuotaManager extends AbstractKafkaQuotaManager {

    private AdminClientContext adminClientContext;

    /**
     * Creates a new {@link AdminClientKafkaQuotaManager} instance.
     */
    public AdminClientKafkaQuotaManager() {
    }

    /**
     * Creates a new {@link AdminClientKafkaQuotaManager} instance with the specified
     * application's configuration.
     *
     * @param config the application's configuration.
     */
    public AdminClientKafkaQuotaManager(final @NotNull JikkouConfig config) {
        configure(config);
    }

    /**
     * Creates a new {@link AdminClientKafkaQuotaManager} instance with the specified {@link AdminClientContext}.
     *
     * @param adminClientContext the {@link AdminClientContext} to use for acquiring a new {@link AdminClient}.
     */
    public AdminClientKafkaQuotaManager(final @NotNull AdminClientContext adminClientContext) {
        this.adminClientContext = adminClientContext;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void configure(@NotNull JikkouConfig config) throws ConfigException {
        adminClientContext = new AdminClientContext(config);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<ChangeResult<QuotaChange>> update(final UpdateMode mode,
                                                        final List<V1SpecObject> objects,
                                                        final KafkaResourceUpdateContext<QuotaChangeOptions> context) {
        return adminClientContext.invokeAndClose((adminClient) -> super.update(mode, objects, context));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<V1QuotaObject> describe(final DescribeOptions options) {
        if (adminClientContext.isInitialized())
            return new DescribeQuotas(adminClientContext.current()).describe();

        return adminClientContext.invokeAndClose(adminClient -> new DescribeQuotas(adminClient).describe());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public QuotaOperation getOperationFor(@NotNull final UpdateMode mode) {
        return switch (mode) {
            case CREATE_ONLY -> new CreateQuotasOperation(adminClientContext.current());
            case ALTER_ONLY -> new AlterQuotasOperation(adminClientContext.current());
            case DELETE_ONLY -> new DeleteQuotasOperation(adminClientContext.current());
            case APPLY -> new ApplyQuotasOperation(adminClientContext.current());
        };
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

        public List<V1QuotaObject> describe() {
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
                            V1QuotaLimitsObject configsObject = new V1QuotaLimitsObject(e.getValue());
                            return new V1QuotaObject(V1QuotaType.from(entries), entityObject, configsObject);
                        })
                        .collect(Collectors.toList());
            } catch (Exception e) {
                throw new ExecutionException(e);
            }
        }
    }

}
