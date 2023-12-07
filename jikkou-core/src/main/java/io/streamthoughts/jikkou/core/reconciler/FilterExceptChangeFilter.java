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

final class FilterExceptChangeFilter implements StateChangeFilter {

    private final Set<Operation> operationsToInclude;

    /**
     * Creates a new {@link FilterExceptChangeFilter} instance.
     *
     * @param operationsToInclude The operations to include.
     */
    FilterExceptChangeFilter(Set<Operation> operationsToInclude) {
        this.operationsToInclude = operationsToInclude;
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public boolean test(StateChange change) {
        return operationsToInclude.contains(change.getOp());
    }
}
