/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.extension.confluent.change;

import io.streamthoughts.jikkou.core.data.TypeConverter;
import io.streamthoughts.jikkou.core.models.change.ResourceChange;
import io.streamthoughts.jikkou.core.models.change.SpecificStateChange;
import io.streamthoughts.jikkou.core.reconciler.ChangeHandler;
import io.streamthoughts.jikkou.core.reconciler.ChangeMetadata;
import io.streamthoughts.jikkou.core.reconciler.ChangeResponse;
import io.streamthoughts.jikkou.core.reconciler.Operation;
import io.streamthoughts.jikkou.core.reconciler.TextDescription;
import io.streamthoughts.jikkou.core.reconciler.change.BaseChangeHandler;
import io.streamthoughts.jikkou.extension.confluent.api.ConfluentCloudApiClient;
import io.streamthoughts.jikkou.extension.confluent.api.data.RoleBindingData;
import jakarta.ws.rs.WebApplicationException;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;

public abstract class RoleBindingChangeHandler extends BaseChangeHandler {

    protected final ConfluentCloudApiClient api;

    public RoleBindingChangeHandler(@NotNull final ConfluentCloudApiClient api,
                                    @NotNull final Operation supportedOperation) {
        super(Set.of(supportedOperation));
        this.api = Objects.requireNonNull(api, "api must not be null");
    }

    protected static RoleBindingData getEntry(ResourceChange change) {
        SpecificStateChange<RoleBindingData> entry = change.getSpec()
            .getChanges()
            .getLast("entry", TypeConverter.of(RoleBindingData.class));
        return change.getOp() == Operation.DELETE ? entry.getBefore() : entry.getAfter();
    }

    protected <R> ChangeResponse executeAsync(final ResourceChange change,
                                              final java.util.function.Supplier<R> supplier) {
        CompletableFuture<ChangeMetadata> future = CompletableFuture
            .supplyAsync(() -> {
                try {
                    supplier.get();
                    return ChangeMetadata.empty();
                } catch (WebApplicationException e) {
                    return ChangeMetadata.of(e);
                }
            });
        return new ChangeResponse(change, future);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TextDescription describe(@NotNull ResourceChange change) {
        return RoleBindingChangeDescription.of(change.getSpec().getOp(), getEntry(change));
    }

    public static class Create extends RoleBindingChangeHandler {

        public Create(@NotNull final ConfluentCloudApiClient api) {
            super(api, Operation.CREATE);
        }

        @Override
        public List<ChangeResponse> handleChanges(@NotNull List<ResourceChange> changes) {
            return changes.stream()
                .map(change -> executeAsync(
                    change,
                    () -> api.createRoleBinding(getEntry(change)))
                )
                .collect(Collectors.toList());
        }
    }

    public static class Delete extends RoleBindingChangeHandler {

        public Delete(@NotNull final ConfluentCloudApiClient api) {
            super(api, Operation.DELETE);
        }

        @Override
        public List<ChangeResponse> handleChanges(@NotNull List<ResourceChange> changes) {
            return changes.stream()
                .map(change -> {
                    RoleBindingData entry = getEntry(change);
                    return executeAsync(change, () -> {
                        api.deleteRoleBinding(entry.id());
                        return null;
                    });
                })
                .collect(Collectors.toList());
        }
    }

    public static class None extends ChangeHandler.None {
        public None() {
            super(change -> RoleBindingChangeDescription.of(change.getSpec().getOp(), getEntry(change)));
        }
    }
}
