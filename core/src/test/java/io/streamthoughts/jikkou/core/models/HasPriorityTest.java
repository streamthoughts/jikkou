/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.models;

import io.streamthoughts.jikkou.core.annotation.Priority;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@Priority(HasPriority.HIGHEST_PRECEDENCE)
class HasPriorityTest implements HasPriority{

    @Test
    void shouldGetPriorityForClassWithAnnotation() {
        Assertions.assertEquals(HasPriority.HIGHEST_PRECEDENCE, getPriority());
    }
}