/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.runtime.configurator;

import io.streamthoughts.jikkou.core.ApiConfigurator;
import io.streamthoughts.jikkou.core.JikkouApi;
import io.streamthoughts.jikkou.core.extension.ExtensionDescriptorRegistry;
import io.streamthoughts.jikkou.core.repository.ResourceRepository;
import io.streamthoughts.jikkou.runtime.JikkouConfigProperties;

/**
 * An {@link ApiConfigurator} used to configure {@link JikkouApi} with all {@link ResourceRepository}
 * dynamically passed through the CLI configuration.
 */
public final class RepositoryApiConfigurator extends ExtensionApiConfigurator<ResourceRepository> {

    /**
     * Creates a new {@link RepositoryApiConfigurator} instance.
     *
     * @param registry the ExtensionDescriptorRegistry instance.
     */
    public RepositoryApiConfigurator(final ExtensionDescriptorRegistry registry) {
        super(registry, JikkouConfigProperties.REPOSITORIES_CONFIG);
    }
}