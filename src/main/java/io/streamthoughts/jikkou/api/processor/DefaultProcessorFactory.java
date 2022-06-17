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
import io.streamthoughts.jikkou.api.extensions.DefaultExtensionFactory;
import io.streamthoughts.jikkou.api.extensions.ReflectiveExtensionScanner;
import io.streamthoughts.jikkou.api.transforms.Transformation;
import io.streamthoughts.jikkou.api.validations.Validation;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.function.Supplier;


/**
 * Factory class for creating new {@link DefaultProcessor} instance from a given {@link JikkouConfig}.
 *
 * @see JikkouConfig
 * @see DefaultExtensionRegistry
 * @see ReflectiveExtensionScanner
 */
public class DefaultProcessorFactory {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultProcessorFactory.class);

    private final DefaultExtensionFactory factory;

    /**
     * Creates a new {@link DefaultProcessorFactory} instance.
     *
     * @param factory  the {@link DefaultExtensionRegistry}.
     */
    public DefaultProcessorFactory(@NotNull final DefaultExtensionFactory factory) {
        this.factory = factory;
    }

    /**
     * Creates a new DefaultProcessor from the given configuration.
     *
     * @param config    the {@link JikkouConfig}.
     * @return          a new {@link DefaultProcessor} instance.
     */
    public DefaultProcessor create(final JikkouConfig config) {
        LOG.info("Configuring");

        LOG.info("Loading transformation classes from configuration");
        List<Supplier<Transformation>> transformations = JikkouParams.TRANSFORMATIONS_CONFIG.get(config)
                .stream()
                .peek(tuple -> LOG.info("Added {} with values:\n\t{}", tuple._1(), tuple._2().toPrettyString()))
                .map(tuple -> (Supplier<Transformation>) () -> {
                    String extensionType = tuple._1();
                    JikkouConfig extensionConfig = tuple._2().withFallback(config);
                    return factory.getExtension(extensionType, extensionConfig);
                }).toList();

        LOG.info("Loading validation classes from configuration");
        List<Supplier<Validation>> validations = JikkouParams.VALIDATIONS_CONFIG.get(config)
                .stream()
                .peek(tuple -> LOG.info("Added {} with values:\n\t{}", tuple._1(), tuple._2().toPrettyString()))
                .map(tuple -> (Supplier<Validation>) () -> {
                    String extensionType = tuple._1();
                    JikkouConfig extensionConfig = tuple._2().withFallback(config);
                    return factory.getExtension(extensionType, extensionConfig);
                }).toList();

        return new DefaultProcessor()
                .withTransformations(transformations)
                .withValidations(validations);
    }

}
