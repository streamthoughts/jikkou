/*
 * Copyright 2021 StreamThoughts.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.streamthoughts.kafka.specs.extensions;

import io.streamthoughts.kafka.specs.internal.ClassUtils;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;

import static java.util.stream.Collectors.joining;

/**
 * The {@link ReflectiveExtensionScanner} class can be used to scan the classpath for automatically
 * registering declared sub-type of {@link Extension} classes.
 */
public class ReflectiveExtensionScanner {

    private static final Logger LOG = LoggerFactory.getLogger(ReflectiveExtensionScanner.class);

    private static final FilterBuilder DEFAULT_FILTER_BY = new FilterBuilder();

    private final ExtensionRegistry registry;

    /**
     * Creates a new {@link ReflectiveExtensionScanner} instance.
     *
     * @param registry  the {@link ExtensionRegistry} used to register providers.
     */
    public ReflectiveExtensionScanner(final ExtensionRegistry registry) {
        Objects.requireNonNull(registry, "registry cannot be null");
        this.registry = registry;
    }

    public void scanExtensionPath(final Path extensionPath) {
        ExtensionResolver resolver = new ExtensionResolver(extensionPath);
        final List<ExternalExtension> extensions = resolver.resolves();
        for (ExternalExtension extension : extensions) {
            ReflectiveExtensionScanner.LOG.info("Loading extension from path : {}", extension.location());
            final ExtensionClassLoader classLoader = ExtensionClassLoader.newClassLoader(
                    extension.location(),
                    extension.resources(),
                    ReflectiveExtensionScanner.class.getClassLoader()
            );
            ReflectiveExtensionScanner.LOG.info("Initialized new ClassLoader: {}", classLoader);
            scanUrlsForComponents(extension.resources(), classLoader, ReflectiveExtensionScanner.DEFAULT_FILTER_BY);
        }
    }

    public void scan(final List<String> extensionPaths) {

        for (final String path : extensionPaths) {
            try {
                final Path extensionPath = Paths.get(path).toAbsolutePath();
                scanExtensionPath(extensionPath);
            } catch (InvalidPathException e) {
                LOG.error("Ignoring top-level extension location '{}', invalid path.", path);
            }
        }
    }

    private void scanUrlsForComponents(final URL[] urls,
                                       final ClassLoader classLoader,
                                       final Predicate<String> filterBy) {
        LOG.info("Scanning extensions from paths : {}",
                Arrays.stream(urls).map(URL::getPath).collect(joining("\n\t", "\n\t", "")));

        ConfigurationBuilder builder = new ConfigurationBuilder();
        builder.setClassLoaders(new ClassLoader[]{classLoader});
        builder.addUrls(urls);
        builder.filterInputsBy(filterBy);
        builder.setScanners(Scanners.SubTypes);
        builder.setParallel(true);

        Reflections reflections = new Reflections(builder);
        registerExtensionClasses(reflections, classLoader);
    }

    private void registerExtensionClasses(final Reflections reflections,
                                          final ClassLoader classLoader) {
        final Set<Class<? extends Extension>> extensions = reflections.getSubTypesOf(Extension.class);
        for (Class<?> cls : extensions) {
            if (ClassUtils.canBeInstantiated(cls)) {
                LOG.info("Registering external extension: {}", cls);
                registry.register(cls.getName(), () -> (Extension) ClassUtils.newInstance(cls, classLoader));
            } else {
                LOG.debug("Scanned class is not accessible '{}'", cls);
            }
        }
    }
}

