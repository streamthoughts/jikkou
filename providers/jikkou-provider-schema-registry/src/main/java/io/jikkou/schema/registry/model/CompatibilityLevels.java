/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.jikkou.schema.registry.model;

/**
 * Schema compatibility levels, ordered by restrictiveness.
 */
public enum CompatibilityLevels {

    BACKWARD(1),
    BACKWARD_TRANSITIVE(2),
    FORWARD(1),
    FORWARD_TRANSITIVE(2),
    FULL(3),
    FULL_TRANSITIVE(4),
    NONE(0);

    private final int restrictiveness;

    CompatibilityLevels(int restrictiveness) {
        this.restrictiveness = restrictiveness;
    }

    /**
     * Returns {@code true} if this level is more restrictive than the given level.
     *
     * @param other the level to compare against.
     * @return {@code true} if this level is more restrictive.
     */
    public boolean isMoreRestrictiveThan(CompatibilityLevels other) {
        return this.restrictiveness > other.restrictiveness;
    }
}
