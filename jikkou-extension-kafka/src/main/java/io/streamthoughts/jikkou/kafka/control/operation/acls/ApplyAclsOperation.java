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

import io.streamthoughts.jikkou.api.control.ChangeDescription;
import io.streamthoughts.jikkou.api.control.ChangeType;
import io.streamthoughts.jikkou.kafka.control.change.AclChange;
import io.streamthoughts.jikkou.kafka.model.AccessControlPolicy;
import io.vavr.concurrent.Future;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.apache.kafka.clients.admin.AdminClient;
import org.jetbrains.annotations.NotNull;

public class ApplyAclsOperation implements AclOperation {

    final CreateAclsOperation create;
    final DeleteAclsOperation delete;

    /**
     * Creates a new {@link ApplyAclsOperation} instance.
     *
     * @param adminClient the {@link AdminClient}.
     */
    public ApplyAclsOperation(@NotNull final AdminClient adminClient) {
        Objects.requireNonNull(adminClient, "'adminClient should not be null'");
        this.create = new CreateAclsOperation(adminClient);
        this.delete = new DeleteAclsOperation(adminClient);
    }

    /** {@inheritDoc} */
    @Override
    public ChangeDescription getDescriptionFor(final @NotNull AclChange change) {
        return switch (change.getChange()) {
            case ADD -> create.getDescriptionFor(change);
            case DELETE -> delete.getDescriptionFor(change);
            case NONE -> new AclChangeDescription(change);
            default -> throw new UnsupportedOperationException("Unsupported operation type: " + change.getChange());
        };
    }

    /** {@inheritDoc} */
    @Override
    public boolean test(final AclChange change) {
        return List.of(ChangeType.ADD, ChangeType.DELETE).contains(change.getChange());
    }

    /** {@inheritDoc} */
    @Override
    public @NotNull Map<AccessControlPolicy, List<Future<Void>>> doApply(@NotNull Collection<AclChange> changes) {
        HashMap<AccessControlPolicy, List<Future<Void>>> results = new HashMap<>();
        results.putAll(delete.apply(changes));
        results.putAll(create.apply(changes));
        return results;
    }
}
