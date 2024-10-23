/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.runtime;

import static io.streamthoughts.jikkou.runtime.JikkouConfigProperties.EXTENSION_PATHS;

import io.streamthoughts.jikkou.core.ApiConfigurator;
import io.streamthoughts.jikkou.core.DefaultApi;
import io.streamthoughts.jikkou.core.JikkouApi;
import io.streamthoughts.jikkou.core.config.Configuration;
import io.streamthoughts.jikkou.core.extension.ClassExtensionAliasesGenerator;
import io.streamthoughts.jikkou.core.extension.DefaultExtensionDescriptorFactory;
import io.streamthoughts.jikkou.core.extension.DefaultExtensionFactory;
import io.streamthoughts.jikkou.core.extension.DefaultExtensionRegistry;
import io.streamthoughts.jikkou.core.extension.ExtensionFactory;
import io.streamthoughts.jikkou.core.resource.DefaultResourceRegistry;
import io.streamthoughts.jikkou.core.resource.LatestApiVersionResourceTypeResolver;
import io.streamthoughts.jikkou.core.resource.ResourceDeserializer;
import io.streamthoughts.jikkou.core.resource.ResourceRegistry;
import io.streamthoughts.jikkou.runtime.configurator.ProviderApiConfigurator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class JikkouContext {

    private static final Logger LOG = LoggerFactory.getLogger(JikkouContext.class);
    private final ExtensionFactory extensionFactory;
    private final Configuration configuration;
    private final ResourceRegistry resourceRegistry;
    private final List<String> extensionPaths;

    /**
     * Static helper method for constructing a new {@link JikkouContext}.
     *
     * @return a new {@link JikkouContext}.
     */
    public static JikkouContext defaultContext() {
        return defaultContext(Configuration.empty());
    }

    /**
     * Static helper method for constructing a new {@link JikkouContext}.
     *
     * @return a new {@link JikkouContext}.
     */
    public static JikkouContext defaultContext(Configuration configuration) {
        DefaultExtensionRegistry extensionRegistry = new DefaultExtensionRegistry(
            new DefaultExtensionDescriptorFactory(),
            new ClassExtensionAliasesGenerator()
        );
        DefaultExtensionFactory extensionFactory = new DefaultExtensionFactory(extensionRegistry);
        return new JikkouContext(configuration, extensionFactory, new DefaultResourceRegistry());
    }

    /**
     * Creates a new {@link JikkouContext} instance.
     *
     * @param configuration    the configuration.
     * @param extensionFactory the extension factory.
     */
    public JikkouContext(@NotNull final Configuration configuration,
                         @NotNull final ExtensionFactory extensionFactory,
                         @NotNull final ResourceRegistry resourceRegistry) {
        this(configuration, extensionFactory, resourceRegistry, new ArrayList<>());
    }

    /**
     * Creates a new {@link JikkouContext} instance.
     *
     * @param configuration    the configuration.
     * @param extensionFactory the extension factory.
     * @param extensionPaths   the list of external paths from which to load extensions.
     */
    public JikkouContext(@NotNull final Configuration configuration,
                         @NotNull final ExtensionFactory extensionFactory,
                         @NotNull final ResourceRegistry resourceRegistry,
                         @NotNull final List<String> extensionPaths) {
        this.configuration = Objects.requireNonNull(configuration, "'configuration' must not be null");
        this.extensionFactory = Objects.requireNonNull(extensionFactory, "'extensionFactory' must not be null");
        this.resourceRegistry = Objects.requireNonNull(resourceRegistry, "'resourceRegistry' must not be null");
        Objects.requireNonNull(extensionPaths, "'extensionPaths' must not be null");

        ResourceDeserializer.registerResolverType(new LatestApiVersionResourceTypeResolver(resourceRegistry));

        this.extensionPaths = new ArrayList<>(extensionPaths);
        this.extensionPaths.addAll(EXTENSION_PATHS.get(configuration));
    }

    /**
     * Gets the extension factory.
     *
     * @return an {@link ExtensionFactory} instance.
     */
    public @NotNull ExtensionFactory getExtensionFactory() {
        return extensionFactory;
    }

    /**
     * Gets the resource context.
     *
     * @return an {@link ResourceRegistry} instance.
     */
    public @NotNull ResourceRegistry getResourceRegistry() {
        return resourceRegistry;
    }

    public JikkouApi createApi(ApiConfigurator... configurators) {
        return createApi(newApiBuilder(), configurators) ;
    }

    public DefaultApi.Builder newApiBuilder() {
        return new DefaultApi.Builder(extensionFactory, resourceRegistry);
    }

    private @NotNull List<ApiConfigurator> defaultApiConfigurators() {
        List<ApiConfigurator> defaultApiConfigurators = new ArrayList<>();
        defaultApiConfigurators.add(new ProviderApiConfigurator(extensionPaths));
        return defaultApiConfigurators;
    }

    public <A extends JikkouApi, B extends JikkouApi.ApiBuilder<A, B>> A createApi(B builder,
                                                                                   ApiConfigurator... configurators) {
        LOG.info("Start JikkouApi configuration.");
        List<ApiConfigurator> allConfigurators = defaultApiConfigurators();
        allConfigurators.addAll(Arrays.stream(configurators).toList());

        A api = ApiConfigurator.emptyList()
            .with(allConfigurators.toArray(new ApiConfigurator[]{}))
            .configure(builder, configuration)
            .build();
        
        LOG.info("JikkouApi configuration completed.");
        return api;
    }
}
