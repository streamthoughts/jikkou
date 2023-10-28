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
package io.streamthoughts.jikkou.core;

import io.streamthoughts.jikkou.common.utils.Pair;
import io.streamthoughts.jikkou.core.config.Configuration;
import io.streamthoughts.jikkou.core.extension.Extension;
import io.streamthoughts.jikkou.core.extension.ExtensionDescriptorModifier;
import io.streamthoughts.jikkou.core.extension.ExtensionFactory;
import io.streamthoughts.jikkou.core.extension.qualifier.Qualifiers;
import io.streamthoughts.jikkou.core.models.ApiGroup;
import io.streamthoughts.jikkou.core.models.ApiGroupList;
import io.streamthoughts.jikkou.core.models.ApiGroupVersion;
import io.streamthoughts.jikkou.core.models.ApiResource;
import io.streamthoughts.jikkou.core.models.ApiResourceList;
import io.streamthoughts.jikkou.core.models.CoreAnnotations;
import io.streamthoughts.jikkou.core.models.GenericResourceListObject;
import io.streamthoughts.jikkou.core.models.HasItems;
import io.streamthoughts.jikkou.core.models.HasMetadata;
import io.streamthoughts.jikkou.core.models.HasMetadataChange;
import io.streamthoughts.jikkou.core.models.ObjectMeta;
import io.streamthoughts.jikkou.core.models.ReconciliationChangeResultList;
import io.streamthoughts.jikkou.core.models.ResourceListObject;
import io.streamthoughts.jikkou.core.models.ResourceType;
import io.streamthoughts.jikkou.core.reconcilier.Change;
import io.streamthoughts.jikkou.core.reconcilier.ChangeResult;
import io.streamthoughts.jikkou.core.reconcilier.Controller;
import io.streamthoughts.jikkou.core.reconcilier.Reconcilier;
import io.streamthoughts.jikkou.core.reporter.ChangeReporter;
import io.streamthoughts.jikkou.core.reporter.CombineChangeReporter;
import io.streamthoughts.jikkou.core.resource.ResourceCollector;
import io.streamthoughts.jikkou.core.resource.ResourceDescriptor;
import io.streamthoughts.jikkou.core.resource.ResourceRegistry;
import io.streamthoughts.jikkou.core.resource.converter.ResourceConverter;
import io.streamthoughts.jikkou.core.resource.transform.ResourceTransformation;
import io.streamthoughts.jikkou.core.resource.transform.ResourceTransformationChain;
import io.streamthoughts.jikkou.core.resource.validation.ResourceValidation;
import io.streamthoughts.jikkou.core.resource.validation.ResourceValidationChain;
import io.streamthoughts.jikkou.core.resource.validation.ValidationResult;
import io.streamthoughts.jikkou.core.selectors.AggregateSelector;
import io.streamthoughts.jikkou.core.selectors.ResourceSelector;
import java.time.Instant;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The default implementation of the {@link JikkouApi} interface.
 */
public final class DefaultApi implements AutoCloseable, JikkouApi {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultApi.class);

    public static Builder builder(ExtensionFactory extensionFactory,
                                  ResourceRegistry resourceRegistry) {
        return new Builder(extensionFactory, resourceRegistry);
    }

    /**
     * Builder class for creating a new {@link DefaultApi} object instance.
     */
    public static final class Builder implements JikkouApi.ApiBuilder<DefaultApi, Builder> {

        private final ExtensionFactory extensionFactory;

        private final ResourceRegistry resourceRegistry;

        /**
         * Creates a new {@link ExtensionFactory} instance.
         *
         * @param extensionFactory the extension factory.
         * @param resourceRegistry the resource registry.
         */
        public Builder(@NotNull ExtensionFactory extensionFactory,
                       @NotNull ResourceRegistry resourceRegistry) {
            this.extensionFactory = Objects.requireNonNull(extensionFactory, "extensionFactory must not be null");
            this.resourceRegistry = Objects.requireNonNull(resourceRegistry, "resourceRegistry must not be null");
        }

        /**
         * {@inheritDoc}
         **/
        @Override
        public <T extends Extension> Builder register(@NotNull Class<T> type, @NotNull Supplier<T> supplier) {
            extensionFactory.register(type, supplier);
            return this;
        }

        /**
         * {@inheritDoc}
         **/
        @Override
        public <T extends Extension> Builder register(@NotNull Class<T> type,
                                                      @NotNull Supplier<T> supplier,
                                                      ExtensionDescriptorModifier... modifiers) {
            extensionFactory.register(type, supplier, modifiers);
            return this;
        }

        /**
         * {@inheritDoc}
         **/
        @Override
        public DefaultApi build() {
            return new DefaultApi(extensionFactory, resourceRegistry);
        }
    }

    private final List<ChangeReporter> reporters = new LinkedList<>();
    private final ExtensionFactory extensionFactory;
    private final ResourceRegistry resourceRegistry;

    /**
     * Creates a new {@link DefaultApi} instance.
     */
    private DefaultApi(@NotNull final ExtensionFactory extensionFactory,
                       @NotNull final ResourceRegistry resourceRegistry) {
        this.extensionFactory = Objects.requireNonNull(extensionFactory, "extensionFactory must not be null");
        this.resourceRegistry = Objects.requireNonNull(resourceRegistry, "resourceRegistry must not be null");
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public List<ApiResourceList> listApiResources() {
        ApiGroupList apiGroupList = listApiGroups();
        return apiGroupList.groups()
                .stream()
                .flatMap(group -> group.versions()
                        .stream()
                        .map(apiGroupVersion -> Pair.of(group.name(), apiGroupVersion.version()))
                )
                .sorted(Comparator.comparing(Pair::_1))
                .map(groupVersion -> listApiResources(groupVersion._1(), groupVersion._2()))
                .toList();
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public ApiResourceList listApiResources(@NotNull String group, @NotNull String version) {
        List<ResourceDescriptor> descriptors = resourceRegistry.findDescriptorsByGroupVersion(group, version)
                .stream()
                .filter(ResourceDescriptor::isEnabled)
                .filter(Predicate.not(ResourceDescriptor::isResourceListObject))
                .toList();

        List<ApiResource> resources = descriptors.stream()
                .map(it -> {
                    String name = it.pluralName()
                            .orElse(it.resourceType().getKind())
                            .toLowerCase(Locale.ROOT);
                    return new ApiResource(
                            name,
                            it.kind(),
                            it.singularName(),
                            it.shortNames(),
                            it.description(),
                            it.orderedVerbs()
                    );
                })
                .sorted(Comparator.comparing(ApiResource::name))
                .toList();
        return new ApiResourceList(group + "/" + version, resources);
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public ApiGroupList listApiGroups() {
        List<ResourceDescriptor> allResourceDescriptors = resourceRegistry.getAllResourceDescriptors();
        Map<String, List<ResourceDescriptor>> descriptorsByGroup = allResourceDescriptors
                .stream()
                .filter(ResourceDescriptor::isEnabled)
                .filter(Predicate.not(ResourceDescriptor::isResourceListObject))
                .collect(Collectors.groupingBy(ResourceDescriptor::group, Collectors.toList()));

        List<ApiGroup> groups = descriptorsByGroup.entrySet()
                .stream()
                .map(entry -> {
                    List<ResourceDescriptor> descriptors = entry.getValue();
                    Set<ApiGroupVersion> versions = descriptors
                            .stream()
                            .map(it -> new ApiGroupVersion(
                                    it.group() + "/" + it.apiVersion(),
                                    it.apiVersion())
                            )
                            .collect(Collectors.toSet());
                    return new ApiGroup(entry.getKey(), versions);
                })
                .toList();
        return new ApiGroupList(groups);
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public ReconciliationChangeResultList<Change> apply(@NotNull final HasItems resources,
                                                        @NotNull final ReconciliationMode mode,
                                                        @NotNull final ReconciliationContext context) {
        LOG.info("Starting reconciliation of {} resource objects in {} mode.", resources.getItems().size(), mode);
        Map<ResourceType, List<HasMetadata>> resourcesByType = validate(resources, context).get().groupByType();
        List<ChangeResult<Change>> results = resourcesByType
                .entrySet()
                .stream()
                .map(e -> executeReconciliation(mode, context, e.getKey(), e.getValue()))
                .flatMap(Collection::stream)
                .toList();
        if (!context.isDryRun() && !reporters.isEmpty()) {
            List<ChangeResult<Change>> reportable = results.stream()
                    .filter(t -> !CoreAnnotations.isAnnotatedWithNoReport(t.data()))
                    .toList();
            new CombineChangeReporter(reporters).report(reportable);
        }
        return new ReconciliationChangeResultList<>(
                context.isDryRun(),
                new ObjectMeta(),
                results
        );
    }

    private List<ChangeResult<Change>> executeReconciliation(@NotNull ReconciliationMode mode,
                                                             @NotNull ReconciliationContext context,
                                                             @NotNull ResourceType resourceType,
                                                             @NotNull List<HasMetadata> resources) {
        Controller<HasMetadata, Change> controller = getMatchingResourceController(resourceType);
        LOG.info("Starting reconciliation using controller: '{}' (mode: {}, dryRun: {})",
                controller.getName(),
                mode,
                context.isDryRun()
        );

        Reconcilier<HasMetadata, Change> reconcilier = new Reconcilier<>(controller);
        List<ChangeResult<Change>> changes = reconcilier.reconcile(resources, mode, context);

        LOG.info("Reconciliation completed using controller: '{}' (mode: {}, dryRun: {})",
                controller.getName(),
                mode,
                context.isDryRun()
        );
        return changes;
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public ApiResourceValidationResult validate(final @NotNull HasItems resources,
                                                final @NotNull ReconciliationContext context) {

        List<HasMetadata> converted = resources.groupByType()
                .entrySet()
                .stream()
                .flatMap(entry -> {
                    ResourceType type = entry.getKey();
                    if (type.isTransient()) {
                        return entry.getValue().stream();
                    } else {
                        Controller<HasMetadata, Change> controller = getMatchingResourceController(type);
                        ResourceConverter<HasMetadata, HasMetadata> converter = controller.getResourceConverter(type);
                        return converter.convertFrom(entry.getValue()).stream();
                    }
                }).toList();

        GenericResourceListObject<HasMetadata> allResources = new GenericResourceListObject<>(converted);

        Stream<Pair<ResourceType, List<HasMetadata>>> stream = allResources.groupByType()
                .entrySet()
                .stream()
                .map(Pair::of);

        // Execute transformation
        ResourceTransformationChain transformationChain = getResourceTransformationChain();
        stream = stream
                .map(t -> t.mapRight(resource ->
                        transformationChain.transformAll(resource, allResources, context)
                ));

        // Execute selectors
        stream = stream
                .filter(t -> !t._1().isTransient())
                .map(t -> t.mapRight(resource -> {
                    List<ResourceSelector> selectors = context.selectors();
                    return new GenericResourceListObject<>(t._2()).getAllMatching(selectors);
                }));

        Map<ResourceType, List<HasMetadata>> transformed = stream
                .collect(Collectors.toMap(Pair::_1, Pair::_2));

        // Execute validations
        ResourceValidationChain validationChain = getResourceValidationChain();

        ValidationResult result = validationChain.validate(transformed);
        if (result.isValid()) {
            List<HasMetadata> list = transformed.values().stream().flatMap(Collection::stream).toList();
            return new ApiResourceValidationResult(new GenericResourceListObject<>(list));
        }

        return new ApiResourceValidationResult(result.errors());
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public List<ResourceListObject<HasMetadataChange<Change>>> getDiff(final @NotNull HasItems resources,
                                                                       final @NotNull ReconciliationContext context) {

        Map<ResourceType, List<HasMetadata>> resourcesByType = validate(resources, context).get().groupByType();

        return resourcesByType.entrySet()
                .stream()
                .map(e -> {
                    Controller<HasMetadata, Change> controller = getMatchingResourceController(e.getKey());
                    List<HasMetadata> resource = e.getValue();
                    ResourceListObject<? extends HasMetadataChange<Change>> changes = controller.plan(
                            resource,
                            context
                    );
                    return (ResourceListObject<HasMetadataChange<Change>>) changes;
                })
                .toList();
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public List<HasMetadata> getResources(final @NotNull ResourceType resourceType,
                                          final @NotNull List<ResourceSelector> selectors,
                                          final @NotNull Configuration configuration) {
        ResourceCollector<HasMetadata> collector = getMatchingResourceCollector(resourceType);
        List<HasMetadata> resources = collector.listAll(configuration, selectors);
        ResourceConverter<HasMetadata, HasMetadata> converter = collector.getResourceConverter(resourceType);
        List<HasMetadata> result = resources.stream()
                .map(DefaultApi::enrichWithGeneratedAnnotation)
                .filter(new AggregateSelector(selectors)::apply)
                .toList();
        return converter.convertTo(result);
    }

    private static HasMetadata enrichWithGeneratedAnnotation(HasMetadata resource) {
        ObjectMeta meta = resource
                .optionalMetadata()
                .orElse(new ObjectMeta()).toBuilder()
                .withAnnotation(CoreAnnotations.JKKOU_IO_RESOURCE_GENERATED, Instant.now())
                .build();
        return resource.withMetadata(meta);
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public Builder toBuilder() {
        return new Builder(extensionFactory.duplicate(), resourceRegistry);
    }

    private ResourceValidationChain getResourceValidationChain() {
        @SuppressWarnings("rawtypes")
        List<ResourceValidation> validations = extensionFactory
                .getAllExtensions(ResourceValidation.class, Qualifiers.enabled());
        return new ResourceValidationChain(validations);
    }

    private ResourceTransformationChain getResourceTransformationChain() {
        @SuppressWarnings("rawtypes")
        List<ResourceTransformation> transformations = extensionFactory.getAllExtensions(
                ResourceTransformation.class, Qualifiers.enabled());
        return new ResourceTransformationChain(transformations);
    }

    @SuppressWarnings("unchecked")
    private ResourceCollector<HasMetadata> getMatchingResourceCollector(@NotNull ResourceType resource) {
        LOG.info("Looking for a collector accepting resource type: group={}, version={} and kind={}",
                resource.getGroup(),
                resource.getApiVersion(),
                resource.getKind()
        );
        return extensionFactory.getExtension(
                ResourceCollector.class,
                Qualifiers.byAcceptedResource(resource)
        );
    }

    @SuppressWarnings("unchecked")
    private Controller<HasMetadata, Change> getMatchingResourceController(@NotNull ResourceType resource) {
        LOG.info("Looking for a controller accepting resource type: group={}, version={} and kind={}",
                resource.getGroup(),
                resource.getApiVersion(),
                resource.getKind()
        );
        return extensionFactory.getExtension(
                Controller.class,
                Qualifiers.byAcceptedResource(resource)
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() {
        // intentionally left blank - this class is not responsible for closing any instances (for the moment).
    }
}
