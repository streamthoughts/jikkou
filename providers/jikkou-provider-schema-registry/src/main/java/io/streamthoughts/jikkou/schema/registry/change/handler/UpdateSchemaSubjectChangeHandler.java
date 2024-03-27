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
import static io.streamthoughts.jikkou.schema.registry.change.SchemaSubjectChangeComputer.DATA_COMPATIBILITY_LEVEL;
import static io.streamthoughts.jikkou.schema.registry.change.SchemaSubjectChangeComputer.DATA_SCHEMA;

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
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Mono;

public final class UpdateSchemaSubjectChangeHandler
        extends AbstractSchemaSubjectChangeHandler
        implements ChangeHandler {

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
    public List<ChangeResponse> handleChanges(@NotNull List<ResourceChange> changes) {

        List<ChangeResponse> results = new ArrayList<>();
        for (ResourceChange change : changes) {
            Mono<Void> mono = Mono.empty();
            // COMPATIBILITY
            StateChange compatibilityLevels = StateChangeList
                    .of(change.getSpec().getChanges())
                    .getLast(DATA_COMPATIBILITY_LEVEL);

            if (UPDATE == compatibilityLevels.getOp() || CREATE == compatibilityLevels.getOp()) {
                mono = mono.then(updateCompatibilityLevel(change));
            }

            if (DELETE == compatibilityLevels.getOp()) {
                mono = mono.then(deleteCompatibilityLevel(change));
            }

            // MODE
            StateChange modes = StateChangeList
                .of(change.getSpec().getChanges())
                .getLast(DATA_MODE);

            if (UPDATE == modes.getOp() || CREATE == modes.getOp()) {
                mono = mono.then(updateMode(change));
            }

            if (DELETE == modes.getOp()) {
                mono = mono.then(deleteMode(change));
            }

            // SCHEMA
            StateChange schema = StateChangeList
                    .of(change.getSpec().getChanges())
                    .getLast(DATA_SCHEMA);

            if (UPDATE == schema.getOp()) {
                mono = mono.then(registerSubjectVersion(change));
            }

            results.add(toChangeResponse(change, mono.toFuture()));
        }
        return results;
    }
}
