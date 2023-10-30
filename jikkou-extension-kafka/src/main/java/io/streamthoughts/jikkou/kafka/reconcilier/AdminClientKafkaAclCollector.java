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
package io.streamthoughts.jikkou.kafka.reconcilier;

import io.streamthoughts.jikkou.core.annotation.HandledResource;
import io.streamthoughts.jikkou.core.config.Configuration;
import io.streamthoughts.jikkou.core.exceptions.ConfigException;
import io.streamthoughts.jikkou.core.exceptions.JikkouRuntimeException;
import io.streamthoughts.jikkou.core.models.ResourceListObject;
import io.streamthoughts.jikkou.core.reconcilier.Collector;
import io.streamthoughts.jikkou.core.selectors.AggregateSelector;
import io.streamthoughts.jikkou.core.selectors.Selector;
import io.streamthoughts.jikkou.kafka.MetadataAnnotations;
import io.streamthoughts.jikkou.kafka.adapters.KafkaAclBindingAdapter;
import io.streamthoughts.jikkou.kafka.adapters.V1KafkaPrincipalAuthorizationSupport;
import io.streamthoughts.jikkou.kafka.collections.V1V1KafkaPrincipalAuthorizationList;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@HandledResource(type = V1KafkaPrincipalAuthorization.class)
public final class AdminClientKafkaAclCollector
        implements Collector<V1KafkaPrincipalAuthorization> {

    private static final Logger LOG = LoggerFactory.getLogger(AdminClientKafkaAclCollector.class);

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
    public ResourceListObject<V1KafkaPrincipalAuthorization> listAll(@NotNull final Configuration configuration,
                                                                     @NotNull final List<Selector> selectors) {


        try (AdminClientContext adminClientContext = adminClientContextFactory.createAdminClientContext()) {

            List<V1KafkaPrincipalAuthorization> resources = listAll(adminClientContext.getAdminClient())
                    .stream()
                    .filter(new AggregateSelector(selectors)::apply)
                    .toList();

            String clusterId = adminClientContext.getClusterId();
            List<V1KafkaPrincipalAuthorization> items = resources.stream().map(resource -> resource
                            .toBuilder()
                            .withMetadata(resource.getMetadata()
                                    .toBuilder()
                                    .withAnnotation(MetadataAnnotations.JIKKOU_IO_KAFKA_CLUSTER_ID, clusterId)
                                    .build()
                            )
                            .build()
                    )
                    .toList();
            return new V1V1KafkaPrincipalAuthorizationList(items);
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
