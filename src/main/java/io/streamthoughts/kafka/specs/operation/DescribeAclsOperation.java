/*
 * Copyright 2020 StreamThoughts.
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
package io.streamthoughts.kafka.specs.operation;

import io.streamthoughts.kafka.specs.acl.AclRule;
import io.streamthoughts.kafka.specs.resources.ResourcesIterable;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.DescribeAclsResult;
import org.apache.kafka.common.acl.AclBinding;
import org.apache.kafka.common.acl.AclBindingFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class DescribeAclsOperation implements ClusterOperation<ResourcesIterable<AclRule>, ResourceOperationOptions, Collection<AclRule>> {

    private static final Logger LOG = LoggerFactory.getLogger(DescribeAclsOperation.class);

    private AdminClient client;

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<AclRule> execute(final AdminClient client,
                                       final ResourcesIterable<AclRule> resource,
                                       final ResourceOperationOptions options) {
        return executeAsync(client, resource, options).join();
    }

    public CompletableFuture<List<AclRule>> executeAsync(final AdminClient client,
                                                         final ResourcesIterable<AclRule> resource,
                                                         final ResourceOperationOptions options) {
        this.client = client;
        return describeAllAcls()
                .thenApply(this::toAclRules)
                .exceptionally(e -> {
                    LOG.warn(e.getCause().getMessage());
                    return Collections.emptyList();
                });
    }

    private List<AclRule> toAclRules(final Collection<AclBinding> bindings) {
        return bindings.stream()
        .map(binding -> AclRule.newBuilder()
         .withPrincipal(binding.entry().principal())
         .withPermission(binding.entry().permissionType())
         .withHost(binding.entry().host())
         .withhOperation(binding.entry().operation())
         .withResourceType(binding.pattern().resourceType())
         .withResourcePattern(binding.pattern().name())
         .withPatternType(binding.pattern().patternType())
         .build())
        .collect(Collectors.toList());
    }


    private CompletableFuture<Collection<AclBinding>> describeAllAcls() {
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
