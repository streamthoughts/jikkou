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
package io.streamthoughts.jikkou.api.manager.kafka;

import io.streamthoughts.jikkou.api.change.AclChange;
import io.streamthoughts.jikkou.api.change.AclChangeComputer;
import io.streamthoughts.jikkou.api.change.AclChangeOptions;
import io.streamthoughts.jikkou.api.change.ChangeExecutor;
import io.streamthoughts.jikkou.api.change.ChangeResult;
import io.streamthoughts.jikkou.api.config.JikkouConfig;
import io.streamthoughts.jikkou.api.error.ConfigException;
import io.streamthoughts.jikkou.api.manager.AclDescribeOptions;
import io.streamthoughts.jikkou.api.manager.KafkaAclsManager;
import io.streamthoughts.jikkou.api.manager.KafkaResourceUpdateContext;
import io.streamthoughts.jikkou.api.model.V1AccessRoleObject;
import io.streamthoughts.jikkou.api.model.V1AccessUserObject;
import io.streamthoughts.jikkou.api.model.V1SecurityObject;
import io.streamthoughts.jikkou.api.model.V1SpecObject;
import io.streamthoughts.jikkou.api.operation.acls.AclOperation;
import io.streamthoughts.jikkou.api.operation.acls.ApplyAclsOperation;
import io.streamthoughts.jikkou.api.operation.acls.CreateAclsOperation;
import io.streamthoughts.jikkou.api.operation.acls.DeleteAclsOperation;
import io.streamthoughts.jikkou.api.resources.Named;
import io.streamthoughts.jikkou.api.resources.acl.AccessControlPolicy;
import io.streamthoughts.jikkou.api.resources.acl.AclRulesBuilder;
import io.streamthoughts.jikkou.api.resources.acl.builder.LiteralAclRulesBuilder;
import io.streamthoughts.jikkou.api.resources.acl.builder.TopicMatchingAclRulesBuilder;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.DescribeAclsResult;
import org.apache.kafka.common.acl.AclBinding;
import org.apache.kafka.common.acl.AclBindingFilter;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class AdminClientKafkaAclsManager implements KafkaAclsManager {

    private AdminClientContext adminClientContext;

    /**
     * Creates a new {@link AdminClientKafkaAclsManager} instance.
     */
    public AdminClientKafkaAclsManager() {
    }

    /**
     * Creates a new {@link AdminClientKafkaAclsManager} instance.
     *
     * @param config the application's configuration.
     */
    public AdminClientKafkaAclsManager(final @NotNull JikkouConfig config) {
        configure(config);
    }

    /**
     * Creates a new {@link AdminClientKafkaAclsManager} instance.
     *
     * @param adminClientContext the {@link AdminClientContext} to use for acquiring a new {@link AdminClient}.
     */
    public AdminClientKafkaAclsManager(final @NotNull AdminClientContext adminClientContext) {
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
    public Collection<ChangeResult<AclChange>> update(final UpdateMode mode,
                                                      final List<V1SpecObject> objects,
                                                      final KafkaResourceUpdateContext<AclChangeOptions> context) {

        return adminClientContext.invokeAndClose((adminClient) -> objects
                .stream()
                .flatMap(spec -> {
                    Optional<V1SecurityObject> optional = spec.security();

                    if (optional.isEmpty()) {
                        return Stream.empty();
                    }

                    final V1SecurityObject resource = optional.get();
                    final AclRulesBuilder builder = AclRulesBuilder.combines(
                            new LiteralAclRulesBuilder(),
                            new TopicMatchingAclRulesBuilder(adminClient));

                    final Map<String, V1AccessRoleObject> groups = Named.keyByName(resource.roles());
                    final Collection<V1AccessUserObject> users = resource.users();

                    List<AccessControlPolicy> expectedStates = users
                            .stream()
                            .flatMap(user -> builder.toAccessControlPolicy(groups.values(), user).stream())
                            .filter(it -> context.getResourceFilter().test(it.name()))
                            .collect(Collectors.toList());

                    // Get the actual state from the cluster.
                    AclDescribeOptions options = new AclDescribeOptions().withResourcePredicate(context.getResourceFilter());
                    List<AccessControlPolicy> actualStates = doDescribe(adminClient, options);

                    // Compute state changes
                    Supplier<List<AclChange>> supplier = () -> new AclChangeComputer().
                            computeChanges(
                                    actualStates,
                                    expectedStates,
                                    context.getOptions()
                            );

                    return ChangeExecutor.ofSupplier(supplier)
                            .execute(getOperationFor(mode), context.isDryRun()).stream();

                }).collect(Collectors.toList()));
    }

    @NotNull
    private List<AccessControlPolicy> doDescribe(final AdminClient adminClient,
                                                 final AclDescribeOptions options) {
        return new DescribeACLs(adminClient)
                .describe()
                .stream()
                .filter(it -> options.resourcePredicate().test(it.name()))
                .collect(Collectors.toList());
    }

    public AclOperation getOperationFor(@NotNull final UpdateMode mode) {
        return switch (mode) {
            case CREATE_ONLY -> new CreateAclsOperation(adminClientContext.current());
            case DELETE_ONLY -> new DeleteAclsOperation(adminClientContext.current());
            case APPLY -> new ApplyAclsOperation(adminClientContext.current());
            default -> throw new UnsupportedOperationException();
        };
    }

    @Override
    public List<V1AccessUserObject> describe(final AclDescribeOptions options) {

        return adminClientContext.invokeAndClose((adminClient) -> {
            Collection<AccessControlPolicy> policies = doDescribe(adminClient, options);
            AclRulesBuilder builder = AclRulesBuilder.combines(
                    new LiteralAclRulesBuilder(),
                    new TopicMatchingAclRulesBuilder(adminClient));

            return builder.toAccessUserObjects(policies);
        });
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
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException("Failed to describe ACLs", e);
            }
        }

        private List<AccessControlPolicy> toAccessControlPolicy(final Collection<AclBinding> bindings) {
            return bindings.stream()
                    .map(binding -> AccessControlPolicy.newBuilder()
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
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }
}
