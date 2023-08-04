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
package io.streamthoughts.jikkou.kafka.control;

import io.streamthoughts.jikkou.annotation.AcceptsResource;
import io.streamthoughts.jikkou.api.config.Configuration;
import io.streamthoughts.jikkou.api.control.ResourceCollector;
import io.streamthoughts.jikkou.api.error.JikkouRuntimeException;
import io.streamthoughts.jikkou.api.selector.AggregateSelector;
import io.streamthoughts.jikkou.api.selector.ResourceSelector;
import io.streamthoughts.jikkou.kafka.AdminClientContext;
import io.streamthoughts.jikkou.kafka.MetadataAnnotations;
import io.streamthoughts.jikkou.kafka.adapters.KafkaAclBindingAdapter;
import io.streamthoughts.jikkou.kafka.adapters.V1KafkaPrincipalAuthorizationSupport;
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

@AcceptsResource(type = V1KafkaPrincipalAuthorization.class)
public final class AdminClientKafkaAclCollector extends AbstractAdminClientKafkaController
        implements ResourceCollector<V1KafkaPrincipalAuthorization> {

    /**
     * Creates a new {@link AdminClientKafkaAclCollector} instance.
     */
    public AdminClientKafkaAclCollector() {
        super();
    }

    /**
     * Creates a new {@link AdminClientKafkaAclCollector} instance.
     *
     * @param config the application's configuration.
     */
    public AdminClientKafkaAclCollector(final @NotNull Configuration config) {
        super(config);
    }

    /**
     * Creates a new {@link AdminClientKafkaAclCollector} instance.
     *
     * @param adminClientContext the {@link AdminClientContext} to use for acquiring a new {@link AdminClient}.
     */
    public AdminClientKafkaAclCollector(final @NotNull AdminClientContext adminClientContext) {
        super(adminClientContext);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<V1KafkaPrincipalAuthorization> listAll(@NotNull final Configuration configuration,
                                                       @NotNull final List<ResourceSelector> selectors) {

        KafkaFunction<List<V1KafkaPrincipalAuthorization>> function = client -> {
            return listAll(client)
                    .stream()
                    .filter(new AggregateSelector(selectors)::apply)
                    .toList();
        };

        List<V1KafkaPrincipalAuthorization> resources = adminClientContext.invoke(function);

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
