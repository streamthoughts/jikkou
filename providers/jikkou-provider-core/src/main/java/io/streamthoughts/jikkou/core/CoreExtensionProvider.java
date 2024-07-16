/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core;

import io.streamthoughts.jikkou.core.action.SystemTimeAction;
import io.streamthoughts.jikkou.core.annotation.Provider;
import io.streamthoughts.jikkou.core.converter.ResourceListConverter;
import io.streamthoughts.jikkou.core.extension.ExtensionRegistry;
import io.streamthoughts.jikkou.core.models.ConfigMap;
import io.streamthoughts.jikkou.core.models.ConfigMapList;
import io.streamthoughts.jikkou.core.resource.ResourceRegistry;
import io.streamthoughts.jikkou.core.transform.ConfigMapsTransformation;
import io.streamthoughts.jikkou.core.transform.EnrichMetadataTransformation;
import io.streamthoughts.jikkou.core.transform.ExcludeIgnoreResourceTransformation;
import io.streamthoughts.jikkou.spi.ExtensionProvider;
import org.jetbrains.annotations.NotNull;

@Provider(
    name = "core",
    description = "Core Extension provider",
    tags = {"Jikkou"}
)
public final class CoreExtensionProvider implements ExtensionProvider {

    /**
     * {@inheritDoc}
     **/
    @Override
    public void registerExtensions(@NotNull ExtensionRegistry registry) {
        registry.register(ExcludeIgnoreResourceTransformation.class, ExcludeIgnoreResourceTransformation::new);
        registry.register(ConfigMapsTransformation.class, ConfigMapsTransformation::new);
        registry.register(EnrichMetadataTransformation.class, EnrichMetadataTransformation::new);
        registry.register(ResourceListConverter.class, ResourceListConverter::new);
        registry.register(SystemTimeAction.class, SystemTimeAction::new);
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public void registerResources(@NotNull ResourceRegistry registry) {
        registry.register(ConfigMap.class);
        registry.register(ConfigMapList.class);
    }
}
