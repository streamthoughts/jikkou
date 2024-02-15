/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.extension;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class ContextualExtensionTest {

    @Test
    void shouldThrowIllegalStateWhenNotInitialized() {
        ContextualExtension extension = new ContextualExtension() {};
        Assertions.assertThrows(IllegalStateException.class, extension::extensionContext);
    }

    @Test
    void shouldGetSameContextPassedThroughInitMethod() {
        ExtensionContext context = Mockito.mock(ExtensionContext.class);
        ContextualExtension extension = new ContextualExtension() {};
        extension.init(context);
        Assertions.assertSame(context, extension.extensionContext());
    }
}