/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.jikkou.core;

import io.jikkou.core.action.SystemTimeAction;
import io.jikkou.core.annotation.Provider;
import io.jikkou.core.converter.ResourceListConverter;
import io.jikkou.core.extension.ExtensionRegistry;
import io.jikkou.core.models.ConfigMap;
import io.jikkou.core.models.ConfigMapList;
import io.jikkou.core.policy.model.ValidatingResourcePolicy;
import io.jikkou.core.repository.GitHubResourceRepository;
import io.jikkou.core.repository.LocalResourceRepository;
import io.jikkou.core.resource.ResourceRegistry;
import io.jikkou.core.transform.ConfigMapsTransformation;
import io.jikkou.core.transform.EnrichMetadataTransformation;
import io.jikkou.core.transform.ExcludeIgnoreResourceTransformation;
import io.jikkou.spi.ExtensionProvider;
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
        registry.register(LocalResourceRepository.class, LocalResourceRepository::new);
        registry.register(GitHubResourceRepository.class, GitHubResourceRepository::new);
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public void registerResources(@NotNull ResourceRegistry registry) {
        registry.register(ConfigMap.class);
        registry.register(ConfigMapList.class);
        registry.register(ValidatingResourcePolicy.class);
    }
}
