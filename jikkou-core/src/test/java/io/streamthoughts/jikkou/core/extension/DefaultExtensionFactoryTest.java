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

import io.streamthoughts.jikkou.core.extension.exceptions.NoSuchExtensionException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DefaultExtensionFactoryTest {


    private DefaultExtensionFactory factory;

    @BeforeEach
    void beforeEach() {
        var registry = new DefaultExtensionRegistry(
                new DefaultExtensionDescriptorFactory(),
                new ClassExtensionAliasesGenerator()
        );
        factory = new DefaultExtensionFactory(registry);
    }

    @Test
    void shouldThrowNoSuchExtensionExceptionForNonExistingExtensionByClass() {
        Assertions.assertThrows(NoSuchExtensionException.class, () -> factory.getExtension(DefaultExtensionFactoryTest.class));
    }

    @Test
    void shouldThrowNoSuchExtensionExceptionForNonExistingExtensionByAlias() {
        Assertions.assertThrows(NoSuchExtensionException.class, () -> factory.getExtension("alias"));
    }

    @Test
    void shouldReturnContainsTrueForRegisteredExtension() {
        factory.register(DefaultExtensionFactoryTest.class, DefaultExtensionFactoryTest::new);
        Assertions.assertTrue(factory.containsExtension(DefaultExtensionFactoryTest.class));
    }

    @Test
    void shouldReturnContainsFalseForNotRegisteredExtension() {
        Assertions.assertFalse(factory.containsExtension(DefaultExtensionFactoryTest.class));
    }


}