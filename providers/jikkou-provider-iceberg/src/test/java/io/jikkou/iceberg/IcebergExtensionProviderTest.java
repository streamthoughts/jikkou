/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.jikkou.iceberg;

import io.jikkou.core.extension.DefaultExtensionDescriptorFactory;
import io.jikkou.core.extension.DefaultExtensionRegistry;
import io.jikkou.core.resource.DefaultResourceRegistry;
import io.jikkou.core.resource.ResourceRegistry;
import io.jikkou.iceberg.health.IcebergCatalogHealthIndicator;
import io.jikkou.iceberg.namespace.IcebergNamespaceCollector;
import io.jikkou.iceberg.namespace.IcebergNamespaceController;
import io.jikkou.iceberg.table.IcebergTableCollector;
import io.jikkou.iceberg.table.IcebergTableController;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class IcebergExtensionProviderTest {

    @Test
    void shouldRegisterAllExtensions() {
        IcebergExtensionProvider provider = new IcebergExtensionProvider();
        DefaultExtensionRegistry registry = new DefaultExtensionRegistry(
            new DefaultExtensionDescriptorFactory(), null);

        provider.registerExtensions(registry);

        Assertions.assertTrue(registry.findDescriptorByClass(IcebergNamespaceCollector.class).isPresent());
        Assertions.assertTrue(registry.findDescriptorByClass(IcebergTableCollector.class).isPresent());
        Assertions.assertTrue(registry.findDescriptorByClass(IcebergNamespaceController.class).isPresent());
        Assertions.assertTrue(registry.findDescriptorByClass(IcebergTableController.class).isPresent());
        Assertions.assertTrue(registry.findDescriptorByClass(IcebergCatalogHealthIndicator.class).isPresent());
    }

    @Test
    void shouldRegisterAllResources() {
        IcebergExtensionProvider provider = new IcebergExtensionProvider();
        ResourceRegistry registry = new DefaultResourceRegistry();

        provider.registerResources(registry);

        // Verify that resource descriptors have been registered (namespace, table, their lists, and changes)
        Assertions.assertTrue(registry.allDescriptors().size() >= 4,
            "Expected at least 4 resource descriptors, got: " + registry.allDescriptors().size());
    }
}
