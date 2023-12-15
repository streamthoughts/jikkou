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

import io.streamthoughts.jikkou.core.models.NamedValueSet;
import io.streamthoughts.jikkou.core.models.change.ResourceChange;
import java.util.List;

/**
 * Interface for filtering {@link ResourceChange} objects.
 */
public interface ResourceChangeFilter {

    /**
     * Applies this filter on the given changes.
     *
     * @param changes The changes.
     * @return The list of {@link ResourceChange}.
     */
    List<ResourceChange> filter(List<ResourceChange> changes);


    /**
     * Gets an optional representation of this filter as a set of named values.
     *
     * @return  The optional configuration.
     */
    default NamedValueSet toValues() {
        return NamedValueSet.emptySet();
    }

    /**
     * A filter that does nothing.
     */
    class Noop implements ResourceChangeFilter {
        /** {@inheritDoc} **/
        @Override
        public List<ResourceChange> filter(List<ResourceChange> changes) {
            return changes;
        }
    }
}
