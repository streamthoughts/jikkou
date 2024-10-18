/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core;

import io.streamthoughts.jikkou.core.action.Action;
import io.streamthoughts.jikkou.core.action.ExecutionResultSet;
import io.streamthoughts.jikkou.core.config.Configuration;
import io.streamthoughts.jikkou.core.exceptions.ResourceNotFoundException;
import io.streamthoughts.jikkou.core.extension.Extension;
import io.streamthoughts.jikkou.core.extension.ExtensionCategory;
import io.streamthoughts.jikkou.core.extension.ExtensionDescriptor;
import io.streamthoughts.jikkou.core.extension.ExtensionDescriptorModifier;
import io.streamthoughts.jikkou.core.extension.ExtensionFactory;
import io.streamthoughts.jikkou.core.extension.exceptions.NoSuchExtensionException;
import io.streamthoughts.jikkou.core.extension.qualifier.Qualifiers;
import io.streamthoughts.jikkou.core.health.Health;
import io.streamthoughts.jikkou.core.health.HealthAggregator;
import io.streamthoughts.jikkou.core.health.HealthIndicator;
import io.streamthoughts.jikkou.core.models.ApiActionResultSet;
import io.streamthoughts.jikkou.core.models.ApiChangeResultList;
import io.streamthoughts.jikkou.core.models.ApiExtension;
import io.streamthoughts.jikkou.core.models.ApiExtensionList;
import io.streamthoughts.jikkou.core.models.ApiExtensionSpec;
import io.streamthoughts.jikkou.core.models.ApiExtensionSummary;
import io.streamthoughts.jikkou.core.models.ApiGroup;
import io.streamthoughts.jikkou.core.models.ApiGroupList;
import io.streamthoughts.jikkou.core.models.ApiGroupVersion;
import io.streamthoughts.jikkou.core.models.ApiHealthIndicator;
import io.streamthoughts.jikkou.core.models.ApiHealthIndicatorList;
import io.streamthoughts.jikkou.core.models.ApiHealthResult;
import io.streamthoughts.jikkou.core.models.ApiOptionSpec;
import io.streamthoughts.jikkou.core.models.ApiResource;
import io.streamthoughts.jikkou.core.models.ApiResourceChangeList;
import io.streamthoughts.jikkou.core.models.ApiResourceList;
import io.streamthoughts.jikkou.core.models.ApiResourceVerbOptionList;
import io.streamthoughts.jikkou.core.models.ApiValidationResult;
import io.streamthoughts.jikkou.core.models.CoreAnnotations;
import io.streamthoughts.jikkou.core.models.DefaultResourceListObject;
import io.streamthoughts.jikkou.core.models.HasItems;
import io.streamthoughts.jikkou.core.models.HasMetadata;
import io.streamthoughts.jikkou.core.models.HasMetadataAcceptable;
import io.streamthoughts.jikkou.core.models.ObjectMeta;
import io.streamthoughts.jikkou.core.models.ResourceListObject;
import io.streamthoughts.jikkou.core.models.ResourceType;
import io.streamthoughts.jikkou.core.models.Verb;
import io.streamthoughts.jikkou.core.models.change.ResourceChange;
import io.streamthoughts.jikkou.core.reconciler.ChangeResult;
import io.streamthoughts.jikkou.core.reconciler.Collector;
import io.streamthoughts.jikkou.core.reconciler.Controller;
import io.streamthoughts.jikkou.core.reconciler.Reconciler;
import io.streamthoughts.jikkou.core.reconciler.ResourceChangeFilter;
import io.streamthoughts.jikkou.core.reconciler.config.ApiOptionSpecFactory;
import io.streamthoughts.jikkou.core.reporter.ChangeReporter;
import io.streamthoughts.jikkou.core.reporter.CombineChangeReporter;
import io.streamthoughts.jikkou.core.resource.ResourceDescriptor;
import io.streamthoughts.jikkou.core.resource.ResourceRegistry;
import io.streamthoughts.jikkou.core.selector.Selector;
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
    public static final ResourceChangeFilter.Noop NOOP_RESOURCE_CHANGE_FILTER = new ResourceChangeFilter.Noop();
    private static final String GROUP_API_VERSION_SEPARATOR = "/";

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
        List<ResourceDescriptor> descriptors = resourceRegistry.getDescriptorsByGroupAndVersion(group, version)
            .stream()
            .filter(ResourceDescriptor::isEnabled)
            .filter(Predicate.not(ResourceDescriptor::isTransient))
            .filter(Predicate.not(ResourceDescriptor::isResourceListObject))
            .toList();

        ApiOptionSpecFactory optionListFactory = new ApiOptionSpecFactory();
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

                if (resource.isVerbSupported(Verb.LIST) || resource.isVerbSupported(Verb.GET)) {
                    Optional<ExtensionDescriptor<Collector>> optional = extensionFactory
                        .findDescriptorByClass(Collector.class, Qualifiers.bySupportedResource(type));
                    if (optional.isPresent()) {
                        ExtensionDescriptor<Collector> collector = optional.get();
                        List<ApiOptionSpec> optionSpecs = optionListFactory.make(collector);
                        if (!optionSpecs.isEmpty()) {
                            if (resource.isVerbSupported(Verb.LIST)) {
                                resource = resource.withApiResourceVerbOptionList(
                                    new ApiResourceVerbOptionList(Verb.LIST, optionSpecs)
                                );
                            }
                            if (resource.isVerbSupported(Verb.GET)) {
                                resource = resource.withApiResourceVerbOptionList(
                                    new ApiResourceVerbOptionList(Verb.GET, optionSpecs)
                                );
                            }
                        }
                    }
                }
                return resource;

            })
            .sorted(Comparator.comparing(ApiResource::name))
            .toList();
        return new ApiResourceList(group + GROUP_API_VERSION_SEPARATOR + version, resources);
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public ApiGroupList listApiGroups() {
        List<ResourceDescriptor> allResourceDescriptors = resourceRegistry.allDescriptors();
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
                        it.group() + GROUP_API_VERSION_SEPARATOR + it.apiVersion(),
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
        return getApiExtensionList(extensionFactory.getAllDescriptors());
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public ApiExtensionList getApiExtensions(@NotNull String type) {
        return getApiExtensionList(extensionFactory.findAllDescriptorsByAlias(type));
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public ApiExtensionList getApiExtensions(@NotNull Class<?> extensionType) {
        return getApiExtensionList(extensionFactory.findAllDescriptorsByClass(extensionType));
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public ApiExtensionList getApiExtensions(@NotNull ExtensionCategory category) {
        return getApiExtensionList(extensionFactory.findAllDescriptors(Qualifiers.byCategory(category)));
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public ApiExtension getApiExtension(@NotNull String extensionName) {
        return getApiExtension(Extension.class, extensionName);
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public ApiExtension getApiExtension(@NotNull Class<?> extensionType,
                                        @NotNull String extensionName) {
        ExtensionDescriptor<?> descriptor = extensionFactory
            .findDescriptorByClass(extensionType, Qualifiers.byName(extensionName))
            .orElseThrow(() -> new NoSuchExtensionException("No such extension exists for name '" + extensionName + "'"));

        ApiOptionSpecFactory optionListFactory = new ApiOptionSpecFactory();
        ApiExtensionSpec apiExtensionSpec = new ApiExtensionSpec(
            descriptor.name(),
            descriptor.title(),
            descriptor.description(),
            descriptor.examples(),
            descriptor.category().name(),
            descriptor.provider(),
            optionListFactory.make(descriptor),
            HasMetadataAcceptable.getSupportedResources(descriptor.type())
        );
        return new ApiExtension(apiExtensionSpec);
    }

    @NotNull
    private static ApiExtensionList getApiExtensionList(List<? extends ExtensionDescriptor> descriptors) {
        List<ApiExtensionSummary> extensions = descriptors
            .stream()
            .map(descriptor -> new ApiExtensionSummary(
                    descriptor.name(),
                    descriptor.category().name(),
                    descriptor.provider()
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
        return patch(getDiff(resources, NOOP_RESOURCE_CHANGE_FILTER, context).getItems(), mode, context);
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public ApiChangeResultList patch(@NotNull List<ResourceChange> changes,
                                     @NotNull ReconciliationMode mode,
                                     @NotNull ReconciliationContext context) {
        Map<ResourceType, List<ResourceChange>> changesGroupByResourceType = ResourceListObject
            .of(changes)
            .groupBy(ResourceType::of);

        final List<ChangeResult> changeResults = new LinkedList<>();

        for (Map.Entry<ResourceType, List<ResourceChange>> entry : changesGroupByResourceType.entrySet()) {
            ResourceType type = entry.getKey();
            Controller<HasMetadata, ResourceChange> controller = getMatchingController(type);
            List<ResourceChange> items = entry.getValue();
            LOG.info("Applying {} changes for group={}, apiVersion={} and kind={} using controller: '{}' (mode: {}, dryRun: {}).",
                items.size(),
                type.group(),
                type.apiVersion(),
                type.kind(),
                controller.getName(),
                mode,
                context.isDryRun()
            );
            Reconciler<HasMetadata, ResourceChange> reconciler = new Reconciler<>(controller);
            List<ChangeResult> results = reconciler.apply(items, mode, context);
            changeResults.addAll(results);
            LOG.info("Executed {} changes for group={}, apiVersion={} and kind={} using controller: '{}' (mode: {}, dryRun: {}).",
                results.size(),
                type.group(),
                type.apiVersion(),
                type.kind(),
                controller.getName(),
                mode,
                context.isDryRun()
            );
        }

        if (!context.isDryRun() && !reporters.isEmpty()) {
            List<ChangeResult> reportable = changeResults.stream()
                .filter(t -> !CoreAnnotations.isAnnotatedWithNoReport(t.change()))
                .collect(Collectors.toList());
            CombineChangeReporter reporter = new CombineChangeReporter(reporters);
            reporter.report(reportable);
        }
        return new ApiChangeResultList(context.isDryRun(), new ObjectMeta(), changeResults);
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public ApiActionResultSet execute(@NotNull String name,
                                      @NotNull Configuration configuration) {
        Objects.requireNonNull(name, "name cannot be null");
        Objects.requireNonNull(configuration, "configuration cannot be null");
        Action<HasMetadata> action = getMatchingAction(name);
        if (LOG.isDebugEnabled()) {
            LOG.debug(
                "Executing action '{}' with configuration: {}",
                name,
                configuration.asMap()
            );
        }
        ExecutionResultSet<HasMetadata> resultSet = action.execute(configuration);
        ObjectMeta.ObjectMetaBuilder builder = ObjectMeta.builder();
        for (String key : configuration.keys()) {
            builder.withAnnotation(String.format("configs.jikkou.io/%s", key), configuration.getAny(key));
        }
        ObjectMeta objectMeta = builder.build();
        return new ApiActionResultSet<>(objectMeta, resultSet.results());
    }

    @SuppressWarnings("unchecked")
    private Action<HasMetadata> getMatchingAction(@NotNull String action) {
        LOG.info("Looking for an action named '{}'", action);
        return extensionFactory.findExtension(
            Action.class,
            Qualifiers.byQualifiers(
                Qualifiers.byName(action)
            )

        ).orElseThrow(() -> new NoSuchExtensionException(String.format("Cannot find action '%s'", action)));
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
            new ApiValidationResult(ResourceListObject.of(items)) :
            new ApiValidationResult(result.errors());
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public <T extends HasMetadata> T getResource(@NotNull ResourceType type,
                                                 @NotNull String name,
                                                 @NotNull Configuration configuration) {
        Collector<T> collector = getMatchingCollector(type);
        final OffsetDateTime timestamp = OffsetDateTime.now(ZoneOffset.UTC);
        return collector.get(name, configuration)
            .map(item -> addBuiltInAnnotations(item, timestamp))
            .orElseThrow(() -> new ResourceNotFoundException(
                String.format(
                    "Cannot found resource for group '%s', version '%s', kind '%s', and name '%s'.",
                    type.group(),
                    type.apiVersion(),
                    type.kind(),
                    name
                ))
            );
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public ApiResourceChangeList getDiff(@NotNull HasItems resources,
                                         @NotNull ResourceChangeFilter filter,
                                         @NotNull ReconciliationContext context) {
        // Exclude any resource changes that might be transmitted by inadvertence to the diff command.
        List<? extends HasMetadata> list = resources.getItems()
            .stream()
            .filter(Predicate.not(o -> o instanceof ResourceChange))
            .toList();
        DefaultResourceListObject filtered = new DefaultResourceListObject<>(list);

        Map<ResourceType, List<HasMetadata>> resourcesByType = validate(filtered, context).get().groupByType();
        List<ResourceChange> results = resourcesByType.entrySet()
            .stream()
            .map(e -> {
                final ResourceType type = e.getKey();
                Controller<HasMetadata, ResourceChange> controller = getMatchingController(type);
                List<HasMetadata> items = e.getValue();
                LOG.info("Planning changes of {} resources for group={}, apiVersion={} and kind={} using controller: '{}'.",
                    items.size(),
                    type.group(),
                    type.apiVersion(),
                    type.kind(),
                    controller.getName()
                );
                List<ResourceChange> changes = controller.plan(items, context);
                return filter.filter(changes);
            })
            .flatMap(Collection::stream)
            .collect(Collectors.toList());
        return new ApiResourceChangeList(results);
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public <T extends HasMetadata> ResourceListObject<T> listResources(final @NotNull ResourceType type,
                                                                       final @NotNull Selector selector,
                                                                       final @NotNull Configuration configuration) {
        final Collector<T> collector = getMatchingCollector(type);

        ResourceListObject<T> result = collector.listAll(configuration, selector);

        final OffsetDateTime timestamp = OffsetDateTime.now(ZoneOffset.UTC);

        List<T> items = result.getItems()
            .stream()
            // Always apply selector on each item.
            .filter(selector::apply)
            // Always add build-in annotations on each item.
            .map(item -> addBuiltInAnnotations(item, timestamp))
            .collect(Collectors.toList());

        result = new DefaultResourceListObject<>(
            result.getKind(),
            result.getApiVersion(),
            result.getMetadata(),
            items
        );
        return addBuiltInAnnotations(result, timestamp);
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
