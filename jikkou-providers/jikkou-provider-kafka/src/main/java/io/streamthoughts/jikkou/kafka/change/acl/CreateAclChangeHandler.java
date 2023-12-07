/*
 * Copyright 2023 The original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.streamthoughts.jikkou.kafka.change.acl;

import io.streamthoughts.jikkou.common.utils.Pair;
import io.streamthoughts.jikkou.core.data.TypeConverter;
import io.streamthoughts.jikkou.core.models.change.ResourceChange;
import io.streamthoughts.jikkou.core.models.change.SpecificStateChange;
import io.streamthoughts.jikkou.core.reconciler.ChangeMetadata;
import io.streamthoughts.jikkou.core.reconciler.ChangeResponse;
import io.streamthoughts.jikkou.core.reconciler.Operation;
import io.streamthoughts.jikkou.core.reconciler.TextDescription;
import io.streamthoughts.jikkou.core.reconciler.change.BaseChangeHandler;
import io.streamthoughts.jikkou.kafka.model.KafkaAclBinding;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.CreateAclsResult;
import org.apache.kafka.common.acl.AclBinding;
import org.jetbrains.annotations.NotNull;

public final class CreateAclChangeHandler extends BaseChangeHandler<ResourceChange> {

    private final AdminClient client;

    /**
     * Creates a new {@link CreateAclChangeHandler} instance.
     *
     * @param client the {@link AdminClient}.
     */
    public CreateAclChangeHandler(@NotNull final AdminClient client) {
        super(Operation.CREATE);
        this.client = Objects.requireNonNull(client, "client cannot not be null");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ChangeResponse<ResourceChange>> handleChanges(@NotNull final List<ResourceChange> changes) {
        return changes.stream()
                .map(change -> {
                    List<AclBinding> bindings = change
                            .getSpec()
                            .getChanges()
                            .get(AclChangeComputer.ACL)
                            .all(TypeConverter.of(KafkaAclBinding.class))
                            .stream()
                            .map(SpecificStateChange::getAfter)
                            .map(KafkaAclBinding::toAclBinding)
                            .toList();

                    CreateAclsResult result = client.createAcls(bindings);
                    List<CompletableFuture<ChangeMetadata>> futures = result
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
