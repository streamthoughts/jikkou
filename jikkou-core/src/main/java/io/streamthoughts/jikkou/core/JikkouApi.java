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

import io.streamthoughts.jikkou.common.annotation.InterfaceStability;
import io.streamthoughts.jikkou.common.utils.Pair;
import io.streamthoughts.jikkou.core.config.Configuration;
import io.streamthoughts.jikkou.core.exceptions.JikkouApiException;
import io.streamthoughts.jikkou.core.extension.Extension;
import io.streamthoughts.jikkou.core.extension.ExtensionDescriptorModifier;
import io.streamthoughts.jikkou.core.extension.exceptions.ConflictingExtensionDefinitionException;
import io.streamthoughts.jikkou.core.health.HealthIndicator;
import io.streamthoughts.jikkou.core.models.ApiChangeResultList;
import io.streamthoughts.jikkou.core.models.ApiExtensionList;
import io.streamthoughts.jikkou.core.models.ApiGroupList;
import io.streamthoughts.jikkou.core.models.ApiHealthIndicator;
import io.streamthoughts.jikkou.core.models.ApiHealthIndicatorList;
import io.streamthoughts.jikkou.core.models.ApiHealthResult;
import io.streamthoughts.jikkou.core.models.ApiResourceChangeList;
import io.streamthoughts.jikkou.core.models.ApiResourceList;
import io.streamthoughts.jikkou.core.models.ApiValidationResult;
import io.streamthoughts.jikkou.core.models.DefaultResourceListObject;
import io.streamthoughts.jikkou.core.models.HasItems;
import io.streamthoughts.jikkou.core.models.HasMetadata;
import io.streamthoughts.jikkou.core.models.ResourceListObject;
import io.streamthoughts.jikkou.core.models.ResourceType;
import io.streamthoughts.jikkou.core.reconcilier.Collector;
import io.streamthoughts.jikkou.core.selectors.Selector;
import java.time.Duration;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;
import org.jetbrains.annotations.NotNull;

/**
 * The Jikkou API interface.
 */
@InterfaceStability.Evolving
public interface JikkouApi extends AutoCloseable {

    /**
     * The builder interface to create new {@link JikkouApi}.
     *
     * @param <A> the type of api
     * @param <B> the type of builder
     */
    interface ApiBuilder<A extends JikkouApi, B extends JikkouApi.ApiBuilder<A, B>> {

        /**
         * Register an extension supplier for the specified extension type.
         *
         * @param type     the class of the extension.
         * @param supplier the supplier used to create a new instance of {@code T}.
         * @param <T>      type of the extension.
         * @throws NullPointerException                    if the given type of supplier is {@code null}.
         * @throws ConflictingExtensionDefinitionException if an extension is already register for that type.
         */
        <T extends Extension> B register(@NotNull Class<T> type,
                                         @NotNull Supplier<T> supplier);

        /**
         * Registers the component supplier for the specified type and name.
         *
         * @param type      the class of the extension.
         * @param supplier  the supplier used to create a new instance of {@code T}.
         * @param modifiers the component descriptor modifiers.
         * @param <T>       the component-type.
         * @throws NullPointerException                    if the given type of supplier is {@code null}.
         * @throws ConflictingExtensionDefinitionException if an extension is already register for that type.
         */
        <T extends Extension> B register(@NotNull Class<T> type,
                                         @NotNull Supplier<T> supplier,
                                         ExtensionDescriptorModifier... modifiers);

        /**
         * Build and return the {@link JikkouApi}.
         *
         * @return the {@link JikkouApi}; never {@code null}
         */
        A build();
    }


    /**
     * List the supported API resources.
     *
     * @return {@link ApiResourceList}.
     */
    default List<ApiResourceList> listApiResources() {
        ApiGroupList apiGroupList = listApiGroups();
        return apiGroupList.groups()
                .stream()
                .flatMap(group -> group.versions()
                        .stream()
                        .map(apiGroupVersion -> Pair.of(group.name(), apiGroupVersion.version()))
                )
                .map(groupVersion -> listApiResources(groupVersion._1(), groupVersion._2()))
                .sorted(Comparator.comparing(ApiResourceList::groupVersion))
                .toList();
    }

    /**
     * List the supported API resources for the specified API group and API version.
     *
     * @param group   the API group of the resource.
     * @param version the API version of the resource.
     * @return {@link ApiResourceList}.
     */
    ApiResourceList listApiResources(@NotNull String group,
                                     @NotNull String version);

    /**
     * List the supported API groups.
     *
     * @return {@link ApiGroupList}.
     */
    ApiGroupList listApiGroups();

    /**
     * List the supported health indicators.
     *
     * @return a {@link ApiExtensionList} instance.
     */
    ApiHealthIndicatorList getApiHealthIndicators();

    /**
     * Gets the health details for the specified health indicator name.
     *
     * @param name the health indicator name.
     * @return a new {@link HealthIndicator} instance.
     */
    ApiHealthResult getApiHealth(@NotNull String name, @NotNull Duration timeout);

    /**
     * Gets the health details for all supported health indicators.
     *
     * @return a new {@link HealthIndicator} instance.
     */
    ApiHealthResult getApiHealth(@NotNull Duration timeout);

    /**
     * Gets the health details for the specified {@link ApiHealthIndicator}.
     *
     * @param indicator the {@link ApiHealthIndicator}.
     * @return a new {@link HealthIndicator} instance.
     */
    default ApiHealthResult getApiHealth(@NotNull ApiHealthIndicator indicator,
                                         @NotNull Duration timeout) {
        return getApiHealth(indicator.name(), timeout);
    }

    /**
     * List the supported API extensions.
     *
     * @return a {@link ApiExtensionList} instance.
     */
    ApiExtensionList getApiExtensions();

    /**
     * Execute the reconciliation for the given resources using
     *
     * @param resources the list of resource to be reconciled.
     * @param mode      the reconciliation mode.
     * @param context   the context to be used for conciliation.
     * @return the list of 0all changes applied on the target system.
     * @throws JikkouApiException if no {@link Collector} can be found for the specified type,
     *                            or more than one descriptor match the type.
     */
    ApiChangeResultList reconcile(@NotNull HasItems resources,
                                  @NotNull ReconciliationMode mode,
                                  @NotNull ReconciliationContext context);

    /**
     * Execute validations on the given resources.
     *
     * @param resources the list of resource to create.
     * @return the validated {@link DefaultResourceListObject}.
     * @throws JikkouApiException if no {@link Collector} can be found for the specified type,
     *                            or more than one descriptor match the type.
     */
    default ApiValidationResult validate(@NotNull HasItems resources) {
        return validate(resources, ReconciliationContext.Default.EMPTY);
    }

    /**
     * Execute validations of the specified resources.
     *
     * @param resources the list of resource to create.
     * @param context   the reconciliation context.
     * @return the validated {@link DefaultResourceListObject}.
     * @throws JikkouApiException if no {@link Collector} can be found for the specified type,
     *                            or more than one descriptor match the type.
     */
    ApiValidationResult validate(@NotNull HasItems resources,
                                 @NotNull ReconciliationContext context);

    /**
     * Execute transformations of the specified resources.
     *
     * @param resources the list of resources to prepare.
     * @param context   the reconciliation context.
     * @return the transformed resources.
     */
    HasItems prepare(final @NotNull HasItems resources,
                     final @NotNull ReconciliationContext context);

    /**
     * Get the resources associated to the given type.
     *
     * @param resourceClass the class of the resource to be described.
     * @return the {@link HasMetadata}.
     * @throws JikkouApiException if no {@link Collector} can be found for the specified type,
     *                            or more than one descriptor match the type.
     */
    default ResourceListObject<HasMetadata> getResources(@NotNull Class<? extends HasMetadata> resourceClass) {
        return getResources(resourceClass, Collections.emptyList(), Configuration.empty());
    }

    /**
     * Get the resources associated to the given type.
     *
     * @param resourceClass the class of the resource to be described.
     * @return the {@link HasMetadata}.
     * @throws JikkouApiException if no {@link Collector} can be found for the specified type,
     *                            or more than one descriptor match the type.
     */
    default ResourceListObject<HasMetadata> getResources(@NotNull Class<? extends HasMetadata> resourceClass,
                                                         @NotNull List<Selector> selectors) {
        return getResources(resourceClass, selectors, Configuration.empty());
    }

    /**
     * Get the resource associated to the given type.
     *
     * @param resourceClass the class of the resource to be described.
     * @return the {@link HasMetadata}.
     * @throws JikkouApiException if no {@link Collector} can be found for the specified type,
     *                            or more than one descriptor match the type.
     */
    default <T extends HasMetadata> ResourceListObject<T> getResources(@NotNull Class<? extends HasMetadata> resourceClass,
                                                                       @NotNull Configuration configuration) {
        return getResources(resourceClass, Collections.emptyList(), configuration);
    }


    /**
     * Get the resource associated to the given type.
     *
     * @param type          the class of the resource to be described.
     * @param configuration the configuration to be used for describing the resource-type.
     * @return the {@link HasMetadata}.
     * @throws JikkouApiException if no {@link Collector} can be found for the specified type,
     *                            or more than one descriptor match the type.
     */
    @SuppressWarnings("unchecked")
    default <T extends HasMetadata> ResourceListObject<T> getResources(@NotNull Class<? extends HasMetadata> type,
                                                                       @NotNull List<Selector> selectors,
                                                                       @NotNull Configuration configuration) {
        return (ResourceListObject<T>) getResources(ResourceType.of(type), selectors, configuration);
    }

    /**
     * Get all the changes for the given resources.
     *
     * @param resources the list of resource to be reconciled.
     * @return the {@link HasMetadata}.
     */
    ApiResourceChangeList getDiff(@NotNull HasItems resources,
                                  @NotNull ReconciliationContext context);

    /**
     * Get the resource associated to the given type.
     *
     * @param resourceType the type of the resource to be described.
     * @return the {@link HasMetadata}.
     */
    default ResourceListObject<HasMetadata> getResources(@NotNull ResourceType resourceType) {
        return getResources(resourceType, Collections.emptyList(), Configuration.empty());
    }

    /**
     * Get the resource associated to the given type.
     *
     * @param resourceType the type of the resource to be described.
     * @return the {@link HasMetadata}.
     */
    default ResourceListObject<HasMetadata> getResources(@NotNull ResourceType resourceType,
                                                         @NotNull List<Selector> selectors) {
        return getResources(resourceType, selectors, Configuration.empty());
    }

    /**
     * Get the resource associated to the given type.
     *
     * @param resourceType  the type of the resource to be described.
     * @param configuration the option to be used for describing the resource-type.
     * @return the {@link HasMetadata}.
     */
    default <T extends HasMetadata> ResourceListObject<T> getResources(@NotNull ResourceType resourceType,
                                                                       @NotNull Configuration configuration) {
        return (ResourceListObject<T>) getResources(resourceType, Collections.emptyList(), configuration);
    }

    /**
     * Get the resource associated to the given type.
     *
     * @param resourceType  the type of the resource to be described.
     * @param configuration the option to be used for describing the resource-type.
     * @return the {@link HasMetadata}.
     */
    ResourceListObject<HasMetadata> getResources(@NotNull ResourceType resourceType,
                                                 @NotNull List<Selector> selectors,
                                                 @NotNull Configuration configuration);

    @SuppressWarnings("rawtypes")
    ApiBuilder toBuilder();

    /**
     * {@inheritDoc}
     **/
    @Override
    void close();
}
