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

import io.streamthoughts.jikkou.common.annotation.InterfaceStability;
import io.streamthoughts.jikkou.core.config.Configuration;
import io.streamthoughts.jikkou.core.exceptions.JikkouApiException;
import io.streamthoughts.jikkou.core.extension.Extension;
import io.streamthoughts.jikkou.core.extension.ExtensionDescriptorModifier;
import io.streamthoughts.jikkou.core.extension.exceptions.ConflictingExtensionDefinitionException;
import io.streamthoughts.jikkou.core.models.GenericResourceListObject;
import io.streamthoughts.jikkou.core.models.HasItems;
import io.streamthoughts.jikkou.core.models.HasMetadata;
import io.streamthoughts.jikkou.core.models.HasMetadataChange;
import io.streamthoughts.jikkou.core.models.ResourceListObject;
import io.streamthoughts.jikkou.core.models.ResourceType;
import io.streamthoughts.jikkou.core.reconcilier.Change;
import io.streamthoughts.jikkou.core.reconcilier.ChangeResult;
import io.streamthoughts.jikkou.core.resource.ResourceCollector;
import io.streamthoughts.jikkou.core.selectors.ResourceSelector;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import org.jetbrains.annotations.NotNull;

/**
 * The Jikkou API interface.
 */
@InterfaceStability.Evolving
public interface JikkouApi extends AutoCloseable {

    /**
     * The basic interface for JikkouApi builders.
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
     * Execute the reconciliation for the given resources using
     *
     * @param resources the list of resource to be reconciled.
     * @param mode      the reconciliation mode.
     * @param context   the context to be used for conciliation.
     * @return the list of all changes applied on the target system.
     * @throws JikkouApiException if no {@link ResourceCollector} can be found for the specified type,
     *                            or more than one descriptor match the type.
     */
    List<ChangeResult<Change>> apply(@NotNull HasItems resources,
                                     @NotNull ReconciliationMode mode,
                                     @NotNull ReconciliationContext context);

    /**
     * Execute validations on the given resources.
     *
     * @param resources the list of resource to create.
     * @return the validated {@link GenericResourceListObject}.
     * @throws JikkouApiException if no {@link ResourceCollector} can be found for the specified type,
     *                            or more than one descriptor match the type.
     */
    default ApiResourceValidationResult validate(@NotNull HasItems resources) {
        return validate(resources, ReconciliationContext.Default.EMPTY);
    }

    /**
     * Execute validations on the given resources.
     *
     * @param resources the list of resource to create.
     * @param context   the reconciliation context.
     * @return the validated {@link GenericResourceListObject}.
     * @throws JikkouApiException if no {@link ResourceCollector} can be found for the specified type,
     *                            or more than one descriptor match the type.
     */
    ApiResourceValidationResult validate(@NotNull HasItems resources,
                                         @NotNull ReconciliationContext context);

    /**
     * Get the resources associated to the given type.
     *
     * @param resourceClass the class of the resource to be described.
     * @return the {@link HasMetadata}.
     * @throws JikkouApiException if no {@link ResourceCollector} can be found for the specified type,
     *                            or more than one descriptor match the type.
     */
    default List<HasMetadata> getResources(@NotNull Class<? extends HasMetadata> resourceClass) {
        return getResources(resourceClass, Collections.emptyList(), Configuration.empty());
    }

    /**
     * Get the resources associated to the given type.
     *
     * @param resourceClass the class of the resource to be described.
     * @return the {@link HasMetadata}.
     * @throws JikkouApiException if no {@link ResourceCollector} can be found for the specified type,
     *                            or more than one descriptor match the type.
     */
    default List<HasMetadata> getResources(@NotNull Class<? extends HasMetadata> resourceClass,
                                           @NotNull List<ResourceSelector> selectors) {
        return getResources(resourceClass, selectors, Configuration.empty());
    }

    /**
     * Get the resource associated to the given type.
     *
     * @param resourceClass the class of the resource to be described.
     * @return the {@link HasMetadata}.
     * @throws JikkouApiException if no {@link ResourceCollector} can be found for the specified type,
     *                            or more than one descriptor match the type.
     */
    default <T extends HasMetadata> List<T> getResources(@NotNull Class<? extends HasMetadata> resourceClass,
                                                         @NotNull Configuration configuration) {
        return getResources(resourceClass, Collections.emptyList(), configuration);
    }


    /**
     * Get the resource associated to the given type.
     *
     * @param type          the class of the resource to be described.
     * @param configuration the configuration to be used for describing the resource-type.
     * @return the {@link HasMetadata}.
     * @throws JikkouApiException if no {@link ResourceCollector} can be found for the specified type,
     *                            or more than one descriptor match the type.
     */
    @SuppressWarnings("unchecked")
    default <T extends HasMetadata> List<T> getResources(@NotNull Class<? extends HasMetadata> type,
                                                         @NotNull List<ResourceSelector> selectors,
                                                         @NotNull Configuration configuration) {
        return (List<T>) getResources(ResourceType.create(type), selectors, configuration);
    }

    /**
     * Get all the changes for the given resources.
     *
     * @param resources the list of resource to be reconciled.
     * @return the {@link HasMetadata}.
     */
    List<ResourceListObject<HasMetadataChange<Change>>> getDiff(@NotNull HasItems resources,
                                                                @NotNull ReconciliationContext context);

    /**
     * Get the resource associated to the given type.
     *
     * @param resourceType the type of the resource to be described.
     * @return the {@link HasMetadata}.
     */
    default List<HasMetadata> getResources(@NotNull ResourceType resourceType,
                                           @NotNull List<ResourceSelector> selectors) {
        return getResources(resourceType, selectors, Configuration.empty());
    }

    /**
     * Get the resource associated to the given type.
     *
     * @param resourceType  the type of the resource to be described.
     * @param configuration the option to be used for describing the resource-type.
     * @return the {@link HasMetadata}.
     */
    @SuppressWarnings("unchecked")
    default <T extends HasMetadata> List<T> getResources(@NotNull ResourceType resourceType,
                                                         @NotNull Configuration configuration) {
        return (List<T>) getResources(resourceType, Collections.emptyList(), configuration);
    }

    /**
     * Get the resource associated to the given type.
     *
     * @param resourceType  the type of the resource to be described.
     * @param configuration the option to be used for describing the resource-type.
     * @return the {@link HasMetadata}.
     */
    List<HasMetadata> getResources(@NotNull ResourceType resourceType,
                                   @NotNull List<ResourceSelector> selectors,
                                   @NotNull Configuration configuration);

    @SuppressWarnings("rawtypes")
    ApiBuilder toBuilder();

    /**
     * {@inheritDoc}
     **/
    @Override
    void close();
}
