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
import io.streamthoughts.jikkou.core.models.ResourceList;
import io.streamthoughts.jikkou.core.reconciler.Collector;
import io.streamthoughts.jikkou.core.selector.Selector;
import io.streamthoughts.jikkou.kafka.KafkaExtensionProvider;
import io.streamthoughts.jikkou.kafka.KafkaLabelAndAnnotations;
import io.streamthoughts.jikkou.kafka.adapters.KafkaAclBindingAdapter;
import io.streamthoughts.jikkou.kafka.adapters.V1KafkaPrincipalAuthorizationSupport;
import io.streamthoughts.jikkou.kafka.collections.V1KafkaPrincipalAuthorizationList;
import io.streamthoughts.jikkou.kafka.internals.admin.AdminClientContext;
import io.streamthoughts.jikkou.kafka.internals.admin.AdminClientContextFactory;
import io.streamthoughts.jikkou.kafka.model.KafkaAclBinding;
import io.streamthoughts.jikkou.kafka.models.V1KafkaPrincipalAuthorization;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.DescribeAclsResult;
import org.apache.kafka.common.acl.AclBinding;
import org.apache.kafka.common.acl.AclBindingFilter;
import org.jetbrains.annotations.NotNull;

@SupportedResource(type = V1KafkaPrincipalAuthorization.class)
public final class AdminClientKafkaAclCollector
        implements Collector<V1KafkaPrincipalAuthorization> {

    private AdminClientContextFactory adminClientContextFactory;

    /**
     * Creates a new {@link AdminClientKafkaAclCollector} instance.
     * CLI requires any empty constructor.
     */
    public AdminClientKafkaAclCollector() {
        super();
    }

    /**
     * Creates a new {@link AdminClientKafkaAclCollector} instance with the specified {@link AdminClientContext}.
     *
     * @param adminClientContextFactory the {@link AdminClientContextFactory} to use for acquiring a new {@link AdminClientContext}.
     */
    public AdminClientKafkaAclCollector(final @NotNull AdminClientContextFactory adminClientContextFactory) {
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
    public ResourceList<V1KafkaPrincipalAuthorization> listAll(@NotNull final Configuration configuration,
                                                               @NotNull final Selector selector) {


        try (AdminClientContext adminClientContext = adminClientContextFactory.createAdminClientContext()) {
            List<V1KafkaPrincipalAuthorization> resources = listAll(adminClientContext.getAdminClient())
                .stream()
                .filter(selector::apply)
                .toList();

            String clusterId = adminClientContext.getClusterId();
            List<V1KafkaPrincipalAuthorization> items = resources.stream().map(resource -> resource
                    .toBuilder()
                    .withMetadata(resource.getMetadata()
                        .toBuilder()
                        .withAnnotation(KafkaLabelAndAnnotations.JIKKOU_IO_KAFKA_CLUSTER_ID, clusterId)
                        .build()
                    )
                    .build()
                )
                .toList();
            return new V1KafkaPrincipalAuthorizationList.Builder().withItems(items).build();
        }
    }

    List<V1KafkaPrincipalAuthorization> listAll(final @NotNull AdminClient client) {
        return new ArrayList<>(new KafkaAclsClient(client).listAll());
    }

    /**
     * {@link Function} to list all ACLs rules.
     */
    public static final class KafkaAclsClient {

        private final AdminClient client;

        /**
         * Creates a new {@link KafkaAclsClient} instance.
         *
         * @param client the {@link AdminClient}.
         */
        public KafkaAclsClient(@NotNull final AdminClient client) {
            this.client = client;
        }

        public Collection<V1KafkaPrincipalAuthorization> listAll() {
            try {
                List<KafkaAclBinding> bindings = describeAsync()
                        .get()
                        .stream()
                        .map(KafkaAclBindingAdapter::fromAclBinding)
                        .toList();
                return V1KafkaPrincipalAuthorizationSupport.from(bindings).toList();

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new JikkouRuntimeException("Failed to describe ACL due to thread-interruption", e);

            } catch (ExecutionException e) {
                throw new JikkouRuntimeException("Failed to describe ACL due to execution error", e);
            }
        }

        private CompletableFuture<Collection<AclBinding>> describeAsync() {
            return CompletableFuture.supplyAsync(() -> {
                try {
                    DescribeAclsResult result = client.describeAcls(AclBindingFilter.ANY);
                    return result.values().get();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new JikkouRuntimeException("Failed to describe ACL due to thread-interruption", e);

                } catch (ExecutionException e) {
                    throw new JikkouRuntimeException("Failed to describe ACL due to execution error", e);
                }
            });
        }
    }
}
