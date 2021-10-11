/*
 * Copyright 2021 StreamThoughts.
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
package io.streamthoughts.kafka.specs.command.acls.subcommands.internal;

import io.streamthoughts.kafka.specs.resources.acl.AccessControlPolicy;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.DescribeAclsResult;
import org.apache.kafka.common.acl.AclBinding;
import org.apache.kafka.common.acl.AclBindingFilter;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * {@link Function} to list all ACLs rules.
 */
public class DescribeACLs {

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
