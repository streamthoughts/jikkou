/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.runtime.configurator;

import static io.streamthoughts.jikkou.runtime.configurator.ExtensionConfigEntry.CONFIGURATION_CONFIG;
import static io.streamthoughts.jikkou.runtime.configurator.ExtensionConfigEntry.NAME_CONFIG;
import static io.streamthoughts.jikkou.runtime.configurator.ExtensionConfigEntry.PRIORITY_CONFIG;
import static io.streamthoughts.jikkou.runtime.configurator.ExtensionConfigEntry.TYPE_CONFIG;

import io.streamthoughts.jikkou.core.DefaultApi;
import io.streamthoughts.jikkou.core.ReconciliationContext;
import io.streamthoughts.jikkou.core.config.Configuration;
import io.streamthoughts.jikkou.core.extension.ClassExtensionAliasesGenerator;
import io.streamthoughts.jikkou.core.extension.DefaultExtensionDescriptorFactory;
import io.streamthoughts.jikkou.core.extension.DefaultExtensionFactory;
import io.streamthoughts.jikkou.core.extension.DefaultExtensionRegistry;
import io.streamthoughts.jikkou.core.extension.ExtensionDescriptorRegistry;
import io.streamthoughts.jikkou.core.extension.qualifier.Qualifiers;
import io.streamthoughts.jikkou.core.models.HasItems;
import io.streamthoughts.jikkou.core.models.HasMetadata;
import io.streamthoughts.jikkou.core.models.NamedValueSet;
import io.streamthoughts.jikkou.core.resource.DefaultResourceRegistry;
import io.streamthoughts.jikkou.core.transform.Transformation;
import io.streamthoughts.jikkou.runtime.JikkouConfigProperties;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class TransformationApiConfiguratorTest {

    @Test
    void shouldRegisterConfiguredExtensions() {
        // Given
        ExtensionDescriptorRegistry registry = new DefaultExtensionRegistry(
                new DefaultExtensionDescriptorFactory(),
                new ClassExtensionAliasesGenerator()
        );
        TestTransformation transformation = new TestTransformation();
        registry.register(TestTransformation.class, () -> transformation);

        TransformationApiConfigurator configurator = new TransformationApiConfigurator(registry);

        DefaultExtensionFactory factory = new DefaultExtensionFactory(registry);
        DefaultApi.Builder builder = DefaultApi.builder(factory, new DefaultResourceRegistry());
        // When
        Map<Object, Object> extensionConfig = Collections.emptyMap();
        Map<String, Object> extensionConfigEntry = NamedValueSet.emptySet()
                .with(NAME_CONFIG.asValue("test"))
                .with(TYPE_CONFIG.asValue(TestTransformation.class.getName()))
                .with(PRIORITY_CONFIG.asValue(1))
                .with(CONFIGURATION_CONFIG.asValue(extensionConfig))
                .asMap();

        Map<String, Object> config = new HashMap<>();
        config.put(JikkouConfigProperties.TRANSFORMATION_CONFIG.key(), List.of(extensionConfigEntry));
        DefaultApi.Builder configured = configurator.configure(builder, Configuration.from(config));

        // Then
        Assertions.assertNotNull(configured);
        Assertions.assertTrue(registry.findDescriptorByClass(TestTransformation.class, Qualifiers.byName("test")).isPresent());
        Transformation<?> actual = factory.getExtension(Transformation.class, Qualifiers.byName("test"));
        Assertions.assertNotNull(actual);
    }

    public static final class TestTransformation implements Transformation<HasMetadata> {
        @Override
        public @NotNull Optional<HasMetadata> transform(@NotNull HasMetadata resource,
                                                        @NotNull HasItems resources,
                                                        @NotNull ReconciliationContext context) {
            return Optional.of(resource);
        }
    }
}