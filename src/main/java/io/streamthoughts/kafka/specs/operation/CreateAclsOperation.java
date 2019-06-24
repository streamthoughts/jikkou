/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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

import io.streamthoughts.kafka.specs.Description;
import io.streamthoughts.kafka.specs.OperationResult;
import io.streamthoughts.kafka.specs.acl.AclRule;
import io.streamthoughts.kafka.specs.internal.DescriptionProvider;
import io.streamthoughts.kafka.specs.resources.ResourcesIterable;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.CreateAclsResult;
import org.apache.kafka.common.KafkaFuture;
import org.apache.kafka.common.acl.AccessControlEntry;
import org.apache.kafka.common.acl.AclBinding;
import org.apache.kafka.common.resource.ResourcePattern;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class CreateAclsOperation extends AbstractOperation<AclRule, ResourceOperationOptions> {

    public static final DescriptionProvider<AclRule> DESCRIPTION = (r) -> (Description.Create) () -> {
        return String.format("Create a new ACL (%s %s to %s %s:%s:%s)",
                r.permission(),
                r.principal(),
                r.operation(),
                r.resourceType(),
                r.patternType(),
                r.resourcePattern());
    };

    private final AclBindingConverter converter = new AclBindingConverter();

    /**
     * {@inheritDoc}
     */
    @Override
    Description getDescriptionFor(final AclRule resource) {
        return DESCRIPTION.getForResource(resource);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<OperationResult<AclRule>> execute(final AdminClient client,
                                                        final ResourcesIterable<AclRule> rules,
                                                        final ResourceOperationOptions options) {

        Collection<CompletableFuture<OperationResult<AclRule>>> futures =  new LinkedList<>();

        List<AclBinding> bindings = rules.originalCollections()
                .stream()
                .map(converter::toAclBinding)
                .collect(Collectors.toList());

        CreateAclsResult result = client.createAcls(bindings);

        Map<AclBinding, KafkaFuture<Void>> values = result.values();

        futures.addAll(values.entrySet()
                .stream()
                .map(entry -> {
                    final KafkaFuture<Void> future = entry.getValue();
                    return makeCompletableFuture(future, converter.fromAclBinding(entry.getKey()));
                }).collect(Collectors.toList()));
        
        return futures
                .stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());
    }

    private static class AclBindingConverter {

        AclBinding toAclBinding(final AclRule rule) {
            return new AclBinding(
                    new ResourcePattern(rule.resourceType(), rule.resourcePattern(), rule.patternType()),
                    new AccessControlEntry(rule.principal(), rule.host(), rule.operation(), rule.permission())
            );
        }

        AclRule fromAclBinding(final AclBinding binding) {
            String principal = binding.entry().principal();
            String[] principalTypeAndName = principal.split(":");
            ResourcePattern pattern = binding.pattern();
            return AclRule.newBuilder()
                    .withResourcePattern(pattern.name())
                    .withPatternType(pattern.patternType())
                    .withResourceType(pattern.resourceType())
                    .withhOperation(binding.entry().operation())
                    .withPermission(binding.entry().permissionType())
                    .withHost(binding.entry().host())
                    .withPrincipalName(principalTypeAndName[1])
                    .withPrincipalType(principalTypeAndName[0])
                    .build();
        }
    }
}
