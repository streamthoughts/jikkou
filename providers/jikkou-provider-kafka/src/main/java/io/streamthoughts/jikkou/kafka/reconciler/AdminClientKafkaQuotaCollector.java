/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.kafka.reconciler;

import io.streamthoughts.jikkou.core.annotation.SupportedResource;
import io.streamthoughts.jikkou.core.config.Configuration;
import io.streamthoughts.jikkou.core.exceptions.JikkouRuntimeException;
import io.streamthoughts.jikkou.core.extension.ExtensionContext;
import io.streamthoughts.jikkou.core.models.ObjectMeta;
import io.streamthoughts.jikkou.core.models.ResourceList;
import io.streamthoughts.jikkou.core.reconciler.Collector;
import io.streamthoughts.jikkou.core.selector.Selector;
import io.streamthoughts.jikkou.kafka.KafkaExtensionProvider;
import io.streamthoughts.jikkou.kafka.KafkaLabelAndAnnotations;
import io.streamthoughts.jikkou.kafka.adapters.V1KafkaClientQuotaConfigsAdapter;
import io.streamthoughts.jikkou.kafka.collections.V1KafkaClientQuotaList;
import io.streamthoughts.jikkou.kafka.internals.admin.AdminClientContext;
import io.streamthoughts.jikkou.kafka.internals.admin.AdminClientContextFactory;
import io.streamthoughts.jikkou.kafka.model.KafkaClientQuotaEntity;
import io.streamthoughts.jikkou.kafka.model.KafkaClientQuotaType;
import io.streamthoughts.jikkou.kafka.models.V1KafkaClientQuota;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SupportedResource(type = V1KafkaClientQuota.class)
public final class AdminClientKafkaQuotaCollector
        implements Collector<V1KafkaClientQuota> {

    private static final Logger LOG = LoggerFactory.getLogger(AdminClientKafkaQuotaCollector.class);

    private AdminClientContextFactory adminClientContextFactory;

    /**
     * Creates a new {@link AdminClientKafkaQuotaCollector} instance.
     */
    public AdminClientKafkaQuotaCollector() {
        super();
    }

    /**
     * Creates a new {@link AdminClientKafkaQuotaCollector} instance with the specified {@link AdminClientContext}.
     *
     * @param adminClientContextFactory the {@link AdminClientContext} to use for acquiring a new {@link AdminClient}.
     */
    public AdminClientKafkaQuotaCollector(final @NotNull AdminClientContextFactory adminClientContextFactory) {
        this.adminClientContextFactory = adminClientContextFactory;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void init(@NotNull ExtensionContext context) {
        if (adminClientContextFactory == null) {
            adminClientContextFactory = context.<KafkaExtensionProvider>provider().newAdminClientContextFactory();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResourceList<V1KafkaClientQuota> listAll(@NotNull final Configuration configuration,
                                                    @NotNull final Selector selector) {
        try (AdminClientContext context = adminClientContextFactory.createAdminClientContext()) {
            final List<V1KafkaClientQuota> resources = new DescribeQuotas(context.getAdminClient()).describe();
            String clusterId = context.getClusterId();
            List<V1KafkaClientQuota> items = resources
                    .stream()
                    .filter(selector::apply)
                    .map(resource -> resource
                            .toBuilder()
                            .withMetadata(resource.getMetadata()
                                    .toBuilder()
                                    .withAnnotation(KafkaLabelAndAnnotations.JIKKOU_IO_KAFKA_CLUSTER_ID, clusterId)
                                    .build()
                            )
                            .build()
                    )
                    .toList();
            return new V1KafkaClientQuotaList.Builder().withItems(items).build();
        }
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
            return V1KafkaClientQuota
                    .builder()
                    .withMetadata(new ObjectMeta())
                    .withSpec(V1KafkaClientQuotaSpec
                            .builder()
                            .withType(KafkaClientQuotaType.from(entries))
                            .withEntity(KafkaClientQuotaEntity
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
