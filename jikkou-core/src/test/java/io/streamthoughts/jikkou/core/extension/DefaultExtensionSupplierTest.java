/*
 * Copyright 2023 The original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.streamthoughts.jikkou.core.extension;

import io.streamthoughts.jikkou.core.config.Configurable;
import io.streamthoughts.jikkou.core.config.Configuration;
import io.streamthoughts.jikkou.core.extension.exceptions.ExtensionCreationException;
import java.util.Collections;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class DefaultExtensionSupplierTest {

    @Test
    void shouldThrowExceptionForSupplierReturningNull() {
        var descriptor = new DefaultExtensionDescriptor<>(
                ExtensionDescriptorModifiersTest.class.getName(),
                "",
                "",
                Collections.emptyList(),
                ExtensionCategory.EXTENSION,
                "",
                ExtensionDescriptorModifiersTest.class,
                ExtensionDescriptorModifiersTest.class.getClassLoader(),
                () -> null,
                false
        );
        var supplier = new DefaultExtensionSupplier<>(descriptor);

        Assertions.assertThrows(
                ExtensionCreationException.class,
                () -> supplier.get(Configuration.empty())
        );
    }

    @Test
    void shouldInvokeConfigureMethodForConfigurableExtension() {
        Configurable mock = Mockito.mock(Configurable.class);
        var descriptor = new DefaultExtensionDescriptor<>(
                Configurable.class.getName(),
                "",
                "",
                Collections.emptyList(),
                ExtensionCategory.EXTENSION,
                "",
                Configurable.class,
                Configurable.class.getClassLoader(),
                () -> mock,
                false
        );
        var supplier = new DefaultExtensionSupplier<>(descriptor);
        Configurable configurable = supplier.get(Configuration.empty());
        Assertions.assertEquals(mock, configurable);
        Mockito.verify(mock, Mockito.only()).configure(Mockito.any(Configuration.class));
    }
}