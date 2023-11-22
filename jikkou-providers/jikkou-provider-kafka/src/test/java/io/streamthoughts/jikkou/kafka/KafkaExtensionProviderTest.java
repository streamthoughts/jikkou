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
package io.streamthoughts.jikkou.kafka;

import io.streamthoughts.jikkou.core.extension.ClassExtensionAliasesGenerator;
import io.streamthoughts.jikkou.core.extension.DefaultExtensionDescriptorFactory;
import io.streamthoughts.jikkou.core.extension.DefaultExtensionRegistry;
import io.streamthoughts.jikkou.core.reconcilier.Controller;
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