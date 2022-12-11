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
import io.streamthoughts.jikkou.api.error.JikkouException;
import io.streamthoughts.jikkou.api.model.Nameable;
import io.streamthoughts.jikkou.api.model.ObjectMeta;
import io.streamthoughts.jikkou.kafka.AdminClientContext;
import io.streamthoughts.jikkou.kafka.control.change.AclChange;
import io.streamthoughts.jikkou.kafka.control.change.AclChangeComputer;
import io.streamthoughts.jikkou.kafka.control.change.KafkaAclReconciliationConfig;
import io.streamthoughts.jikkou.kafka.control.operation.acls.AclRulesBuilder;
import io.streamthoughts.jikkou.kafka.control.operation.acls.ApplyAclsOperation;
import io.streamthoughts.jikkou.kafka.control.operation.acls.CreateAclsOperation;
import io.streamthoughts.jikkou.kafka.control.operation.acls.DeleteAclsOperation;
import io.streamthoughts.jikkou.kafka.control.operation.acls.builder.LiteralAclRulesBuilder;
import io.streamthoughts.jikkou.kafka.control.operation.acls.builder.TopicMatchingAclRulesBuilder;
import io.streamthoughts.jikkou.kafka.model.AccessControlPolicy;
import io.streamthoughts.jikkou.kafka.models.V1KafkaAccessRoleObject;
import io.streamthoughts.jikkou.kafka.models.V1KafkaAccessUserObject;
import io.streamthoughts.jikkou.kafka.models.V1KafkaAuthorizationList;
import io.streamthoughts.jikkou.kafka.models.V1KafkaAuthorizationSpec;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.DescribeAclsResult;
import org.apache.kafka.common.acl.AclBinding;
import org.apache.kafka.common.acl.AclBindingFilter;
import org.jetbrains.annotations.NotNull;

@AcceptResource(type = V1KafkaAuthorizationList.class)
public final class AdminClientKafkaAuthorizationController extends AdminClientKafkaController
        implements ResourceController<V1KafkaAuthorizationList, AclChange> {

    private static final AclChangeComputer COMPUTER = new AclChangeComputer();

    /**
     * Creates a new {@link AdminClientKafkaAuthorizationController} instance.
     */
    public AdminClientKafkaAuthorizationController() {
        super();
    }

    /**
     * Creates a new {@link AdminClientKafkaAuthorizationController} instance.
     *
     * @param config the application's configuration.
     */
    public AdminClientKafkaAuthorizationController(final @NotNull Configuration config) {
        super(config);
    }

    /**
     * Creates a new {@link AdminClientKafkaAuthorizationController} instance.
     *
     * @param adminClientContext the {@link AdminClientContext} to use for acquiring a new {@link AdminClient}.
     */
    public AdminClientKafkaAuthorizationController(final @NotNull AdminClientContext adminClientContext) {
        super(adminClientContext);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public V1KafkaAuthorizationList describe(@NotNull final Configuration configuration,
                                             @NotNull final ResourceFilter filter) {

        KafkaFunction<List<V1KafkaAccessUserObject>> function = client -> {
            List<AccessControlPolicy> policies = loadAccessControlPolicies(client, filter);

            var builder = AclRulesBuilder.combines(
                    new LiteralAclRulesBuilder(),
                    new TopicMatchingAclRulesBuilder(client));

            return builder.toAccessUserObjects(policies);
        };

        List<V1KafkaAccessUserObject> users = adminClientContext.invoke(function);

        return new V1KafkaAuthorizationList().toBuilder()
                .withMetadata(ObjectMeta
                        .builder()
                        .withAnnotation("jikkou.io/kafka-cluster-id", adminClientContext.getClusterId())
                        .build()
                )
                .withSpec(V1KafkaAuthorizationSpec
                        .builder()
                        .withUsers(users)
                        .build()
                )
                .build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public KafkaAclReconciliationConfig defaultConciliationConfig() {
        return new KafkaAclReconciliationConfig();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<AclChange> computeReconciliationChanges(@NotNull V1KafkaAuthorizationList resource,
                                                        @NotNull ReconciliationContext context) {


        AdminClient adminClient = adminClientContext.client();

        final AclRulesBuilder builder = AclRulesBuilder.combines(
                new LiteralAclRulesBuilder(),
                new TopicMatchingAclRulesBuilder(adminClient)
        );

        final Map<String, V1KafkaAccessRoleObject> groups = Nameable.keyByName(resource.getSpec().getRoles());
        final Collection<V1KafkaAccessUserObject> users = resource.getSpec().getUsers();

        // Get the list of remote resources that are candidates for this reconciliation
        List<AccessControlPolicy> expectedStates = users
                .stream()
                .flatMap(user -> builder.toAccessControlPolicy(groups.values(), user).stream())
                .filter(context.filter()::apply)
                .collect(Collectors.toList());

        // Get the actual state from the cluster.
        List<AccessControlPolicy> actualStates = loadAccessControlPolicies(adminClient, context.filter());

        // Compute state changes
        return COMPUTER.computeChanges(
                actualStates,
                expectedStates,
                new KafkaAclReconciliationConfig(context.configuration())
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<ChangeResult<AclChange>> create(@NotNull List<AclChange> changes, boolean dryRun) {
        var operation = new CreateAclsOperation(adminClientContext.client());
        return ChangeExecutor.ofSupplier(() -> changes).execute(operation, dryRun);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<ChangeResult<AclChange>> update(@NotNull List<AclChange> changes, boolean dryRun) {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<ChangeResult<AclChange>> delete(@NotNull List<AclChange> changes, boolean dryRun) {
        var operation = new DeleteAclsOperation(adminClientContext.client());
        return ChangeExecutor.ofSupplier(() -> changes).execute(operation, dryRun);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<ChangeResult<AclChange>> apply(@NotNull List<AclChange> changes, boolean dryRun) {
        var operation = new ApplyAclsOperation(adminClientContext.client());
        return ChangeExecutor.ofSupplier(() -> changes).execute(operation, dryRun);
    }

    @NotNull
    private List<AccessControlPolicy> loadAccessControlPolicies(final AdminClient client,
                                                                final ResourceFilter filter) {
        return new DescribeACLs(client)
                .describe()
                .stream()
                .filter(filter::apply)
                .collect(Collectors.toList());
    }

    /**
     * {@link Function} to list all ACLs rules.
     */
    public static final class DescribeACLs {

        private final AdminClient client;

        /**
         * Creates a new {@link DescribeACLs} instance.
         *
         * @param client the {@link AdminClient}.
         */
        public DescribeACLs(@NotNull final AdminClient client) {
            this.client = client;
        }

        public Collection<AccessControlPolicy> describe() {
            try {
                return describeAcls().thenApply(this::toAccessControlPolicy).get();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new JikkouException("Failed to describe ACL due to thread-interruption", e);
            } catch (ExecutionException e) {
                throw new JikkouException("Failed to describe ACL due to execution error", e);
            }
        }

        private List<AccessControlPolicy> toAccessControlPolicy(final Collection<AclBinding> bindings) {
            return bindings.stream()
                    .map(binding -> AccessControlPolicy.builder()
                            .withPrincipal(binding.entry().principal())
                            .withPermission(binding.entry().permissionType())
                            .withHost(binding.entry().host())
                            .withOperation(binding.entry().operation())
                            .withResourceType(binding.pattern().resourceType())
                            .withResourcePattern(binding.pattern().name())
                            .withPatternType(binding.pattern().patternType())
                            .build())
                    .collect(Collectors.toList());
        }

        private CompletableFuture<Collection<AclBinding>> describeAcls() {
            return CompletableFuture.supplyAsync(() -> {
                try {
                    DescribeAclsResult result = this.client.describeAcls(AclBindingFilter.ANY);
                    return result.values().get();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new JikkouException(e);
                } catch (ExecutionException e) {
                    throw new JikkouException(e);
                }
            });
        }
    }
}
