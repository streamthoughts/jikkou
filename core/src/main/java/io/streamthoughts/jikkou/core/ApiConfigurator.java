/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core;

import io.streamthoughts.jikkou.core.config.Configuration;

/**
 * Interface that can be used to configure {@link JikkouApi} using a given
 * {@link JikkouApi.ApiBuilder}, and a {@link Configuration}.
 */
public interface ApiConfigurator {

    /**
     * Configures the {@link JikkouApi.ApiBuilder}.
     *
     * @param builder       the {@link JikkouApi.ApiBuilder}.
     * @param configuration the configuration.
     *
     * @return      the {@link JikkouApi.ApiBuilder}.
     * @param <A>   type of the {@link JikkouApi}.
     * @param <B>   type of the {@link JikkouApi.ApiBuilder}.
     */
    <A extends JikkouApi, B extends JikkouApi.ApiBuilder<A, B>> B configure(
            B builder,
            Configuration configuration
    );

    static ApiConfiguratorList emptyList() {
        return new ApiConfiguratorList();
    }

    static ApiConfiguratorList listOf(final ApiConfigurator configurator) {
        return new ApiConfiguratorList(java.util.List.of(configurator));
    }

    static ApiConfiguratorList listOf(final java.util.List<ApiConfigurator> configurators) {
        return new ApiConfiguratorList(configurators);
    }
}
