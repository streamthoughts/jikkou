/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core;

import io.streamthoughts.jikkou.core.annotation.Provider;
import io.streamthoughts.jikkou.core.config.ConfigProperty;
import io.streamthoughts.jikkou.core.config.Configuration;
import io.streamthoughts.jikkou.core.extension.ClassExtensionAliasesGenerator;
import io.streamthoughts.jikkou.core.extension.DefaultExtensionDescriptorFactory;
import io.streamthoughts.jikkou.core.extension.DefaultExtensionFactory;
import io.streamthoughts.jikkou.core.extension.DefaultExtensionRegistry;
import io.streamthoughts.jikkou.core.extension.Extension;
import io.streamthoughts.jikkou.core.extension.ExtensionRegistry;
import io.streamthoughts.jikkou.core.extension.exceptions.NoSuchExtensionException;
import io.streamthoughts.jikkou.core.models.ApiGroup;
import io.streamthoughts.jikkou.core.models.ApiGroupList;
import io.streamthoughts.jikkou.core.models.ApiGroupVersion;
import io.streamthoughts.jikkou.core.models.ApiOptionSpec;
import io.streamthoughts.jikkou.core.models.ApiProvider;
import io.streamthoughts.jikkou.core.models.ApiProviderSpec;
import io.streamthoughts.jikkou.core.models.ApiResource;
import io.streamthoughts.jikkou.core.models.ApiResourceList;
import io.streamthoughts.jikkou.core.models.ResourceType;
import io.streamthoughts.jikkou.core.resource.DefaultResourceRegistry;
import io.streamthoughts.jikkou.core.resource.ResourceDescriptor;
import io.streamthoughts.jikkou.core.resource.ResourceRegistry;
import io.streamthoughts.jikkou.spi.ExtensionProvider;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class DefaultApiTest {

    private final DefaultExtensionFactory factory = new DefaultExtensionFactory(
            new DefaultExtensionRegistry(
                    new DefaultExtensionDescriptorFactory(),
                    new ClassExtensionAliasesGenerator())
    );

    @Test
    public void shouldListApiGroups() {
        DefaultResourceRegistry resourceRegistry = new DefaultResourceRegistry();
        resourceRegistry.register(new ResourceDescriptor(
                new ResourceType("Test", "core", "v1"),
                "",
                TestResource.class
        ));
        resourceRegistry.register(new ResourceDescriptor(
                new ResourceType("Test", "core", "v2"),
                "",
                TestResource.class
        ));
        DefaultApi api = new DefaultApi.Builder(factory, resourceRegistry).build();
        ApiGroupList apiGroupList = api.listApiGroups();
        ApiGroupList expected = new ApiGroupList(List.of(
                new ApiGroup(
                        "core",
                        Set.of(
                                new ApiGroupVersion("core/v1", "v1"),
                                new ApiGroupVersion("core/v2", "v2")
                        )
                )
            )
        );
        Assertions.assertEquals(expected, apiGroupList);
    }

    @Test
    public void shouldListApiResources() {
        DefaultResourceRegistry resourceRegistry = new DefaultResourceRegistry();
        resourceRegistry.register(new ResourceDescriptor(
                new ResourceType("Test", "core", "v1"),
                "",
                TestResource.class
        ));
        resourceRegistry.register(new ResourceDescriptor(
                new ResourceType("Test", "core", "v2"),
                "",
                TestResource.class
        ));
        DefaultApi api = new DefaultApi.Builder(factory, resourceRegistry).build();
        List<ApiResourceList> resources = api.listApiResources();
        List<ApiResourceList> expected = List.of(
                new ApiResourceList("core/v1", List.of(new ApiResource(
                        "test",
                        "Test",
                        "test",
                        Collections.emptySet(),
                        "",
                        Collections.emptySet()
                ))),
                new ApiResourceList("core/v2", List.of(new ApiResource(
                        "test",
                        "Test",
                        "test",
                        Collections.emptySet(),
                        "",
                        Collections.emptySet()
                )))
        );
        Assertions.assertEquals(expected, resources);
    }

    @Test
    public void shouldGetApiProvider() {
        DefaultResourceRegistry resourceRegistry = new DefaultResourceRegistry();
        DefaultApi.Builder builder = new DefaultApi.Builder(factory, resourceRegistry);
        builder.register(new TestExtensionProvider(), Configuration.empty());
        DefaultApi api = builder.build();

        ApiProvider result = api.getApiProvider("test-provider");

        Assertions.assertNotNull(result);
        Assertions.assertEquals("ApiProvider", result.kind());
        Assertions.assertEquals("core.jikkou.io/v1", result.apiVersion());

        ApiProviderSpec spec = result.spec();
        Assertions.assertEquals("test-provider", spec.name());
        Assertions.assertEquals(TestExtensionProvider.class.getName(), spec.type());
        Assertions.assertEquals("A test provider", spec.description());
        Assertions.assertEquals(List.of("test", "unit"), spec.tags());
        Assertions.assertEquals("https://example.com/docs", spec.externalDocs());
        Assertions.assertFalse(spec.extensions().isEmpty());

        Assertions.assertEquals(1, spec.options().size());
        ApiOptionSpec option = spec.options().get(0);
        Assertions.assertEquals("test.url", option.name());
        Assertions.assertEquals("The test URL", option.description());
        Assertions.assertEquals("String", option.type());
        Assertions.assertTrue(option.required());
    }

    @Test
    public void shouldThrowNoSuchExtensionExceptionForUnknownProvider() {
        DefaultResourceRegistry resourceRegistry = new DefaultResourceRegistry();
        DefaultApi api = new DefaultApi.Builder(factory, resourceRegistry).build();

        Assertions.assertThrows(NoSuchExtensionException.class,
                () -> api.getApiProvider("non-existent-provider"));
    }

    @Provider(
            name = "test-provider",
            description = "A test provider",
            tags = {"test", "unit"},
            externalDocs = "https://example.com/docs"
    )
    public static class TestExtensionProvider implements ExtensionProvider {

        @Override
        public List<ConfigProperty<?>> configProperties() {
            return List.of(
                    ConfigProperty.ofString("test.url")
                            .description("The test URL")
                            .required(true)
            );
        }

        @Override
        public void registerExtensions(@NotNull ExtensionRegistry registry) {
            registry.register(TestExtension.class, TestExtension::new);
        }

        @Override
        public void registerResources(@NotNull ResourceRegistry registry) {
        }
    }

    static class TestExtension implements Extension {
    }
}