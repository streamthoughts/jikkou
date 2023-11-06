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

import io.streamthoughts.jikkou.core.config.Configuration;
import io.streamthoughts.jikkou.core.extension.Extension;
import io.streamthoughts.jikkou.core.extension.ExtensionDescriptor;
import io.streamthoughts.jikkou.core.extension.ExtensionDescriptorModifier;
import io.streamthoughts.jikkou.core.extension.ExtensionFactory;
import io.streamthoughts.jikkou.core.extension.qualifier.Qualifiers;
import io.streamthoughts.jikkou.core.health.Health;
import io.streamthoughts.jikkou.core.health.HealthAggregator;
import io.streamthoughts.jikkou.core.health.HealthIndicator;
import io.streamthoughts.jikkou.core.models.ApiChangeResultList;
import io.streamthoughts.jikkou.core.models.ApiExtension;
import io.streamthoughts.jikkou.core.models.ApiExtensionList;
import io.streamthoughts.jikkou.core.models.ApiGroup;
import io.streamthoughts.jikkou.core.models.ApiGroupList;
import io.streamthoughts.jikkou.core.models.ApiGroupVersion;
import io.streamthoughts.jikkou.core.models.ApiHealthIndicator;
import io.streamthoughts.jikkou.core.models.ApiHealthIndicatorList;
import io.streamthoughts.jikkou.core.models.ApiHealthResult;
import io.streamthoughts.jikkou.core.models.ApiResource;
import io.streamthoughts.jikkou.core.models.ApiResourceChangeList;
import io.streamthoughts.jikkou.core.models.ApiResourceList;
import io.streamthoughts.jikkou.core.models.ApiResourceVerbOptionList;
import io.streamthoughts.jikkou.core.models.ApiResourceVerbOptionSpec;
import io.streamthoughts.jikkou.core.models.ApiValidationResult;
import io.streamthoughts.jikkou.core.models.CoreAnnotations;
import io.streamthoughts.jikkou.core.models.DefaultResourceListObject;
import io.streamthoughts.jikkou.core.models.HasItems;
import io.streamthoughts.jikkou.core.models.HasMetadata;
import io.streamthoughts.jikkou.core.models.HasMetadataChange;
import io.streamthoughts.jikkou.core.models.ObjectMeta;
import io.streamthoughts.jikkou.core.models.ResourceListObject;
import io.streamthoughts.jikkou.core.models.ResourceType;
import io.streamthoughts.jikkou.core.models.Verb;
import io.streamthoughts.jikkou.core.reconcilier.Change;
import io.streamthoughts.jikkou.core.reconcilier.ChangeResult;
import io.streamthoughts.jikkou.core.reconcilier.Collector;
import io.streamthoughts.jikkou.core.reconcilier.Controller;
import io.streamthoughts.jikkou.core.reconcilier.Reconcilier;
import io.streamthoughts.jikkou.core.reconcilier.config.ApiResourceVerbOptionSpecFactory;
import io.streamthoughts.jikkou.core.reporter.ChangeReporter;
import io.streamthoughts.jikkou.core.reporter.CombineChangeReporter;
import io.streamthoughts.jikkou.core.resource.ResourceDescriptor;
import io.streamthoughts.jikkou.core.resource.ResourceRegistry;
import io.streamthoughts.jikkou.core.selectors.Selector;
import io.streamthoughts.jikkou.core.validation.ValidationChain;
import io.streamthoughts.jikkou.core.validation.ValidationResult;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The default implementation of the {@link JikkouApi} interface.
 */
@SuppressWarnings("rawtypes")
public final class DefaultApi extends BaseApi implements AutoCloseable, JikkouApi {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultApi.class);

    /**
     * Gets a new builder.
     *
     * @param extensionFactory The ExtensionFactory.
     * @param resourceRegistry The ResourceRegistry.
     * @return The Builder.
     */
    public static Builder builder(@NotNull ExtensionFactory extensionFactory,
                                  @NotNull ResourceRegistry resourceRegistry) {
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
    private final ResourceRegistry resourceRegistry;

    /**
     * Creates a new {@link DefaultApi} instance.
     */
    private DefaultApi(@NotNull final ExtensionFactory extensionFactory,
                       @NotNull final ResourceRegistry resourceRegistry) {
        super(extensionFactory);
        this.resourceRegistry = Objects.requireNonNull(resourceRegistry, "resourceRegistry must not be null");
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public ApiResourceList listApiResources(@NotNull String group, @NotNull String version) {
        List<ResourceDescriptor> descriptors = resourceRegistry.findDescriptorsByGroupVersion(group, version)
                .stream()
                .filter(ResourceDescriptor::isEnabled)
                .filter(Predicate.not(ResourceDescriptor::isTransient))
                .filter(Predicate.not(ResourceDescriptor::isResourceListObject))
                .toList();

        ApiResourceVerbOptionSpecFactory optionListFactory = new ApiResourceVerbOptionSpecFactory();
        List<ApiResource> resources = descriptors.stream()
                .map(descriptor -> {
                    ResourceType type = descriptor.resourceType();
                    String name = descriptor.pluralName()
                            .orElse(type.kind())
                            .toLowerCase(Locale.ROOT);

                    ApiResource resource = new ApiResource(
                            name,
                            descriptor.kind(),
                            descriptor.singularName(),
                            descriptor.shortNames(),
                            descriptor.description(),
                            descriptor.orderedVerbs()
                    );

                    if (resource.isVerbSupported(Verb.LIST)) {
                        Optional<ExtensionDescriptor<Collector>> optional = extensionFactory
                                .findDescriptorByClass(Collector.class, Qualifiers.byAcceptedResource(type));
                        if (optional.isPresent()) {
                            ExtensionDescriptor<Collector> collector = optional.get();
                            List<ApiResourceVerbOptionSpec> optionSpecs = optionListFactory.make(collector.type());
                            if (!optionSpecs.isEmpty()) {
                                resource = resource.withApiResourceVerbOptionList(
                                        new ApiResourceVerbOptionList(Verb.LIST, optionSpecs)
                                );
                            }
                        }
                    }
                    return resource;

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
                .filter(Predicate.not(ResourceDescriptor::isTransient))
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
    public ApiHealthIndicatorList getApiHealthIndicators() {
        List<ApiHealthIndicator> indicators = extensionFactory
                .findAllDescriptorsByClass(HealthIndicator.class)
                .stream()
                .map(descriptor -> new ApiHealthIndicator(descriptor.name(), descriptor.description()))
                .toList();
        return new ApiHealthIndicatorList(indicators);
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public ApiHealthResult getApiHealth(@NotNull String name,
                                        @NotNull Duration timeout) {
        Health health = getHealth(name, timeout);
        return ApiHealthResult.from(health);
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public ApiHealthResult getApiHealth(@NotNull Duration timeout) {
        ApiHealthIndicatorList list = getApiHealthIndicators();
        List<Health> health = list.indicators().stream()
                .map(indicator -> getHealth(indicator.name(), timeout))
                .toList();

        HealthAggregator aggregator = new HealthAggregator();
        Health aggregated = aggregator.aggregate(health);
        return ApiHealthResult.from(aggregated);
    }


    private Health getHealth(@NotNull String name, @NotNull Duration timeout) {
        HealthIndicator extension = extensionFactory.getExtension(
                HealthIndicator.class,
                Qualifiers.byName(name)
        );
        Health health;
        try {
            health = extension.getHealth(timeout);
        } catch (Exception e) {
            health = Health
                    .builder()
                    .down()
                    .name(extension.getName())
                    .exception(e)
                    .build();
        }
        return health;
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public ApiExtensionList getApiExtensions() {
        List<ApiExtension> extensions = extensionFactory.getAllDescriptors()
                .stream()
                .map(descriptor -> new ApiExtension(
                                descriptor.name(),
                                descriptor.category(),
                                descriptor.source(),
                                descriptor.description()
                        )
                ).toList();
        return new ApiExtensionList(extensions);
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public ApiChangeResultList reconcile(@NotNull final HasItems resources,
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
        return new ApiChangeResultList(
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
    public ApiValidationResult validate(final @NotNull HasItems resources,
                                        final @NotNull ReconciliationContext context) {
        HasItems prepared = prepare(resources, context);
        ValidationChain validationChain = getResourceValidationChain();
        List<HasMetadata> items = (List<HasMetadata>) prepared.getItems();
        ValidationResult result = validationChain.validate(items);

        return result.isValid() ?
                new ApiValidationResult(new DefaultResourceListObject<>(items)) :
                new ApiValidationResult(result.errors());
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public ApiResourceChangeList getDiff(final @NotNull HasItems resources,
                                         final @NotNull ReconciliationContext context) {

        Map<ResourceType, List<HasMetadata>> resourcesByType = validate(resources, context).get().groupByType();

        List<ResourceListObject<HasMetadataChange<Change>>> changes = resourcesByType.entrySet()
                .stream()
                .map(e -> {
                    Controller<HasMetadata, Change> controller = getMatchingResourceController(e.getKey());
                    List<HasMetadata> resource = e.getValue();
                    ResourceListObject<? extends HasMetadataChange<Change>> result = controller.plan(
                            resource,
                            context
                    );
                    return (ResourceListObject<HasMetadataChange<Change>>) result;
                })
                .toList();
        return new ApiResourceChangeList(changes);
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public ResourceListObject<HasMetadata> getResources(final @NotNull ResourceType resourceType,
                                                        final @NotNull List<Selector> selectors,
                                                        final @NotNull Configuration configuration) {
        Collector<HasMetadata> collector = getMatchingResourceCollector(resourceType);
        ResourceListObject<HasMetadata> resourceListObject = collector.listAll(configuration, selectors);

        OffsetDateTime timestamp = OffsetDateTime.now(ZoneOffset.UTC);

        List<HasMetadata> items = resourceListObject.getItems()
                .stream()
                .map(item -> addBuiltInAnnotations(item, timestamp))
                .collect(Collectors.toList());

        DefaultResourceListObject<HasMetadata> metadata = new DefaultResourceListObject<>(
                resourceListObject.getKind(),
                resourceListObject.getApiVersion(),
                resourceListObject.getMetadata(),
                items
        );
        return addBuiltInAnnotations(metadata, timestamp);
    }

    private <T extends HasMetadata> T addBuiltInAnnotations(T resource, OffsetDateTime timestamp) {
        ObjectMeta meta = resource
                .optionalMetadata()
                .orElse(new ObjectMeta()).toBuilder()
                .withAnnotation(
                        CoreAnnotations.JKKOU_IO_RESOURCE_GENERATED,
                        timestamp.toString()
                )
                .build();
        return (T) resource.withMetadata(meta);
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public Builder toBuilder() {
        return new Builder(extensionFactory.duplicate(), resourceRegistry);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() {
        // intentionally left blank - this class is not responsible for closing any instances (for the moment).
    }
}
