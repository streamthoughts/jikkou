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
package io.streamthoughts.jikkou.core.resource;

import io.streamthoughts.jikkou.core.annotation.ApiVersion;
import io.streamthoughts.jikkou.core.annotation.Description;
import io.streamthoughts.jikkou.core.annotation.Kind;
import io.streamthoughts.jikkou.core.annotation.Names;
import io.streamthoughts.jikkou.core.extension.exceptions.ConflictingExtensionDefinitionException;
import io.streamthoughts.jikkou.core.extension.exceptions.NoSuchExtensionException;
import io.streamthoughts.jikkou.core.models.HasMetadata;
import io.streamthoughts.jikkou.core.models.ResourceType;
import io.streamthoughts.jikkou.core.models.Verb;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DefaultResourceRegistryTest {

    static final ResourceDescriptor DESCRIPTOR = new ResourceDescriptor(
            ResourceType.of(TestResource.class),
            "Test description",
            TestResource.class,
            "test",
            "tests",
            Set.of("t", "ts"),
            Set.of(Verb.LIST),
            false
    );

    private DefaultResourceRegistry registry;

    @BeforeEach
    void beforeEach() {
        this.registry = new DefaultResourceRegistry();
    }


    @Test
    void shouldReturnDescriptorAfterRegisteringResource() {
        ResourceDescriptor descriptor = registry.register(TestResource.class);
        Assertions.assertEquals(DESCRIPTOR, descriptor);
    }

    @Test
    void shouldThrowConflictingExtensionDefinitionExceptionForDuplicateResource() {
        registry.register(TestResource.class);
        Assertions.assertThrows(
                ConflictingExtensionDefinitionException.class,
                () -> registry.register(TestResource.class)
        );
    }

    @Test
    void shouldGetDescriptorByTypeForRegisteredResource() {
        registry.register(TestResource.class);
        ResourceDescriptor descriptor = registry.getResourceDescriptorByType(ResourceType.of(TestResource.class));
        Assertions.assertEquals(DESCRIPTOR, descriptor);
    }

    @Test
    void shouldThrowNoSuchExtensionExceptionForNotRegisteredResource() {
        Assertions.assertThrows(
                NoSuchExtensionException.class,
                () -> registry.getResourceDescriptorByType(ResourceType.of(TestResource.class)));
    }

    @Test
    void shouldGetAllDescriptors() {
        registry.register(TestResource.class);
        List<ResourceDescriptor> descriptors = registry.getAllResourceDescriptors();
        Assertions.assertEquals(List.of(DESCRIPTOR), descriptors);
    }

    @Test
    void shouldGetDescriptorForKindCaseSensitiveFalse() {
        registry.register(TestResource.class);
        Optional<ResourceDescriptor> optional = registry.findDescriptorByType(
                "test",
                "test.jikkou.io",
                "v1beta2",
                false);
        Assertions.assertTrue(optional.isPresent());
        Assertions.assertEquals(DESCRIPTOR, optional.get());
    }

    @Test
    void shouldGetDescriptorForKindCaseSensitiveTrue() {
        registry.register(TestResource.class);
        Optional<ResourceDescriptor> optional = registry.findDescriptorByType(
                "test",
                "test.jikkou.io",
                "v1beta2",
                true);
        Assertions.assertTrue(optional.isEmpty());
    }

    @Test
    void shouldGetDescriptorForType() {
        registry.register(TestResource.class);
        Optional<ResourceDescriptor> optional = registry.findDescriptorByType(ResourceType.of(TestResource.class));
        Assertions.assertTrue(optional.isPresent());
        Assertions.assertEquals(DESCRIPTOR, optional.get());
    }

    @ApiVersion("test.jikkou.io/v1beta2")
    @Kind("Test")
    @Description("Test description")
    @Names(
            plural = "tests",
            singular = "test",
            shortNames = { "t" , "ts" }
    )
    static abstract class TestResource implements HasMetadata {};
}