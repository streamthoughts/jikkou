/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.kafka.change.acl;

import io.streamthoughts.jikkou.common.utils.Pair;
import io.streamthoughts.jikkou.core.data.TypeConverter;
import io.streamthoughts.jikkou.core.models.change.GenericStateChange;
import io.streamthoughts.jikkou.core.models.change.ResourceChange;
import io.streamthoughts.jikkou.core.models.change.SpecificStateChange;
import io.streamthoughts.jikkou.core.reconciler.ChangeMetadata;
import io.streamthoughts.jikkou.core.reconciler.ChangeResponse;
import io.streamthoughts.jikkou.core.reconciler.Operation;
import io.streamthoughts.jikkou.core.reconciler.TextDescription;
import io.streamthoughts.jikkou.core.reconciler.change.BaseChangeHandler;
import io.streamthoughts.jikkou.kafka.model.KafkaAclBinding;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.CreateAclsResult;
import org.apache.kafka.clients.admin.DeleteAclsResult;
import org.apache.kafka.common.acl.AclBinding;
import org.apache.kafka.common.acl.AclBindingFilter;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public final class AclChangeHandler extends BaseChangeHandler<ResourceChange> {

    private final AdminClient client;

    /**
     * Creates a new {@link AclChangeHandler} instance.
     *
     * @param client the {@link AdminClient}.
     */
    public AclChangeHandler(@NotNull final AdminClient client) {
        super(Set.of(Operation.CREATE, Operation.UPDATE, Operation.DELETE));
        this.client = Objects.requireNonNull(client, "client cannot not be null");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ChangeResponse<ResourceChange>> handleChanges(@NotNull final List<ResourceChange> changes) {
        return changes.stream()
                .map(change -> {
                    Map<Operation, List<SpecificStateChange<KafkaAclBinding>>> changesByOperation = change
                        .getSpec()
                        .getChanges()
                        .get(AclChangeComputer.ACL)
                        .all(TypeConverter.of(KafkaAclBinding.class))
                        .stream()
                        .collect(Collectors.groupingBy(GenericStateChange::getOp));

                    List<AclBinding> bindingsForCreate = changesByOperation
                        .getOrDefault(Operation.CREATE, List.of())
                        .stream()
                        .map(SpecificStateChange::getAfter)
                        .map(KafkaAclBinding::toAclBinding)
                        .toList();

                    List<AclBindingFilter> bindingsForDelete = changesByOperation
                        .getOrDefault(Operation.DELETE, List.of())
                        .stream()
                        .map(SpecificStateChange::getBefore)
                        .map(KafkaAclBinding::toAclBindingFilter)
                        .toList();

                    List<CompletableFuture<ChangeMetadata>> futures = new LinkedList<>();

                    if (!bindingsForCreate.isEmpty()) {
                        CreateAclsResult resultForCreate = client.createAcls(bindingsForCreate);
                        List<CompletableFuture<ChangeMetadata>> completableFutures = resultForCreate
                            .values()
                            .entrySet()
                            .stream()
                            .map(e -> Pair.of(e.getKey(), e.getValue()))
                            .map(pair -> pair._2()
                                .toCompletionStage()
                                .toCompletableFuture()
                                .thenApply(ignore -> ChangeMetadata.empty())

                            ).toList();
                        futures.addAll(completableFutures);
                    }

                    if (!bindingsForDelete.isEmpty()) {
                        DeleteAclsResult resultForDelete = client.deleteAcls(bindingsForDelete);
                        List<CompletableFuture<ChangeMetadata>> completableFutures = resultForDelete
                            .values()
                            .entrySet()
                            .stream()
                            .map(e -> Pair.of(e.getKey(), e.getValue()))
                            .map(pair -> pair._2()
                                .toCompletionStage()
                                .toCompletableFuture()
                                .thenApply(ignore -> ChangeMetadata.empty())

                            )
                            .toList();
                    }
                    return new ChangeResponse<>(change, futures);
                })
                .toList();
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public TextDescription describe(@NotNull ResourceChange change) {
        return new KafkaPrincipalAuthorizationDescription(change);
    }
}
