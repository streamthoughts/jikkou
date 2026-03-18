/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.client.command;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class OutputFormatMixinTest {

    @Test
    void shouldReturnFormat() {
        // Given
        OutputFormatMixin mixin = new OutputFormatMixin();
        mixin.format = OutputFormat.JSON;

        // When / Then
        Assertions.assertEquals(OutputFormat.JSON, mixin.format());
    }

    @Test
    void shouldDefaultToNullWhenNotInitialized() {
        // Given
        OutputFormatMixin mixin = new OutputFormatMixin();

        // When / Then
        Assertions.assertNull(mixin.format());
    }
}
