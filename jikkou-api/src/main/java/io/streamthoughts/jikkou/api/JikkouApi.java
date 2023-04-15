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
import io.streamthoughts.jikkou.api.control.Change;
import io.streamthoughts.jikkou.api.control.ChangeResult;
import io.streamthoughts.jikkou.api.control.ExternalResourceCollector;
import io.streamthoughts.jikkou.api.control.ExternalResourceController;
import io.streamthoughts.jikkou.api.error.JikkouApiException;
import io.streamthoughts.jikkou.api.model.GenericResourceListObject;
import io.streamthoughts.jikkou.api.model.HasItems;
import io.streamthoughts.jikkou.api.model.HasMetadata;
import io.streamthoughts.jikkou.api.model.HasMetadataChange;
import io.streamthoughts.jikkou.api.model.ResourceListObject;
import io.streamthoughts.jikkou.api.model.ResourceType;
import io.streamthoughts.jikkou.api.selector.ResourceSelector;
import io.streamthoughts.jikkou.api.transform.ResourceTransformation;
import io.streamthoughts.jikkou.api.validation.ResourceValidation;
import io.streamthoughts.jikkou.common.annotation.InterfaceStability;
import java.util.Collections;
import java.util.List;
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
         * Associate the given list of resource validations to the building {@link JikkouApi}.
         *
         * @param validations the list of validation to associate.
         * @return this builder; never null
         */
        B withValidations(@NotNull List<ResourceValidation<HasMetadata>> validations);

        /**
         * Associate the given list of resource transformations to the building {@link JikkouApi}.
         *
         * @param transformations the list of transformations to associate.
         * @return this builder; never null
         */
        B withTransformations(@NotNull List<ResourceTransformation<HasMetadata>> transformations);

        /**
         * Associate the given list of resource controllers to the building {@link JikkouApi}.
         *
         * @param controllers the list of controllers to associate.
         * @return this builder; never null
         */
        @SuppressWarnings("rawtypes")
        B withControllers(final @NotNull List<ExternalResourceController> controllers);

        /**
         * Associate the given list of resource collectors to the building {@link JikkouApi}.
         *
         * @param collectors the list of collectors to associate.
         * @return this builder; never null
         */
        @SuppressWarnings("rawtypes")
        B withCollectors(@NotNull List<ExternalResourceCollector> collectors);

        /**
         * Associate the given resource validation to the building {@link JikkouApi}.
         *
         * @param validation the resource validation; should not be null.
         * @return this builder; never null
         */
        B withValidation(@NotNull ResourceValidation<? extends HasMetadata> validation);

        /**
         * Associate the given resource transformation to the building {@link JikkouApi}.
         *
         * @param transformation the resource transformation; should not be null.
         * @return this builder; never null
         */
        B withTransformation(@NotNull ResourceTransformation<? extends HasMetadata> transformation);

        /**
         * Associate the given resource controller to the building {@link JikkouApi}.
         *
         * @param controller the resource controller; should not be null.
         * @return this builder; never null
         */
        @SuppressWarnings("rawtypes")
        default B withController(final @NotNull ExternalResourceController controller) {
            return withControllers(List.of(controller));
        }

        /**
         * Associate the given resource descriptor to the building {@link JikkouApi}.
         *
         * @param descriptor the resource descriptor; should not be null.
         * @return this builder; never null
         */
        @SuppressWarnings("rawtypes")
        default B withCollector(final @NotNull ExternalResourceCollector descriptor) {
            return withCollectors(List.of(descriptor));
        }

        /**
         * Build and return the {@link JikkouApi}.
         *
         * @return the {@link JikkouApi}; never null
         */
        A build();
    }

    /**
     * Associate a {@link ResourceValidation} to this api. Validations will be executed in order
     * on the resources passed to the {@link #validate(HasItems)}, and
     * {@link #apply(HasItems, ReconciliationMode, ReconciliationContext)} methods.
     *
     * @param validation the resource validation to associate to this api.
     * @return this api object so methods can be chained together; never null
     */
    JikkouApi addValidation(@NotNull ResourceValidation<? extends HasMetadata> validation);

    /**
     * Associate a {@link ResourceTransformation} to this api. Transformation will be executed in order
     * on the resources passed to the {@link #validate(HasItems)}, and
     * {@link #apply(HasItems, ReconciliationMode, ReconciliationContext)} method.
     *
     * @param transformation the resource transformation to associate to this api.
     * @return this api object so methods can be chained together; never null
     */
    JikkouApi addTransformation(@NotNull ResourceTransformation<? extends HasMetadata> transformation);

    /**
     * Execute the reconciliation for the given resources using
     *
     * @param resources the list of resource to be reconciled.
     * @param mode      the reconciliation mode.
     * @param context   the context to be used for conciliation.
     * @return the list of all changes applied on the target system.
     */
    List<ChangeResult<Change>> apply(@NotNull HasItems resources,
                                     @NotNull ReconciliationMode mode,
                                     @NotNull ReconciliationContext context);

    /**
     * Execute validations on the given resources.
     *
     * @param resources the list of resource to create.
     * @return the validated {@link GenericResourceListObject}.
     * @throws JikkouApiException if no {@link ExternalResourceCollector} can be found for the specified type,
     *                            or more than one descriptor match the type.
     */
    default GenericResourceListObject validate(@NotNull HasItems resources) {
        return validate(resources, Collections.emptyList());
    }

    /**
     * Execute validations on the given resources.
     *
     * @param resources the list of resource to create.
     * @return the validated {@link GenericResourceListObject}.
     * @throws JikkouApiException if no {@link ExternalResourceCollector} can be found for the specified type,
     *                            or more than one descriptor match the type.
     */
    GenericResourceListObject validate(@NotNull HasItems resources, final @NotNull List<ResourceSelector> selectors);

    /**
     * Get the resources associated to the given type.
     *
     * @param resourceClass the class of the resource to be described.
     * @return the {@link HasMetadata}.
     * @throws JikkouApiException if no {@link ExternalResourceCollector} can be found for the specified type,
     *                            or more than one descriptor match the type.
     */
    default List<HasMetadata> getResources(final @NotNull Class<? extends HasMetadata> resourceClass) {
        return getResources(resourceClass, Collections.emptyList(), Configuration.empty());
    }

    /**
     * Get the resources associated to the given type.
     *
     * @param resourceClass the class of the resource to be described.
     * @return the {@link HasMetadata}.
     * @throws JikkouApiException if no {@link ExternalResourceCollector} can be found for the specified type,
     *                            or more than one descriptor match the type.
     */
    default List<HasMetadata> getResources(final @NotNull Class<? extends HasMetadata> resourceClass,
                                           final @NotNull List<ResourceSelector> selectors) {
        return getResources(resourceClass, selectors, Configuration.empty());
    }

    /**
     * Get the resource associated to the given type.
     *
     * @param resourceClass the class of the resource to be described.
     * @return the {@link HasMetadata}.
     * @throws JikkouApiException if no {@link ExternalResourceCollector} can be found for the specified type,
     *                            or more than one descriptor match the type.
     */
    default <T extends HasMetadata> List<T> getResources(final @NotNull Class<? extends HasMetadata> resourceClass,
                                                         final @NotNull Configuration configuration) {
        return getResources(resourceClass, Collections.emptyList(), configuration);
    }


    /**
     * Get the resource associated to the given type.
     *
     * @param type          the class of the resource to be described.
     * @param configuration the configuration to be used for describing the resource-type.
     * @return the {@link HasMetadata}.
     * @throws JikkouApiException if no {@link ExternalResourceCollector} can be found for the specified type,
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
    List<ResourceListObject<HasMetadataChange<Change>>> getDiff(@NotNull final HasItems resources,
                                                                @NotNull final List<ResourceSelector> selectors);

    /**
     * Get the resource associated to the given type.
     *
     * @param resourceType the type of the resource to be described.
     * @return the {@link HasMetadata}.
     */
    default List<HasMetadata> getResources(final @NotNull ResourceType resourceType,
                                           final @NotNull List<ResourceSelector> selectors) {
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
    default <T extends HasMetadata> List<T> getResources(final @NotNull ResourceType resourceType,
                                                         final @NotNull Configuration configuration) {
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
