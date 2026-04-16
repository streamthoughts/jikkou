/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.jikkou.schema.registry.change.handler;

import static io.jikkou.core.reconciler.Operation.CREATE;
import static io.jikkou.core.reconciler.Operation.DELETE;
import static io.jikkou.core.reconciler.Operation.UPDATE;
import static io.jikkou.schema.registry.change.SchemaSubjectChangeComputer.*;
import static io.jikkou.schema.registry.change.SchemaSubjectChangeComputer.DATA_COMPATIBILITY_LEVEL;
import static io.jikkou.schema.registry.change.SchemaSubjectChangeComputer.DATA_SCHEMA;

import io.jikkou.core.data.TypeConverter;
import io.jikkou.core.models.change.ResourceChange;
import io.jikkou.core.models.change.StateChange;
import io.jikkou.core.models.change.StateChangeList;
import io.jikkou.core.reconciler.ChangeHandler;
import io.jikkou.core.reconciler.ChangeResponse;
import io.jikkou.core.reconciler.Operation;
import io.jikkou.schema.registry.api.AsyncSchemaRegistryApi;
import io.jikkou.schema.registry.api.SchemaRegistryApi;
import io.jikkou.schema.registry.model.CompatibilityLevels;
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

            StateChange compatibilityChange = StateChangeList
                    .of(change.getSpec().getChanges())
                    .getLast(DATA_COMPATIBILITY_LEVEL);

            StateChange schemaChange = StateChangeList
                    .of(change.getSpec().getChanges())
                    .getLast(DATA_SCHEMA);

            StateChange modeChange = StateChangeList
                .of(change.getSpec().getChanges())
                .getLast(DATA_MODE);

            // When both schema and compatibility are changing, the order matters:
            // - Tightening compatibility (e.g., NONE -> BACKWARD): register schema first
            //   so it is validated under the old, less restrictive level.
            // - Loosening compatibility (e.g., BACKWARD -> NONE): update compatibility first
            //   so the schema can be registered under the new, less restrictive level.
            // See: https://github.com/streamthoughts/jikkou/issues/756
            boolean isTightening = isCompatibilityTightening(compatibilityChange);

            if (isTightening) {
                mono = applySchemaChange(mono, change, schemaChange);
                mono = applyModeChange(mono, change, modeChange);
                mono = applyCompatibilityChange(mono, change, compatibilityChange);
            } else {
                mono = applyCompatibilityChange(mono, change, compatibilityChange);
                mono = applyModeChange(mono, change, modeChange);
                mono = applySchemaChange(mono, change, schemaChange);
            }

            results.add(toChangeResponse(change, mono.toFuture()));
        }
        return results;
    }

    private boolean isCompatibilityTightening(@NotNull StateChange compatibilityChange) {
        if (UPDATE != compatibilityChange.getOp()) {
            return false;
        }
        CompatibilityLevels before = TypeConverter.of(CompatibilityLevels.class)
                .convertValue(compatibilityChange.getBefore());
        CompatibilityLevels after = TypeConverter.of(CompatibilityLevels.class)
                .convertValue(compatibilityChange.getAfter());
        return after.isMoreRestrictiveThan(before);
    }

    private Mono<Void> applyCompatibilityChange(Mono<Void> mono, ResourceChange change, StateChange compatibilityChange) {
        if (UPDATE == compatibilityChange.getOp() || CREATE == compatibilityChange.getOp()) {
            mono = mono.then(updateCompatibilityLevel(change));
        }
        if (DELETE == compatibilityChange.getOp()) {
            mono = mono.then(deleteCompatibilityLevel(change));
        }
        return mono;
    }

    private Mono<Void> applyModeChange(Mono<Void> mono, ResourceChange change, StateChange modeChange) {
        if (UPDATE == modeChange.getOp() || CREATE == modeChange.getOp()) {
            mono = mono.then(updateMode(change));
        }
        if (DELETE == modeChange.getOp()) {
            mono = mono.then(deleteMode(change));
        }
        return mono;
    }

    private Mono<Void> applySchemaChange(Mono<Void> mono, ResourceChange change, StateChange schemaChange) {
        if (UPDATE == schemaChange.getOp()) {
            mono = mono.then(registerSubjectVersion(change));
        }
        return mono;
    }
}
