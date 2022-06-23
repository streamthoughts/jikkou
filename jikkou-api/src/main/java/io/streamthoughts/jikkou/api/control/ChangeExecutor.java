/*
 * Copyright 2021 StreamThoughts.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
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

import io.vavr.Tuple2;
import io.vavr.concurrent.Future;
import io.vavr.control.Option;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;

/**
 * This class is responsible for executing an operation.
 *
 * @param <K>   the type of the key used for identifying an object state.
 * @param <C>   the type of the {@link Change} that will be computed.
 */
public final class ChangeExecutor<K, C extends Change<K>> {

    private final Supplier<List<C>> changeComputer;

    public static <K, T extends Change<K>> ChangeExecutor<K, T> ofSupplier(final Supplier<List<T>> supplier) {
        return new ChangeExecutor<>(supplier);
    }

    /**
     * Creates a new {@link ChangeExecutor} instance.
     *
     * @param changeComputer the {@link Supplier} to be used for computing changes.
     */
    private ChangeExecutor(@NotNull final Supplier<List<C>> changeComputer) {
        this.changeComputer = Objects.requireNonNull(changeComputer, "'changeComputer' cannot be null");
    }

    /**
     * Runs the given {@link ExecutableOperation}.
     *
     * @param operation the {@link ExecutableOperation} to be executed.
     * @param dryRun    {@code true} if the execution should be run as dry-run.
     * @return the list of {@link ChangeResult}.
     */
    public @NotNull Collection<ChangeResult<C>> execute(@NotNull final ExecutableOperation<C, K, ?> operation,
                                                        final boolean dryRun) {
        // Compute all changes
        List<C> changes = changeComputer.get();

        // Execute the operation changes
        if (dryRun) {
            return changes.stream()
                    .filter(it -> operation.test(it) || it.getChange() == ChangeType.NONE)
                    .map(change -> {
                        Description description = operation.getDescriptionFor(change);
                        return change.getChange() == ChangeType.NONE ?
                                ChangeResult.ok(change, description) :
                                ChangeResult.changed(change, description);
                    })
                    .collect(Collectors.toList());
        } else {

            // Do execute the change with the given operation.
            final List<ChangeResult<C>> results = execute(changes, operation);

            // Then, add all resources with no changes
            changes
                    .stream()
                    .filter(it -> it.getChange() == ChangeType.NONE)
                    .map(change -> ChangeResult.ok(change, operation.getDescriptionFor(change)))
                    .forEach(results::add);

            return results;
        }
    }

    private List<ChangeResult<C>> execute(final List<C> changes,
                                          final ExecutableOperation<C, K, ?> operation) {

        var changesKeyedById = changes.stream().collect(Collectors.toMap(Change::getKey, e -> e));

        var futures = operation.apply(changes)
                .entrySet()
                .stream()
                .map(e -> new Tuple2<>(changesKeyedById.get(e.getKey()), e.getValue()))
                .map(t -> {

                    final List<Future<Option<Throwable>>> allOptions = t._2().stream()
                            .map(f -> f.map(it -> Option.<Throwable>none()))
                            .map(f -> f.recover(Option::some))
                            .collect(Collectors.toList());

                    final Future<List<Throwable>> allThrowable = Future.fold(
                            allOptions,
                            new ArrayList<>(),
                            (list, option) -> {
                                option.peek(list::add);
                                return list;
                            });

                    return allThrowable
                            .map(l -> l.isEmpty() ?
                                    ChangeResult.changed(t._1(), operation.getDescriptionFor(t._1())) :
                                    ChangeResult.failed(t._1(), operation.getDescriptionFor(t._1()), l))
                            .toCompletableFuture();
                }).toList();

        return futures
                .stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());
    }
}
