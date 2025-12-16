/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.kafka.change.user;

import static io.streamthoughts.jikkou.core.reconciler.Operation.CREATE;
import static io.streamthoughts.jikkou.core.reconciler.Operation.DELETE;
import static io.streamthoughts.jikkou.core.reconciler.Operation.UPDATE;

import io.streamthoughts.jikkou.common.utils.CollectionUtils;
import io.streamthoughts.jikkou.common.utils.Pair;
import io.streamthoughts.jikkou.core.models.change.ResourceChange;
import io.streamthoughts.jikkou.core.models.change.StateChange;
import io.streamthoughts.jikkou.core.models.change.StateChangeList;
import io.streamthoughts.jikkou.core.reconciler.ChangeMetadata;
import io.streamthoughts.jikkou.core.reconciler.ChangeResponse;
import io.streamthoughts.jikkou.core.reconciler.TextDescription;
import io.streamthoughts.jikkou.core.reconciler.change.BaseChangeHandler;
import io.streamthoughts.jikkou.kafka.model.user.V1KafkaUserAuthentication;
import io.streamthoughts.jikkou.kafka.reconciler.service.KafkaUserService;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AlterUserScramCredentialsResult;
import org.apache.kafka.clients.admin.UserScramCredentialAlteration;
import org.jetbrains.annotations.NotNull;

public class UserChangeHandler extends BaseChangeHandler {

    private final AdminClient client;

    public UserChangeHandler(final AdminClient client) {
        super(Set.of(CREATE, DELETE, UPDATE));
        this.client = Objects.requireNonNull(client);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ChangeResponse> handleChanges(@NotNull List<ResourceChange> changes) {

        Map<String, ResourceChange> changesByName = CollectionUtils.keyBy(changes, it -> it.getMetadata().getName());

        Map<V1KafkaUserAuthentication, UserScramCredentialAlteration> alterationsByUser = changes.stream()
            .flatMap(user -> {
                StateChangeList<? extends StateChange> stateChanges = user.getSpec().getChanges();
                return stateChanges
                    .stream()
                    .flatMap(change -> {
                        String userName = user.getMetadata().getName();
                        Pair<V1KafkaUserAuthentication, UserScramCredentialAlteration> pair = switch (change.getOp()) {
                            case NONE -> null;
                            case REPLACE -> null;
                            case CREATE, UPDATE -> {
                                V1KafkaUserAuthentication authentication = (V1KafkaUserAuthentication) change.getAfter();
                                yield switch (authentication) {
                                    // SCRAM_SHA_256
                                    case V1KafkaUserAuthentication.ScramSha256 auth ->
                                        KafkaUserService.handleScramSha256(userName, auth);
                                    // SCRAM_SHA_512
                                    case V1KafkaUserAuthentication.ScramSha512 auth ->
                                        KafkaUserService.handleScramSha512(userName, auth);
                                };
                            }
                            case DELETE -> {
                                V1KafkaUserAuthentication authentication = (V1KafkaUserAuthentication) change.getBefore();
                                yield switch (authentication) {
                                    // SCRAM_SHA_256
                                    case V1KafkaUserAuthentication.ScramSha256 auth ->
                                        KafkaUserService.handleScramSha256(userName, auth);
                                    // SCRAM_SHA_512
                                    case V1KafkaUserAuthentication.ScramSha512 auth ->
                                        KafkaUserService.handleScramSha512(userName, auth);
                                };
                            }
                        };
                        return Optional.ofNullable(pair).stream();
                    });
            })
            .collect(Collectors.toMap(Pair::_1, Pair::_2));

        return alterationsByUser
            .values()
            .stream()
            .flatMap(alteration -> {
                AlterUserScramCredentialsResult result = client.alterUserScramCredentials(List.of(alteration));
                return result.values()
                    .entrySet()
                    .stream()
                    .map(entry -> {
                        ResourceChange resource = changesByName.get(entry.getKey());
                        CompletableFuture<ChangeMetadata> future = entry.getValue()
                            .toCompletionStage()
                            .toCompletableFuture()
                            .thenApply(unused -> ChangeMetadata.empty());
                        return new ChangeResponse(resource, future);
                    });
            })
            .toList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TextDescription describe(@NotNull ResourceChange change) {
        return UserChangeDescription.of(change);
    }
}
