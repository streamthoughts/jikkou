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
package io.streamthoughts.jikkou.core.reconcilier;

import io.streamthoughts.jikkou.core.ReconciliationContext;
import io.streamthoughts.jikkou.core.models.HasMetadataChange;
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
 *
 * @param <C> the type of the {@link Change} that will be computed.
 */
public final class DefaultChangeExecutor<C extends Change> implements ChangeExecutor<C> {

    private final List<HasMetadataChange<C>> changes;
    private final ReconciliationContext context;

    /**
     * Creates a new {@link DefaultChangeExecutor} instances.
     *
     * @param context the reconciliation context. Cannot be {@code null}.
     * @param changes the list of changes to be executed. Cannot be {@code null}.
     */
    public DefaultChangeExecutor(@NotNull ReconciliationContext context,
                                 @NotNull List<HasMetadataChange<C>> changes) {
        this.changes = Collections.unmodifiableList(changes);
        this.context = Objects.requireNonNull(context, "'context' must not be null");
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public @NotNull List<HasMetadataChange<C>> changes() {
        return changes;
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public @NotNull List<ChangeResult<C>> execute(@NotNull List<? extends ChangeHandler<C>> handlers) {
        Objects.requireNonNull(handlers, "handlers cannot be null");

        Map<ChangeType, ChangeHandler<C>> handlersByType = new HashMap<>();
        for (ChangeHandler<C> handler : handlers) {
            for (var type : handler.supportedChangeTypes()) {
                if (handlersByType.put(type, handler) != null) {
                    throw new IllegalArgumentException("ChangeHandler already registered for type: " + type);
                }
            }
        }

        List<HasMetadataChange<C>> supportedChanges = changes.stream()
                .filter(it -> handlersByType.containsKey(it.getChange().operation()))
                .toList();

        return context.isDryRun() ?
                executeInDryRun(supportedChanges, handlersByType) :
                execute(supportedChanges, handlersByType);
    }

    @NotNull
    private List<ChangeResult<C>> executeInDryRun(List<HasMetadataChange<C>> changes,
                                                  Map<ChangeType, ChangeHandler<C>> handlers) {
        return changes.stream()
                .map(object -> {
                    ChangeHandler<C> handler = handlers.get(object.getChange().operation());
                    ChangeDescription description = handler.getDescriptionFor(object);
                    return object.getChange().operation() == ChangeType.NONE ?
                            DefaultChangeResult.ok(object, description) :
                            DefaultChangeResult.changed(object, description);
                })
                .toList();
    }

    @NotNull
    private List<ChangeResult<C>> execute(List<HasMetadataChange<C>> changes,
                                          Map<ChangeType, ChangeHandler<C>> handlers) {
        Map<ChangeType, List<HasMetadataChange<C>>> changesByType = changes
                .stream()
                .collect(Collectors.groupingBy(it -> it.getChange().operation()));

        return changesByType.entrySet()
                .stream()
                .flatMap(e -> execute(handlers.get(e.getKey()), e.getValue()))
                .map(CompletableFuture::join)
                .collect(Collectors.toList());
    }

    private Stream<CompletableFuture<ChangeResult<C>>> execute(final ChangeHandler<C> handler,
                                                               final List<HasMetadataChange<C>> changes) {
        return handler.apply(changes)
                .stream()
                .map(response -> {
                    CompletableFuture<? extends List<ChangeMetadata>> future = response.getResults();
                    return future.thenApply(metadata -> {
                        HasMetadataChange<C> object = response.getChange();

                        ChangeDescription description = handler.getDescriptionFor(object);

                        if (object.getChange().operation() == ChangeType.NONE) {
                            return DefaultChangeResult.ok(object, description);
                        }

                        List<ChangeError> errors = metadata.stream()
                                .map(ChangeMetadata::getError)
                                .flatMap(Optional::stream)
                                .toList();

                        return errors.isEmpty() ?
                                DefaultChangeResult.changed(object, description) :
                                DefaultChangeResult.failed(object, description, errors);
                    });
                });
    }
}
