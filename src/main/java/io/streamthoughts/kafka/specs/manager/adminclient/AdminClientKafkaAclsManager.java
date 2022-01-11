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
package io.streamthoughts.kafka.specs.manager.adminclient;

import io.streamthoughts.kafka.specs.change.AclChange;
import io.streamthoughts.kafka.specs.change.AclChangeComputer;
import io.streamthoughts.kafka.specs.change.AclChangeOptions;
import io.streamthoughts.kafka.specs.change.ChangeExecutor;
import io.streamthoughts.kafka.specs.change.ChangeResult;
import io.streamthoughts.kafka.specs.config.JikkouConfig;
import io.streamthoughts.kafka.specs.error.ConfigException;
import io.streamthoughts.kafka.specs.manager.AclDescribeOptions;
import io.streamthoughts.kafka.specs.manager.KafkaAclsManager;
import io.streamthoughts.kafka.specs.manager.KafkaResourceOperationContext;
import io.streamthoughts.kafka.specs.model.V1AccessRoleObject;
import io.streamthoughts.kafka.specs.model.V1AccessUserObject;
import io.streamthoughts.kafka.specs.model.V1SecurityObject;
import io.streamthoughts.kafka.specs.model.V1SpecsObject;
import io.streamthoughts.kafka.specs.operation.acls.AclOperation;
import io.streamthoughts.kafka.specs.operation.acls.ApplyAclsOperation;
import io.streamthoughts.kafka.specs.operation.acls.CreateAclsOperation;
import io.streamthoughts.kafka.specs.operation.acls.DeleteAclsOperation;
import io.streamthoughts.kafka.specs.resources.Named;
import io.streamthoughts.kafka.specs.resources.acl.AccessControlPolicy;
import io.streamthoughts.kafka.specs.resources.acl.AclRulesBuilder;
import io.streamthoughts.kafka.specs.resources.acl.builder.LiteralAclRulesBuilder;
import io.streamthoughts.kafka.specs.resources.acl.builder.TopicMatchingAclRulesBuilder;
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
     * {@inheritDoc}
     */
    @Override
    public void configure(@NotNull JikkouConfig config) throws ConfigException {
        adminClientContext = new AdminClientContext(config);
    }

    @Override
    public Collection<ChangeResult<AclChange>> update(final UpdateMode mode,
                                                      final List<V1SpecsObject> objects,
                                                      final KafkaResourceOperationContext<AclChangeOptions> context) {

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
                            .filter(it -> context.getResourcePredicate().test(it.name()))
                            .collect(Collectors.toList());

                    // Get the actual state from the cluster.
                    AclDescribeOptions options = new AclDescribeOptions().withResourcePredicate(context.getResourcePredicate());
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
            case CREATE -> new CreateAclsOperation(adminClientContext.current());
            case DELETE -> new DeleteAclsOperation(adminClientContext.current());
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
    public final class DescribeACLs {

        private final AdminClient client;

        /**
         * Creates a new {@link DescribeACLs} instance.
         *
         * @param client the {@link AdminClient}.
         */
        public DescribeACLs(final AdminClient client) {
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
