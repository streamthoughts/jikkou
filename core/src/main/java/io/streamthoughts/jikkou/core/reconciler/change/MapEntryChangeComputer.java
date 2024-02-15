/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
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
