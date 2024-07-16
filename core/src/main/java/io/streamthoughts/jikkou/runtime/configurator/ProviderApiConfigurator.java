/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.runtime.configurator;

import static io.streamthoughts.jikkou.runtime.JikkouConfigProperties.EXTENSIONS_PROVIDER_DEFAULT_ENABLED;
import static io.streamthoughts.jikkou.runtime.JikkouConfigProperties.EXTENSION_PROVIDER_CONFIG_PREFIX;
import static io.streamthoughts.jikkou.runtime.JikkouConfigProperties.PROVIDERS_CONFIG;

import io.streamthoughts.jikkou.common.utils.ServiceLoaders;
import io.streamthoughts.jikkou.core.BaseApiConfigurator;
import io.streamthoughts.jikkou.core.JikkouApi;
import io.streamthoughts.jikkou.core.config.Configuration;
import io.streamthoughts.jikkou.core.exceptions.InvalidConfigException;
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
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
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
        final Map<String, ExtensionConfigEntry> providers = Optional.ofNullable(getPropertyValue(PROVIDERS_CONFIG)).orElse(List.of())
            .stream()
            .filter(ExtensionConfigEntry::enabled)
            .collect(Collectors.toMap(ExtensionConfigEntry::type, Function.identity(), (o1, o2) -> {
                throw new InvalidConfigException(
                    String.format(
                        "Multiple providers are configured and enabled for type: %s. Please disable or delete one of the configurations.", o1.type())
                );
            }));

        // Load all ExtensionProviders
        for (ExtensionProvider provider : ServiceLoaders.loadAllServices(ExtensionProvider.class, cls)) {
            String type = provider.getClass().getName();
            if (providers.containsKey(type)) {
                builder = builder.register(provider, providers.get(type).config());
            } else {
                // backward-compatibility
                Boolean extensionEnabledByDefault = EXTENSIONS_PROVIDER_DEFAULT_ENABLED.get(configuration());
                if (legacyIsExtensionProviderEnabled(configuration(), provider.getName(), extensionEnabledByDefault)) {
                    Optional<Configuration> legacyConfiguration = configuration().findConfig(provider.getName());
                    LOG.warn("Deprecated provider configuration `jikkou.{}` was detected. Please, you should consider using the new `jikkou.providers`.", provider.getName());
                    builder = builder.register(provider, legacyConfiguration.orElse(Configuration.empty()));
                } else {
                    LOG.debug(
                        "Provider '{}' was found but will be ignored. This provider is either not configured or disabled through.",
                        type
                    );
                }
            }
        }
        return builder;
    }

    private static boolean legacyIsExtensionProviderEnabled(@NotNull Configuration configuration,
                                                            @NotNull String name,
                                                            boolean defaultValue) {
        String property = String.format(EXTENSION_PROVIDER_CONFIG_PREFIX + ".%s.enabled", name.toLowerCase(Locale.ROOT));
        return configuration.findBoolean(property).orElse(defaultValue);
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
