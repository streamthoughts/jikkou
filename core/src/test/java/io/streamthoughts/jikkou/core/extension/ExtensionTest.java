/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.extension;

import io.streamthoughts.jikkou.core.extension.annotations.ExtensionOptionSpec;
import io.streamthoughts.jikkou.core.extension.annotations.ExtensionSpec;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ExtensionTest {

    @Test
    void shouldGetExtensionName() {
        ExtensionWithSpec extension = new ExtensionWithSpec();
        Assertions.assertEquals("ExtensionWithSpec", extension.getName());
    }

    @ExtensionSpec(
            options = {
                    @ExtensionOptionSpec(
                            name = "optional",
                            type = String.class
                    ),
                    @ExtensionOptionSpec(
                            name = "required",
                            type = String.class,
                            required = true
                    ),
                    @ExtensionOptionSpec(
                            name = "with_default",
                            type = Long.class,
                            defaultValue = "1"
                    ),
            }
    )
    private static class ExtensionWithSpec implements Extension {

    }
}
