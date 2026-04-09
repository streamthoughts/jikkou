/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.jikkou.kafka.reconciler;

import io.jikkou.core.annotation.Description;
import io.jikkou.core.annotation.SupportedResource;
import io.jikkou.core.annotation.Title;
import io.jikkou.core.config.Configuration;
import io.jikkou.core.extension.ContextualExtension;
import io.jikkou.core.extension.ExtensionContext;
import io.jikkou.core.models.ResourceList;
import io.jikkou.core.models.SpecificResource;
import io.jikkou.core.models.generics.GenericResourceList;
import io.jikkou.core.reconciler.Collector;
import io.jikkou.core.selector.Selector;
import io.jikkou.kafka.KafkaExtensionProvider;
import io.jikkou.kafka.KafkaLabelAndAnnotations;
import io.jikkou.kafka.internals.admin.AdminClientContext;
import io.jikkou.kafka.internals.admin.AdminClientContextFactory;
import io.jikkou.kafka.model.user.V1KafkaUser;
import io.jikkou.kafka.reconciler.service.KafkaUserService;
import java.util.List;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Title("Collect Kafka users")
@Description("Collects all Kafka user SCRAM credential resources from a Kafka cluster using the AdminClient API.")
@SupportedResource(type = V1KafkaUser.class)
public final class AdminClientKafkaUserCollector extends ContextualExtension implements Collector<V1KafkaUser> {

    private static final Logger LOG = LoggerFactory.getLogger(AdminClientKafkaUserCollector.class);

    private AdminClientContextFactory adminClientContextFactory;

    /**
     * Creates a new {@link AdminClientKafkaUserCollector} instance.
     */
    public AdminClientKafkaUserCollector() {
        super();
    }

    /**
     * Creates a new {@link AdminClientKafkaUserCollector} instance.
     *
     * @param AdminClientContextFactory the {@link AdminClientContextFactory} to use for acquiring a new {@link AdminClientContext}.
     */
    public AdminClientKafkaUserCollector(final @NotNull AdminClientContextFactory AdminClientContextFactory) {
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
    public Optional<V1KafkaUser> get(@NotNull final String name,
                                     @NotNull final Configuration configuration) {
        try (AdminClientContext context = adminClientContextFactory.createAdminClientContext()) {

            return new KafkaUserService(context.getAdminClient())
                .findUserScramCredentials(name)
                .map(resource -> addClusterIdToMetadataAnnotations(resource, context.getClusterId()));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResourceList<V1KafkaUser> listAll(@NotNull final Configuration configuration,
                                             @NotNull final Selector selector) {

        if (LOG.isDebugEnabled()) {
            LOG.debug("Listing all kafka users with configuration: {}", configuration.asMap());
        }

        try (AdminClientContext context = adminClientContextFactory.createAdminClientContext()) {

            List<V1KafkaUser> resources = new KafkaUserService(context.getAdminClient())
                .listUserScramCredentials()
                .stream()
                .filter(selector::apply)
                .toList();

            if (LOG.isDebugEnabled()) {
                LOG.debug("Found '{}' kafka users matching the given selector(s).", resources.size());
            }

            String clusterId = context.getClusterId();

            List<V1KafkaUser> items = resources
                .stream()
                .filter(selector::apply)
                .map(resource -> addClusterIdToMetadataAnnotations(resource, clusterId))
                .toList();

            return new GenericResourceList.Builder<V1KafkaUser>().withItems(items).build();
        }
    }

    private <S, T extends SpecificResource<T, S>> T addClusterIdToMetadataAnnotations(T resource,
                                                                                      String clusterId) {
        return resource
            .toBuilder()
            .withMetadata(resource.getMetadata()
                .toBuilder()
                .withAnnotation(KafkaLabelAndAnnotations.JIKKOU_IO_KAFKA_CLUSTER_ID, clusterId)
                .build()
            )
            .build();
    }
}
