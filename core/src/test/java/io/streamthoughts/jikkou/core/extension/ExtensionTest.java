/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.extension;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ExtensionTest {

    @Test
    void shouldGetExtensionName() {
        ExtensionWithSpec extension = new ExtensionWithSpec();
        Assertions.assertEquals("ExtensionWithSpec", extension.getName());
    }

    private static class ExtensionWithSpec implements Extension {

    }
}
