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

import io.micronaut.http.hateoas.Link;
import io.micronaut.http.uri.UriBuilder;
import io.streamthoughts.jikkou.core.ReconciliationContext;
import io.streamthoughts.jikkou.core.ReconciliationMode;
import io.streamthoughts.jikkou.core.config.Configuration;
import io.streamthoughts.jikkou.core.health.Health;
import io.streamthoughts.jikkou.core.health.HealthAggregator;
import io.streamthoughts.jikkou.core.models.ApiChangeResultList;
import io.streamthoughts.jikkou.core.models.ApiExtensionList;
import io.streamthoughts.jikkou.core.models.ApiGroupList;
import io.streamthoughts.jikkou.core.models.ApiHealthIndicatorList;
import io.streamthoughts.jikkou.core.models.ApiHealthResult;
import io.streamthoughts.jikkou.core.models.ApiResource;
import io.streamthoughts.jikkou.core.models.ApiResourceChangeList;
import io.streamthoughts.jikkou.core.models.ApiResourceList;
import io.streamthoughts.jikkou.core.models.HasMetadata;
import io.streamthoughts.jikkou.core.models.ResourceListObject;
import io.streamthoughts.jikkou.core.models.ResourceType;
import io.streamthoughts.jikkou.core.selectors.Selector;
import io.streamthoughts.jikkou.http.client.adapter.ResourceReconcileRequestFactory;
import io.streamthoughts.jikkou.http.client.exception.JikkouApiClientException;
import io.streamthoughts.jikkou.http.client.exception.UnsupportedApiResourceException;
import io.streamthoughts.jikkou.http.client.hateoas.Links;
import io.streamthoughts.jikkou.rest.data.Info;
import io.streamthoughts.jikkou.rest.data.ResourceListRequest;
import io.streamthoughts.jikkou.rest.data.ResourceReconcileRequest;
import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import okhttp3.HttpUrl;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.jetbrains.annotations.NotNull;

/**
 * Default implementation of the {@link JikkouApiClient} interface.
 */
public final class DefaultJikkouApiClient implements JikkouApiClient {

    private static final String API_CORE_GROUP = "core.jikkou.io";
    private static final String API_CORE_VERSION = "v1";
    private static final String QUERY_PARAM_DRY_RUN = "dry-run";
    private static final String PATH_SEGMENT_APIS = "apis";
    private static final String API_HEALTHS = "healths";
    private static final String PATH_PARAM_RECONCILE_MODE = "mode";
    private static final String CONTENT_TYPE_APPLICATION_JSON = "application/json";

    private final ApiClient apiClient;

    // Constants for resource Hypermedia Application Links Key
    private static class ResourceLinkKeys {
        static String LIST = "list";
        static String RECONCILE = "reconcile";
        static String VALIDATE = "validate";
        static String DIFF = "diff";
        static String SELECT = "select";
    }

    /**
     * Creates a new {@link DefaultJikkouApiClient} instance.
     *
     * @param apiClient the {@link ApiClient} instance.
     */
    public DefaultJikkouApiClient(@NotNull ApiClient apiClient) {
        this.apiClient = Objects.requireNonNull(apiClient, "apiClient cannot be null");
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public Info getServerInfo() {
        String basePath = apiClient.getBasePath();
        Request request = new Request.Builder().url(basePath).get().build();
        return apiClient.execute(request, Info.class).getData();
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public ApiGroupList getApiGroupList() {
        HttpUrl url = baseHttpUrlBuilder(PATH_SEGMENT_APIS).build();
        Request request = new Request.Builder().url(url).build();
        ApiResponse<ApiGroupList> response = apiClient.execute(request, ApiGroupList.class);
        return response.getData();
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public ApiResourceList getApiResources(String group, String version) {
        HttpUrl url = getHttpUrlBuilderForApiGroupVersion(group, version).build();

        Request request = new Request.Builder().url(url).get().build();
        ApiResponse<ApiResourceList> response = apiClient.execute(request, ApiResourceList.class);
        return response.getData();
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public ApiHealthIndicatorList getApiHealthIndicators() {
        HttpUrl url = getHttpUrlBuilderForApiResource(API_CORE_GROUP, API_CORE_VERSION, API_HEALTHS).build();
        Request request = new Request.Builder().url(url).get().build();
        ApiResponse<ApiHealthIndicatorList> response = apiClient.execute(request, ApiHealthIndicatorList.class);
        return response.getData();
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public ApiHealthResult getApiHealth(@NotNull String name,
                                        @NotNull Duration timeout) {
        HttpUrl url = getHttpUrlBuilderForApiResource(API_CORE_GROUP, API_CORE_VERSION, API_HEALTHS)
                .addPathSegments(name)
                .addPathSegments("status")
                .addQueryParameter("timeout", String.valueOf(timeout.toMillis()))
                .build();
        Request httpRequest = new Request.Builder().url(url).get().build();
        ApiResponse<ApiHealthResult> response = apiClient.execute(httpRequest, ApiHealthResult.class);
        return response.getData();
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public ApiHealthResult getApiHealth(@NotNull Duration timeout) {
        ApiHealthIndicatorList list = getApiHealthIndicators();
        List<Health> health = list.indicators().stream()
                .map(indicator -> getApiHealth(indicator.name(), timeout))
                .map(result -> Health
                        .builder()
                        .name(result.name())
                        .status(result.status())
                        .details(result.details())
                        .build()
                )
                .toList();

        HealthAggregator aggregator = new HealthAggregator();
        Health aggregated = aggregator.aggregate(health);
        return ApiHealthResult.from(aggregated);
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public ApiExtensionList getApiExtensions() {
        HttpUrl url = getHttpUrlBuilderForApiResource(API_CORE_GROUP, API_CORE_VERSION, "extensions").build();
        Request httpRequest = new Request.Builder().url(url).get().build();
        ApiResponse<ApiExtensionList> response = apiClient.execute(
                httpRequest,
                ApiExtensionList.class
        );
        return response.getData();
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public ApiExtensionList getApiExtensions(String type) {
        HttpUrl url = getHttpUrlBuilderForApiResource(API_CORE_GROUP, API_CORE_VERSION, "extensions")
                .addQueryParameter("type", type)
                .build();
        Request httpRequest = new Request.Builder().url(url).get().build();
        ApiResponse<ApiExtensionList> response = apiClient.execute(
                httpRequest,
                ApiExtensionList.class
        );
        return response.getData();
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public <T extends HasMetadata> ResourceListObject<T> getResources(@NotNull ResourceType type) {
        ApiResource resource = queryApiResourceForType(type);
        HttpUrl url = toHttpUrl(findResourceLinkByKey(Links.of(resource.metadata()), ResourceLinkKeys.LIST, type));
        Request httpRequest = new Request.Builder().url(url).get().build();
        ApiResponse<ResourceListObject> response = apiClient.execute(
                httpRequest,
                ResourceListObject.class
        );
        return (ResourceListObject<T>) response.getData();
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public <T extends HasMetadata> ResourceListObject<T> listResources(@NotNull ResourceType type,
                                                                       @NotNull Selector selector,
                                                                       @NotNull Configuration configuration) {
        ApiResource resource = queryApiResourceForType(type);
        HttpUrl url = toHttpUrl(findResourceLinkByKey(Links.of(resource.metadata()), ResourceLinkKeys.SELECT, type));

        ResourceListRequest payload = new ResourceListRequest(
                configuration.asMap(),
                selector.getSelectorExpressions()
        );
        RequestBody requestBody = apiClient.serialize(payload, "application/json");
        Request httpRequest = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();

        ApiResponse<ResourceListObject> response = apiClient.execute(
                httpRequest,
                ResourceListObject.class
        );
        return (ResourceListObject<T>) response.getData();
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public <T extends HasMetadata> ApiChangeResultList reconcile(@NotNull ResourceType type,
                                                                 @NotNull List<T> resources,
                                                                 @NotNull ReconciliationMode mode,
                                                                 @NotNull ReconciliationContext context) {
        ApiResource apiResource = queryApiResourceForType(type);

        Link link = findResourceLinkByKey(Links.of(apiResource.metadata()), ResourceLinkKeys.RECONCILE, type);
        final URI uri = UriBuilder.of(apiClient.getBasePath())
                .path(link.getHref())
                .expand(Map.of(
                                PATH_PARAM_RECONCILE_MODE, mode.name().toLowerCase(Locale.ROOT),
                                QUERY_PARAM_DRY_RUN, context.isDryRun()
                        )
                );

        ResourceReconcileRequest request = new ResourceReconcileRequestFactory().create(resources, context);
        RequestBody body = apiClient.serialize(request, CONTENT_TYPE_APPLICATION_JSON);

        Request.Builder builder = new Request.Builder();
        Request httpRequest = builder.url(HttpUrl.get(uri))
                .post(body)
                .build();

        ApiResponse<ApiChangeResultList> response = apiClient.execute(
                httpRequest,
                ApiChangeResultList.class
        );

        return response.getData();
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public <T extends HasMetadata> ResourceListObject<T> validate(@NotNull ResourceType type,
                                                                  @NotNull List<T> resources,
                                                                  @NotNull ReconciliationContext context) {
        ApiResource apiResource = queryApiResourceForType(type);
        HttpUrl url = toHttpUrl(findResourceLinkByKey(Links.of(apiResource.metadata()), ResourceLinkKeys.VALIDATE, type));

        ResourceReconcileRequest request = new ResourceReconcileRequestFactory().create(resources, context);
        RequestBody body = apiClient.serialize(request, CONTENT_TYPE_APPLICATION_JSON);

        Request httpRequest = new Request.Builder()
                .post(body)
                .url(url)
                .build();

        ApiResponse<ResourceListObject> response = apiClient.execute(
                httpRequest,
                ResourceListObject.class
        );
        return (ResourceListObject<T>) response.getData();
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public <T extends HasMetadata> ApiResourceChangeList getDiff(@NotNull ResourceType type,
                                                                 @NotNull List<T> resources,
                                                                 @NotNull ReconciliationContext context) {
        ApiResource apiResource = queryApiResourceForType(type);
        HttpUrl url = toHttpUrl(findResourceLinkByKey(Links.of(apiResource.metadata()), ResourceLinkKeys.DIFF, type));

        ResourceReconcileRequest request = new ResourceReconcileRequestFactory().create(resources, context);
        RequestBody body = apiClient.serialize(request, CONTENT_TYPE_APPLICATION_JSON);

        Request httpRequest = new Request.Builder()
                .post(body)
                .url(url)
                .build();

        ApiResponse<ApiResourceChangeList> response = apiClient.execute(
                httpRequest,
                ApiResourceChangeList.class
        );
        return response.getData();
    }

    @NotNull
    private ApiResource queryApiResourceForType(@NotNull final ResourceType type) {
        return queryApiResourceForType(type.group(), type.apiVersion(), type.kind());
    }

    @NotNull
    private ApiResource queryApiResourceForType(@NotNull String group,
                                                @NotNull String version,
                                                @NotNull String kind) {
        ApiResourceList apiResources = getApiResources(group, version);
        return apiResources.resources()
                .stream()
                .filter(r -> r.kind().equalsIgnoreCase(kind))
                .findFirst()
                .orElseThrow(() -> new UnsupportedApiResourceException(group, version, kind));
    }

    @NotNull
    private Link findResourceLinkByKey(@NotNull Links links,
                                       @NotNull String action,
                                       @NotNull ResourceType type) {
        return links.findLinkByKey(action)
                .orElseThrow(() -> new JikkouApiClientException(String.format(
                        "Failed to %s resources for group '%s', version '%s', and kind '%s'. " +
                                "Cannot find _links['%s'] field from returned ApiResourceList metadata.(%s)",
                        action,
                        type.group(),
                        type.group(),
                        type.kind(),
                        action,
                        getHttpUrlBuilderForApiGroupVersion(type.group(), type.apiVersion()).build()
                )));
    }

    private HttpUrl toHttpUrl(@NotNull Link link) {
        return baseHttpUrlBuilder(link.getHref()).build();
    }

    @NotNull
    private HttpUrl.Builder baseHttpUrlBuilder(@NotNull String link) {
        return apiClient.getBaseUrl().newBuilder(link);
    }

    @NotNull
    private HttpUrl.Builder getHttpUrlBuilderForApiGroupVersion(String group,
                                                                String version) {
        return baseHttpUrlBuilder(PATH_SEGMENT_APIS)
                .addPathSegments(group)
                .addPathSegments(version);
    }

    @NotNull
    private HttpUrl.Builder getHttpUrlBuilderForApiResource(String group,
                                                            String version,
                                                            String resource) {
        return getHttpUrlBuilderForApiGroupVersion(group, version).
                addPathSegments(resource);
    }

}
