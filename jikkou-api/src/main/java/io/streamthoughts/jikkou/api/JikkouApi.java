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
import io.streamthoughts.jikkou.api.control.ChangeResult;
import io.streamthoughts.jikkou.api.control.ResourceController;
import io.streamthoughts.jikkou.api.control.ResourceDescriptor;
import io.streamthoughts.jikkou.api.error.JikkouApiException;
import io.streamthoughts.jikkou.api.model.HasMetadata;
import io.streamthoughts.jikkou.api.model.Resource;
import io.streamthoughts.jikkou.api.model.ResourceList;
import io.streamthoughts.jikkou.api.model.ResourceTransformation;
import io.streamthoughts.jikkou.api.model.ResourceType;
import io.streamthoughts.jikkou.api.model.ResourceValidation;
import io.streamthoughts.jikkou.common.annotation.InterfaceStability;
import java.util.Collection;
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
         * @param validations   the list of validation to associate.
         * @return  this builder; never null
         */
        B withValidations(@NotNull List<ResourceValidation> validations);

        /**
         * Associate the given list of resource transformations to the building {@link JikkouApi}.
         *
         * @param transformations   the list of transformations to associate.
         * @return  this builder; never null
         */
        B withTransformations(@NotNull List<ResourceTransformation> transformations);

        /**
         * Associate the given list of resource controllers to the building {@link JikkouApi}.
         *
         * @param controllers   the list of controllers to associate.
         * @return  this builder; never null
         */
        @SuppressWarnings("rawtypes")
        B withControllers(final @NotNull List<ResourceController> controllers);

        /**
         * Associate the given list of resource descriptors to the building {@link JikkouApi}.
         *
         * @param descriptors   the list of descriptors to associate.
         * @return  this builder; never null
         */
        @SuppressWarnings("rawtypes")
        B withDescriptors(@NotNull List<ResourceDescriptor> descriptors);

        /**
         * Associate the given resource validation to the building {@link JikkouApi}.
         *
         * @param validation   the resource validation; should not be null.
         * @return  this builder; never null
         */
        default B withValidation(@NotNull ResourceValidation validation) {
           return withValidations(List.of(validation));
        }

        /**
         * Associate the given resource transformation to the building {@link JikkouApi}.
         *
         * @param transformation   the resource transformation; should not be null.
         * @return  this builder; never null
         */
        default B withTransformations(@NotNull ResourceTransformation transformation) {
            return withTransformations(List.of(transformation));
        }

        /**
         * Associate the given resource controller to the building {@link JikkouApi}.
         *
         * @param controller   the resource controller; should not be null.
         * @return  this builder; never null
         */
        @SuppressWarnings("rawtypes")
        default B withController(final @NotNull ResourceController controller) {
            return withControllers(List.of(controller));
        }

        /**
         * Associate the given resource descriptor to the building {@link JikkouApi}.
         *
         * @param descriptor   the resource descriptor; should not be null.
         * @return  this builder; never null
         */
        @SuppressWarnings("rawtypes")
        default B withDescriptor(final @NotNull ResourceDescriptor descriptor) {
            return withDescriptors(List.of(descriptor));
        }

        /**
         * Build and return the {@link JikkouApi}.
         *
         * @return the {@link JikkouApi}; never null
         */
        A build();
    }

    /**
     * Associate a {@link ResourceListHandler} to this api. Handlers will be executed in order
     * on the resources passed to the {@link #validate(ResourceList)}, and
     * {@link #apply(ResourceList, ReconciliationMode, ReconciliationContext)} methods.
     *
     * @param handler   the resource handler to associate to this api.
     * @return this api object so methods can be chained together; never null
     */
    JikkouApi addHandler(@NotNull ResourceListHandler handler);

    /**
     * Associate a {@link ResourceValidation} to this api. Validations will be executed in order
     * on the resources passed to the {@link #validate(ResourceList)}, and
     * {@link #apply(ResourceList, ReconciliationMode, ReconciliationContext)} methods.
     *
     * @param validation   the resource validation to associate to this api.
     * @return this api object so methods can be chained together; never null
     */
    JikkouApi addValidation(@NotNull ResourceValidation validation);

    /**
     * Associate a {@link ResourceTransformation} to this api. Transformation will be executed in order
     * on the resources passed to the {@link #validate(ResourceList)}, and
     * {@link #apply(ResourceList, ReconciliationMode, ReconciliationContext)} method.
     *
     * @param transformation   the resource transformation to associate to this api.
     * @return this api object so methods can be chained together; never null
     */
    JikkouApi addTransformation(@NotNull ResourceTransformation transformation);

    /**
     * Execute the reconciliation for the given resources using
     *
     * @param resources the list of resource to be reconciled.
     * @param mode the reconciliation mode.
     * @param context   the context to be used for conciliation.
     *
     * @return the list of all changes applied on the target system.
     */
    Collection<ChangeResult<?>> apply(@NotNull ResourceList resources,
                                      @NotNull ReconciliationMode mode,
                                      @NotNull ReconciliationContext context);

    /**
     * Execute validations on the given resources.
     *
     * @param resources the list of resource to create.
     * @return the validated {@link ResourceList}.
     * @throws JikkouApiException if no {@link ResourceDescriptor} can be found for the specified type,
     *                            or more than one descriptor match the type.
     */
    ResourceList validate(@NotNull ResourceList resources);

    /**
     * Get the resource associated to the given type.
     *
     * @param resourceClass the class of the resource to be described.
     * @return the {@link HasMetadata}.
     * @throws JikkouApiException if no {@link ResourceDescriptor} can be found for the specified type,
     *                            or more than one descriptor match the type.
     */
    default HasMetadata getResource(final @NotNull Class<? extends Resource> resourceClass) {
        return getResource(resourceClass, ResourceFilter.DEFAULT, Configuration.empty());
    }

    /**
     * Get the resource associated to the given type.
     *
     * @param resourceClass the class of the resource to be described.
     * @return the {@link HasMetadata}.
     * @throws JikkouApiException if no {@link ResourceDescriptor} can be found for the specified type,
     *                            or more than one descriptor match the type.
     */
    default HasMetadata getResource(final @NotNull Class<? extends Resource> resourceClass,
                                   final @NotNull ResourceFilter filter) {
        return getResource(resourceClass, filter, Configuration.empty());
    }

    /**
     * Get the resource associated to the given type.
     *
     * @param resourceClass the class of the resource to be described.
     * @return the {@link HasMetadata}.
     * @throws JikkouApiException if no {@link ResourceDescriptor} can be found for the specified type,
     *                            or more than one descriptor match the type.
     */
    default <T extends HasMetadata> T getResource(final @NotNull Class<? extends Resource> resourceClass,
                                                  final @NotNull Configuration configuration) {
        return getResource(resourceClass, ResourceFilter.DEFAULT, configuration);
    }


    /**
     * Get the resource associated to the given type.
     *
     * @param resourceClass the class of the resource to be described.
     * @param configuration the configuration to be used for describing the resource-type.
     * @return the {@link HasMetadata}.
     * @throws JikkouApiException if no {@link ResourceDescriptor} can be found for the specified type,
     *                            or more than one descriptor match the type.
     */
    <T extends HasMetadata> T getResource(@NotNull Class<? extends Resource> resourceClass,
                                          @NotNull ResourceFilter filter,
                                          @NotNull Configuration configuration);

    /**
     * Get the resource associated to the given type.
     *
     * @param resourceType the type of the resource to be described.
     * @return the {@link HasMetadata}.
     */
    default HasMetadata getResource(final @NotNull ResourceType resourceType,
                                    final @NotNull ResourceFilter resourceFilter) {
        return getResource(resourceType, resourceFilter, Configuration.empty());
    }

    /**
     * Get the resource associated to the given type.
     *
     * @param resourceType  the type of the resource to be described.
     * @param configuration the option to be used for describing the resource-type.
     * @return the {@link HasMetadata}.
     */
    @SuppressWarnings("unchecked")
    default <T extends HasMetadata> T getResource(final @NotNull ResourceType resourceType,
                                    final @NotNull Configuration configuration) {
        return (T) getResource(resourceType, ResourceFilter.DEFAULT, configuration);
    }

    /**
     * Get the resource associated to the given type.
     *
     * @param resourceType  the type of the resource to be described.
     * @param configuration the option to be used for describing the resource-type.
     * @return the {@link HasMetadata}.
     */
    HasMetadata getResource(@NotNull ResourceType resourceType,
                            @NotNull ResourceFilter resourceFilter,
                            @NotNull Configuration configuration);
    /** {@inheritDoc} **/
    @Override
    void close();
}
