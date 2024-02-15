/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.http.client;

import io.streamthoughts.jikkou.common.utils.CollectionUtils;
import io.streamthoughts.jikkou.core.BaseApi;
import io.streamthoughts.jikkou.core.DefaultApi;
import io.streamthoughts.jikkou.core.JikkouApi;
import io.streamthoughts.jikkou.core.ReconciliationContext;
import io.streamthoughts.jikkou.core.ReconciliationMode;
import io.streamthoughts.jikkou.core.config.Configuration;
import io.streamthoughts.jikkou.core.extension.Extension;
import io.streamthoughts.jikkou.core.extension.ExtensionCategory;
import io.streamthoughts.jikkou.core.extension.ExtensionDescriptorModifier;
import io.streamthoughts.jikkou.core.extension.ExtensionFactory;
import io.streamthoughts.jikkou.core.models.ApiActionResultSet;
import io.streamthoughts.jikkou.core.models.ApiChangeResultList;
import io.streamthoughts.jikkou.core.models.ApiExtension;
import io.streamthoughts.jikkou.core.models.ApiExtensionList;
import io.streamthoughts.jikkou.core.models.ApiGroupList;
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
import io.streamthoughts.jikkou.core.models.change.ResourceChange;
import io.streamthoughts.jikkou.core.reconciler.ChangeResult;
import io.streamthoughts.jikkou.core.reconciler.ResourceChangeFilter;
import io.streamthoughts.jikkou.core.selector.Selector;
import io.streamthoughts.jikkou.core.validation.ValidationError;
import io.streamthoughts.jikkou.http.client.exception.JikkouApiResponseException;
import io.streamthoughts.jikkou.rest.data.ErrorEntity;
import io.streamthoughts.jikkou.rest.data.ErrorResponse;
import io.streamthoughts.jikkou.rest.data.errors.Errors;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;
import org.jetbrains.annotations.NotNull;

/**
 * Jikkou API delegating to Jikkou REST Proxy.
 */
public final class JikkouApiProxy extends BaseApi implements JikkouApi {

    /**
     * Builder class for creating a new {@link DefaultApi} object instance.
     */
    public static final class Builder implements JikkouApi.ApiBuilder<JikkouApiProxy, JikkouApiProxy.Builder> {

        private final JikkouApiClient apiClient;
        private final ExtensionFactory extensionFactory;

        /**
         * Creates a new {@link ExtensionFactory} instance.
         *
         * @param apiClient the apiClient.
         */
        public Builder(@NotNull ExtensionFactory extensionFactory,
                       @NotNull JikkouApiClient apiClient) {
            this.extensionFactory = Objects.requireNonNull(extensionFactory, "extensionFactory must not be null");
            this.apiClient = Objects.requireNonNull(apiClient, "apiClient must not be null");
        }

        /**
         * {@inheritDoc}
         **/
        @Override
        public <T extends Extension> Builder register(@NotNull Class<T> type,
                                                      @NotNull Supplier<T> supplier) {
            extensionFactory.register(type, supplier);
            return this;
        }

        /**
         * {@inheritDoc}
         **/
        @Override
        public <T extends Extension> Builder register(@NotNull Class<T> type,
                                                      @NotNull Supplier<T> supplier,
                                                      @NotNull ExtensionDescriptorModifier... modifiers) {
            extensionFactory.register(type, supplier, modifiers);
            return this;
        }


        /**
         * {@inheritDoc}
         **/
        @Override
        public JikkouApiProxy build() {
            return new JikkouApiProxy(extensionFactory, apiClient);
        }
    }

    private final JikkouApiClient apiClient;

    /**
     * Creates a new {@link JikkouApiProxy} instance.
     *
     * @param apiClient the {@link JikkouApiClient}.
     */
    public JikkouApiProxy(final @NotNull ExtensionFactory extensionFactory,
                          final @NotNull JikkouApiClient apiClient) {
        super(extensionFactory);
        this.apiClient = Objects.requireNonNull(apiClient, "apiClient must not be null");
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public ApiResourceList listApiResources(@NotNull String group, @NotNull String version) {
        return apiClient.getApiResources(group, version);
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public ApiGroupList listApiGroups() {
        return apiClient.getApiGroupList();
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public ApiHealthIndicatorList getApiHealthIndicators() {
        return apiClient.getApiHealthIndicators();
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public ApiHealthResult getApiHealth(@NotNull String name, @NotNull Duration timeout) {
        return apiClient.getApiHealth(name, timeout);
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public ApiHealthResult getApiHealth(@NotNull Duration timeout) {
        return apiClient.getApiHealth(timeout);
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public ApiExtensionList getApiExtensions() {
        return apiClient.getApiExtensions();
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public ApiExtensionList getApiExtensions(@NotNull String type) {
        return apiClient.getApiExtensions(type);
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public ApiExtensionList getApiExtensions(@NotNull Class<?> extensionType) {
        return getApiExtensions(extensionType.getName());
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public ApiExtensionList getApiExtensions(@NotNull ExtensionCategory category) {
        return apiClient.getApiExtensions(category);
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
        return apiClient.getApiExtension(extensionType, extensionName);
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public ApiChangeResultList reconcile(@NotNull HasItems resources,
                                         @NotNull ReconciliationMode mode,
                                         @NotNull ReconciliationContext context) {

        Map<ResourceType, List<HasMetadata>> resourcesByType = prepare(resources, context).groupByType();

        List<ChangeResult> changes = new ArrayList<>();
        for (Map.Entry<ResourceType, List<HasMetadata>> entry : resourcesByType.entrySet()) {
            ApiChangeResultList result = apiClient.reconcile(entry.getKey(), entry.getValue(), mode, context);
            changes.addAll(result.results());
        }
        return new ApiChangeResultList(context.isDryRun(), CollectionUtils.cast(changes));
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public ApiChangeResultList patch(@NotNull List<ResourceChange> changes,
                                     @NotNull ReconciliationMode mode,
                                     @NotNull ReconciliationContext context) {
        return apiClient.patch(changes, mode, context);
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public ApiActionResultSet<?> execute(@NotNull String action,
                                         @NotNull Configuration configuration) {
        return apiClient.execute(action, configuration);
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    @SuppressWarnings("unchecked")
    public ApiValidationResult validate(@NotNull HasItems resources,
                                        @NotNull ReconciliationContext context) {
        Map<ResourceType, List<HasMetadata>> resourcesByType = prepare(resources, context).groupByType();

        List<HasMetadata> validated = new LinkedList<>();
        List<ValidationError> errors = new ArrayList<>();
        for (Map.Entry<ResourceType, List<HasMetadata>> entry : resourcesByType.entrySet()) {
            try {
                ResourceListObject<HasMetadata> result = apiClient.validate(entry.getKey(), entry.getValue(), context);
                validated.addAll(result.getItems());
            } catch (JikkouApiResponseException exception) {
                ErrorResponse errorResponse = exception.getErrorResponse();
                for (ErrorEntity error : errorResponse.errors()) {
                    if (error.errorCode().equalsIgnoreCase(Errors.API_RESOURCE_VALIDATION_FAILED_ERROR_CODE)) {
                        errors.add(new ValidationError(
                                (String) error.details().get("name"),
                                null,
                                error.message(),
                                (Map) error.details().get("details")
                        ));
                    }
                }
            }
        }

        if (errors.isEmpty()) {
            return new ApiValidationResult(new DefaultResourceListObject<>(validated));
        } else {
            return new ApiValidationResult(errors);
        }
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public <T extends HasMetadata> T getResource(@NotNull ResourceType type,
                                                 @NotNull String name,
                                                 @NotNull Configuration configuration) {
        return apiClient.getResource(type, name, configuration);
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public ApiResourceChangeList getDiff(@NotNull HasItems resources,
                                         @NotNull ResourceChangeFilter filter,
                                         @NotNull ReconciliationContext context) {

        Map<ResourceType, List<HasMetadata>> resourcesByType = prepare(resources, context).groupByType();

        List<ResourceChange> changes = new ArrayList<>();
        for (Map.Entry<ResourceType, List<HasMetadata>> entry : resourcesByType.entrySet()) {
            ApiResourceChangeList result = apiClient.getDiff(entry.getKey(), entry.getValue(), filter, context);
            changes.addAll(result.items());
        }
        return new ApiResourceChangeList(changes);
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public <T extends HasMetadata> ResourceListObject<T> listResources(@NotNull ResourceType resourceType,
                                                                       @NotNull Selector selector,
                                                                       @NotNull Configuration configuration) {
        return apiClient.listResources(resourceType, selector, configuration);
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public JikkouApiProxy.Builder toBuilder() {
        return new JikkouApiProxy.Builder(extensionFactory.duplicate(), apiClient);
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public void close() {
    }
}
