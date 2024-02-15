/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.reconciler.change;

import io.streamthoughts.jikkou.common.annotation.InterfaceStability;
import io.streamthoughts.jikkou.core.models.change.StateChange;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * Service interface for computing the changes required for reconciling resources.
 *
 * @param <T> The type of the resource - can be any type of object.
 * @param <R> The type of the change - can be any type of object.
 */
@InterfaceStability.Evolving
public interface ChangeComputer<T, R> {

    /**
     * Computes the changes between the given states.
     *
     * @param actualStates   The actual states.
     * @param expectedStates The expected states.
     * @return The list of ResourceDataChange.
     */
    List<R> computeChanges(Iterable<T> actualStates,
                           Iterable<T> expectedStates);

    /**
     * Convenient method for computing changes between the given {@link Map}.
     *
     * @param actualStates   The actual states.
     * @param expectedStates The expected states.
     * @param deleteOrphans  Specifies whether orphans entries must be deleted or ignored.
     * @return The list of {@link StateChange}.
     */
    static List<StateChange> computeChanges(Map<String, ?> actualStates,
                                            Map<String, ?> expectedStates,
                                            boolean deleteOrphans) {
        return new MapEntryChangeComputer(deleteOrphans)
                .computeChanges(
                        new HashSet<>(actualStates.entrySet()),
                        new HashSet<>(expectedStates.entrySet())
                );
    }

    /**
     * Creates a new {@link ChangeComputerBuilder}.
     *
     * @param <T> The type of the resource.
     * @return The new Builder.
     */
    static <I, T, R> ChangeComputerBuilder<I, T, R> builder() {
        return new DefaultChangeComputerBuilder<>();
    }


}
