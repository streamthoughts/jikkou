/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.schema.registry.change.handler;

import static io.streamthoughts.jikkou.schema.registry.change.SchemaSubjectChangeComputer.DATA_COMPATIBILITY_LEVEL;

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

public final class CreateSchemaSubjectChangeHandler
        extends AbstractSchemaSubjectChangeHandler
        implements ChangeHandler {

    /**
     * Creates a new {@link CreateSchemaSubjectChangeHandler} instance.
     *
     * @param api the {@link SchemaRegistryApi} instance.
     */
    public CreateSchemaSubjectChangeHandler(@NotNull final AsyncSchemaRegistryApi api) {
        super(api);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<Operation> supportedChangeTypes() {
        return Set.of(Operation.CREATE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ChangeResponse> handleChanges(@NotNull List<ResourceChange> changes) {

        List<ChangeResponse> results = new ArrayList<>();
        for (ResourceChange change : changes) {
            Mono<Void> mono = registerSubjectVersion(change);

            StateChange compatibilityLevels = StateChangeList
                    .of(change.getSpec().getChanges())
                    .getLast(DATA_COMPATIBILITY_LEVEL);

            if (compatibilityLevels != null) {
                mono = mono.then(updateCompatibilityLevel(change));
            }

            results.add(toChangeResponse(change, mono.toFuture()));
        }
        return results;
    }
}
