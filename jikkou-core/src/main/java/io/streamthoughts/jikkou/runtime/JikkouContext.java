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
package io.streamthoughts.jikkou.runtime;

import static io.streamthoughts.jikkou.runtime.JikkouConfigProperties.EXTENSIONS_PROVIDER_DEFAULT_ENABLED;

import io.streamthoughts.jikkou.core.ApiConfigurator;
import io.streamthoughts.jikkou.core.DefaultApi;
import io.streamthoughts.jikkou.core.JikkouApi;
import io.streamthoughts.jikkou.core.config.Configuration;
import io.streamthoughts.jikkou.core.extension.ExtensionClassLoader;
import io.streamthoughts.jikkou.core.extension.ExtensionFactory;
import io.streamthoughts.jikkou.core.extension.ExtensionResolver;
import io.streamthoughts.jikkou.core.extension.ExternalExtension;
import io.streamthoughts.jikkou.core.io.ResourceDeserializer;
import io.streamthoughts.jikkou.core.resource.DefaultResourceRegistry;
import io.streamthoughts.jikkou.core.resource.ResourceDescriptor;
import io.streamthoughts.jikkou.core.resource.ResourceRegistry;
import io.streamthoughts.jikkou.spi.ExtensionProvider;
import io.streamthoughts.jikkou.spi.ResourceProvider;
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

    private final List<String> extensionPaths;
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
                         @NotNull final ExtensionFactory extensionFactory) {
        this(configuration, extensionFactory, new ArrayList<>());
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
                         @NotNull final List<String> extensionPaths) {
        this.configuration = Objects.requireNonNull(configuration, "'configuration' must not be null");
        this.extensionPaths = Objects.requireNonNull(extensionPaths, "'extensionPaths' must not be null");
        this.extensionFactory = Objects.requireNonNull(extensionFactory, "'extensionFactory' must not be null");

        Boolean extensionEnabledByDefault = EXTENSIONS_PROVIDER_DEFAULT_ENABLED.evaluate(configuration);
        LOG.info("Start context initialization ({}={}).", EXTENSIONS_PROVIDER_DEFAULT_ENABLED.key(), extensionEnabledByDefault);
        Set<ClassLoader> cls = getAllClassLoaders();
        loadAllServices(ExtensionProvider.class, cls)
                .forEach(provider -> {
                    final String name = provider.getName();
                    if (isExtensionProviderEnabled(configuration, name, extensionEnabledByDefault)) {
                        LOG.info("Loading all '{}' extensions", name);
                        provider.registerExtensions(extensionFactory, configuration);
                    } else {
                        LOG.info(
                                "Extensions for group '{}' are ignored (config setting 'extensions.provider.{}.enabled' is set to 'false').",
                                name,
                                name
                        );
                    }
                });


        resourceRegistry = new DefaultResourceRegistry();
        loadAllServices(ResourceProvider.class, cls)
            .stream()
            .flatMap(resourceProvider -> {
                final String name = resourceProvider.getName();
                LOG.info("Loading resources from ResourceProvider '{}'", name);
                Boolean extensionEnabled = isExtensionProviderEnabled(configuration, name, extensionEnabledByDefault);
                if (!extensionEnabled) {
                    LOG.info(
                            "Resources for group '{}' are disabled (config setting 'extensions.provider.{}.enabled' is set to 'false').",
                            name,
                            name
                    );
                }
                var registry = new DefaultResourceRegistry(false);
                resourceProvider.registerAll(registry);
                return registry.getAllResourceDescriptors()
                        .stream()
                        .peek(descriptor -> descriptor.isEnabled(extensionEnabled));

            })
            .forEach(resourceRegistry::register);

        resourceRegistry.getAllResourceDescriptors()
                .stream()
                .filter(ResourceDescriptor::isEnabled)
                .forEach(desc -> ResourceDeserializer.registerKind(
                        desc.group() + "/" + desc.apiVersion(),
                        desc.kind(),
                        desc.resourceClass())
                );
        LOG.info("JikkouContext initialized.");
    }

    @NotNull
    private static Boolean isExtensionProviderEnabled(@NotNull Configuration configuration,
                                                      @NotNull String name,
                                                      boolean defaultValue) {
        String property = String.format("extensions.provider.%s.enabled", name.toLowerCase(Locale.ROOT));
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
    public @NotNull ResourceRegistry getResourceContext() {
        return resourceRegistry;
    }

    public JikkouApi createApi(ApiConfigurator... configurators) {
        LOG.info("Start JikkouApi configuration.");
        DefaultApi api = ApiConfigurator.emptyList()
                .with(configurators)
                .configure(DefaultApi.builder(extensionFactory), configuration)
                .build();
        LOG.info("JikkouApi configuration completed.");
        return api;
    }

    /**
     * @return the set of known {@link ClassLoader}.
     */
    private Set<ClassLoader> getAllClassLoaders() {
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
