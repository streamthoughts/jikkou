/*
 * Copyright 2022 The original authors
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

import io.streamthoughts.jikkou.JikkouMetadataAnnotations;
import io.streamthoughts.jikkou.api.change.Change;
import io.streamthoughts.jikkou.api.change.ChangeResult;
import io.streamthoughts.jikkou.api.config.Configuration;
import io.streamthoughts.jikkou.api.control.ResourceCollector;
import io.streamthoughts.jikkou.api.control.ResourceController;
import io.streamthoughts.jikkou.api.converter.ResourceConverter;
import io.streamthoughts.jikkou.api.error.JikkouApiException;
import io.streamthoughts.jikkou.api.model.GenericResourceListObject;
import io.streamthoughts.jikkou.api.model.HasItems;
import io.streamthoughts.jikkou.api.model.HasMetadata;
import io.streamthoughts.jikkou.api.model.HasMetadataAcceptableList;
import io.streamthoughts.jikkou.api.model.HasMetadataChange;
import io.streamthoughts.jikkou.api.model.ObjectMeta;
import io.streamthoughts.jikkou.api.model.ResourceListObject;
import io.streamthoughts.jikkou.api.model.ResourceType;
import io.streamthoughts.jikkou.api.reporter.ChangeReporter;
import io.streamthoughts.jikkou.api.reporter.CombineChangeReporter;
import io.streamthoughts.jikkou.api.selector.ResourceSelector;
import io.streamthoughts.jikkou.api.transform.ResourceTransformation;
import io.streamthoughts.jikkou.api.transform.ResourceTransformationChain;
import io.streamthoughts.jikkou.api.validation.ResourceValidation;
import io.streamthoughts.jikkou.api.validation.ResourceValidationChain;
import io.streamthoughts.jikkou.common.utils.Tuple2;
import java.time.Instant;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The default implementation of the {@link JikkouApi} interface.
 */
public final class DefaultApi implements AutoCloseable, JikkouApi {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultApi.class);

    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder class for creating a new {@link DefaultApi} object instance.
     */
    public static final class Builder implements JikkouApi.ApiBuilder<DefaultApi, Builder> {

        private final List<ResourceValidation<? extends HasMetadata>> validations = new LinkedList<>();
        private final List<ResourceTransformation<? extends HasMetadata>> transformations = new LinkedList<>();
        @SuppressWarnings("rawtypes")
        private final List<ResourceController> controllers = new LinkedList<>();
        @SuppressWarnings("rawtypes")
        private final List<ResourceCollector> collectors = new LinkedList<>();
        private final List<ChangeReporter> reporters = new LinkedList<>();

        /**
         * {@inheritDoc}
         **/
        @Override
        public Builder withValidations(@NotNull List<ResourceValidation<HasMetadata>> validations) {
            this.validations.addAll(validations);
            return this;
        }

        /**
         * {@inheritDoc}
         **/
        @Override
        public Builder withTransformations(@NotNull List<ResourceTransformation<HasMetadata>> transformations) {
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
        public Builder withCollectors(final @NotNull List<ResourceCollector> collectors) {
            this.collectors.addAll(collectors);
            return this;
        }

        /**
         * {@inheritDoc}
         **/
        @Override
        public Builder withValidation(@NotNull ResourceValidation<? extends HasMetadata> validation) {
            this.validations.add(validation);
            return this;
        }

        /**
         * {@inheritDoc}
         **/
        @Override
        public Builder withTransformation(@NotNull ResourceTransformation<? extends HasMetadata> transformation) {
            this.transformations.add(transformation);
            return this;
        }


        /**
         * {@inheritDoc}
         **/
        @Override
        public Builder withReporters(@NotNull List<ChangeReporter> reporters) {
            this.reporters.addAll(reporters);
            return this;
        }

        /**
         * {@inheritDoc}
         **/
        @Override
        public DefaultApi build() {
            DefaultApi api = new DefaultApi(controllers, collectors);
            validations.forEach(api::addValidation);
            transformations.forEach(api::addTransformation);
            reporters.forEach(api::addReporter);
            return api;
        }
    }

    private final List<ResourceValidation<HasMetadata>> validations = new LinkedList<>();

    private final List<ResourceTransformation<HasMetadata>> transformations = new LinkedList<>();
    @SuppressWarnings("rawtypes")
    private final HasMetadataAcceptableList<ResourceController> controllers;
    @SuppressWarnings("rawtypes")
    private final HasMetadataAcceptableList<ResourceCollector> collectors;
    private final List<ChangeReporter> reporters = new LinkedList<>();

    /**
     * Creates a new {@link DefaultApi} instance.
     */
    @SuppressWarnings("rawtypes")
    private DefaultApi(@NotNull final List<ResourceController> controllers,
                       @NotNull final List<ResourceCollector> collectors) {
        this.controllers = new HasMetadataAcceptableList<>(controllers);
        this.collectors = new HasMetadataAcceptableList<>(collectors);
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public DefaultApi addValidation(@NotNull ResourceValidation<? extends HasMetadata> validation) {
        this.validations.add((ResourceValidation<HasMetadata>) validation);
        return this;
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public DefaultApi addTransformation(@NotNull ResourceTransformation<? extends HasMetadata> transformation) {
        this.transformations.add((ResourceTransformation<HasMetadata>) transformation);
        return this;
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public DefaultApi addReporter(@NotNull ChangeReporter reporter) {
        this.reporters.add(reporter);
        return this;
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public List<ChangeResult<Change>> apply(@NotNull final HasItems resources,
                                            @NotNull final ReconciliationMode mode,
                                            @NotNull final ReconciliationContext context) {
        LOG.info("Starting reconciliation of {} resource objects in {} mode.", resources.getItems().size(), mode);
        List<ChangeResult<Change>> results = handleResources(resources, context.selectors())
                .entrySet()
                .stream()
                .map(e -> {
                    ResourceController<HasMetadata, Change> controller = getControllerForResource(e.getKey());
                    return controller.reconcile(e.getValue(), mode, context);
                })
                .flatMap(Collection::stream)
                .toList();
        if (!context.isDryRun() && !reporters.isEmpty()) {
            List<ChangeResult<Change>> reportable = results.stream()
                    .filter(t -> !JikkouMetadataAnnotations.isAnnotatedWithNoReport(t.data()))
                    .toList();
            new CombineChangeReporter(reporters).report(reportable);
        }
        return results;
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public GenericResourceListObject<HasMetadata> validate(final @NotNull HasItems resources,
                                                           final @NotNull List<ResourceSelector> selectors) {
        return GenericResourceListObject.of(handleResources(resources, selectors)
                .values()
                .stream()
                .flatMap(Collection::stream)
                .toList());
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public List<ResourceListObject<HasMetadataChange<Change>>> getDiff(@NotNull final HasItems resources,
                                                                       @NotNull final List<ResourceSelector> selectors) {

        ReconciliationContext.Default context = new ReconciliationContext.Default(
                selectors,
                Configuration.empty(),
                true

        );

        return handleResources(resources, selectors)
                .entrySet()
                .stream()
                .map(e -> {
                    ResourceController<HasMetadata, Change> controller = getControllerForResource(e.getKey());
                    List<HasMetadata> resource = e.getValue();
                    ResourceListObject<? extends HasMetadataChange<Change>> changes = controller.computeReconciliationChanges(
                            resource,
                            ReconciliationMode.APPLY_ALL,
                            context
                    );
                    return (ResourceListObject<HasMetadataChange<Change>>) changes;
                })
                .toList();
    }

    @NotNull
    private Map<ResourceType, List<HasMetadata>> handleResources(@NotNull HasItems resources,
                                                                 @NotNull List<ResourceSelector> selectors) {

        List<HasMetadata> converted = resources.groupByType()
                .entrySet()
                .stream()
                .flatMap(entry -> {
                    ResourceType type = entry.getKey();
                    if (type.isTransient()) {
                        return entry.getValue().stream();
                    } else {
                        ResourceController<HasMetadata, Change> controller = getControllerForResource(type);
                        ResourceConverter<HasMetadata, HasMetadata> converter = controller.getResourceConverter(type);
                        return converter.convertFrom(entry.getValue()).stream();
                    }
                }).toList();

        ResourceTransformationChain transformationChain = new ResourceTransformationChain(transformations);

        Map<ResourceType, List<HasMetadata>> result = GenericResourceListObject.of(converted).groupByType()
                .entrySet()
                .stream()
                .map(Tuple2::of)
                .map(t -> t.mapRight(resource -> {
                    return transformationChain.transformAll(resource, new GenericResourceListObject<>(converted));
                }))
                .map(t -> t.mapRight(resource -> new GenericResourceListObject<>(t._2()).getAllMatching(selectors)))
                .filter(t -> !t._1().isTransient())
                .collect(Collectors.toMap(Tuple2::_1, Tuple2::_2));

        ResourceValidationChain validationChain = new ResourceValidationChain(validations);
        validationChain.validate(result.values()
                .stream()
                .flatMap(Collection::stream)
                .toList());

        return result;
    }


    /**
     * {@inheritDoc}
     **/
    @Override
    public List<HasMetadata> getResources(final @NotNull ResourceType resourceType,
                                          final @NotNull List<ResourceSelector> selectors,
                                          final @NotNull Configuration configuration) {
        try (ResourceCollector<HasMetadata> collector = getResourceCollectorForType(resourceType)) {
            List<HasMetadata> resources = collector.listAll(configuration, selectors);
            ResourceConverter<HasMetadata, HasMetadata> converter = collector.getResourceConverter(resourceType);
            List<HasMetadata> result = resources.stream()
                    .map(resource -> {
                        ObjectMeta meta = resource
                                .optionalMetadata()
                                .orElse(new ObjectMeta()).toBuilder()
                                .withAnnotation(ObjectMeta.ANNOT_GENERATED, Instant.now())
                                .build();
                        return resource.withMetadata(meta);
                    })
                    .toList();
            return converter.convertTo(result);
        }
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public Builder toBuilder() {
        return new Builder()
                .withTransformations(transformations)
                .withValidations(validations)
                .withControllers(controllers.getItems())
                .withCollectors(collectors.getItems());
    }

    private ResourceCollector<HasMetadata> getResourceCollectorForType(@NotNull ResourceType resource) {

        var acceptedDescriptors = collectors.allResourcesAccepting(resource);

        if (acceptedDescriptors.isEmpty()) {
            throw new JikkouApiException(String.format(
                    "No resource collector found for version=%s and kind=%s ",
                    resource.getApiVersion(),
                    resource.getKind()
            ));
        }

        int numMatchingHandlers = acceptedDescriptors.size();
        if (numMatchingHandlers > 1) {
            throw new JikkouApiException(String.format(
                    "Expected single matching resource collector for version=%s and kind=%s but found %s",
                    resource.getApiVersion(),
                    resource.getKind(),
                    numMatchingHandlers
            ));
        }
        return acceptedDescriptors.first();
    }


    private ResourceController<HasMetadata, Change> getControllerForResource(@NotNull ResourceType resource) {
        var result = controllers.allResourcesAccepting(resource);
        if (result.isEmpty()) {
            throw new JikkouApiException(String.format(
                    "Cannot find controller for resource type: group=%s, version=%s and kind=%s",
                    resource.getGroup(),
                    resource.getApiVersion(),
                    resource.getKind()
            ));
        }

        int numMatchingHandlers = result.size();
        if (numMatchingHandlers > 1) {
            throw new JikkouApiException(String.format(
                    "Expected single matching controller for resource type: group=%s, version=%s and kind=%s, but found %s",
                    resource.getGroup(),
                    resource.getApiVersion(),
                    resource.getKind(),
                    numMatchingHandlers
            ));
        }
        @SuppressWarnings({"unchecked"})
        ResourceController<HasMetadata, Change> res = result.first();
        return res;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() {
        new CombineChangeReporter(reporters).close();
    }
}
