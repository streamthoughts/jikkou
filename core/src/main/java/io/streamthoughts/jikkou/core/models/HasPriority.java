/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.models;

import io.streamthoughts.jikkou.core.annotation.Priority;

/**
 * Interface for objects that have a priority.
 */
public interface HasPriority {

    /**
     * Constant for the highest precedence value.
     */
    int	HIGHEST_PRECEDENCE = Integer.MIN_VALUE;

    /**
     * Constant for the lowest precedence value.
     */
    int	LOWEST_PRECEDENCE = Integer.MAX_VALUE;

    int NO_ORDER = 0;

    /**
     * Gets the order value. Defaults to zero (no order).
     *
     * @return the order value
     */
    default int getPriority() {
        Priority priority = this.getClass().getAnnotation(Priority.class);
        if (priority != null) {
            return priority.value();
        }
        return NO_ORDER;
    }
}
