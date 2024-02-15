/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core;

import io.streamthoughts.jikkou.core.extension.ClassExtensionAliasesGenerator;
import io.streamthoughts.jikkou.core.extension.DefaultExtensionDescriptorFactory;
import io.streamthoughts.jikkou.core.extension.DefaultExtensionFactory;
import io.streamthoughts.jikkou.core.extension.DefaultExtensionRegistry;
import io.streamthoughts.jikkou.core.models.ApiGroup;
import io.streamthoughts.jikkou.core.models.ApiGroupList;
import io.streamthoughts.jikkou.core.models.ApiGroupVersion;
import io.streamthoughts.jikkou.core.models.ApiResource;
import io.streamthoughts.jikkou.core.models.ApiResourceList;
import io.streamthoughts.jikkou.core.models.ResourceType;
import io.streamthoughts.jikkou.core.resource.DefaultResourceRegistry;
import io.streamthoughts.jikkou.core.resource.ResourceDescriptor;
import java.util.Collections;
import java.util.List;
import java.util.Set;
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
}