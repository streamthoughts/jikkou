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

import static io.streamthoughts.jikkou.common.utils.CollectionUtils.cast;

import io.streamthoughts.jikkou.api.config.Configuration;
import io.streamthoughts.jikkou.api.control.ResourceCollector;
import io.streamthoughts.jikkou.api.control.ResourceController;
import io.streamthoughts.jikkou.api.extensions.DefaultExtensionFactory;
import io.streamthoughts.jikkou.api.extensions.Extension;
import io.streamthoughts.jikkou.api.extensions.ExtensionClassLoader;
import io.streamthoughts.jikkou.api.extensions.ExtensionDescriptor;
import io.streamthoughts.jikkou.api.extensions.ExtensionFactory;
import io.streamthoughts.jikkou.api.extensions.ExtensionResolver;
import io.streamthoughts.jikkou.api.extensions.ExternalExtension;
import io.streamthoughts.jikkou.api.io.ResourceDeserializer;
import io.streamthoughts.jikkou.api.reporter.ChangeReporter;
import io.streamthoughts.jikkou.api.transform.ResourceTransformation;
import io.streamthoughts.jikkou.api.validation.ResourceValidation;
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
    private final ExtensionFactory extensionFactory = new DefaultExtensionFactory();
    private final Configuration configuration;
    private final ResourceContext resourceContext;

    /**
     * Creates a new {@link JikkouContext} instance.
     */
    public JikkouContext(@NotNull final Configuration configuration) {
        this(configuration, new ArrayList<>());
    }

    /**
     * Creates a new {@link JikkouContext} instance.
     *
     * @param extensionPaths a list of external paths from which to load extensions.
     */
    public JikkouContext(@NotNull final Configuration configuration,
                         @NotNull final List<String> extensionPaths) {
        this.configuration = Objects.requireNonNull(configuration, "'configuration' must not be null");
        this.extensionPaths = Objects.requireNonNull(extensionPaths, "'extensionPaths' must not be null");
        this.resourceContext = new ResourceContext();

        Set<ClassLoader> cls = getAllClassLoaders();
        loadAllServices(ExtensionProvider.class, cls)
                .forEach(provider -> {
                    final String name = provider.getName();
                    if (isExtensionEnabled(configuration, name)) {
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

        loadAllServices(ResourceProvider.class, cls)
                .forEach(provider -> {
                    final String name = provider.getName();
                    if (isExtensionEnabled(configuration, name)) {
                        LOG.info("Loading all '{}' resources", name);
                        provider.registerAll(resourceContext);
                    } else {
                        LOG.info(
                                "Resources for group '{}' are ignored (config setting 'extensions.provider.{}.enabled' is set to 'false').",
                                name,
                                name
                        );
                    }

                });

        resourceContext.getAllResourceDescriptors()
                .forEach(desc -> ResourceDeserializer.registerKind(
                        desc.group() + "/" + desc.apiVersion(),
                        desc.kind(),
                        desc.resourceClass())
                );
    }

    @NotNull
    private static Boolean isExtensionEnabled(@NotNull Configuration configuration, String name) {
        String property = String.format("extensions.provider.%s.enabled", name.toLowerCase(Locale.ROOT));
        return configuration.findBoolean(property).orElse(true);
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
     * @return an {@link ResourceContext} instance.
     */
    public @NotNull ResourceContext getResourceContext() {
        return resourceContext;
    }

    public JikkouApi createApi(ApiConfigurator... configurators) {
        return ApiConfigurator.emptyList()
                .with(configurators)
                .with(new ExtensionsApiConfigurator(extensionFactory))
                .configure(DefaultApi.builder(), configuration)
                .build();
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

    private static final class ExtensionsApiConfigurator implements ApiConfigurator {

        private final ExtensionFactory extensionFactory;

        /**
         * Creates a new {@link ExtensionsApiConfigurator} instance.
         *
         * @param extensionFactory the {@link ExtensionFactory} instance.
         */
        public ExtensionsApiConfigurator(@NotNull ExtensionFactory extensionFactory) {
            this.extensionFactory = extensionFactory;
        }

        /**
         * {@inheritDoc}
         **/
        @Override
        public <A extends JikkouApi, B extends JikkouApi.ApiBuilder<A, B>> B configure(B builder,
                                                                                       Configuration configuration) {
            return builder
                    .withControllers(getAllEnabledExtensionsForType(ResourceController.class, configuration))
                    .withCollectors(getAllEnabledExtensionsForType(ResourceCollector.class, configuration))
                    .withValidations(cast(getAllEnabledExtensionsForType(ResourceValidation.class, configuration)))
                    .withTransformations(cast(getAllEnabledExtensionsForType(ResourceTransformation.class, configuration)))
                    .withReporters(cast(getAllEnabledExtensionsForType(ChangeReporter.class, configuration)));
        }

        private <T extends Extension> java.util.List<T> getAllEnabledExtensionsForType(Class<T> type, Configuration configuration) {
            return extensionFactory.getAllExtensions(type, configuration, ExtensionDescriptor::isEnabled);
        }
    }
}
