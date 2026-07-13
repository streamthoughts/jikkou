/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.jikkou.core.reconciler;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class OperationTest {

    @Test
    void testHumanize() {
        Assertions.assertEquals("Create", Operation.CREATE.humanize());
        Assertions.assertEquals("Delete", Operation.DELETE.humanize());
        Assertions.assertEquals("Update", Operation.UPDATE.humanize());
        Assertions.assertEquals("Unchanged", Operation.NONE.humanize());
    }

    @Test
    void shouldReturnChangedForAllOperationsExceptNone() {
        Assertions.assertFalse(Operation.NONE.isChanged());
        Assertions.assertTrue(Operation.CREATE.isChanged());
        Assertions.assertTrue(Operation.UPDATE.isChanged());
        Assertions.assertTrue(Operation.DELETE.isChanged());
        Assertions.assertTrue(Operation.REPLACE.isChanged());
    }
}