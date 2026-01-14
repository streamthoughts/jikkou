/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.http.client;

import io.micronaut.http.hateoas.Link;
import io.micronaut.http.uri.UriBuilder;
import io.streamthoughts.jikkou.core.GetContext;
import io.streamthoughts.jikkou.core.ListContext;
import io.streamthoughts.jikkou.core.ReconciliationContext;
import io.streamthoughts.jikkou.core.ReconciliationMode;
import io.streamthoughts.jikkou.core.config.Configuration;
import io.streamthoughts.jikkou.core.extension.ExtensionCategory;
import io.streamthoughts.jikkou.core.health.Health;
import io.streamthoughts.jikkou.core.health.HealthAggregator;
import io.streamthoughts.jikkou.core.models.ApiActionResultSet;
import io.streamthoughts.jikkou.core.models.ApiChangeResultList;
import io.streamthoughts.jikkou.core.models.ApiExtension;
import io.streamthoughts.jikkou.core.models.ApiExtensionList;
import io.streamthoughts.jikkou.core.models.ApiGroupList;
import io.streamthoughts.jikkou.core.models.ApiHealthIndicatorList;
import io.streamthoughts.jikkou.core.models.ApiHealthResult;
import io.streamthoughts.jikkou.core.models.ApiResource;
import io.streamthoughts.jikkou.core.models.ApiResourceChangeList;
import io.streamthoughts.jikkou.core.models.ApiResourceList;
import io.streamthoughts.jikkou.core.models.HasItems;
import io.streamthoughts.jikkou.core.models.HasMetadata;
import io.streamthoughts.jikkou.core.models.NamedValueSet;
import io.streamthoughts.jikkou.core.models.ResourceList;
import io.streamthoughts.jikkou.core.models.ResourceType;
import io.streamthoughts.jikkou.core.reconciler.ResourceChangeFilter;
import io.streamthoughts.jikkou.core.selector.Selector;
import io.streamthoughts.jikkou.http.client.adapter.ResourceReconcileRequestFactory;
import io.streamthoughts.jikkou.http.client.exception.JikkouApiClientException;
import io.streamthoughts.jikkou.http.client.exception.UnsupportedApiResourceException;
import io.streamthoughts.jikkou.http.client.hateoas.Links;
import io.streamthoughts.jikkou.rest.data.Info;
import io.streamthoughts.jikkou.rest.data.ResourceListRequest;
import io.streamthoughts.jikkou.rest.data.ResourceReconcileRequest;
import java.net.URI;
import java.time.Duration;
import java.util.HashMap;
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

    private static final String API_CORE_VERSION = "api/v1";
    private static final String QUERY_PARAM_DRY_RUN = "dry-run";
    private static final String PATH_SEGMENT_APIS = "apis";
    private static final String API_HEALTHS = "healths";
    private static final String API_EXTENSIONS = "extensions";
    private static final String API_ACTIONS = "actions";
    private static final String API_RESOURCES = "resources";
    private static final String PATH_PARAM_RECONCILE_MODE = "mode";
    private static final String PATH_PARAM_NAME = "name";
    private static final String CONTENT_TYPE_APPLICATION_JSON = "application/json";
    private static final RequestBody EMPTY_REQUEST = RequestBody.create(null, new byte[0]);

    private final ApiClient apiClient;

    // Constants for resource Hypermedia Application Links Key
    private static class ResourceLinkKeys {
        static String LIST = "list";
        static String RECONCILE = "reconcile";
        static String REPLACE = "replace";
        static String VALIDATE = "validate";
        static String DIFF = "diff";
        static String SELECT = "select";
        static String GET = "get";
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
        // Build Request
        Request request = new Request.Builder()
            .url(basePath)
            .get()
            .build();
        // Execute Request
        return apiClient.execute(request, Info.class).getData();
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public ApiGroupList getApiGroupList() {
        HttpUrl url = baseHttpUrlBuilder(PATH_SEGMENT_APIS).build();
        // Build Request
        Request request = new Request.Builder()
            .url(url)
            .build();
        ApiResponse<ApiGroupList> response = apiClient.execute(request, ApiGroupList.class);
        // Execute Request
        return response.getData();
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public ApiResourceList getApiResources(String group, String version) {
        HttpUrl url = getHttpUrlBuilderForApiGroupVersion(group, version).build();
        // Build Request
        Request request = new Request.Builder().url(url).get().build();
        // Execute Request
        ApiResponse<ApiResourceList> response = apiClient.execute(request, ApiResourceList.class);
        return response.getData();
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public ApiHealthIndicatorList getApiHealthIndicators() {
        HttpUrl url = baseHttpUrlBuilder(API_CORE_VERSION)
            .addPathSegments(API_HEALTHS)
            .build();
        // Build Request
        Request request = new Request.Builder()
            .url(url)
            .get()
            .build();
        // Execute Request
        ApiResponse<ApiHealthIndicatorList> response = apiClient.execute(request, ApiHealthIndicatorList.class);
        return response.getData();
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public ApiHealthResult getApiHealth(@NotNull String name,
                                        @NotNull Duration timeout,
                                        String providerName) {
        HttpUrl.Builder urlBuilder = baseHttpUrlBuilder(API_CORE_VERSION)
            .addPathSegments(API_HEALTHS)
            .addPathSegments(name)
            .addPathSegments("status")
            .addQueryParameter("timeout", String.valueOf(timeout.toMillis()));
        if (providerName != null) {
            urlBuilder.addQueryParameter("provider", providerName);
        }
        HttpUrl url = urlBuilder.build();
        // Build Request
        Request httpRequest = new Request.Builder()
            .url(url)
            .get()
            .build();
        // Execute Request
        ApiResponse<ApiHealthResult> response = apiClient.execute(httpRequest, ApiHealthResult.class);
        return response.getData();
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public ApiHealthResult getApiHealth(@NotNull Duration timeout, String providerName) {
        ApiHealthIndicatorList list = getApiHealthIndicators();
        List<Health> health = list.indicators().stream()
            .map(indicator -> getApiHealth(indicator.name(), timeout, providerName))
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
        HttpUrl url = baseHttpUrlBuilder(API_CORE_VERSION)
            .addPathSegments(API_EXTENSIONS)
            .build();
        // Build Request
        Request httpRequest = new Request.Builder()
            .url(url)
            .get()
            .build();
        // Execute Request
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
        HttpUrl url = baseHttpUrlBuilder(API_CORE_VERSION)
            .addPathSegments(API_EXTENSIONS)
            .addQueryParameter("type", type)
            .build();
        // Build Request
        Request httpRequest = new Request.Builder()
            .url(url).get()
            .build();
        // Execute Request
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
    public ApiExtensionList getApiExtensions(ExtensionCategory category) {
        HttpUrl url = baseHttpUrlBuilder(API_CORE_VERSION)
            .addPathSegments(API_EXTENSIONS)
            .addQueryParameter("category", category.name())
            .build();
        // Build Request
        Request httpRequest = new Request
            .Builder()
            .url(url)
            .get()
            .build();
        // Execute Request
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
    public ApiExtension getApiExtension(Class<?> extensionType,
                                        String extensionName) {
        HttpUrl url = baseHttpUrlBuilder(API_CORE_VERSION)
            .addPathSegments(API_EXTENSIONS)
            .addPathSegments(extensionName)
            .build();
        // Build Request
        Request httpRequest = new Request.Builder()
            .url(url)
            .get()
            .build();
        // Execute Request
        ApiResponse<ApiExtension> response = apiClient.execute(
            httpRequest,
            ApiExtension.class
        );
        return response.getData();
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public <T extends HasMetadata> ResourceList<T> getResources(@NotNull ResourceType type) {
        ApiResource resource = queryApiResourceForType(type);
        HttpUrl url = toHttpUrl(findResourceLinkByKey(Links.of(resource.metadata()), ResourceLinkKeys.LIST, type));
        // Build Request
        Request httpRequest = new Request.Builder()
            .url(url)
            .get()
            .build();
        // Execute Request
        ApiResponse<ResourceList> response = apiClient.execute(
            httpRequest,
            ResourceList.class
        );
        return (ResourceList<T>) response.getData();
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    @SuppressWarnings("unchecked")
    public <T extends HasMetadata> T getResource(@NotNull ResourceType type,
                                                 @NotNull String name,
                                                 @NotNull GetContext context) {
        ApiResource resource = queryApiResourceForType(type);
        Link link = findResourceLinkByKey(Links.of(resource.metadata()), ResourceLinkKeys.GET, type);

        Configuration configuration = context.configuration();
        String providerName = context.providerName();

        HashMap<String, Object> expandValues = new HashMap<>(configuration.asMap());
        expandValues.put(PATH_PARAM_NAME, name);
        final URI uri = UriBuilder.of(apiClient.getBasePath())
            .path(link.getHref())
            .expand(expandValues);

        HttpUrl.Builder urlBuilder = HttpUrl.get(uri).newBuilder();
        if (providerName != null) {
            urlBuilder.addQueryParameter("provider", providerName);
        }

        // Build Request
        Request httpRequest = new Request.Builder()
            .url(urlBuilder.build())
            .get()
            .build();
        // Execute Request
        ApiResponse<HasMetadata> response = apiClient.execute(
            httpRequest,
            HasMetadata.class
        );
        return (T) response.getData();
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public <T extends HasMetadata> ResourceList<T> listResources(@NotNull ResourceType type,
                                                                 @NotNull ListContext context) {
        ApiResource resource = queryApiResourceForType(type);
        Link link = findResourceLinkByKey(Links.of(resource.metadata()), ResourceLinkKeys.SELECT, type);

        Configuration configuration = context.configuration();
        Selector selector = context.selector();
        String providerName = context.providerName();

        final URI uri = UriBuilder.of(apiClient.getBasePath())
            .path(link.getHref())
            .expand(configuration.asMap());

        ResourceListRequest payload = new ResourceListRequest(
            configuration.asMap(),
            selector.getSelectorExpressions(),
            selector.getSelectorMatchingStrategy(),
            providerName
        );
        // Build Request
        RequestBody requestBody = apiClient.serialize(payload, "application/json");
        Request httpRequest = new Request.Builder()
            .url(HttpUrl.get(uri))
            .post(requestBody)
            .build();
        // Execute Request
        ApiResponse<ResourceList> response = apiClient.execute(
            httpRequest,
            ResourceList.class
        );
        return (ResourceList<T>) response.getData();
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public <T extends HasMetadata> ApiChangeResultList reconcile(@NotNull ResourceType type,
                                                                 @NotNull List<T> resources,
                                                                 @NotNull ReconciliationMode mode,
                                                                 @NotNull ReconciliationContext context) {
        return doSendReconcileRequest(type, resources, mode, context, ResourceLinkKeys.RECONCILE);
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public <T extends HasMetadata> ApiChangeResultList replace(@NotNull ResourceType type,
                                                               @NotNull List<T> resources,
                                                               @NotNull ReconciliationContext context) {
        return doSendReconcileRequest(type, resources, ReconciliationMode.FULL, context, ResourceLinkKeys.REPLACE);
    }

    private <T extends HasMetadata> ApiChangeResultList doSendReconcileRequest(@NotNull ResourceType type,
                                                                               @NotNull List<T> resources,
                                                                               @NotNull ReconciliationMode mode,
                                                                               @NotNull ReconciliationContext context,
                                                                               @NotNull String linkKey) {
        ApiResource apiResource = queryApiResourceForType(type);

        Link link = findResourceLinkByKey(Links.of(apiResource.metadata()), linkKey, type);
        final URI uri = UriBuilder.of(apiClient.getBasePath())
            .path(link.getHref())
            .expand(Map.of(
                    PATH_PARAM_RECONCILE_MODE, mode.name().toLowerCase(Locale.ROOT),
                    QUERY_PARAM_DRY_RUN, context.isDryRun()
                )
            );

        // Build Request
        ResourceReconcileRequest request = new ResourceReconcileRequestFactory().create(resources, context);
        RequestBody body = apiClient.serialize(request, CONTENT_TYPE_APPLICATION_JSON);

        Request.Builder builder = new Request.Builder();
        Request httpRequest = builder.url(HttpUrl.get(uri))
            .post(body)
            .build();

        // Execute Request
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
    public ApiChangeResultList patch(@NotNull HasItems resources,
                                     @NotNull ReconciliationMode mode,
                                     @NotNull ReconciliationContext context) {
        HttpUrl url = baseHttpUrlBuilder(API_CORE_VERSION)
            .addPathSegments(API_RESOURCES)
            .addPathSegments("patch")
            .addPathSegments("mode")
            .addPathSegments(mode.name().toLowerCase(Locale.ROOT))
            .addQueryParameter("dry-run", String.valueOf(context.isDryRun()))
            .build();
        
        // Build Request
        ResourceReconcileRequest request = new ResourceReconcileRequestFactory().create(resources.getItems(), context);
        RequestBody body = apiClient.serialize(request, CONTENT_TYPE_APPLICATION_JSON);

        Request.Builder builder = new Request.Builder();
        Request httpRequest = builder.url(url)
            .post(body)
            .build();
        // Execute Request
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
    public <T extends HasMetadata> ResourceList<T> validate(@NotNull ResourceType type,
                                                            @NotNull List<T> resources,
                                                            @NotNull ReconciliationContext context) {
        ApiResource apiResource = queryApiResourceForType(type);
        HttpUrl url = toHttpUrl(findResourceLinkByKey(Links.of(apiResource.metadata()), ResourceLinkKeys.VALIDATE, type));

        // Build Request
        ResourceReconcileRequest request = new ResourceReconcileRequestFactory().create(resources, context);
        RequestBody body = apiClient.serialize(request, CONTENT_TYPE_APPLICATION_JSON);

        Request httpRequest = new Request.Builder()
            .post(body)
            .url(url)
            .build();
        // Execute Request
        ApiResponse<ResourceList> response = apiClient.execute(
            httpRequest,
            ResourceList.class
        );
        return (ResourceList<T>) response.getData();
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public <T extends HasMetadata> ApiResourceChangeList getDiff(@NotNull ResourceType type,
                                                                 @NotNull List<T> resources,
                                                                 @NotNull ResourceChangeFilter filter,
                                                                 @NotNull ReconciliationContext context) {
        ApiResource apiResource = queryApiResourceForType(type);
        HttpUrl.Builder urlBuilder = toHttpUrl(findResourceLinkByKey(Links.of(apiResource.metadata()), ResourceLinkKeys.DIFF, type))
            .newBuilder();
        // Build Request
        ResourceReconcileRequest request = new ResourceReconcileRequestFactory().create(resources, context);

        NamedValueSet values = filter.toValues();
        values.forEach(val -> urlBuilder.addQueryParameter(val.getName(), val.getValue().toString()));

        RequestBody body = apiClient.serialize(request, CONTENT_TYPE_APPLICATION_JSON);

        Request httpRequest = new Request.Builder()
            .post(body)
            .url(urlBuilder.build())
            .build();

        // Execute Request
        ApiResponse<ApiResourceChangeList> response = apiClient.execute(
            httpRequest,
            ApiResourceChangeList.class
        );
        return response.getData();
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    @SuppressWarnings("rawuse")
    public ApiActionResultSet<?> execute(@NotNull String action, @NotNull Configuration configuration, String providerName) {
        // Build Path
        HttpUrl.Builder builder = baseHttpUrlBuilder(API_CORE_VERSION)
            .addPathSegments(API_ACTIONS)
            .addPathSegments(action)
            .addPathSegments("execute");

        if (providerName != null) {
            builder.addQueryParameter("provider", providerName);
        }

        for (String key : configuration.keys()) {
            Object value = configuration.getAny(key);
            if (List.class.isAssignableFrom(value.getClass())) {
                List<String> values = configuration.getStringList(key);
                values.forEach(val -> builder.addQueryParameter(key, val));
            } else {
                builder.addQueryParameter(key, configuration.getString(key));
            }
        }
        HttpUrl url = builder.build();
        // Build Request
        Request httpRequest = new Request.Builder().url(url)
            .post(EMPTY_REQUEST)
            .header("Content-Length", "0")
            .build();
        // Execute Request
        ApiResponse<ApiActionResultSet> response = apiClient.execute(
            httpRequest,
            ApiActionResultSet.class
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
                type.apiVersion(),
                type.kind(),
                action,
                getHttpUrlBuilderForApiGroupVersion(type.group(), type.apiVersion()).build()
            )));
    }

    @NotNull
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
}
