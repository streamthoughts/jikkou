/*
 * Copyright 2021 The original authors
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
package io.streamthoughts.jikkou.api.control;

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
 * This class is responsible to execute an operation.
 *
 * @param <C> the type of the {@link Change} that will be computed.
 */
public final class ChangeExecutor<C extends Change> {

    private final Map<ChangeType, ChangeHandler<C>> handlers;

    /**
     * Creates a new {@link ChangeExecutor} instance.
     *
     * @param handlers the list of handlers.
     */
    public ChangeExecutor(@NotNull final List<? extends ChangeHandler<C>> handlers) {
        Objects.requireNonNull(handlers, "'handlers' cannot be null");
        this.handlers = new HashMap<>();
        for (ChangeHandler<C> handler : handlers) {
            for (var type : handler.supportedChangeTypes()) {
                if (this.handlers.put(type, handler) != null) {
                    throw new IllegalArgumentException("ChangeHandler already registered for type: " + type);
                }
            }
        }
    }

    /**
     * Executes all the given changes.
     *
     * @param changes the list of changes to execute.
     * @param dryRun  {@code true} if the execution should be run as dry-run.
     * @return the list of {@link ChangeResult}.
     */
    public @NotNull List<ChangeResult<C>> execute(@NotNull final List<C> changes, final boolean dryRun) {

        List<C> supportedChanges = changes.stream()
                .filter(this::isChangeSupported)
                .toList();

        if (dryRun) {
            return supportedChanges.stream()
                    .map(change -> {
                        ChangeHandler<C> handler = handlers.get(change.getChangeType());
                        ChangeDescription description = handler.getDescriptionFor(change);
                        return change.getChangeType() == ChangeType.NONE ?
                                ChangeResult.ok(change, description) :
                                ChangeResult.changed(change, description);
                    })
                    .toList();
        } else {
            // Execute supported all changes
            return execute(supportedChanges);
        }
    }

    private boolean isChangeSupported(C change) {
        return handlers.containsKey(change.getChangeType());
    }

    private List<ChangeResult<C>> execute(final List<C> changes) {
        Map<ChangeType, List<C>> changesGroupedByType = changes
                .stream()
                .collect(Collectors.groupingBy(Change::getChangeType));

        return changesGroupedByType.entrySet()
                .stream()
                .flatMap(e -> execute(handlers.get(e.getKey()), e.getValue()))
                .map(CompletableFuture::join)
                .collect(Collectors.toList());
    }

    private Stream<CompletableFuture<ChangeResult<C>>> execute(final ChangeHandler<C> handler,
                                                               final List<C> changes) {
        return handler.apply(changes)
                .stream()
                .map(response -> {
                    CompletableFuture<? extends List<ChangeMetadata>> future = response.getResults();
                    return future.thenApply(metadata -> {
                        C change = response.getChange();

                        ChangeDescription description = handler.getDescriptionFor(change);

                        if (change.getChangeType() == ChangeType.NONE) {
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
