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
package io.streamthoughts.jikkou.core;

import io.streamthoughts.jikkou.core.converter.Converter;
import io.streamthoughts.jikkou.core.converter.ConverterChain;
import io.streamthoughts.jikkou.core.exceptions.JikkouRuntimeException;
import io.streamthoughts.jikkou.core.extension.ExtensionFactory;
import io.streamthoughts.jikkou.core.extension.qualifier.Qualifiers;
import io.streamthoughts.jikkou.core.models.DefaultResourceListObject;
import io.streamthoughts.jikkou.core.models.HasItems;
import io.streamthoughts.jikkou.core.models.HasMetadata;
import io.streamthoughts.jikkou.core.models.Resource;
import io.streamthoughts.jikkou.core.models.ResourceListObject;
import io.streamthoughts.jikkou.core.models.ResourceType;
import io.streamthoughts.jikkou.core.reconcilier.Change;
import io.streamthoughts.jikkou.core.reconcilier.Collector;
import io.streamthoughts.jikkou.core.reconcilier.Controller;
import io.streamthoughts.jikkou.core.selectors.AggregateSelector;
import io.streamthoughts.jikkou.core.transform.Transformation;
import io.streamthoughts.jikkou.core.transform.TransformationChain;
import io.streamthoughts.jikkou.core.validation.Validation;
import io.streamthoughts.jikkou.core.validation.ValidationChain;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Stream;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BaseApi implements JikkouApi {

    private static final Logger LOG = LoggerFactory.getLogger(BaseApi.class);

    protected final ExtensionFactory extensionFactory;

    /**
     * Creates a new {@link BaseApi} instance.
     *
     * @param extensionFactory The ExtensionFactory.
     */
    public BaseApi(@NotNull ExtensionFactory extensionFactory) {
        this.extensionFactory = Objects.requireNonNull(extensionFactory, "extensionFactory cannot be null");
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public HasItems prepare(final @NotNull HasItems resources,
                            final @NotNull ReconciliationContext context) {

        return Stream.of(resources)
                .map(this::convert)
                .map(items -> transform(items, context))
                .map(items -> select(items, context))
                .findAny()
                .get();
    }

    private ResourceListObject<HasMetadata> convert(final @NotNull HasItems resources) {

        ConverterChain converter = getConverterChain();
        List<HasMetadata> converted = resources.getItems()
                .stream()
                .map(resource -> {
                    try {
                        return converter.apply(resource);
                    } catch (Exception e) {
                        ResourceType type = ResourceType.of(resource);
                        throw new JikkouRuntimeException(String.format(
                                "Failed to apply converter '%s' on resource of  resource type: group=%s, version=%s and kind=%s. Cause: %s",
                                converter.getName(),
                                type.group(),
                                type.apiVersion(),
                                type.kind(),
                                e.getLocalizedMessage()

                        ));
                    }
                })
                .flatMap(Collection::stream)
                .toList();

        return new DefaultResourceListObject<>(converted);
    }

    private ResourceListObject<HasMetadata> transform(final @NotNull HasItems items,
                                                      final @NotNull ReconciliationContext context) {

        TransformationChain transformationChain = getResourceTransformationChain();

        List<HasMetadata> transformed = new LinkedList<>();
        for (Map.Entry<ResourceType, List<HasMetadata>> entry : items.groupByType().entrySet()) {
            ResourceType type = entry.getKey();
            try {
                transformed.addAll(transformationChain.transformAll(entry.getValue(), items, context));
            } catch (Exception e) {
                throw new JikkouRuntimeException(String.format(
                        "Failed to apply transformations on resources of type: group=%s, version=%s and kind=%s. Cause: %s",
                        type.group(),
                        type.apiVersion(),
                        type.kind(),
                        e.getLocalizedMessage()

                ));
            }
        }
        return new DefaultResourceListObject<>(transformed);
    }

    private ResourceListObject<HasMetadata> select(final @NotNull HasItems resources,
                                                   final @NotNull ReconciliationContext context) {

        List<? extends HasMetadata> filtered = resources.getItems().stream()
                .filter(Predicate.not(resource -> Resource.isTransient(resource.getClass())))
                .filter(new AggregateSelector(context.selectors())::apply)
                .toList();
        return new DefaultResourceListObject<>(filtered);
    }

    protected ValidationChain getResourceValidationChain() {
        @SuppressWarnings("rawtypes")
        List<Validation> validations = extensionFactory
                .getAllExtensions(Validation.class, Qualifiers.enabled());
        return new ValidationChain(validations);
    }

    protected TransformationChain getResourceTransformationChain() {
        @SuppressWarnings("rawtypes")
        List<Transformation> transformations = extensionFactory.getAllExtensions(
                Transformation.class, Qualifiers.enabled());
        return new TransformationChain(transformations);
    }

    protected ConverterChain getConverterChain() {
        @SuppressWarnings("rawtypes")
        List<Converter> converters = extensionFactory.getAllExtensions(
                Converter.class,
                Qualifiers.enabled());
        return new ConverterChain(converters);
    }

    @SuppressWarnings("unchecked")
    protected Collector<HasMetadata> getMatchingResourceCollector(@NotNull ResourceType resource) {
        LOG.info("Looking for a collector accepting resource type: group={}, version={} and kind={}",
                resource.group(),
                resource.apiVersion(),
                resource.kind()
        );
        return extensionFactory.findExtension(
                Collector.class,
                Qualifiers.byAcceptedResource(resource)
        ).orElseThrow(() -> new JikkouRuntimeException(String.format(
                "Cannot found register collector for resource type: group='%s', version='%s' and kind='%s",
                resource.group(),
                resource.apiVersion(),
                resource.kind()
        )));
    }

    @SuppressWarnings("unchecked")
    protected Controller<HasMetadata, Change> getMatchingResourceController(@NotNull ResourceType resource) {
        LOG.info("Looking for a controller accepting resource type: group={}, version={} and kind={}",
                resource.group(),
                resource.apiVersion(),
                resource.kind()
        );
        return extensionFactory.findExtension(
                Controller.class,
                Qualifiers.byAcceptedResource(resource)
        ).orElseThrow(() -> new JikkouRuntimeException(String.format(
                "Cannot found controller for resource type: group='%s', version='%s' and kind='%s",
                resource.group(),
                resource.apiVersion(),
                resource.kind()
        )));
    }
}
