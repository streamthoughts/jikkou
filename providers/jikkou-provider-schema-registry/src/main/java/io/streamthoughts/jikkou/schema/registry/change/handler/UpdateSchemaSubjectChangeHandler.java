/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.schema.registry.change.handler;

import static io.streamthoughts.jikkou.core.reconciler.Operation.CREATE;
import static io.streamthoughts.jikkou.core.reconciler.Operation.DELETE;
import static io.streamthoughts.jikkou.core.reconciler.Operation.UPDATE;
import static io.streamthoughts.jikkou.schema.registry.change.SchemaSubjectChangeComputer.*;

import io.streamthoughts.jikkou.core.models.change.ResourceChange;
import io.streamthoughts.jikkou.core.models.change.StateChange;
import io.streamthoughts.jikkou.core.models.change.StateChangeList;
import io.streamthoughts.jikkou.core.reconciler.ChangeHandler;
import io.streamthoughts.jikkou.core.reconciler.ChangeResponse;
import io.streamthoughts.jikkou.core.reconciler.Operation;
import io.streamthoughts.jikkou.schema.registry.api.AsyncSchemaRegistryApi;
import io.streamthoughts.jikkou.schema.registry.api.SchemaRegistryApi;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import org.jetbrains.annotations.NotNull;

public final class UpdateSchemaSubjectChangeHandler
        extends AbstractSchemaSubjectChangeHandler
        implements ChangeHandler<ResourceChange> {

    /**
     * Creates a new {@link UpdateSchemaSubjectChangeHandler} instance.
     *
     * @param api the {@link SchemaRegistryApi} instance.
     */
    public UpdateSchemaSubjectChangeHandler(@NotNull final AsyncSchemaRegistryApi api) {
        super(api);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<Operation> supportedChangeTypes() {
        return Set.of(UPDATE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ChangeResponse<ResourceChange>> handleChanges(@NotNull List<ResourceChange> changes) {

        List<ChangeResponse<ResourceChange>> results = new ArrayList<>();
        for (ResourceChange change : changes) {
            CompletableFuture<Void> future = CompletableFuture.completedFuture(null);

            StateChange schema = StateChangeList
                    .of(change.getSpec().getChanges())
                    .getLast(DATA_SCHEMA);

            if (UPDATE == schema.getOp()) {
                future = future.thenComposeAsync(unused -> registerSubjectVersion(change));
            }

            StateChange compatibilityLevels = StateChangeList
                    .of(change.getSpec().getChanges())
                    .getLast(DATA_COMPATIBILITY_LEVEL);

            if (UPDATE == compatibilityLevels.getOp() || CREATE == compatibilityLevels.getOp()) {
                future = future.thenComposeAsync(unused -> updateCompatibilityLevel(change));
            }

            if (DELETE == compatibilityLevels.getOp()) {
                future = future.thenComposeAsync(unused -> deleteCompatibilityLevel(change));
            }

            StateChange modes = StateChangeList
                    .of(change.getSpec().getChanges())
                    .getLast(DATA_MODE);

            if (UPDATE == modes.getOp() || CREATE == modes.getOp()) {
                future = future.thenComposeAsync(unused -> updateMode(change));
            }

            if (DELETE == modes.getOp()) {
                future = future.thenComposeAsync(unused -> deleteMode(change));
            }
            results.add(toChangeResponse(change, future));
        }
        return results;
    }
}
