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
package io.streamthoughts.jikkou.core;

import io.streamthoughts.jikkou.core.config.Configuration;
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

public class CoreExtensionProvider implements ExtensionProvider {
    
    /**
     * {@inheritDoc}
     **/
    @Override
    public void registerExtensions(@NotNull ExtensionRegistry registry,
                                   @NotNull Configuration configuration) {
        registry.register(ExcludeIgnoreResourceTransformation.class, ExcludeIgnoreResourceTransformation::new);
        registry.register(ConfigMapsTransformation.class, ConfigMapsTransformation::new);
        registry.register(EnrichMetadataTransformation.class, EnrichMetadataTransformation::new);
        registry.register(ResourceListConverter.class, ResourceListConverter::new);
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
