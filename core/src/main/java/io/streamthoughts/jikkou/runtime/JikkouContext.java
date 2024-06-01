/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.runtime;

import static io.streamthoughts.jikkou.runtime.JikkouConfigProperties.EXTENSIONS_PROVIDER_DEFAULT_ENABLED;
import static io.streamthoughts.jikkou.runtime.JikkouConfigProperties.EXTENSION_PATHS;
import static io.streamthoughts.jikkou.runtime.JikkouConfigProperties.EXTENSION_PROVIDER_CONFIG_PREFIX;

import io.streamthoughts.jikkou.core.ApiConfigurator;
import io.streamthoughts.jikkou.core.DefaultApi;
import io.streamthoughts.jikkou.core.JikkouApi;
import io.streamthoughts.jikkou.core.config.Configuration;
import io.streamthoughts.jikkou.core.extension.ExtensionClassLoader;
import io.streamthoughts.jikkou.core.extension.ExtensionFactory;
import io.streamthoughts.jikkou.core.extension.ExtensionGroupAwareRegistry;
import io.streamthoughts.jikkou.core.extension.ExtensionResolver;
import io.streamthoughts.jikkou.core.extension.ExternalExtension;
import io.streamthoughts.jikkou.core.resource.DefaultResourceRegistry;
import io.streamthoughts.jikkou.core.resource.LatestApiVersionResourceTypeResolver;
import io.streamthoughts.jikkou.core.resource.ResourceDescriptor;
import io.streamthoughts.jikkou.core.resource.ResourceDeserializer;
import io.streamthoughts.jikkou.core.resource.ResourceRegistry;
import io.streamthoughts.jikkou.spi.ExtensionProvider;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.ServiceLoader;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class JikkouContext {

    private static final Logger LOG = LoggerFactory.getLogger(JikkouContext.class);
    private final ExtensionFactory extensionFactory;
    private final Configuration configuration;
    private final ResourceRegistry resourceRegistry;

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

        List<String> paths = new ArrayList<>(extensionPaths);
        paths.addAll(EXTENSION_PATHS.get(configuration));

        Boolean extensionEnabledByDefault = EXTENSIONS_PROVIDER_DEFAULT_ENABLED.get(configuration);
        LOG.info("Start context initialization ({}={}).", EXTENSIONS_PROVIDER_DEFAULT_ENABLED.key(), extensionEnabledByDefault);
        Set<ClassLoader> cls = getAllClassLoaders(paths);

        for (ExtensionProvider provider : loadAllServices(ExtensionProvider.class, cls)) {
            loadExtensionProviders(provider, configuration, extensionEnabledByDefault);
        }

        ResourceDeserializer.registerResolverType(new LatestApiVersionResourceTypeResolver(resourceRegistry));

        resourceRegistry.allDescriptors()
                .stream()
                .filter(ResourceDescriptor::isEnabled)
                .forEach(desc -> ResourceDeserializer.registerKind(
                        desc.group() + "/" + desc.apiVersion(),
                        desc.kind(),
                        desc.resourceClass())
                );
        LOG.info("JikkouContext initialized.");
    }

    private void loadExtensionProviders(@NotNull ExtensionProvider provider,
                                        @NotNull Configuration configuration,
                                        boolean extensionEnabledByDefault) {
        final String name = provider.getName();
        final boolean isExtensionProviderEnabled = isExtensionProviderEnabled(configuration, name, extensionEnabledByDefault);
        if (isExtensionProviderEnabled) {
            LOG.info("Configuring extensions provider '{}'", name);
            provider.configure(configuration);

            LOG.info("Loading extensions from provider '{}'", name);
            provider.registerExtensions(new ExtensionGroupAwareRegistry(extensionFactory, name));

            LOG.info("Loading resources from provider '{}'", name);
            var registry = new DefaultResourceRegistry(false);
            provider.registerResources(registry);
            registry.allDescriptors().forEach(resourceRegistry::register);
        } else {
            LOG.warn(
                    "Extensions from provider '{}' are ignored (config setting '{}.{}.enabled' is set to 'false').",
                    name,
                    EXTENSION_PROVIDER_CONFIG_PREFIX,
                    name.toLowerCase(Locale.ROOT)
            );
            LOG.warn(
                    "Resources from provider '{}' are ignored (config setting '{}.{}.enabled' is set to 'false').",
                    name,
                    EXTENSION_PROVIDER_CONFIG_PREFIX,
                    name.toLowerCase(Locale.ROOT)
            );
        }

    }

    private static boolean isExtensionProviderEnabled(@NotNull Configuration configuration,
                                                      @NotNull String name,
                                                      boolean defaultValue) {
        String property = String.format(EXTENSION_PROVIDER_CONFIG_PREFIX + ".%s.enabled", name.toLowerCase(Locale.ROOT));
        return configuration.findBoolean(property).orElse(defaultValue);
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
        return createApi(new DefaultApi.Builder(extensionFactory, resourceRegistry), configurators);
    }

    public <A extends JikkouApi, B extends JikkouApi.ApiBuilder<A, B>> A createApi(B builder,
                                                                                   ApiConfigurator... configurators) {
        LOG.info("Start JikkouApi configuration.");
        A api = ApiConfigurator.emptyList()
                .with(configurators)
                .configure(builder, configuration)
                .build();
        LOG.info("JikkouApi configuration completed.");
        return api;
    }

    /**
     * @return the set of known {@link ClassLoader}.
     */
    private Set<ClassLoader> getAllClassLoaders(List<String> extensionPaths) {
        Set<ClassLoader> classLoaders = new HashSet<>();
        classLoaders.add(JikkouContext.class.getClassLoader());
        classLoaders.addAll(getClassLoadersForPath(extensionPaths));
        return classLoaders;
    }

    /**
     * Loads all services for the given type using the standard Java {@link java.util.ServiceLoader} mechanism.
     *
     * @param type the service Class type.
     * @param <T>  the service type.
     * @return the services implementing the given type.
     */
    private <T> List<T> loadAllServices(final Class<T> type, final Set<ClassLoader> classLoaders) {
        final List<T> loaded = new LinkedList<>();
        final Set<Class<? extends T>> types = new HashSet<>();
        for (ClassLoader cl : classLoaders) {
            ServiceLoader<T> serviceLoader = ServiceLoader.load(type, cl);
            var providers = serviceLoader.stream().toList();
            for (ServiceLoader.Provider<T> provider : providers) {
                if (!types.contains(provider.type())) {
                    types.add(provider.type());
                    loaded.add(provider.get());
                }
            }
        }
        return loaded;
    }

    private Set<ClassLoader> getClassLoadersForPath(List<String> extensionPaths) {
        Set<ClassLoader> classLoaders = new HashSet<>();
        for (final String path : extensionPaths) {
            try {
                final Path extensionPath = Paths.get(path).toAbsolutePath();
                classLoaders.addAll(getClassLoadersForPaths(extensionPath));
            } catch (InvalidPathException e) {
                LOG.error("Ignoring top-level extension location '{}', invalid path.", path);
            }
        }
        return classLoaders;
    }

    private Set<ClassLoader> getClassLoadersForPaths(Path extensionPath) {
        ExtensionResolver resolver = new ExtensionResolver(extensionPath);
        final List<ExternalExtension> extensions = resolver.resolves();
        Set<ClassLoader> classLoaders = new HashSet<>();
        for (ExternalExtension extension : extensions) {
            LOG.info("Loading extension from path : {}", extension.location());
            final ExtensionClassLoader classLoader = ExtensionClassLoader.newClassLoader(
                    extension.location(),
                    extension.resources(),
                    JikkouContext.class.getClassLoader()
            );
            LOG.info("Initialized new ClassLoader: {}", classLoader);
            classLoaders.add(classLoader);
        }
        return classLoaders;
    }
}
