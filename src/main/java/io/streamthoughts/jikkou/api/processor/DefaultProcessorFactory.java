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
import io.vavr.Lazy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;


/**
 * Factory class for creating new {@link DefaultProcessor} instance.
 */
public class DefaultProcessorFactory {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultProcessorFactory.class);

    private final ExtensionRegistry registry;

    /**
     * Creates a new {@link DefaultProcessorFactory} instance.
     *
     * @param registry  the {@link ExtensionRegistry}.
     */
    public DefaultProcessorFactory(final ExtensionRegistry registry) {
        this.registry = registry;
    }

    public DefaultProcessor create(final JikkouConfig config) {
        LOG.info("Configuring");
        final java.util.List<String> extensionPaths = JikkouParams.EXTENSION_PATHS
                .getOption(config)
                .getOrElse(Collections.emptyList());

        if (!extensionPaths.isEmpty()) {
            new ReflectiveExtensionScanner(registry).scan(extensionPaths);
        }

        java.util.List<Lazy<Transformation>> transformations = new java.util.ArrayList<>(JikkouParams.TRANSFORMATIONS_CONFIG.get(config)
                .stream()
                .peek(tuple -> LOG.info("Added {} with values:\n\t{}", tuple._1(), tuple._2().toPrettyString()))
                .map(tuple -> Lazy.of(() -> {
                    var extension = (Transformation) registry.getExtensionForClass(tuple._1());
                    extension.configure(tuple._2().withFallback(config));
                    return extension;
                })).toList());

        java.util.List<Lazy<Validation>> validations = JikkouParams.VALIDATIONS_CONFIG.get(config)
                .stream()
                .peek(tuple -> LOG.info("Added {} with values:\n\t{}", tuple._1(), tuple._2().toPrettyString()))
                .map(tuple -> Lazy.of(() -> {
                    var extension = (Validation) registry.getExtensionForClass(tuple._1());
                    extension.configure(tuple._2().withFallback(config));
                    return extension;
                })).toList();

        return new DefaultProcessor()
                .withTransformations(transformations)
                .withValidations(validations);
    }
}
