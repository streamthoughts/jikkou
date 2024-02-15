/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.kafka;

import io.streamthoughts.jikkou.core.extension.ClassExtensionAliasesGenerator;
import io.streamthoughts.jikkou.core.extension.DefaultExtensionDescriptorFactory;
import io.streamthoughts.jikkou.core.extension.DefaultExtensionRegistry;
import io.streamthoughts.jikkou.core.reconciler.Controller;
import io.streamthoughts.jikkou.core.transform.Transformation;
import io.streamthoughts.jikkou.core.validation.Validation;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class KafkaExtensionProviderTest {

    @Test
    void shouldRegisterExtensions() {
        // Given
        KafkaExtensionProvider provider = new KafkaExtensionProvider();
        DefaultExtensionRegistry registry = new DefaultExtensionRegistry(
                new DefaultExtensionDescriptorFactory(),
                new ClassExtensionAliasesGenerator()
        );

        // When
        provider.registerExtensions(registry);

        // Then
        Assertions.assertFalse(registry.findAllDescriptorsByClass(Validation.class).isEmpty());
        Assertions.assertFalse(registry.findAllDescriptorsByClass(Transformation.class).isEmpty());
        Assertions.assertFalse(registry.findAllDescriptorsByClass(Controller.class).isEmpty());
    }
}