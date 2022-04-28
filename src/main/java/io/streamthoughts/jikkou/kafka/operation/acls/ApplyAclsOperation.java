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
package io.streamthoughts.jikkou.kafka.operation.acls;

import io.streamthoughts.jikkou.kafka.Description;
import io.streamthoughts.jikkou.kafka.change.AclChange;
import io.streamthoughts.jikkou.kafka.change.Change;
import io.streamthoughts.jikkou.kafka.internal.DescriptionProvider;
import io.streamthoughts.jikkou.kafka.resources.acl.AccessControlPolicy;
import io.vavr.concurrent.Future;
import org.apache.kafka.clients.admin.AdminClient;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ApplyAclsOperation implements AclOperation {

    public static final DescriptionProvider<AccessControlPolicy> DESCRIPTION = (r) -> (Description.None) () -> {
        return String.format("Unchanged ACL (%s %s to %s %s:%s:%s)",
                r.permission(),
                r.principal(),
                r.operation(),
                r.resourceType(),
                r.patternType(),
                r.resourcePattern());
    };

    final CreateAclsOperation create;
    final DeleteAclsOperation delete;

    /**
     * Creates a new {@link ApplyAclsOperation} instance.
     *
     * @param adminClient   the {@link AdminClient}.
     */
    public ApplyAclsOperation(@NotNull final AdminClient adminClient) {
        Objects.requireNonNull(adminClient, "'adminClient should not be null'");
        this.create = new CreateAclsOperation(adminClient);
        this.delete = new DeleteAclsOperation(adminClient);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Description getDescriptionFor(final @NotNull AclChange change) {
        return switch (change.getOperation()) {
            case ADD -> create.getDescriptionFor(change);
            case DELETE -> delete.getDescriptionFor(change);
            case NONE -> DESCRIPTION.getForResource(change.getAccessControlPolicy());
            default -> throw new UnsupportedOperationException("Unsupported operation type: " + change.getOperation());
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean test(final AclChange change) {
        return List.of(Change.OperationType.ADD, Change.OperationType.DELETE).contains(change.getOperation());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull Map<AccessControlPolicy, List<Future<Void>>> doApply(@NotNull Collection<AclChange> changes) {
        HashMap<AccessControlPolicy, List<Future<Void>>> results = new HashMap<>();
        results.putAll(delete.apply(changes));
        results.putAll(create.apply(changes));
        return results;
    }
}
