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
package io.streamthoughts.kafka.specs.command.quotas.subcommands;

import io.streamthoughts.kafka.specs.Description;
import io.streamthoughts.kafka.specs.change.Change;
import io.streamthoughts.kafka.specs.change.QuotaChange;
import io.streamthoughts.kafka.specs.change.QuotaChanges;
import io.streamthoughts.kafka.specs.command.quotas.QuotasCommand;
import io.streamthoughts.kafka.specs.internal.DescriptionProvider;
import io.streamthoughts.kafka.specs.operation.quotas.AlterQuotasOperation;
import io.streamthoughts.kafka.specs.operation.quotas.CreateQuotasOperation;
import io.streamthoughts.kafka.specs.operation.quotas.DeleteQuotasOperation;
import io.streamthoughts.kafka.specs.operation.quotas.QuotaOperation;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.common.KafkaFuture;
import org.apache.kafka.common.quota.ClientQuotaEntity;
import org.jetbrains.annotations.NotNull;
import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.streamthoughts.kafka.specs.change.Change.OperationType.ADD;
import static io.streamthoughts.kafka.specs.change.Change.OperationType.DELETE;
import static io.streamthoughts.kafka.specs.change.Change.OperationType.UPDATE;

@Command(name = "apply",
         description = "Apply all changes to the Kafka client quotas."
)
public class Apply extends QuotasCommand.Base {

    @CommandLine.Option(
            names = "--delete-config-orphans",
            defaultValue = "false",
            description = "Delete config entries overridden on the cluster but absent from the specification file"
    )
    Boolean deleteConfigOrphans;

    @CommandLine.Option(
            names = "--delete-quota-orphans",
            defaultValue = "false",
            description = "Delete client-quotas which exist on the cluster but absent from the specification files"
    )
    Boolean deleteQuotaOrphans;

    public static DescriptionProvider<QuotaChange> DESCRIPTION = (resource -> {
        return (Description.Create) () -> String.format("Unchanged client-quotas %s %s",
                resource.getType(),
                resource.getType().toPettyString(resource.getEntity())
        );
    });

    /**
     * {@inheritDoc}
     */
    @Override
    public QuotaOperation getOperation(@NotNull final AdminClient client) {
        return new QuotaOperation() {
            final CreateQuotasOperation create = new CreateQuotasOperation(client);
            final AlterQuotasOperation alter = new AlterQuotasOperation(client, deleteConfigOrphans);
            final DeleteQuotasOperation delete = new DeleteQuotasOperation(client);

            @Override
            public Description getDescriptionFor(final @NotNull QuotaChange change) {
                switch (change.getOperation()) {
                    case ADD:
                        return create.getDescriptionFor(change);
                    case UPDATE:
                        return alter.getDescriptionFor(change);
                    case DELETE:
                        return delete.getDescriptionFor(change);
                    case NONE:
                        return DESCRIPTION.getForResource(change);
                    default:
                        throw new UnsupportedOperationException("Unsupported operation type: " + change.getOperation());
                }
            }

            @Override
            public boolean test(@NotNull final QuotaChange change) {
                Change.OperationType operation = change.getOperation();
                return (operation == DELETE && deleteQuotaOrphans) || List.of(ADD, UPDATE).contains(operation);
            }

            @Override
            public @NotNull Map<ClientQuotaEntity, KafkaFuture<Void>> apply(@NotNull QuotaChanges changes) {
                HashMap<ClientQuotaEntity, KafkaFuture<Void>> results = new HashMap<>();
                if (deleteQuotaOrphans) {
                    results.putAll(delete.apply(changes));
                }
                results.putAll(create.apply(changes));
                results.putAll(alter.apply(changes));
                return results;
            }
        };
    }
}
