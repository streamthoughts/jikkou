/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.jikkou.schema.registry.model;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class CompatibilityLevelsTest {

    @Test
    void shouldReturnTrueWhenBackwardIsMoreRestrictiveThanNone() {
        assertTrue(CompatibilityLevels.BACKWARD.isMoreRestrictiveThan(CompatibilityLevels.NONE));
    }

    @Test
    void shouldReturnTrueWhenFullTransitiveIsMoreRestrictiveThanNone() {
        assertTrue(CompatibilityLevels.FULL_TRANSITIVE.isMoreRestrictiveThan(CompatibilityLevels.NONE));
    }

    @Test
    void shouldReturnTrueWhenFullIsMoreRestrictiveThanBackward() {
        assertTrue(CompatibilityLevels.FULL.isMoreRestrictiveThan(CompatibilityLevels.BACKWARD));
    }

    @Test
    void shouldReturnTrueWhenFullTransitiveIsMoreRestrictiveThanFull() {
        assertTrue(CompatibilityLevels.FULL_TRANSITIVE.isMoreRestrictiveThan(CompatibilityLevels.FULL));
    }

    @Test
    void shouldReturnFalseWhenNoneIsMoreRestrictiveThanBackward() {
        assertFalse(CompatibilityLevels.NONE.isMoreRestrictiveThan(CompatibilityLevels.BACKWARD));
    }

    @Test
    void shouldReturnFalseWhenSameLevel() {
        assertFalse(CompatibilityLevels.BACKWARD.isMoreRestrictiveThan(CompatibilityLevels.BACKWARD));
    }

    @Test
    void shouldReturnFalseWhenBackwardComparedToForward() {
        assertFalse(CompatibilityLevels.BACKWARD.isMoreRestrictiveThan(CompatibilityLevels.FORWARD));
    }
}
