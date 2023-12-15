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
import io.streamthoughts.jikkou.core.config.Configuration;
import io.streamthoughts.jikkou.core.exceptions.JikkouApiException;
import io.streamthoughts.jikkou.core.exceptions.ResourceNotFoundException;
import io.streamthoughts.jikkou.core.extension.Extension;
import io.streamthoughts.jikkou.core.extension.ExtensionCategory;
import io.streamthoughts.jikkou.core.extension.ExtensionDescriptorModifier;
import io.streamthoughts.jikkou.core.extension.exceptions.ConflictingExtensionDefinitionException;
import io.streamthoughts.jikkou.core.health.HealthIndicator;
import io.streamthoughts.jikkou.core.models.ApiActionResultSet;
import io.streamthoughts.jikkou.core.models.ApiChangeResultList;
import io.streamthoughts.jikkou.core.models.ApiExtension;
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
import io.streamthoughts.jikkou.core.reconciler.Collector;
import io.streamthoughts.jikkou.core.reconciler.ResourceChangeFilter;
import io.streamthoughts.jikkou.core.selector.Selector;
import io.streamthoughts.jikkou.core.selector.Selectors;
import java.time.Duration;
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
    List<ApiResourceList> listApiResources();

    /**
     * List the supported API resources for the specified API group
     *
     * @param group the API group of the resource.
     * @return {@link ApiResourceList}.
     */
    List<ApiResourceList> listApiResources(@NotNull String group);

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
     * List the supported API extensions for the specified extension type.
     *
     * @param extensionType The type of the extension.
     * @return a {@link ApiExtensionList} instance.
     */
    ApiExtensionList getApiExtensions(@NotNull String extensionType);

    /**
     * List the supported API extensions for the specified extension type.
     *
     * @param extensionType The type of the extension.
     * @return a {@link ApiExtensionList} instance.
     */
    ApiExtensionList getApiExtensions(@NotNull Class<?> extensionType);

    /**
     * List the supported API extensions for the specified category.
     *
     * @param extensionCategory The category of the extension.
     * @return a {@link ApiExtensionList} instance.
     */
    ApiExtensionList getApiExtensions(@NotNull ExtensionCategory extensionCategory);

    /**
     * Gets the API extension for the specified name.
     *
     * @param extensionName The name of the extension.
     * @return a {@link ApiExtensionList} instance.
     */
    ApiExtension getApiExtension(@NotNull String extensionName);

    /**
     * Gets the API extension for the specified name and type.
     *
     * @param extensionType The type of the extension.
     * @param extensionName The name of the extension.
     * @return a {@link ApiExtensionList} instance.
     */
    ApiExtension getApiExtension(@NotNull Class<?> extensionType,
                                 @NotNull String extensionName);

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
     * Executes the specified action for the specified resource type.
     *
     * @param action        The name of the action.
     * @param configuration The configuration.
     * @return The ApiExecutionResult.
     */
    ApiActionResultSet<?> execute(@NotNull String action, @NotNull Configuration configuration);

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
     * Get the resource associated for the specified type.
     *
     * @param type The class of the resource to be described.
     * @param name The name of the resource.
     * @return the {@link HasMetadata}.
     * @throws JikkouApiException if no {@link Collector} can be found for the specified type,
     *                            or more than one descriptor match the type.
     */
    default <T extends HasMetadata> T getResource(@NotNull Class<? extends HasMetadata> type,
                                                  @NotNull String name,
                                                  @NotNull Configuration configuration) {
        return getResource(ResourceType.of(type), name, configuration);
    }

    /**
     * Get the resource associated for the specified type.
     *
     * @param type The class of the resource to be described.
     * @param name The name of the resource.
     * @return the {@link HasMetadata}.
     * @throws JikkouApiException        if no {@link Collector} can be found for the specified type,
     *                                   or more than one descriptor match the type.
     * @throws ResourceNotFoundException if no resource can be found for the given name.
     */
    <T extends HasMetadata> T getResource(@NotNull ResourceType type,
                                          @NotNull String name,
                                          @NotNull Configuration configuration);

    /**
     * Get all the changes for the given resources.
     *
     * @param resources The list of resource to be reconciled.
     * @return the {@link HasMetadata}.
     */
    default ApiResourceChangeList getDiff(@NotNull HasItems resources,
                                          @NotNull ReconciliationContext context) {
        return getDiff(resources, new ResourceChangeFilter.Noop(), context);
    }

    /**
     * Get all the changes for the given resources.
     *
     * @param resources The list of resource to be reconciled.
     * @return the {@link HasMetadata}.
     */
    ApiResourceChangeList getDiff(@NotNull HasItems resources,
                                  @NotNull ResourceChangeFilter filter,
                                  @NotNull ReconciliationContext context);

    /**
     * List the resources associated for the specified type.
     *
     * @param resourceClass The class of the resource to be described.
     * @return the {@link HasMetadata}.
     * @throws JikkouApiException if no {@link Collector} can be found for the specified type,
     *                            or more than one descriptor match the type.
     */
    default ResourceListObject<HasMetadata> listResources(@NotNull Class<? extends HasMetadata> resourceClass) {
        return listResources(resourceClass, Selectors.NO_SELECTOR, Configuration.empty());
    }

    /**
     * List the resources associated for the specified type.
     *
     * @param resourceClass the class of the resource to be described.
     * @return the {@link HasMetadata}.
     * @throws JikkouApiException if no {@link Collector} can be found for the specified type,
     *                            or more than one descriptor match the type.
     */
    default ResourceListObject<HasMetadata> listResources(@NotNull Class<? extends HasMetadata> resourceClass,
                                                          @NotNull Selector selector) {
        return listResources(resourceClass, selector, Configuration.empty());
    }

    /**
     * List the resources associated for the specified type.
     *
     * @param type          the class of the resource to be described.
     * @param configuration the configuration to be used for describing the resource-type.
     * @return the {@link HasMetadata}.
     * @throws JikkouApiException if no {@link Collector} can be found for the specified type,
     *                            or more than one descriptor match the type.
     */
    default <T extends HasMetadata> ResourceListObject<T> listResources(@NotNull Class<? extends HasMetadata> type,
                                                                        @NotNull Selector selector,
                                                                        @NotNull Configuration configuration) {
        return listResources(ResourceType.of(type), selector, configuration);
    }

    /**
     * List the resources associated for the specified type.
     *
     * @param resourceType the type of the resource to be described.
     * @return the {@link HasMetadata}.
     */
    default <T extends HasMetadata> ResourceListObject<T> listResources(@NotNull ResourceType resourceType) {
        return listResources(resourceType, Selectors.NO_SELECTOR, Configuration.empty());
    }

    /**
     * List the resources associated for the specified type.
     *
     * @param resourceType the type of the resource to be described.
     * @return the {@link HasMetadata}.
     */
    default <T extends HasMetadata> ResourceListObject<T> listResources(@NotNull ResourceType resourceType,
                                                                        @NotNull Selector selector) {
        return listResources(resourceType, selector, Configuration.empty());
    }

    /**
     * List the resources associated for the specified type.
     *
     * @param resourceType  the type of the resource to be described.
     * @param configuration the option to be used for describing the resource-type.
     * @return the {@link HasMetadata}.
     */
    <T extends HasMetadata> ResourceListObject<T> listResources(@NotNull ResourceType resourceType,
                                                                @NotNull Selector selector,
                                                                @NotNull Configuration configuration);

    @SuppressWarnings("rawtypes")
    ApiBuilder toBuilder();

    /**
     * {@inheritDoc}
     **/
    @Override
    void close();
}
