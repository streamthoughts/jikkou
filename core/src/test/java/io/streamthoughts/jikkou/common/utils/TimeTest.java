/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.common.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class TimeTest {

    @Test
    void should_get_current_time() {
        long milliseconds = Time.SYSTEM.milliseconds();
        Assertions.assertTrue(milliseconds > 0);
    }
}