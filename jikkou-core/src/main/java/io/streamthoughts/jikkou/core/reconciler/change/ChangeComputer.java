/*
 * Copyright 2023 The original authors
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
