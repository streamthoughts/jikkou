/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.reconciler;

import org.junit.jupiter.api.Test;

class ChangeTest {

    @Test
    void shouldGetNoneForOnlyNoneChanges() {
        Change.computeOperation(() -> Operation.NONE, () -> Operation.NONE);
    }

    @Test
    void shouldGetUpdateForUpdate() {
        Change.computeOperation(() -> Operation.NONE, () -> Operation.UPDATE);
    }

    @Test
    void shouldGetUpdateForDelete() {
        Change.computeOperation(() -> Operation.NONE, () -> Operation.DELETE);
    }

    @Test
    void shouldGetUpdateForAdd() {
        Change.computeOperation(() -> Operation.NONE, () -> Operation.CREATE);
    }
}