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
package io.streamthoughts.kafka.specs.processor;

import io.streamthoughts.kafka.specs.config.JikkouConfig;
import io.streamthoughts.kafka.specs.config.JikkouParams;
import io.streamthoughts.kafka.specs.error.ConfigException;
import io.streamthoughts.kafka.specs.extensions.ExtensionRegistry;
import io.streamthoughts.kafka.specs.extensions.ReflectiveExtensionScanner;
import io.streamthoughts.kafka.specs.model.V1SpecFile;
import io.streamthoughts.kafka.specs.model.V1SpecsObject;
import io.streamthoughts.kafka.specs.transforms.ApplyConfigMapsTransformation;
import io.streamthoughts.kafka.specs.transforms.Transformation;
import io.streamthoughts.kafka.specs.validations.NoDuplicateRolesAllowedValidation;
import io.streamthoughts.kafka.specs.validations.NoDuplicateTopicsAllowedValidation;
import io.streamthoughts.kafka.specs.validations.NoDuplicateUsersAllowedValidation;
import io.streamthoughts.kafka.specs.validations.QuotasEntityValidation;
import io.streamthoughts.kafka.specs.validations.TopicMinNumPartitionsValidation;
import io.streamthoughts.kafka.specs.validations.TopicMinReplicationFactorValidation;
import io.streamthoughts.kafka.specs.validations.Validation;
import io.streamthoughts.kafka.specs.validations.ValidationException;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public final class V1SpecFileProcessor implements Processor<V1SpecFileProcessor> {

    private static final Logger LOG = LoggerFactory.getLogger(V1SpecFileProcessor.class);

    private final List<Transformation> transformations;
    private final List<Validation> validations;
    private JikkouConfig config;

    private final ExtensionRegistry registry = new ExtensionRegistry();

    public static V1SpecFileProcessor create(final @NotNull JikkouConfig config) {
        var processor = new V1SpecFileProcessor()
                .withTransformation(new ApplyConfigMapsTransformation())
                .withValidation(new NoDuplicateTopicsAllowedValidation())
                .withValidation(new NoDuplicateUsersAllowedValidation())
                .withValidation(new NoDuplicateRolesAllowedValidation())
                .withValidation(new TopicMinNumPartitionsValidation())
                .withValidation(new TopicMinReplicationFactorValidation())
                .withValidation(new QuotasEntityValidation());
        processor.configure(config);
        return processor;
    }

    /**
     * Creates a new {@link V1SpecFileProcessor} instance.
     */
    public V1SpecFileProcessor() {
        this(new LinkedList<>(), new LinkedList<>());
    }

    /**
     * Creates a new {@link V1SpecFileProcessor} instance.
     *
     * @param transformations the list of {@link Transformation}.
     * @param validations     the list of {@link Validation}.
     */
    private V1SpecFileProcessor(final @NotNull List<Transformation> transformations,
                                final @NotNull List<Validation> validations) {
        this.transformations = Objects.requireNonNull(transformations, "'transformations' cannot be null");
        this.validations = Objects.requireNonNull(validations, "'validations' cannot be null");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void configure(final @NotNull JikkouConfig config) throws ConfigException {
        LOG.info("Configuring {}", V1SpecFileProcessor.class.getName());
        this.config = config;

        final List<String> extensionPaths = JikkouParams.EXTENSION_PATHS
                .getOption(config).
                getOrElse(Collections.emptyList());

        if (!extensionPaths.isEmpty()) {
            new ReflectiveExtensionScanner(registry).scan(extensionPaths);
        }

        JikkouParams.TRANSFORMATIONS_CONFIG.get(config)
                .stream()
                .map(cls -> (Transformation) registry.getExtensionForClass(cls))
                .forEach(this::withTransformation);

        JikkouParams.VALIDATIONS_CONFIG.get(config)
                .stream()
                .map(cls -> (Validation) registry.getExtensionForClass(cls))
                .forEach(this::withValidation);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull V1SpecFileProcessor withTransformation(@NotNull final Transformation transformation) {
        LOG.info("Adding {}", transformation.getClass());
        this.transformations.add(transformation);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull V1SpecFileProcessor withValidation(@NotNull final Validation validation) {
        LOG.info("Adding {}", validation.getClass());
        this.validations.add(validation);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull V1SpecFile apply(@NotNull final V1SpecFile file) {
        V1SpecsObject specs = file.specs();
        for (Transformation transformation : transformations) {
            transformation.configure(config);
            specs = transformation.transform(specs);
        }

        List<ValidationException> exceptions = new LinkedList<>();
        for (Validation validation : validations) {
            try {
                validation.configure(config);
                validation.validate(specs);
            } catch (ValidationException e) {
                exceptions.add(e);
            }
        }

        if (!exceptions.isEmpty()) {
            throw new ValidationException(exceptions)
                    .errorSuffixMessage("\t- ")
                    .suffixMessage("Validation rule violation:\n");
        }

        return new V1SpecFile(file.metadata(), specs);
    }
}
