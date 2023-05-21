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

import io.streamthoughts.jikkou.api.annotations.AcceptsResource;
import io.streamthoughts.jikkou.api.config.Configuration;
import io.streamthoughts.jikkou.api.control.ExternalResourceCollector;
import io.streamthoughts.jikkou.api.error.JikkouRuntimeException;
import io.streamthoughts.jikkou.api.model.ObjectMeta;
import io.streamthoughts.jikkou.api.selector.ResourceSelector;
import io.streamthoughts.jikkou.kafka.AdminClientContext;
import io.streamthoughts.jikkou.kafka.MetadataAnnotations;
import io.streamthoughts.jikkou.kafka.adapters.V1KafkaClientQuotaConfigsAdapter;
import io.streamthoughts.jikkou.kafka.converters.V1KafkaClientQuotaListConverter;
import io.streamthoughts.jikkou.kafka.model.KafkaClientQuotaType;
import io.streamthoughts.jikkou.kafka.models.V1KafkaClientQuota;
import io.streamthoughts.jikkou.kafka.models.V1KafkaClientQuotaEntity;
import io.streamthoughts.jikkou.kafka.models.V1KafkaClientQuotaList;
import io.streamthoughts.jikkou.kafka.models.V1KafkaClientQuotaSpec;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.DescribeClientQuotasResult;
import org.apache.kafka.common.KafkaFuture;
import org.apache.kafka.common.quota.ClientQuotaEntity;
import org.apache.kafka.common.quota.ClientQuotaFilter;
import org.jetbrains.annotations.NotNull;

@AcceptsResource(type = V1KafkaClientQuota.class)
@AcceptsResource(type = V1KafkaClientQuotaList.class, converter = V1KafkaClientQuotaListConverter.class)
public final class AdminClientKafkaQuotaCollector extends AbstractAdminClientKafkaController
        implements ExternalResourceCollector<V1KafkaClientQuota> {

    /**
     * Creates a new {@link AdminClientKafkaQuotaCollector} instance.
     */
    public AdminClientKafkaQuotaCollector() {
        super();
    }

    /**
     * Creates a new {@link AdminClientKafkaQuotaCollector} instance with the specified
     * application's configuration.
     *
     * @param config the application's configuration.
     */
    public AdminClientKafkaQuotaCollector(final @NotNull Configuration config) {
        super(config);
    }

    /**
     * Creates a new {@link AdminClientKafkaQuotaCollector} instance with the specified {@link AdminClientContext}.
     *
     * @param adminClientContext the {@link AdminClientContext} to use for acquiring a new {@link AdminClient}.
     */
    public AdminClientKafkaQuotaCollector(final @NotNull AdminClientContext adminClientContext) {
        super(adminClientContext);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<V1KafkaClientQuota> listAll(@NotNull final Configuration configuration,
                                            @NotNull final List<ResourceSelector> selectors) {
        final List<V1KafkaClientQuota> resources;
        if (adminClientContext.isInitialized()) {
            resources = new DescribeQuotas(adminClientContext.client()).describe();
        } else {
            resources = adminClientContext.invoke(adminClient -> new DescribeQuotas(adminClient).describe());
        }

        String clusterId = adminClientContext.getClusterId();
        return resources.stream().map(resource -> resource
                        .toBuilder()
                        .withMetadata(resource.getMetadata()
                                .toBuilder()
                                .withAnnotation(MetadataAnnotations.JIKKOU_IO_KAFKA_CLUSTER_ID, clusterId)
                                .build()
                        )
                        .build()
                )
                .toList();
    }

    public static final class DescribeQuotas {

        private final AdminClient client;

        /**
         * Creates a new {@link DescribeQuotas} instance.
         *
         * @param client the {@link AdminClient}.
         */
        public DescribeQuotas(final AdminClient client) {
            this.client = client;
        }

        public List<V1KafkaClientQuota> describe() {
            DescribeClientQuotasResult result = client.describeClientQuotas(ClientQuotaFilter.all());
            KafkaFuture<Map<ClientQuotaEntity, Map<String, Double>>> future = result.entities();
            try {
                Map<ClientQuotaEntity, Map<String, Double>> entities = future.get();
                return entities.entrySet()
                        .stream()
                        .map(e -> toV1KafkaClientQuota(e.getKey(), e.getValue()))
                        .collect(Collectors.toList());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new JikkouRuntimeException(e);
            } catch (ExecutionException e) {
                throw new JikkouRuntimeException(e);
            }
        }

        private static V1KafkaClientQuota toV1KafkaClientQuota(ClientQuotaEntity entity,
                                                               Map<String, Double> configs) {
            Map<String, String> entries = entity.entries();
            return new V1KafkaClientQuota()
                    .toBuilder()
                    .withMetadata(new ObjectMeta())
                    .withSpec(V1KafkaClientQuotaSpec
                            .builder()
                            .withType(KafkaClientQuotaType.from(entries))
                            .withEntity(V1KafkaClientQuotaEntity
                                    .builder()
                                    .withUser(entries.get(ClientQuotaEntity.USER))
                                    .withClientId(entries.get(ClientQuotaEntity.CLIENT_ID))
                                    .build())
                            .withConfigs(V1KafkaClientQuotaConfigsAdapter.toClientQuotaConfigs(configs))
                            .build()
                    )
                    .build();
        }
    }
}
