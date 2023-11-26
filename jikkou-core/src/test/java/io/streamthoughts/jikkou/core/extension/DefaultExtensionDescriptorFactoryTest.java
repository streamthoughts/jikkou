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

import io.streamthoughts.jikkou.core.annotation.Description;
import io.streamthoughts.jikkou.core.annotation.Enabled;
import io.streamthoughts.jikkou.core.annotation.Named;
import io.streamthoughts.jikkou.core.annotation.Title;
import io.streamthoughts.jikkou.core.config.ConfigPropertySpec;
import io.streamthoughts.jikkou.core.extension.annotations.ExtensionOptionSpec;
import io.streamthoughts.jikkou.core.extension.annotations.ExtensionSpec;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class DefaultExtensionDescriptorFactoryTest {

    /**
     * @see io.streamthoughts.jikkou.common.annotation.InterfaceStability
     */
    public static final ExtensionAttribute EVOLVING_EXTENSION = new ExtensionAttribute("evolving");
    public static final String DESCRIPTION = "This is a description";
    public static final String TITLE = "This is a title";
    public static final String NAME = "TestExtension";

    @Test
    void shouldGetCategory() {
        Assertions.assertEquals(ExtensionCategory.EXTENSION,
                DefaultExtensionDescriptorFactory.getCategory(TestAnnotatedExtension.class));
    }

    @Test
    void shouldGetTitle() {
        Assertions.assertEquals(TITLE,
                DefaultExtensionDescriptorFactory.getTitle(TestAnnotatedExtension.class));
    }

    @Test
    void shouldGetDescription() {
        Assertions.assertEquals(DESCRIPTION,
                DefaultExtensionDescriptorFactory.getDescription(TestAnnotatedExtension.class));
    }

    @Test
    void shouldGetConfigProperties() {
        Assertions.assertEquals(
                List.of(new ConfigPropertySpec(
                        "Test",
                        String.class,
                        "Description",
                        "default",
                        false
                )),
                DefaultExtensionDescriptorFactory.getConfigProperties(TestAnnotatedExtension.class)
        );
    }

    @Test
    void shouldGetIsEnabled() {
        Assertions.assertTrue(DefaultExtensionDescriptorFactory.isEnabled(TestAnnotatedExtension.class));
    }

    @Test
    void shouldGetExtensionMetadata() {
        ExtensionMetadata metadata = DefaultExtensionDescriptorFactory
                .getExtensionMetadata(TestAnnotatedExtension.class);

        Assertions.assertNotNull(metadata.attributesForName("named"));
        Assertions.assertNotNull(metadata.attributesForName("title"));
        Assertions.assertNotNull(metadata.attributesForName("description"));
        Assertions.assertNotNull(metadata.attributesForName("enabled"));
        Assertions.assertNotNull(metadata.attributesForName("category"));

    }

    @Test
    void shouldCreateDescriptorForExtensionWithAnnotations() {
        // Given
        DefaultExtensionDescriptorFactory factory = new DefaultExtensionDescriptorFactory();

        // When
        ExtensionDescriptor<TestAnnotatedExtension> descriptor = factory.make(TestAnnotatedExtension.class, TestAnnotatedExtension::new);

        // Then
        Assertions.assertNotNull(descriptor);
        Assertions.assertNotNull(descriptor.supplier());
        Assertions.assertNotNull(descriptor.classLoader());
        Assertions.assertEquals(NAME, descriptor.name());
        Assertions.assertEquals(DESCRIPTION, descriptor.description());
        Assertions.assertEquals(TITLE, descriptor.title());
        Assertions.assertNotNull(descriptor.metadata());
    }

    @Named(NAME)
    @Title(TITLE)
    @Description(DESCRIPTION)
    @Enabled
    @ExtensionSpec(
            options = {
                    @ExtensionOptionSpec(
                            name = "Test",
                            description = "Description",
                            type = String.class,
                            defaultValue = "default"
                    )
            }
    )
    public static class TestAnnotatedExtension implements Extension {
    }

}