/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.runtime.configurator;

import static io.streamthoughts.jikkou.runtime.JikkouConfigProperties.PROVIDER_CONFIG;

import io.streamthoughts.jikkou.common.utils.ServiceLoaders;
import io.streamthoughts.jikkou.core.BaseApiConfigurator;
import io.streamthoughts.jikkou.core.JikkouApi;
import io.streamthoughts.jikkou.core.config.Configuration;
import io.streamthoughts.jikkou.core.extension.ExtensionClassLoader;
import io.streamthoughts.jikkou.core.extension.ExtensionResolver;
import io.streamthoughts.jikkou.core.extension.ExternalExtension;
import io.streamthoughts.jikkou.runtime.JikkouContext;
import io.streamthoughts.jikkou.spi.ExtensionProvider;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProviderApiConfigurator extends BaseApiConfigurator {

    private static final Logger LOG = LoggerFactory.getLogger(ProviderApiConfigurator.class);

    private final List<String> extensionPaths;

    /**
     * Creates a new {@link BaseApiConfigurator} instance.
     *
     * @param extensionPaths the list of paths from where to load extensions.
     */
    public ProviderApiConfigurator(@NotNull final List<String> extensionPaths) {
        super(null);
        this.extensionPaths = extensionPaths;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected <A extends JikkouApi, B extends JikkouApi.ApiBuilder<A, B>> B configure(B builder) {
        // Get all ClassLoader for all configured extension paths.
        Set<ClassLoader> cls = getAllClassLoaders(extensionPaths);

        // Load ExtensionProvider configurations
        final List<ExtensionConfigEntry> providers = Optional.ofNullable(getPropertyValue(PROVIDER_CONFIG)).orElse(List.of())
            .stream()
            .filter(ExtensionConfigEntry::enabled)
            .toList();

        final Map<String, List<ExtensionConfigEntry>> providerConfigByType = providers.stream()
            .filter(entry -> entry.type() != null)
            .collect(Collectors.groupingBy(ExtensionConfigEntry::type));

        // Load all ExtensionProviders
        for (ExtensionProvider provider : ServiceLoaders.loadAllServices(ExtensionProvider.class, cls)) {
            final String providerName = provider.getName();
            final String providerType = provider.getClass().getName();

            if (providerConfigByType.containsKey(providerType)) {
                // Register provider
                builder.register(provider);

                // Register provider configurations
                providerConfigByType.get(providerType).forEach(extensionConfigEntry -> {

                    // Looking for direct provider config override
                    Configuration config = extensionConfigEntry.config();
                    Optional<Configuration> override = configuration().findConfig(extensionConfigEntry.name());
                    if (override.isPresent()) {
                        config = override.get().withFallback(config);
                    }

                    // Register configuration
                    builder.registerProviderConfiguration(
                        extensionConfigEntry.name(),
                        extensionConfigEntry.type(),
                        config,
                        extensionConfigEntry.isDefault()
                    );
                });
            }
        }
        return builder;
    }

    /**
     * @return the set of known {@link ClassLoader}.
     */
    private static Set<ClassLoader> getAllClassLoaders(List<String> extensionPaths) {
        Set<ClassLoader> classLoaders = new HashSet<>();
        classLoaders.add(JikkouContext.class.getClassLoader());
        classLoaders.addAll(getClassLoadersForPath(extensionPaths));
        return classLoaders;
    }

    private static Set<ClassLoader> getClassLoadersForPath(final List<String> extensionPaths) {
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

    private static Set<ClassLoader> getClassLoadersForPaths(final Path extensionPath) {
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
