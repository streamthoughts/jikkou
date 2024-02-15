/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
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
