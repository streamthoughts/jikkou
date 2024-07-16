/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.resource;

import io.streamthoughts.jikkou.core.annotation.ApiVersion;
import io.streamthoughts.jikkou.core.annotation.Description;
import io.streamthoughts.jikkou.core.annotation.Kind;
import io.streamthoughts.jikkou.core.annotation.Names;
import io.streamthoughts.jikkou.core.models.HasMetadata;
import io.streamthoughts.jikkou.core.models.ResourceList;
import io.streamthoughts.jikkou.core.models.ResourceType;
import java.util.Locale;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ResourceDescriptorTest {

    public static final String TEST_KIND = "Test";

    @Test
    void shouldReturnLowercaseKindWhenNoSingularName() {
        ResourceDescriptor descriptor = new ResourceDescriptor(
                ResourceType.of(TestResource.class),
                "",
                TestResource.class
        );
        Assertions.assertEquals(TEST_KIND.toLowerCase(Locale.ROOT), descriptor.singularName());
    }

    @Test
    void shouldReturnFalseWhenVerifyResourceListObjectForResource() {
        ResourceDescriptor descriptor = new ResourceDescriptor(
                ResourceType.of(TestResource.class),
                "",
                TestResource.class
        );
        Assertions.assertFalse(descriptor.isResourceListObject());
    }

    @Test
    void shouldReturnTrueWhenVerifyResourceListObjectForResourceList() {
        ResourceDescriptor descriptor = new ResourceDescriptor(
                ResourceType.of(TestListResource.class),
                "",
                TestListResource.class
        );
        Assertions.assertTrue(descriptor.isResourceListObject());
    }

    @ApiVersion("test.jikkou.io/v1beta2")
    @Kind("TestList")
    static abstract class TestListResource implements ResourceList<HasMetadata> {
    }

    @ApiVersion("test.jikkou.io/v1beta2")
    @Kind("Test")
    @Description("Test description")
    @Names(
            plural = "tests",
            singular = "test",
            shortNames = {"t", "ts"}
    )
    static abstract class TestResource implements HasMetadata {}
}
