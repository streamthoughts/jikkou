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
package io.streamthoughts.jikkou.api;

import io.streamthoughts.jikkou.api.config.Configuration;
import io.streamthoughts.jikkou.api.control.ChangeResult;
import io.streamthoughts.jikkou.api.control.ResourceController;
import io.streamthoughts.jikkou.api.control.ResourceDescriptor;
import io.streamthoughts.jikkou.api.error.JikkouApiException;
import io.streamthoughts.jikkou.api.model.HasMetadata;
import io.streamthoughts.jikkou.api.model.HasMetadataAcceptableList;
import io.streamthoughts.jikkou.api.model.ObjectMeta;
import io.streamthoughts.jikkou.api.model.Resource;
import io.streamthoughts.jikkou.api.model.ResourceList;
import io.streamthoughts.jikkou.api.model.ResourceTransformation;
import io.streamthoughts.jikkou.api.model.ResourceType;
import io.streamthoughts.jikkou.api.model.ResourceValidation;
import java.time.Instant;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The default API class for using Jikkou.
 */
public final class SimpleJikkouApi implements AutoCloseable, JikkouApi {

    private static final Logger LOG = LoggerFactory.getLogger(SimpleJikkouApi.class);

    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder class for creating a new {@link SimpleJikkouApi} object instance.
     */
    public static final class Builder implements JikkouApi.ApiBuilder<SimpleJikkouApi, Builder> {

        private final List<ResourceValidation> validations = new LinkedList<>();
        private final List<ResourceTransformation> transformations = new LinkedList<>();
        @SuppressWarnings("rawtypes")
        private final List<ResourceController> controllers = new LinkedList<>();
        @SuppressWarnings("rawtypes")
        private final List<ResourceDescriptor> descriptors = new LinkedList<>();

        private final List<ResourceListHandler> handlers = new LinkedList<>();

        /**
         * {@inheritDoc}
         **/
        @Override
        public Builder withValidations(final @NotNull List<ResourceValidation> validations) {
            this.validations.addAll(validations);
            return this;
        }

        /**
         * {@inheritDoc}
         **/
        @Override
        public Builder withTransformations(final @NotNull List<ResourceTransformation> transformations) {
            this.transformations.addAll(transformations);
            return this;
        }

        /**
         * {@inheritDoc}
         **/
        @Override
        @SuppressWarnings("rawtypes")
        public Builder withControllers(final @NotNull List<ResourceController> controllers) {
            this.controllers.addAll(controllers);
            return this;
        }

        /**
         * {@inheritDoc}
         **/
        @Override
        @SuppressWarnings("rawtypes")
        public Builder withDescriptors(final @NotNull List<ResourceDescriptor> descriptors) {
            this.descriptors.addAll(descriptors);
            return this;
        }

        /**
         * {@inheritDoc}
         **/
        @Override
        public Builder withHandlers(final @NotNull List<ResourceListHandler> handlers) {
            this.handlers.addAll(handlers);
            return this;
        }

        /**
         * {@inheritDoc}
         **/
        @Override
        public SimpleJikkouApi build() {
            SimpleJikkouApi api = new SimpleJikkouApi(controllers, descriptors);
            validations.forEach(api::addValidation);
            transformations.forEach(api::addTransformation);
            handlers.forEach(api::addHandler);
            return api;
        }
    }

    private final List<ResourceValidation> validations = new LinkedList<>();

    private final List<ResourceTransformation> transformations = new LinkedList<>();

    @SuppressWarnings("rawtypes")
    private final HasMetadataAcceptableList<ResourceController> controllers;
    @SuppressWarnings("rawtypes")
    private final HasMetadataAcceptableList<ResourceDescriptor> descriptors;

    private final List<ResourceListHandler> handlers = new LinkedList<>();

    /**
     * Creates a new {@link SimpleJikkouApi} instance.
     */
    @SuppressWarnings("rawtypes")
    private SimpleJikkouApi(@NotNull final List<ResourceController> controllers,
                            @NotNull final List<ResourceDescriptor> descriptors) {
        this.controllers = new HasMetadataAcceptableList<>(controllers);
        this.descriptors = new HasMetadataAcceptableList<>(
                Stream.concat(controllers.stream(), descriptors.stream()).toList()
        );
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public SimpleJikkouApi addHandler(final @NotNull ResourceListHandler handler) {
        this.handlers.add(handler);
        return this;
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public SimpleJikkouApi addValidation(@NotNull ResourceValidation validation) {
        this.validations.add(validation);
        return this;
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public SimpleJikkouApi addTransformation(@NotNull ResourceTransformation transformation) {
        this.transformations.add(transformation);
        return this;
    }


    @Override
    public Collection<ChangeResult<?>> apply(@NotNull final ResourceList resources,
                                             @NotNull final ReconciliationMode mode,
                                             @NotNull final ReconciliationContext context) {

        ResourceList handled = new ResourceListHandlers(handlers).handle(resources);

        ResourceList validated = validate(handled);

        List<ChangeResult<?>> changeResults = validated.stream()
                .flatMap(resource -> findControllerForResource(resource)
                        .stream()
                        .map(c -> c.getReconciliationTask(resource, mode, context)))
                .map(ReconciliationCallable::call)
                .flatMap(Collection::stream)
                .toList();

        return changeResults;
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public ResourceList validate(@NotNull final ResourceList resources) {

        ResourceList handled = new ResourceListHandlers(handlers).handle(resources);

        ResourceList transformed = applyResourceTransformations(handled);
        applyResourceValidations(transformed);
        return transformed;
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    @SuppressWarnings("unchecked")
    public <T extends HasMetadata> T getResource(final @NotNull Class<? extends Resource> resourceClass,
                                                 final @NotNull ResourceFilter filter,
                                                 final @NotNull Configuration configuration) {
        String kind = HasMetadata.getKind(resourceClass);
        String apiVersion = HasMetadata.getApiVersion(resourceClass);
        ResourceType type = ResourceType.create(kind, apiVersion);
        return (T) getResource(type, filter, configuration);
    }

    /** {@inheritDoc} **/
    @Override
    public HasMetadata getResource(final @NotNull ResourceType resourceType,
                                   final @NotNull ResourceFilter resourceFilter,
                                   final @NotNull Configuration configuration) {
        try (var descriptor = findDescriptorForResource(resourceType)) {
            HasMetadata resource = descriptor.describe(configuration, resourceFilter);
            ObjectMeta.ObjectMetaBuilder objectMetaBuilder = resource.getMetadata().toBuilder();
            return resource.withMetadata(
                    objectMetaBuilder.withAnnotation(ObjectMeta.ANNOT_GENERATED, Instant.now()).build()
            );
        }
    }

    /** {@inheritDoc} **/
    @Override
    public Builder toBuilder() {
        return new Builder()
                .withTransformations(transformations)
                .withValidations(validations)
                .withControllers(controllers.items())
                .withDescriptors(descriptors.items())
                .withHandlers(handlers);
    }

    private void applyResourceValidations(@NotNull ResourceList resources) {

        var validationList = new HasMetadataAcceptableList<>(validations);

        resources.items()
                .forEach(resource -> validationList.allResourcesAccepting(resource)
                        .stream()
                        .forEach(validation -> validation.validate(resource)));
    }

    private ResourceList applyResourceTransformations(@NotNull ResourceList resources) {
        var transformationList = new HasMetadataAcceptableList<>(transformations);
        return new ResourceList(resources.items().stream()
                .map(it -> {
                    var transformations = transformationList.allResourcesAccepting(it);
                    return io.vavr.collection.List.ofAll(transformations)
                            .foldLeft(it, (resource, transformation) -> transformation.transform(resource, resources));

                })
                .toList()
        );
    }

    @SuppressWarnings({"rawtypes"})
    private Optional<ResourceController> findControllerForResource(@NotNull HasMetadata resource) {
        return findControllerForResource(
                ResourceType.create(
                        resource.getKind(),
                        resource.getApiVersion()
                )
        );
    }

    @SuppressWarnings({"rawtypes"})
    private ResourceDescriptor findDescriptorForResource(@NotNull ResourceType resource) {

        var acceptedDescriptors = descriptors.allResourcesAccepting(resource);

        if (acceptedDescriptors.isEmpty()) {
            throw new JikkouApiException(String.format(
                    "No resource descriptor found for version=%s and kind=%s ",
                    resource.getApiVersion(),
                    resource.getKind()
            ));
        }

        int numMatchingHandlers = acceptedDescriptors.size();
        if (numMatchingHandlers > 1) {
            throw new JikkouApiException(String.format(
                    "Expected single matching resource descriptor for version=%s and kind=%s but found %s",
                    resource.getApiVersion(),
                    resource.getKind(),
                    numMatchingHandlers
            ));
        }
        return acceptedDescriptors.first();
    }

    @SuppressWarnings({"rawtypes"})
    private Optional<ResourceController> findControllerForResource(@NotNull ResourceType resource) {

        var acceptedController = controllers.allResourcesAccepting(resource);

        if (acceptedController.isEmpty()) {
            LOG.warn("No resource controller found for version={} and kind={}",
                    resource.getApiVersion(),
                    resource.getKind()
            );
            return Optional.empty();
        }

        int numMatchingHandlers = acceptedController.size();
        if (numMatchingHandlers > 1) {
            throw new JikkouApiException(String.format(
                    "Expected single matching resource controller for version=%s and kind=%s but found %s",
                    resource.getApiVersion(),
                    resource.getKind(),
                    numMatchingHandlers
            ));
        }
        return Optional.of(acceptedController.first());
    }

    /** {@inheritDoc} */
    @Override
    public void close() {
    }
}
