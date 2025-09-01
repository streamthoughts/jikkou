/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core;

import io.streamthoughts.jikkou.common.utils.Pair;
import io.streamthoughts.jikkou.core.config.Configuration;
import io.streamthoughts.jikkou.core.converter.Converter;
import io.streamthoughts.jikkou.core.converter.ConverterChain;
import io.streamthoughts.jikkou.core.exceptions.JikkouRuntimeException;
import io.streamthoughts.jikkou.core.extension.Extension;
import io.streamthoughts.jikkou.core.extension.ExtensionDescriptorModifier;
import io.streamthoughts.jikkou.core.extension.ExtensionFactory;
import io.streamthoughts.jikkou.core.extension.ExtensionProviderAwareRegistry;
import io.streamthoughts.jikkou.core.extension.qualifier.Qualifiers;
import io.streamthoughts.jikkou.core.models.ApiGroup;
import io.streamthoughts.jikkou.core.models.ApiResourceList;
import io.streamthoughts.jikkou.core.models.HasItems;
import io.streamthoughts.jikkou.core.models.HasMetadata;
import io.streamthoughts.jikkou.core.models.Resource;
import io.streamthoughts.jikkou.core.models.ResourceList;
import io.streamthoughts.jikkou.core.models.ResourceType;
import io.streamthoughts.jikkou.core.models.change.ResourceChange;
import io.streamthoughts.jikkou.core.reconciler.Collector;
import io.streamthoughts.jikkou.core.reconciler.Controller;
import io.streamthoughts.jikkou.core.repository.ResourceRepository;
import io.streamthoughts.jikkou.core.resource.DefaultResourceRegistry;
import io.streamthoughts.jikkou.core.resource.ResourceDescriptor;
import io.streamthoughts.jikkou.core.resource.ResourceDeserializer;
import io.streamthoughts.jikkou.core.resource.ResourceRegistry;
import io.streamthoughts.jikkou.core.transform.Transformation;
import io.streamthoughts.jikkou.core.transform.TransformationChain;
import io.streamthoughts.jikkou.core.validation.Validation;
import io.streamthoughts.jikkou.core.validation.ValidationChain;
import io.streamthoughts.jikkou.spi.ExtensionProvider;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An abstract base implementation of the {@link JikkouApi}.
 */
public abstract class BaseApi implements JikkouApi {

    private static final Logger LOG = LoggerFactory.getLogger(BaseApi.class);

    protected final ExtensionFactory extensionFactory;

    /**
     * An abstract base implementation of the {@link BaseBuilder} object instance.
     */
    public static abstract class BaseBuilder<A extends JikkouApi, B extends BaseBuilder<A, B>> implements JikkouApi.ApiBuilder<A, B> {

        protected final ExtensionFactory extensionFactory;
        protected final ResourceRegistry resourceRegistry;

        /**
         * Creates a new {@link ExtensionFactory} instance.
         */
        public BaseBuilder(@NotNull ExtensionFactory extensionFactory,
                           @NotNull ResourceRegistry resourceRegistry) {
            this.extensionFactory = Objects.requireNonNull(extensionFactory, "extensionFactory must not be null");
            this.resourceRegistry = Objects.requireNonNull(resourceRegistry, "resourceRegistry must not be null");
        }

        /**
         * {@inheritDoc}
         **/
        @Override
        @SuppressWarnings("unchecked")
        public B register(@NotNull ExtensionProvider provider,
                          @NotNull Configuration configuration) {
            final String name = provider.getName();

            LOG.info("Loading extensions from provider '{}'", name);
            provider.registerExtensions(new ExtensionProviderAwareRegistry(extensionFactory, provider.getClass(), configuration));

            LOG.info("Loading resources from provider '{}'", name);
            var registry = new DefaultResourceRegistry(false);
            provider.registerResources(registry);

            // Register resource descriptors to the global registry
            registry.allDescriptors().forEach(resourceRegistry::register);

            // Register resource descriptors to the Resource Deserializer.
            registry.allDescriptors()
                .stream()
                .filter(ResourceDescriptor::isEnabled)
                .forEach(desc -> ResourceDeserializer.registerKind(
                    desc.group() + "/" + desc.apiVersion(),
                    desc.kind(),
                    desc.resourceClass())
                );
            return (B) this;
        }

        /**
         * {@inheritDoc}
         **/
        @Override
        @SuppressWarnings("unchecked")
        public <T extends Extension> B register(@NotNull Class<T> type,
                                                @NotNull Supplier<T> supplier) {
            extensionFactory.register(type, supplier);
            return (B) this;
        }

        /**
         * {@inheritDoc}
         **/
        @Override
        @SuppressWarnings("unchecked")
        public <T extends Extension> B register(@NotNull Class<T> type,
                                                @NotNull Supplier<T> supplier,
                                                @NotNull ExtensionDescriptorModifier... modifiers) {
            extensionFactory.register(type, supplier, modifiers);
            return (B) this;
        }
    }

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
    public List<ApiResourceList> listApiResources() {
        return listApiResources(listApiGroups().groups().stream());
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public List<ApiResourceList> listApiResources(@NotNull String group) {
        Stream<ApiGroup> stream = listApiGroups()
            .groups()
            .stream()
            .filter(api -> api.name().equalsIgnoreCase(group));
        return listApiResources(stream);
    }

    private List<ApiResourceList> listApiResources(@NotNull Stream<ApiGroup> stream) {
        return stream
            .flatMap(api -> api.versions()
                .stream()
                .map(apiGroupVersion -> Pair.of(api.name(), apiGroupVersion.version()))
            )
            .map(groupVersion -> listApiResources(groupVersion._1(), groupVersion._2()))
            .sorted(Comparator.comparing(ApiResourceList::groupVersion))
            .toList();
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public HasItems prepare(final @NotNull HasItems resources,
                            final @NotNull ReconciliationContext context) {

        // Load resources from repositories
        ResourceList<HasMetadata> all = addAllResourcesFromRepositories(resources);

        return doPrepare(all, context);
    }

    @NotNull
    protected ResourceList<HasMetadata> doPrepare(@NotNull HasItems resources, @NotNull ReconciliationContext context) {
        return Stream.of(resources)
            .map(this::convert)
            .map(items -> transform(items, context))
            .map(items -> select(items, context))
            .findAny()
            .get();
    }

    private ResourceList<HasMetadata> convert(final @NotNull HasItems resources) {

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

        return ResourceList.of(converted);
    }

    private ResourceList<HasMetadata> transform(final @NotNull HasItems items,
                                                final @NotNull ReconciliationContext context) {

        TransformationChain transformationChain = newResourceTransformationChain();

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
        return ResourceList.of(transformed);
    }

    @SuppressWarnings("unchecked")
    private ResourceList<HasMetadata> select(final @NotNull HasItems resources,
                                             final @NotNull ReconciliationContext context) {

        List<? extends HasMetadata> filtered = resources.getItems().stream()
            .filter(Predicate.not(resource -> Resource.isTransient(resource.getClass())))
            .filter(context.selector()::apply)
            .toList();
        return ResourceList.of((List<HasMetadata>)filtered);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    protected ValidationChain newResourceValidationChain() {
        return new ValidationChain((List)extensionFactory.getAllExtensions(Validation.class, Qualifiers.enabled()));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    protected TransformationChain newResourceTransformationChain() {
        return new TransformationChain((List)extensionFactory.getAllExtensions(Transformation.class, Qualifiers.enabled()));
    }

    protected ConverterChain getConverterChain() {
        @SuppressWarnings("rawtypes")
        List<Converter> converters = extensionFactory.getAllExtensions(
            Converter.class,
            Qualifiers.enabled());
        return new ConverterChain(converters);
    }

    protected ResourceList<HasMetadata> addAllResourcesFromRepositories(HasItems resources) {
        Stream<? extends HasMetadata> stream = extensionFactory
            .getAllExtensions(ResourceRepository.class, Qualifiers.enabled())
            .stream().flatMap(repository -> {
                LOG.info("Loading resources from repository '{}'", repository.getName());
                return repository.all().stream();
            });
        return ResourceList.of(Stream.concat(stream, resources.getItems().stream()).toList());
    }

    @SuppressWarnings("unchecked")
    protected <T extends HasMetadata> Collector<T> getMatchingCollector(@NotNull ResourceType resource) {
        LOG.info("Looking for a collector accepting resource type: group={}, version={} and kind={}",
            resource.group(),
            resource.apiVersion(),
            resource.kind()
        );
        return extensionFactory.
            findExtension(Collector.class, Qualifiers.bySupportedResource(resource))
            .orElseThrow(() -> new JikkouRuntimeException(String.format(
                "Cannot find collector for resource type: group='%s', apiVersion='%s' and kind='%s",
                resource.group(),
                resource.apiVersion(),
                resource.kind()
            )));
    }

    @SuppressWarnings("unchecked")
    protected Controller<HasMetadata, ResourceChange> getMatchingController(@NotNull ResourceType resource) {
        LOG.info("Looking for a controller accepting resource type: group={}, apiVersion={} and kind={}",
            resource.group(),
            resource.apiVersion(),
            resource.kind()
        );
        return extensionFactory
            .findExtension(Controller.class, Qualifiers.bySupportedResource(resource))
            .orElseThrow(() -> new JikkouRuntimeException(String.format(
                "Cannot find controller for resource type: group='%s', apiVersion='%s' and kind='%s",
                resource.group(),
                resource.apiVersion(),
                resource.kind()
            )));
    }

}
