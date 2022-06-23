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
package io.streamthoughts.jikkou.kafka.control.operation.acls;

import io.streamthoughts.jikkou.api.control.ChangeType;
import io.streamthoughts.jikkou.api.control.Description;
import io.streamthoughts.jikkou.kafka.control.change.AclChange;
import io.streamthoughts.jikkou.kafka.model.AccessControlPolicy;
import io.streamthoughts.jikkou.utils.DescriptionProvider;
import io.vavr.Tuple2;
import io.vavr.concurrent.Future;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.CreateAclsResult;
import org.apache.kafka.common.KafkaFuture;
import org.apache.kafka.common.acl.AccessControlEntry;
import org.apache.kafka.common.acl.AclBinding;
import org.apache.kafka.common.resource.ResourcePattern;
import org.jetbrains.annotations.NotNull;

public class CreateAclsOperation implements AclOperation {

    public static final DescriptionProvider<AccessControlPolicy> DESCRIPTION = (r) -> (Description.Create) () -> {
        return String.format("Create a new ACL (%s %s to %s %s:%s:%s)",
                r.permission(),
                r.principal(),
                r.operation(),
                r.resourceType(),
                r.patternType(),
                r.resourcePattern()
        );
    };

    private final AclBindingConverter converter = new AclBindingConverter();

    private final AdminClient adminClient;

    /**
     * Creates a new {@link CreateAclsOperation} instance.
     *
     * @param adminClient the {@link AdminClient}.
     */
    public CreateAclsOperation(@NotNull final AdminClient adminClient) {
        this.adminClient = Objects.requireNonNull(adminClient, "'adminClient should not be null'");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Description getDescriptionFor(@NotNull final AclChange change) {
        return DESCRIPTION.getForResource(change.getAccessControlPolicy());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean test(final AclChange change) {
        return change.getChange() == ChangeType.ADD;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull Map<AccessControlPolicy, List<Future<Void>>> doApply(@NotNull final Collection<AclChange> changes) {
        List<AclBinding> bindings = changes
                .stream()
                .peek(this::verify)
                .map(AclChange::getAccessControlPolicy)
                .map(converter::toAclBinding)
                .collect(Collectors.toList());

        CreateAclsResult result = adminClient.createAcls(bindings);

        Map<AclBinding, KafkaFuture<Void>> kafkaResults = result.values();

        return kafkaResults.entrySet()
                .stream()
                .map(e -> new Tuple2<>(converter.fromAclBinding(e.getKey()), e.getValue()))
                .map(t -> t.map2(Future::fromJavaFuture))
                .map(t -> t.map2(List::of))
                .collect(Collectors.toMap(Tuple2::_1, Tuple2::_2));
    }

    private void verify(final @NotNull AclChange change) {
        if (!test(change)) {
            throw new IllegalArgumentException("This operation does not support the passed change: " + change);
        }
    }

    private static class AclBindingConverter {

        AclBinding toAclBinding(final AccessControlPolicy rule) {
            return new AclBinding(
                    new ResourcePattern(rule.resourceType(), rule.resourcePattern(), rule.patternType()),
                    new AccessControlEntry(rule.principal(), rule.host(), rule.operation(), rule.permission())
            );
        }

        AccessControlPolicy fromAclBinding(final AclBinding binding) {
            final ResourcePattern pattern = binding.pattern();
            return AccessControlPolicy.newBuilder()
                    .withResourcePattern(pattern.name())
                    .withPatternType(pattern.patternType())
                    .withResourceType(pattern.resourceType())
                    .withOperation(binding.entry().operation())
                    .withPermission(binding.entry().permissionType())
                    .withHost(binding.entry().host())
                    .withPrincipal(binding.entry().principal())
                    .build();
        }
    }
}
