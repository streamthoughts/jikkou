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
package io.streamthoughts.jikkou.core.reconciler;

import io.streamthoughts.jikkou.core.models.change.StateChange;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Interface for filtering changes.
 */
@FunctionalInterface
public interface StateChangeFilter extends Predicate<StateChange> {

    /**
     * Factory method to construct filter that filters out all changes except ones for operations includes in the given set.
     *
     * @param operations The operations.
     * @return The new {@link StateChangeFilter}.
     */
    static StateChangeFilter filterOutAllExcept(Set<Operation> operations) {
        return new FilterExceptChangeFilter(operations);
    }

    /**
     * Evaluates this filter on the given change.
     *
     * @param change The change.
     * @return {@code true} if the input change matches the filter, otherwise {@code false}.
     */
    @Override
    boolean test(StateChange change);
}
