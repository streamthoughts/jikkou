/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.reconciler;

import io.streamthoughts.jikkou.core.ReconciliationContext;
import io.streamthoughts.jikkou.core.models.change.ResourceChange;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jetbrains.annotations.NotNull;

/**
 * The default change executor.
 */
public final class DefaultChangeExecutor implements ChangeExecutor {

    private final List<ResourceChange> changes;
    private final ReconciliationContext context;

    /**
     * Creates a new {@link DefaultChangeExecutor} instances.
     *
     * @param context the reconciliation context. Cannot be {@code null}.
     * @param changes the list of changes to be executed. Cannot be {@code null}.
     */
    public DefaultChangeExecutor(@NotNull ReconciliationContext context,
                                 @NotNull List<ResourceChange> changes) {
        this.changes = Collections.unmodifiableList(changes);
        this.context = Objects.requireNonNull(context, "'context' must not be null");
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public @NotNull List<ResourceChange> changes() {
        return changes;
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public List<ChangeResult> applyChanges(@NotNull List<? extends ChangeHandler> handlers) {
        Objects.requireNonNull(handlers, "handlers cannot be null");

        Map<Operation, ChangeHandler> handlersByType = new HashMap<>();
        for (ChangeHandler handler : handlers) {
            for (var type : handler.supportedChangeTypes()) {
                if (handlersByType.put(type, handler) != null) {
                    throw new IllegalArgumentException("ChangeHandler already registered for type: " + type);
                }
            }
        }

        List<ResourceChange> supportedChanges = changes.stream()
                .filter(it -> handlersByType.containsKey(it.getSpec().getOp()))
                .toList();

        return context.isDryRun() ?
                executeInDryRun(supportedChanges, handlersByType) :
                execute(supportedChanges, handlersByType);
    }

    private List<ChangeResult> executeInDryRun(List<ResourceChange> changes,
                                               Map<Operation, ChangeHandler> handlers) {
        return changes.stream()
                .map(object -> {
                    Operation operation = object.getSpec().getOp();
                    ChangeHandler handler = handlers.get(operation);
                    TextDescription description = handler.describe(object);
                    return operation == Operation.NONE ?
                            ChangeResult.ok(object, description) :
                            ChangeResult.changed(object, description);
                })
                .toList();
    }

    private List<ChangeResult> execute(List<ResourceChange> changes,
                                       Map<Operation, ChangeHandler> handlers) {
        Map<Operation, List<ResourceChange>> changesByType = changes
                .stream()
                .collect(Collectors.groupingBy(it -> it.getSpec().getOp()));

        return changesByType.entrySet()
                .stream()
                .flatMap(e -> execute(handlers.get(e.getKey()), e.getValue()))
                .map(CompletableFuture::join)
                .collect(Collectors.toList());
    }

    private Stream<CompletableFuture<ChangeResult>> execute(final ChangeHandler handler,
                                                            final List<ResourceChange> changes) {
        return handler.handleChanges(changes)
                .stream()
                .map(response -> {
                    CompletableFuture<? extends List<ChangeMetadata>> future = response.getResults();
                    return future.thenApply(metadata -> {
                        ResourceChange change = response.getChange();

                        TextDescription description = handler.describe(change);

                        if (change.getSpec().getOp() == Operation.NONE) {
                            return ChangeResult.ok(change, description);
                        }

                        List<ChangeError> errors = metadata.stream()
                                .map(ChangeMetadata::getError)
                                .flatMap(Optional::stream)
                                .toList();

                        return errors.isEmpty() ?
                                ChangeResult.changed(change, description) :
                                ChangeResult.failed(change, description, errors);
                    });
                });
    }
}
