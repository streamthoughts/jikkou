/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.kafka.reconciler;

import static io.streamthoughts.jikkou.kafka.reconciler.KafkaConfigsConfig.*;
import static io.streamthoughts.jikkou.kafka.reconciler.KafkaConfigsConfig.DEFAULT_CONFIGS;

import io.streamthoughts.jikkou.core.annotation.SupportedResource;
import io.streamthoughts.jikkou.core.config.ConfigProperty;
import io.streamthoughts.jikkou.core.config.Configuration;
import io.streamthoughts.jikkou.core.extension.ContextualExtension;
import io.streamthoughts.jikkou.core.extension.ExtensionContext;
import io.streamthoughts.jikkou.core.models.ResourceList;
import io.streamthoughts.jikkou.core.reconciler.Collector;
import io.streamthoughts.jikkou.core.selector.Selector;
import io.streamthoughts.jikkou.core.selector.Selectors;
import io.streamthoughts.jikkou.kafka.KafkaExtensionProvider;
import io.streamthoughts.jikkou.kafka.KafkaLabelAndAnnotations;
import io.streamthoughts.jikkou.kafka.collections.V1KafkaTopicList;
import io.streamthoughts.jikkou.kafka.internals.KafkaConfigPredicate;
import io.streamthoughts.jikkou.kafka.internals.admin.AdminClientContext;
import io.streamthoughts.jikkou.kafka.internals.admin.AdminClientContextFactory;
import io.streamthoughts.jikkou.kafka.models.V1KafkaTopic;
import io.streamthoughts.jikkou.kafka.reconciler.service.KafkaTopicService;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SupportedResource(type = V1KafkaTopic.class)
public final class AdminClientKafkaTopicCollector extends ContextualExtension implements Collector<V1KafkaTopic> {

    private static final Logger LOG = LoggerFactory.getLogger(AdminClientKafkaTopicCollector.class);

    /**
     * The Extension config
     */
    public interface Config {
        ConfigProperty<Boolean> STATUS_CONFIG = ConfigProperty
            .ofBoolean("status")
            .description("Specify whether to describe status information about the topic-partitions")
            .defaultValue(false);
    }

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
            this.adminClientContextFactory = context.<KafkaExtensionProvider>provider().newAdminClientContextFactory();
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

        final KafkaConfigPredicate predicate = newConfigPredicate(configuration);
        try (AdminClientContext context = adminClientContextFactory.createAdminClientContext()) {

            boolean status = Config.STATUS_CONFIG.get(configuration);

            List<V1KafkaTopic> resources = new KafkaTopicService(context.getAdminClient())
                    .listAll(Set.of(name), predicate, status);

            if (resources.isEmpty()) {
                return Optional.empty();
            }

            V1KafkaTopic resource = resources.getFirst();
            resource = addClusterIdToMetadataAnnotations(resource, context.getClusterId());
            return Optional.of(resource);
        }
    }

    ResourceList<V1KafkaTopic> listAll() {
        return listAll(Configuration.from(Map.of(
                DEFAULT_CONFIGS.key(), true,
                DYNAMIC_BROKER_CONFIGS.key(), true,
                STATIC_BROKER_CONFIGS.key(), true
        )), Selectors.NO_SELECTOR);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResourceList<V1KafkaTopic> listAll(@NotNull final Configuration configuration,
                                              @NotNull final Selector selector) {

        if (LOG.isInfoEnabled()) {
            LOG.info("Listing all kafka topics with configuration: {}", configuration.asMap());
        }

        final KafkaConfigPredicate predicate = newConfigPredicate(configuration);
        try (AdminClientContext context = adminClientContextFactory.createAdminClientContext()) {

            boolean status = Config.STATUS_CONFIG.get(configuration);

            List<V1KafkaTopic> resources = new KafkaTopicService(context.getAdminClient())
                    .listAll(predicate, status);

            if (LOG.isInfoEnabled()) {
                LOG.info("Found '{}' kafka topics matching the given selector(s).", resources.size());
            }

            String clusterId = context.getClusterId();

            List<V1KafkaTopic> items = resources
                    .stream()
                    .filter(selector::apply)
                    .map(resource -> addClusterIdToMetadataAnnotations(resource, clusterId))
                    .toList();
            return new V1KafkaTopicList.Builder().withItems(items).build();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ConfigProperty<?>> configProperties() {
        return List.of(
            DEFAULT_CONFIGS,
            DYNAMIC_BROKER_CONFIGS,
            STATIC_BROKER_CONFIGS,
            Config.STATUS_CONFIG
        );
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
