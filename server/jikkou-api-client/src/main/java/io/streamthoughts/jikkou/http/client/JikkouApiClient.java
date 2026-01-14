/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.http.client;

import io.streamthoughts.jikkou.core.GetContext;
import io.streamthoughts.jikkou.core.ListContext;
import io.streamthoughts.jikkou.core.ReconciliationContext;
import io.streamthoughts.jikkou.core.ReconciliationMode;
import io.streamthoughts.jikkou.core.config.Configuration;
import io.streamthoughts.jikkou.core.exceptions.JikkouApiException;
import io.streamthoughts.jikkou.core.exceptions.JikkouRuntimeException;
import io.streamthoughts.jikkou.core.extension.ExtensionCategory;
import io.streamthoughts.jikkou.core.models.ApiActionResultSet;
import io.streamthoughts.jikkou.core.models.ApiChangeResultList;
import io.streamthoughts.jikkou.core.models.ApiExtension;
import io.streamthoughts.jikkou.core.models.ApiExtensionList;
import io.streamthoughts.jikkou.core.models.ApiGroupList;
import io.streamthoughts.jikkou.core.models.ApiHealthIndicatorList;
import io.streamthoughts.jikkou.core.models.ApiHealthResult;
import io.streamthoughts.jikkou.core.models.ApiResourceChangeList;
import io.streamthoughts.jikkou.core.models.ApiResourceList;
import io.streamthoughts.jikkou.core.models.HasItems;
import io.streamthoughts.jikkou.core.models.HasMetadata;
import io.streamthoughts.jikkou.core.models.ResourceList;
import io.streamthoughts.jikkou.core.models.ResourceType;
import io.streamthoughts.jikkou.core.reconciler.Collector;
import io.streamthoughts.jikkou.core.reconciler.ResourceChangeFilter;
import io.streamthoughts.jikkou.http.client.exception.JikkouApiClientException;
import io.streamthoughts.jikkou.http.client.exception.JikkouApiResponseException;
import io.streamthoughts.jikkou.rest.data.Info;
import java.time.Duration;
import java.util.List;
import org.jetbrains.annotations.NotNull;

/**
 * API Client interface for Jikkou.
 */
public interface JikkouApiClient {

    /**
     * Gets API server information.
     *
     * @return The Info.
     * @throws JikkouApiResponseException if the client receives an error response from the server.
     * @throws JikkouApiClientException   if the client has encountered an error while communicating with the server.
     */
    Info getServerInfo();

    /**
     * Gets the list of supported API groups.
     *
     * @return a {@link ApiGroupList}.
     * @throws JikkouApiResponseException if the client receives an error response from the server.
     * @throws JikkouApiClientException   if the client has encountered an error while communicating with the server.
     * @throws JikkouRuntimeException     if the client has encountered a previous fatal error or for any other unexpected error.
     */
    ApiGroupList getApiGroupList();

    /**
     * Gets the list of supported API resources for the specified group and version.
     *
     * @param group   the name of the resource API group.
     * @param version the version of the resource API.
     * @return a {@link ApiResourceList}.
     * @throws JikkouApiResponseException if the client receives an error response from the server.
     * @throws JikkouApiClientException   if the client has encountered an error while communicating with the server.
     * @throws JikkouRuntimeException     if the client has encountered a previous fatal error or for any other unexpected error.
     */
    ApiResourceList getApiResources(String group, String version);

    /**
     * Gets the list of supported Health Indicators.
     *
     * @return a {@link ApiResourceList}.
     * @throws JikkouApiResponseException if the client receives an error response from the server.
     * @throws JikkouApiClientException   if the client has encountered an error while communicating with the server.
     * @throws JikkouRuntimeException     if the client has encountered a previous fatal error or for any other unexpected error.
     */
    ApiHealthIndicatorList getApiHealthIndicators();

    /**
     * Gets the health details for the specified health indicator name.
     *
     * @param name the health indicator name.
     * @param timeout the timeout duration.
     * @param providerName the provider name for selecting specific provider instance, or null for default.
     * @return a new {@link ApiHealthResult} instance.
     * @throws JikkouApiResponseException if the client receives an error response from the server.
     * @throws JikkouApiClientException   if the client has encountered an error while communicating with the server.
     * @throws JikkouRuntimeException     if the client has encountered a previous fatal error or for any other unexpected error.
     */
    ApiHealthResult getApiHealth(@NotNull String name, @NotNull Duration timeout, String providerName);

    /**
     * Get the health details for all supported health indicators.
     *
     * @param timeout the timeout duration.
     * @param providerName the provider name for selecting specific provider instance, or null for default.
     * @return a new {@link ApiHealthResult} instance.
     * @throws JikkouApiResponseException if the client receives an error response from the server.
     * @throws JikkouApiClientException   if the client has encountered an error while communicating with the server.
     * @throws JikkouRuntimeException     if the client has encountered a previous fatal error or for any other unexpected error.
     */
    ApiHealthResult getApiHealth(@NotNull Duration timeout, String providerName);

    /**
     * Get the supported API extensions.
     *
     * @return a {@link ApiExtensionList} instance.
     * @throws JikkouApiResponseException if the client receives an error response from the server.
     * @throws JikkouApiClientException   if the client has encountered an error while communicating with the server.
     * @throws JikkouRuntimeException     if the client has encountered a previous fatal error or for any other unexpected error.
     */
    ApiExtensionList getApiExtensions();

    /**
     * Get the supported API extensions for the supported type.
     *
     * @return a {@link ApiExtensionList} instance.
     * @throws JikkouApiResponseException if the client receives an error response from the server.
     * @throws JikkouApiClientException   if the client has encountered an error while communicating with the server.
     * @throws JikkouRuntimeException     if the client has encountered a previous fatal error or for any other unexpected error.
     */
    ApiExtensionList getApiExtensions(String type);

    /**
     * Get the supported API extensions for the supported category.
     *
     * @return a {@link ApiExtensionList} instance.
     * @throws JikkouApiResponseException if the client receives an error response from the server.
     * @throws JikkouApiClientException   if the client has encountered an error while communicating with the server.
     * @throws JikkouRuntimeException     if the client has encountered a previous fatal error or for any other unexpected error.
     */
    ApiExtensionList getApiExtensions(ExtensionCategory category);

    /**
     * Gets the API extension for the specified name and type.
     *
     * @param extensionType The type of the extension.
     * @param extensionName The name of the extension.
     * @return a {@link ApiExtensionList} instance.
     * @throws JikkouApiResponseException if the client receives an error response from the server.
     * @throws JikkouApiClientException   if the client has encountered an error while communicating with the server.
     * @throws JikkouRuntimeException     if the client has encountered a previous fatal error or for any other unexpected error.
     */
    ApiExtension getApiExtension(Class<?> extensionType, String extensionName);

    /**
     * List all resources matching the specified type and selectors.
     *
     * @param <T> type of the resource-list items.
     * @return a {@link ResourceList}.
     * @throws JikkouApiResponseException if the client receives an error response from the server.
     * @throws JikkouApiClientException   if the client has encountered an error while communicating with the server.
     * @throws JikkouRuntimeException     if the client has encountered a previous fatal error or for any other unexpected error.
     */
    <T extends HasMetadata> ResourceList<T> getResources(@NotNull ResourceType resourceType);

    /**
     * Get the resource associated for the specified type.
     *
     * @param type    The type of the resource to be described.
     * @param name    The name of the resource.
     * @param context The get context containing configuration and provider.
     * @return the {@link HasMetadata}.
     * @throws JikkouApiResponseException if the client receives an error response from the server.
     * @throws JikkouApiClientException   if the client has encountered an error while communicating with the server.
     * @throws JikkouRuntimeException     if the client has encountered a previous fatal error or for any other unexpected error.
     * @since 0.37.0
     */
    <T extends HasMetadata> T getResource(@NotNull ResourceType type,
                                          @NotNull String name,
                                          @NotNull GetContext context);

    /**
     * List all resources matching the specified type and context.
     *
     * @param resourceType the resource type.
     * @param context      the list context containing selector, configuration and provider.
     * @param <T> type of the resource-list items.
     * @return a {@link ResourceList}.
     * @throws JikkouApiResponseException if the client receives an error response from the server.
     * @throws JikkouApiClientException   if the client has encountered an error while communicating with the server.
     * @throws JikkouRuntimeException     if the client has encountered a previous fatal error or for any other unexpected error.
     * @since 0.37.0
     */
    <T extends HasMetadata> ResourceList<T> listResources(@NotNull ResourceType resourceType,
                                                          @NotNull ListContext context);

    /**
     * Applies the creation changes required to reconcile the specified resources.
     *
     * @param type      the type of the resources.
     * @param resources the resources to validate.
     * @param mode      the reconciliation mode.
     * @param context   the reconciliation context.
     * @return the {@link ApiChangeResultList}.
     * @throws JikkouApiResponseException if the client receives an error response from the server.
     * @throws JikkouApiClientException   if the client has encountered an error while communicating with the server.
     * @throws JikkouRuntimeException     if the client has encountered a previous fatal error or for any other unexpected error.
     */
    <T extends HasMetadata> ApiChangeResultList reconcile(@NotNull ResourceType type,
                                                          @NotNull List<T> resources,
                                                          @NotNull ReconciliationMode mode,
                                                          @NotNull ReconciliationContext context);
    /**
     * Replaces all the given list of resources.
     *
     * @param resources the resources.
     * @param context   the context to be used for conciliation.
     * @return the results of the changes applied on resources.
     * @throws JikkouApiException if no {@link Collector} can be found for the specified type,
     *                            or more than one descriptor match the type.
     */
    <T extends HasMetadata> ApiChangeResultList replace(@NotNull ResourceType type,
                                                        @NotNull List<T> resources,
                                                        @NotNull ReconciliationContext context);

    /**
     * Applies all the changes provided through the given list of resources.
     *
     * @param resources The resource changes.
     * @param mode    the reconciliation mode.
     * @param context the context to be used for conciliation.
     * @return the {@link ApiChangeResultList}.
     * @throws JikkouApiResponseException if the client receives an error response from the server.
     * @throws JikkouApiClientException   if the client has encountered an error while communicating with the server.
     * @throws JikkouRuntimeException     if the client has encountered a previous fatal error or for any other unexpected error.
     */
    ApiChangeResultList patch(@NotNull HasItems resources,
                              @NotNull ReconciliationMode mode,
                              @NotNull ReconciliationContext context);

    /**
     * Validates the specified resources.
     *
     * @param type      the type of the resources.
     * @param resources the resources to validate.
     * @param context   the reconciliation context.
     * @return the {@link ApiChangeResultList}.
     * @throws JikkouApiResponseException if the client receives an error response from the server.
     * @throws JikkouApiClientException   if the client has encountered an error while communicating with the server.
     * @throws JikkouRuntimeException     if the client has encountered a previous fatal error or for any other unexpected error.
     */
    <T extends HasMetadata> ResourceList<T> validate(@NotNull ResourceType type,
                                                     @NotNull List<T> resources,
                                                     @NotNull ReconciliationContext context);

    /**
     * Gets the differences from the specified resources and the existing ones.
     *
     * @param type      the type of the resources.
     * @param resources the resources to validate.
     * @param context   the reconciliation context.
     * @return the {@link ApiChangeResultList}.
     * @throws JikkouApiResponseException if the client receives an error response from the server.
     * @throws JikkouApiClientException   if the client has encountered an error while communicating with the server.
     * @throws JikkouRuntimeException     if the client has encountered a previous fatal error or for any other unexpected error.
     */
    <T extends HasMetadata> ApiResourceChangeList getDiff(@NotNull ResourceType type,
                                                          @NotNull List<T> resources,
                                                          @NotNull ResourceChangeFilter filter,
                                                          @NotNull ReconciliationContext context);

    /**
     * Executes the specified action for the specified resource type.
     *
     * @param action        The name of the action.
     * @param configuration The context of the execution.
     * @param providerName  the provider name for selecting specific provider instance, or null for default.
     * @return The ApiExecutionResult.
     * @throws JikkouApiResponseException if the client receives an error response from the server.
     * @throws JikkouApiClientException   if the client has encountered an error while communicating with the server.
     * @throws JikkouRuntimeException     if the client has encountered a previous fatal error or for any other unexpected error.
     */
    ApiActionResultSet<?> execute(@NotNull String action, @NotNull Configuration configuration, String providerName);

}
