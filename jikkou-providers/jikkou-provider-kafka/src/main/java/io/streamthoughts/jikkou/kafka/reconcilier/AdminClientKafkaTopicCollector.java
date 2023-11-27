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

import io.streamthoughts.jikkou.core.annotation.SupportedResource;
import io.streamthoughts.jikkou.core.config.Configuration;
import io.streamthoughts.jikkou.core.extension.ExtensionContext;
import io.streamthoughts.jikkou.core.models.ResourceListObject;
import io.streamthoughts.jikkou.core.reconcilier.Collector;
import io.streamthoughts.jikkou.core.selector.Selector;
import io.streamthoughts.jikkou.kafka.KafkaLabelAndAnnotations;
import io.streamthoughts.jikkou.kafka.collections.V1KafkaTopicList;
import io.streamthoughts.jikkou.kafka.internals.KafkaConfigPredicate;
import io.streamthoughts.jikkou.kafka.internals.admin.AdminClientContext;
import io.streamthoughts.jikkou.kafka.internals.admin.AdminClientContextFactory;
import io.streamthoughts.jikkou.kafka.models.V1KafkaTopic;
import io.streamthoughts.jikkou.kafka.reconcilier.service.KafkaTopicService;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SupportedResource(type = V1KafkaTopic.class)
public final class AdminClientKafkaTopicCollector
        extends AdminClientKafkaConfigs implements Collector<V1KafkaTopic> {

    private static final Logger LOG = LoggerFactory.getLogger(AdminClientKafkaTopicCollector.class);

    private AdminClientContextFactory adminClientContextFactory;

    /**
     * Creates a new {@link AdminClientKafkaTopicCollector} instance.
     */
    public AdminClientKafkaTopicCollector() {
        super();
    }

    /**
     * Creates a new {@link AdminClientKafkaTopicCollector} instance.
     *
     * @param AdminClientContextFactory the {@link AdminClientContextFactory} to use for acquiring a new {@link AdminClientContext}.
     */
    public AdminClientKafkaTopicCollector(final @NotNull AdminClientContextFactory AdminClientContextFactory) {
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
    public Optional<V1KafkaTopic> get(@NotNull final String name,
                                      @NotNull final Configuration configuration) {

        if (LOG.isInfoEnabled()) {
            LOG.info("Listing all kafka topics with configuration: {}", configuration.asMap());
        }

        final KafkaConfigPredicate predicate = kafkaConfigPredicate(configuration);
        try (AdminClientContext context = adminClientContextFactory.createAdminClientContext()) {
            List<V1KafkaTopic> resources = new KafkaTopicService(context.getAdminClient())
                    .listAll(Set.of(name), predicate);

            if (resources.isEmpty()) {
                return Optional.empty();
            }

            V1KafkaTopic resource = resources.get(0);
            resource = addClusterIdToMetadataAnnotations(resource, context.getClusterId());
            return Optional.of(resource);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResourceListObject<V1KafkaTopic> listAll(@NotNull final Configuration configuration,
                                                    @NotNull final Selector selector) {

        if (LOG.isInfoEnabled()) {
            LOG.info("Listing all kafka topics with configuration: {}", configuration.asMap());
        }

        final KafkaConfigPredicate predicate = kafkaConfigPredicate(configuration);
        try (AdminClientContext context = adminClientContextFactory.createAdminClientContext()) {

            List<V1KafkaTopic> resources = new KafkaTopicService(context.getAdminClient())
                    .listAll(predicate);

            if (LOG.isInfoEnabled()) {
                LOG.info("Found '{}' kafka topics matching the given selector(s).", resources.size());
            }

            String clusterId = context.getClusterId();

            List<V1KafkaTopic> items = resources
                    .stream()
                    .filter(selector::apply)
                    .map(resource -> addClusterIdToMetadataAnnotations(resource, clusterId))
                    .toList();
            return new V1KafkaTopicList(items);
        }
    }

    private V1KafkaTopic addClusterIdToMetadataAnnotations(V1KafkaTopic resource,
                                                           String clusterId) {
        return resource.toBuilder()
                .withMetadata(resource.getMetadata()
                        .toBuilder()
                        .withAnnotation(KafkaLabelAndAnnotations.JIKKOU_IO_KAFKA_CLUSTER_ID, clusterId)
                        .build()
                )
                .build();
    }
}
