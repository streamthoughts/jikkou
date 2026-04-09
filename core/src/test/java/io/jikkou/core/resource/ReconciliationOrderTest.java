/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.jikkou.core.resource;

import io.jikkou.core.annotation.ApiVersion;
import io.jikkou.core.annotation.Kind;
import io.jikkou.core.annotation.ReconciliationOrder;
import io.jikkou.core.models.HasMetadata;
import io.jikkou.core.models.HasPriority;
import io.jikkou.core.models.ResourceType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ReconciliationOrderTest {

    private final ResourceDescriptorFactory factory = new ResourceDescriptorFactory();

    @Test
    void shouldExtractReconciliationOrderFromAnnotation() {
        ResourceDescriptor descriptor = factory.make(
                ResourceType.of(OrderedResource.class), OrderedResource.class);
        Assertions.assertEquals(100, descriptor.reconciliationOrder());
    }

    @Test
    void shouldDefaultToNoOrderWhenAnnotationAbsent() {
        ResourceDescriptor descriptor = factory.make(
                ResourceType.of(UnorderedResource.class), UnorderedResource.class);
        Assertions.assertEquals(HasPriority.NO_ORDER, descriptor.reconciliationOrder());
    }

    @Test
    void shouldPreserveOrderThroughSetter() {
        ResourceDescriptor descriptor = new ResourceDescriptor(
                ResourceType.of(UnorderedResource.class), "", UnorderedResource.class);
        descriptor.setReconciliationOrder(42);
        Assertions.assertEquals(42, descriptor.reconciliationOrder());
    }

    @ApiVersion("test.jikkou.io/v1")
    @Kind("Ordered")
    @ReconciliationOrder(100)
    static abstract class OrderedResource implements HasMetadata {}

    @ApiVersion("test.jikkou.io/v1")
    @Kind("Unordered")
    static abstract class UnorderedResource implements HasMetadata {}
}
