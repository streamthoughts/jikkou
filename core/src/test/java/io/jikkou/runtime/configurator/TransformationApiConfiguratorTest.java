/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.jikkou.runtime.configurator;

import static io.jikkou.runtime.configurator.ExtensionConfigEntry.CONFIGURATION_CONFIG;
import static io.jikkou.runtime.configurator.ExtensionConfigEntry.NAME_CONFIG;
import static io.jikkou.runtime.configurator.ExtensionConfigEntry.PRIORITY_CONFIG;
import static io.jikkou.runtime.configurator.ExtensionConfigEntry.TYPE_CONFIG;

import io.jikkou.core.DefaultApi;
import io.jikkou.core.ReconciliationContext;
import io.jikkou.core.config.Configuration;
import io.jikkou.core.extension.ClassExtensionAliasesGenerator;
import io.jikkou.core.extension.DefaultExtensionDescriptorFactory;
import io.jikkou.core.extension.DefaultExtensionFactory;
import io.jikkou.core.extension.DefaultExtensionRegistry;
import io.jikkou.core.extension.ExtensionDescriptorRegistry;
import io.jikkou.core.extension.qualifier.Qualifiers;
import io.jikkou.core.models.HasItems;
import io.jikkou.core.models.HasMetadata;
import io.jikkou.core.models.NamedValueSet;
import io.jikkou.core.resource.DefaultResourceRegistry;
import io.jikkou.core.transform.Transformation;
import io.jikkou.runtime.JikkouConfigProperties;
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