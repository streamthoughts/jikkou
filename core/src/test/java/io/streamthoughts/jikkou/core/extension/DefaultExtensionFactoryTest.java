/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.extension;

import io.streamthoughts.jikkou.core.extension.exceptions.NoSuchExtensionException;
import java.util.List;
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
    void shouldGetInstanceForRegisteredExtension() {
        factory.register(DefaultExtensionFactoryTest.class, DefaultExtensionFactoryTest::new);
        Assertions.assertNotNull(factory.getExtension(DefaultExtensionFactoryTest.class));
    }

    @Test
    void shouldGetAllInstancesForRegisteredExtension() {
        factory.register(DefaultExtensionFactoryTest.class, DefaultExtensionFactoryTest::new);
        List<DefaultExtensionFactoryTest> extensions = factory.getAllExtensions(DefaultExtensionFactoryTest.class);
        Assertions.assertNotNull(extensions);
        Assertions.assertEquals(1, extensions.size());
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