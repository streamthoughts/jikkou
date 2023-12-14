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
import io.streamthoughts.jikkou.core.models.NamedValueSet;
import io.streamthoughts.jikkou.core.reconciler.ChangeResult;
import io.streamthoughts.jikkou.core.reporter.ChangeReporter;
import io.streamthoughts.jikkou.core.resource.DefaultResourceRegistry;
import io.streamthoughts.jikkou.runtime.JikkouConfigProperties;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ChangeReporterApiConfiguratorTest {

    @Test
    void shouldRegisterConfiguredExtensions() {
        // Given
        ExtensionDescriptorRegistry registry = new DefaultExtensionRegistry(
                new DefaultExtensionDescriptorFactory(),
                new ClassExtensionAliasesGenerator()
        );
        TestReporter reporter = new TestReporter();
        registry.register(TestReporter.class, () -> reporter);

        ChangeReporterApiConfigurator configurator = new ChangeReporterApiConfigurator(registry);

        DefaultExtensionFactory factory = new DefaultExtensionFactory(registry);
        DefaultApi.Builder builder = DefaultApi.builder(factory, new DefaultResourceRegistry());
        // When
        Map<Object, Object> extensionConfig = Collections.emptyMap();
        Map<String, Object> extensionConfigEntry = NamedValueSet.emptySet()
                .with(NAME_CONFIG.asValue("test"))
                .with(TYPE_CONFIG.asValue(TestReporter.class.getName()))
                .with(PRIORITY_CONFIG.asValue(1))
                .with(CONFIGURATION_CONFIG.asValue(extensionConfig))
                .asMap();

        Map<String, Object> config = new HashMap<>();
        config.put(JikkouConfigProperties.REPORTERS_CONFIG.key(), List.of(extensionConfigEntry));
        DefaultApi.Builder configured = configurator.configure(builder, Configuration.from(config));

        // Then
        Assertions.assertNotNull(configured);
        Assertions.assertTrue(registry.findDescriptorByClass(TestReporter.class, Qualifiers.byName("test")).isPresent());
        ChangeReporter actual = factory.getExtension(ChangeReporter.class, Qualifiers.byName("test"));
        Assertions.assertNotNull(actual);
    }

    public static final class TestReporter implements ChangeReporter {
        @Override
        public void report(List<ChangeResult> results) {

        }
    }
}