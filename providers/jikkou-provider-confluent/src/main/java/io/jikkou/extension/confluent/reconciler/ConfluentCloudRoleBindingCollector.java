/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.jikkou.extension.confluent.reconciler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import io.jikkou.core.annotation.SupportedResource;
import io.jikkou.core.config.Configuration;
import io.jikkou.core.exceptions.ConfigException;
import io.jikkou.core.extension.ExtensionContext;
import io.jikkou.core.io.Jackson;
import io.jikkou.core.models.ObjectMeta;
import io.jikkou.core.models.ResourceList;
import io.jikkou.core.reconciler.Collector;
import io.jikkou.core.selector.Selector;
import io.jikkou.extension.confluent.ConfluentCloudExtensionProvider;
import io.jikkou.extension.confluent.adapter.RoleBindingAdapter;
import io.jikkou.extension.confluent.api.ConfluentCloudApiClient;
import io.jikkou.extension.confluent.api.ConfluentCloudApiClientConfig;
import io.jikkou.extension.confluent.api.ConfluentCloudApiClientException;
import io.jikkou.extension.confluent.api.ConfluentCloudApiClientFactory;
import io.jikkou.extension.confluent.api.data.RoleBindingData;
import io.jikkou.extension.confluent.collections.V1RoleBindingList;
import io.jikkou.extension.confluent.models.V1RoleBinding;
import jakarta.ws.rs.WebApplicationException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SupportedResource(type = V1RoleBinding.class)
public class ConfluentCloudRoleBindingCollector implements Collector<V1RoleBinding> {

    private static final Logger LOG = LoggerFactory.getLogger(ConfluentCloudRoleBindingCollector.class);

    public static final String LABEL_PRINCIPAL_NAME = "confluent.cloud/principal-name";
    public static final String LABEL_PRINCIPAL_EMAIL = "confluent.cloud/principal-email";

    private ConfluentCloudApiClientConfig apiClientConfig;

    public ConfluentCloudRoleBindingCollector() {
    }

    public ConfluentCloudRoleBindingCollector(ConfluentCloudApiClientConfig apiClientConfig) {
        init(apiClientConfig);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void init(@NotNull final ExtensionContext context) {
        init(context.<ConfluentCloudExtensionProvider>provider().apiClientConfig());
    }

    private void init(@NotNull ConfluentCloudApiClientConfig config) throws ConfigException {
        this.apiClientConfig = config;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResourceList<V1RoleBinding> listAll(@NotNull Configuration configuration,
                                               @NotNull Selector selector) {
        ConfluentCloudApiClient api = ConfluentCloudApiClientFactory.create(apiClientConfig);
        try {
            List<RoleBindingData> response = api.listRoleBindings();
            Map<String, PrincipalInfo> principalLookup = buildPrincipalLookup(api);

            List<V1RoleBinding> items = RoleBindingAdapter.map(response)
                .stream()
                .map(rb -> enrichWithPrincipalInfo(rb, principalLookup))
                .filter(selector::apply)
                .collect(Collectors.toList());

            return new V1RoleBindingList.Builder().withItems(items).build();

        } catch (WebApplicationException e) {
            String response;
            try {
                response = Jackson.JSON_OBJECT_MAPPER.writerWithDefaultPrettyPrinter()
                    .writeValueAsString(e.getResponse().readEntity(JsonNode.class));
            } catch (JsonProcessingException ex) {
                response = e.getResponse().readEntity(String.class);
            }
            throw new ConfluentCloudApiClientException(String.format(
                "failed to list Confluent Cloud role bindings. %s:%n%s",
                e.getLocalizedMessage(),
                response
            ), e);
        } finally {
            api.close();
        }
    }

    private Map<String, PrincipalInfo> buildPrincipalLookup(ConfluentCloudApiClient api) {
        Map<String, PrincipalInfo> lookup = new HashMap<>();
        try {
            api.listUsers().forEach(user ->
                lookup.put("User:" + user.id(), new PrincipalInfo(user.fullName(), user.email()))
            );
        } catch (Exception e) {
            LOG.warn("Failed to fetch users for principal name resolution: {}", e.getMessage());
        }
        try {
            api.listServiceAccounts().forEach(sa ->
                lookup.put("User:" + sa.id(), new PrincipalInfo(sa.displayName(), null))
            );
        } catch (Exception e) {
            LOG.warn("Failed to fetch service accounts for principal name resolution: {}", e.getMessage());
        }
        return lookup;
    }

    private V1RoleBinding enrichWithPrincipalInfo(V1RoleBinding rb, Map<String, PrincipalInfo> lookup) {
        String principal = rb.getSpec().getPrincipal();
        PrincipalInfo info = lookup.get(principal);
        if (info == null) {
            return rb;
        }

        ObjectMeta meta = rb.getMetadata() != null ? rb.getMetadata() : ObjectMeta.builder().build();
        ObjectMeta.ObjectMetaBuilder builder = meta.toBuilder();

        if (info.name() != null) {
            builder = builder.withLabel(LABEL_PRINCIPAL_NAME, info.name());
        }
        if (info.email() != null) {
            builder = builder.withLabel(LABEL_PRINCIPAL_EMAIL, info.email());
        }
        return rb.withMetadata(builder.build());
    }

    private record PrincipalInfo(String name, String email) {
    }
}
