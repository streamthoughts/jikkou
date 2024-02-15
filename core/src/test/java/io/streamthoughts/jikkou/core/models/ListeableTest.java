/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.models;

import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ListeableTest {

    @Test
    void test() {
        Listeable<String> listeable = () -> List.of("foo", "bar");
        Assertions.assertEquals("foo", listeable.first());
        Assertions.assertEquals(2, listeable.size());
        Assertions.assertFalse(listeable.isEmpty());
    }

}