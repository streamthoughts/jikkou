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
package io.streamthoughts.jikkou.http.client;

import io.streamthoughts.jikkou.core.ReconciliationContext;
import io.streamthoughts.jikkou.core.ReconciliationMode;
import io.streamthoughts.jikkou.core.config.Configuration;
import io.streamthoughts.jikkou.core.exceptions.JikkouRuntimeException;
import io.streamthoughts.jikkou.core.models.ApiChangeResultList;
import io.streamthoughts.jikkou.core.models.ApiExtensionList;
import io.streamthoughts.jikkou.core.models.ApiGroupList;
import io.streamthoughts.jikkou.core.models.ApiHealthIndicatorList;
import io.streamthoughts.jikkou.core.models.ApiHealthResult;
import io.streamthoughts.jikkou.core.models.ApiResourceChangeList;
import io.streamthoughts.jikkou.core.models.ApiResourceList;
import io.streamthoughts.jikkou.core.models.DefaultResourceListObject;
import io.streamthoughts.jikkou.core.models.HasMetadata;
import io.streamthoughts.jikkou.core.models.ResourceListObject;
import io.streamthoughts.jikkou.core.models.ResourceType;
import io.streamthoughts.jikkou.core.selector.Selector;
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
     * @return a new {@link ApiHealthResult} instance.
     * @throws JikkouApiResponseException if the client receives an error response from the server.
     * @throws JikkouApiClientException   if the client has encountered an error while communicating with the server.
     * @throws JikkouRuntimeException     if the client has encountered a previous fatal error or for any other unexpected error.
     */
    ApiHealthResult getApiHealth(@NotNull String name, @NotNull Duration timeout);

    /**
     * Get the health details for all supported health indicators.
     *
     * @return a new {@link ApiHealthResult} instance.
     * @throws JikkouApiResponseException if the client receives an error response from the server.
     * @throws JikkouApiClientException   if the client has encountered an error while communicating with the server.
     * @throws JikkouRuntimeException     if the client has encountered a previous fatal error or for any other unexpected error.
     */
    ApiHealthResult getApiHealth(@NotNull Duration timeout);

    /**
     * Get the supported API extensions.
     *
     * @return a {@link ApiExtensionList} instance.
     */
    ApiExtensionList getApiExtensions();

    /**
     * Get the supported API extensions for the supported type.
     *
     * @return a {@link ApiExtensionList} instance.
     */
    ApiExtensionList getApiExtensions(String type);

    /**
     * List all resources matching the specified type and selectors.
     *
     * @param <T> type of the resource-list items.
     * @return a {@link ResourceListObject}.
     * @throws JikkouApiResponseException if the client receives an error response from the server.
     * @throws JikkouApiClientException   if the client has encountered an error while communicating with the server.
     * @throws JikkouRuntimeException     if the client has encountered a previous fatal error or for any other unexpected error.
     */
    <T extends HasMetadata> ResourceListObject<T> getResources(@NotNull ResourceType resourceType);

    /**
     * List all resources matching the specified type and selectors.
     *
     * @param <T> type of the resource-list items.
     * @return a {@link ResourceListObject}.
     * @throws JikkouApiResponseException if the client receives an error response from the server.
     * @throws JikkouApiClientException   if the client has encountered an error while communicating with the server.
     * @throws JikkouRuntimeException     if the client has encountered a previous fatal error or for any other unexpected error.
     */
    <T extends HasMetadata> ResourceListObject<T> listResources(@NotNull ResourceType resourceType,
                                                                @NotNull Selector selector,
                                                                @NotNull Configuration configuration);

    /**
     * Applies the creation changes required to reconcile the specified resources.
     *
     * @param type      the type of the resources.
     * @param resources the resources to validate.
     * @param mode      the reconciliation mode.
     * @param context   the reconciliation context.
     * @return the validated {@link DefaultResourceListObject}.
     * @throws JikkouApiResponseException if the client receives an error response from the server.
     * @throws JikkouApiClientException   if the client has encountered an error while communicating with the server.
     * @throws JikkouRuntimeException     if the client has encountered a previous fatal error or for any other unexpected error.
     */
    <T extends HasMetadata> ApiChangeResultList reconcile(@NotNull ResourceType type,
                                                          @NotNull List<T> resources,
                                                          @NotNull ReconciliationMode mode,
                                                          @NotNull ReconciliationContext context);

    /**
     * Validates the specified resources.
     *
     * @param type      the type of the resources.
     * @param resources the resources to validate.
     * @param context   the reconciliation context.
     * @return the validated {@link DefaultResourceListObject}.
     * @throws JikkouApiResponseException if the client receives an error response from the server.
     * @throws JikkouApiClientException   if the client has encountered an error while communicating with the server.
     * @throws JikkouRuntimeException     if the client has encountered a previous fatal error or for any other unexpected error.
     */
    <T extends HasMetadata> ResourceListObject<T> validate(@NotNull ResourceType type,
                                                           @NotNull List<T> resources,
                                                           @NotNull ReconciliationContext context);

    /**
     * Gets the differences from the specified resources and the existing ones.
     *
     * @param type      the type of the resources.
     * @param resources the resources to validate.
     * @param context   the reconciliation context.
     * @return the validated {@link DefaultResourceListObject}.
     * @throws JikkouApiResponseException if the client receives an error response from the server.
     * @throws JikkouApiClientException   if the client has encountered an error while communicating with the server.
     * @throws JikkouRuntimeException     if the client has encountered a previous fatal error or for any other unexpected error.
     */
    <T extends HasMetadata> ApiResourceChangeList getDiff(@NotNull ResourceType type,
                                                          @NotNull List<T> resources,
                                                          @NotNull ReconciliationContext context);

}
