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
package io.streamthoughts.jikkou.api;

import io.streamthoughts.jikkou.api.config.Configuration;

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
