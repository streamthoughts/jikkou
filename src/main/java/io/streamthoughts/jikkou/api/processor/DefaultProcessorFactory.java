/*
 * Copyright 2022 StreamThoughts.
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
package io.streamthoughts.jikkou.api.processor;

import io.streamthoughts.jikkou.api.config.JikkouConfig;
import io.streamthoughts.jikkou.api.config.JikkouParams;
import io.streamthoughts.jikkou.api.extensions.ExtensionRegistry;
import io.streamthoughts.jikkou.api.extensions.ReflectiveExtensionScanner;
import io.streamthoughts.jikkou.api.transforms.Transformation;
import io.streamthoughts.jikkou.api.validations.Validation;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Supplier;


/**
 * Factory class for creating new {@link DefaultProcessor} instance from a given {@link JikkouConfig}.
 *
 * @see JikkouConfig
 * @see ExtensionRegistry
 * @see ReflectiveExtensionScanner
 */
public class DefaultProcessorFactory {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultProcessorFactory.class);

    private final ExtensionRegistry registry;
    private final ReflectiveExtensionScanner scanner;

    private final Set<String> extensionPaths = new HashSet<>();

    /**
     * Creates a new {@link DefaultProcessorFactory} instance.
     *
     * @param registry  the {@link ExtensionRegistry}.
     */
    public DefaultProcessorFactory(@NotNull final ExtensionRegistry registry) {
        this.registry = registry;
        this.scanner = new ReflectiveExtensionScanner(registry);
    }

    /**
     * Configure a path to be scanned for extensions.
     *
     * @param extensionPath the path to add for loading extensions.
     * @return  {@code this}
     */
    public DefaultProcessorFactory withExtensionPath(final String extensionPath) {
        extensionPaths.add(extensionPath);
        return this;
    }

    /**
     * Creates a new DefaultProcessor from the given configuration.
     *
     * @param config    the {@link JikkouConfig}.
     * @return          a new {@link DefaultProcessor} instance.
     */
    public DefaultProcessor create(final JikkouConfig config) {
        LOG.info("Configuring");

        final Set<String> extensions = new HashSet<>(extensionPaths);
        extensions.addAll(JikkouParams.EXTENSION_PATHS
                .getOption(config)
                .getOrElse(Collections.emptyList()));

        if (!extensions.isEmpty()) {
            scanner.scan(new ArrayList<>(extensions));
        } else {
            LOG.info("No extension paths was configured. Extension scan is disabled");
        }

        LOG.info("Loading transformation classes from configuration");
        List<Supplier<Transformation>> transformations = JikkouParams.TRANSFORMATIONS_CONFIG.get(config)
                .stream()
                .peek(tuple -> LOG.info("Added {} with values:\n\t{}", tuple._1(), tuple._2().toPrettyString()))
                .map(tuple -> (Supplier<Transformation>) () -> {
                    var extension = (Transformation) registry.getExtensionForClass(tuple._1());
                    extension.configure(tuple._2().withFallback(config));
                    return extension;
                }).toList();

        LOG.info("Loading validation classes from configuration");
        List<Supplier<Validation>> validations = JikkouParams.VALIDATIONS_CONFIG.get(config)
                .stream()
                .peek(tuple -> LOG.info("Added {} with values:\n\t{}", tuple._1(), tuple._2().toPrettyString()))
                .map(tuple -> (Supplier<Validation>) () -> {
                    var extension = (Validation) registry.getExtensionForClass(tuple._1());
                    extension.configure(tuple._2().withFallback(config));
                    return extension;
                }).toList();

        return new DefaultProcessor()
                .withTransformations(transformations)
                .withValidations(validations);
    }
}
