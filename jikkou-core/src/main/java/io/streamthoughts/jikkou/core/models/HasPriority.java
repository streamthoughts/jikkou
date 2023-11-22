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