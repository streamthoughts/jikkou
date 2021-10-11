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
package io.streamthoughts.kafka.specs.command.acls.subcommands;

import io.streamthoughts.kafka.specs.Description;
import io.streamthoughts.kafka.specs.resources.acl.AccessControlPolicy;
import io.streamthoughts.kafka.specs.change.AclChange;
import io.streamthoughts.kafka.specs.change.AclChanges;
import io.streamthoughts.kafka.specs.command.acls.AclsCommand;
import io.streamthoughts.kafka.specs.operation.AclOperation;
import io.streamthoughts.kafka.specs.operation.CreateAclsOperation;
import io.streamthoughts.kafka.specs.operation.DeleteAclsOperation;
import org.apache.kafka.clients.admin.AdminClient;
import org.jetbrains.annotations.NotNull;
import picocli.CommandLine.Command;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Command(name = "apply",
         description = "Apply all ACL changes on remote cluster."
)
public class Apply extends AclsCommand.Base {

    /**
     * {@inheritDoc}
     */
    @Override
    public AclOperation getOperation(final AdminClient client) {
        return new AclOperation() {
            final CreateAclsOperation create = new CreateAclsOperation(client);
            final DeleteAclsOperation delete = new DeleteAclsOperation(client);

            @Override
            public Description getDescriptionFor(final @NotNull AclChange change) {
                switch (change.getOperation()) {
                    case ADD:
                        return create.getDescriptionFor(change);
                    case DELETE:
                        return delete.getDescriptionFor(change);
                    default:
                        throw new UnsupportedOperationException("Unsupported operation type: " + change.getOperation());
                }
            }

            @Override
            public boolean test(final AclChange change) {
                return true;
            }

            @Override
            public Map<AccessControlPolicy, CompletableFuture<Void>> apply(final @NotNull AclChanges changes) {
                HashMap<AccessControlPolicy, CompletableFuture<Void>> results = new HashMap<>();
                results.putAll(changes.apply(delete));
                results.putAll(changes.apply(create));
                return results;
            }
        };
    }
}
