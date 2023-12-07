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

import io.streamthoughts.jikkou.core.models.change.StateChange;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * A {@link ChangeComputer} implementation for computing map entry changes.
 */
public final class MapEntryChangeComputer implements ChangeComputer<Map.Entry<String, ?>, StateChange> {

    private final ChangeComputer<Map.Entry<String, ?>, StateChange> computer;

    /**
     * Creates a new {@link MapEntryChangeComputer} instance.
     *
     * @param deleteOrphans Specifies whether orphans entries must be deleted or ignored.
     */
    public MapEntryChangeComputer(boolean deleteOrphans) {
        this.computer = ChangeComputer
                .<String, Map.Entry<String, ?>, StateChange>builder()
                .withDeleteOrphans(deleteOrphans)
                .withKeyMapper(Map.Entry::getKey)
                .withChangeFactory((key, before, after) -> {
                    Object beforeValue = Optional.ofNullable(before)
                            .map(Map.Entry::getValue)
                            .orElse(null);

                    Object afterValue = Optional.ofNullable(after)
                            .map(Map.Entry::getValue)
                            .orElse(null);
                    return Optional.of(StateChange.with(key, beforeValue, afterValue));
                })
                .build();
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public List<StateChange> computeChanges(Iterable<Map.Entry<String, ?>> actualStates,
                                            Iterable<Map.Entry<String, ?>> expectedStates) {
        return computer.computeChanges(actualStates, expectedStates);
    }
}
