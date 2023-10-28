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
package io.streamthoughts.jikkou.runtime.configurator;

import static io.streamthoughts.jikkou.runtime.configurator.ExtensionConfigEntry.CONFIGURATION_CONFIG;
import static io.streamthoughts.jikkou.runtime.configurator.ExtensionConfigEntry.NAME_CONFIG;
import static io.streamthoughts.jikkou.runtime.configurator.ExtensionConfigEntry.PRIORITY_CONFIG;
import static io.streamthoughts.jikkou.runtime.configurator.ExtensionConfigEntry.TYPE_CONFIG;

import io.streamthoughts.jikkou.core.DefaultApi;
import io.streamthoughts.jikkou.core.config.Configuration;
import io.streamthoughts.jikkou.core.extension.ClassExtensionAliasesGenerator;
import io.streamthoughts.jikkou.core.extension.DefaultExtensionDescriptorFactory;
import io.streamthoughts.jikkou.core.extension.DefaultExtensionFactory;
import io.streamthoughts.jikkou.core.extension.DefaultExtensionRegistry;
import io.streamthoughts.jikkou.core.extension.ExtensionDescriptorRegistry;
import io.streamthoughts.jikkou.core.extension.qualifier.Qualifiers;
import io.streamthoughts.jikkou.core.models.HasMetadata;
import io.streamthoughts.jikkou.core.models.NamedValue;
import io.streamthoughts.jikkou.core.resource.DefaultResourceRegistry;
import io.streamthoughts.jikkou.core.resource.validation.ResourceValidation;
import io.streamthoughts.jikkou.runtime.JikkouConfigProperties;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ValidationApiConfiguratorTest {

    @Test
    void shouldRegisterConfiguredExtensions() {
        // Given
        ExtensionDescriptorRegistry registry = new DefaultExtensionRegistry(
                new DefaultExtensionDescriptorFactory(),
                new ClassExtensionAliasesGenerator()
        );
        // Extension must be registered
        TestValidation validation = new TestValidation();
        registry.register(TestValidation.class, () -> validation);

        ValidationApiConfigurator configurator = new ValidationApiConfigurator(registry);

        DefaultExtensionFactory factory = new DefaultExtensionFactory(registry);
        DefaultApi.Builder builder = DefaultApi.builder(factory, new DefaultResourceRegistry());

        // When
        Map<Object, Object> validationConfig = Collections.emptyMap();
        Map<String, Object> validationConfigEntry = NamedValue.emptySet()
                .with(NAME_CONFIG.asValue("test"))
                .with(TYPE_CONFIG.asValue(TestValidation.class.getName()))
                .with(PRIORITY_CONFIG.asValue(1))
                .with(CONFIGURATION_CONFIG.asValue(validationConfig))
                .asMap();

        Map<String, Object> config = new HashMap<>();
        config.put(JikkouConfigProperties.VALIDATIONS_CONFIG.key(), List.of(validationConfigEntry));
        DefaultApi.Builder configured = configurator.configure(builder, Configuration.from(config));

        // Then
        Assertions.assertNotNull(configured);
        Assertions.assertTrue(registry.findDescriptorByClass(TestValidation.class, Qualifiers.byName("test")).isPresent());
        ResourceValidation<?> actual = factory.getExtension(ResourceValidation.class, Qualifiers.byName("test"));
        Assertions.assertNotNull(actual);
    }

    public static final class TestValidation implements ResourceValidation<HasMetadata> { }
}