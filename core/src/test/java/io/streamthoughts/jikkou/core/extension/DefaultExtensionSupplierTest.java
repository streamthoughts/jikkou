/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.extension;

import io.streamthoughts.jikkou.core.config.ConfigPropertySpec;
import io.streamthoughts.jikkou.core.config.Configuration;
import io.streamthoughts.jikkou.core.extension.exceptions.ExtensionCreationException;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class DefaultExtensionSupplierTest {

    static final List<Example> NO_EXAMPLES = Collections.emptyList();
    static final List<ConfigPropertySpec> NO_PROPERTIES = Collections.emptyList();

    @Test
    void shouldThrowExceptionForSupplierReturningNull() {
        var descriptor = new DefaultExtensionDescriptor<>(
                ExtensionDescriptorModifiersTest.class.getName(),
                "",
                "",
                NO_EXAMPLES,
                ExtensionCategory.EXTENSION,
                NO_PROPERTIES,
                "",
                ExtensionDescriptorModifiersTest.class,
                ExtensionDescriptorModifiersTest.class.getClassLoader(),
                () -> null,
                false
        );
        var supplier = new DefaultExtensionSupplier<>(Mockito.mock(ExtensionDescriptorRegistry.class), descriptor);

        Assertions.assertThrows(
                ExtensionCreationException.class,
                () -> supplier.get(Configuration.empty())
        );
    }

    @Test
    void shouldInvokeInitMethodForExtension() {
        Extension mock = Mockito.mock(Extension.class);
        var descriptor = new DefaultExtensionDescriptor<>(
                Extension.class.getName(),
                "",
                "",
                NO_EXAMPLES,
                ExtensionCategory.EXTENSION,
                NO_PROPERTIES,
                "",
                Extension.class,
                Extension.class.getClassLoader(),
                () -> mock,
                false
        );
        var supplier = new DefaultExtensionSupplier<>(Mockito.mock(ExtensionDescriptorRegistry.class), descriptor);
        Extension extension = supplier.get(Configuration.empty());
        Assertions.assertEquals(mock, extension);
        Mockito.verify(mock, Mockito.atLeastOnce()).init(Mockito.any(ExtensionContext.class));
    }
}