/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.extension.aiven.reconciler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import io.streamthoughts.jikkou.core.annotation.SupportedResource;
import io.streamthoughts.jikkou.core.config.Configuration;
import io.streamthoughts.jikkou.core.exceptions.ConfigException;
import io.streamthoughts.jikkou.core.extension.ExtensionContext;
import io.streamthoughts.jikkou.core.io.Jackson;
import io.streamthoughts.jikkou.core.models.ResourceList;
import io.streamthoughts.jikkou.core.reconciler.Collector;
import io.streamthoughts.jikkou.core.selector.Selector;
import io.streamthoughts.jikkou.extension.aiven.AivenExtensionProvider;
import io.streamthoughts.jikkou.extension.aiven.adapter.KafkaQuotaAdapter;
import io.streamthoughts.jikkou.extension.aiven.api.AivenApiClient;
import io.streamthoughts.jikkou.extension.aiven.api.AivenApiClientConfig;
import io.streamthoughts.jikkou.extension.aiven.api.AivenApiClientException;
import io.streamthoughts.jikkou.extension.aiven.api.AivenApiClientFactory;
import io.streamthoughts.jikkou.extension.aiven.api.data.ListKafkaQuotaResponse;
import io.streamthoughts.jikkou.extension.aiven.collections.V1KafkaQuotaList;
import io.streamthoughts.jikkou.extension.aiven.models.V1KafkaQuota;
import jakarta.ws.rs.WebApplicationException;
import java.util.List;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;

@SupportedResource(type = V1KafkaQuota.class)
public class AivenKafkaQuotaCollector implements Collector<V1KafkaQuota> {

    private AivenApiClientConfig apiClientConfig;

    /**
     * Creates a new {@link AivenKafkaQuotaCollector} instance.
     */
    public AivenKafkaQuotaCollector() {
    }

    /**
     * Creates a new {@link AivenKafkaQuotaCollector} instance.
     *
     * @param apiClientConfig the configuration.
     */
    public AivenKafkaQuotaCollector(AivenApiClientConfig apiClientConfig) {
        init(apiClientConfig);
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public void init(@NotNull final ExtensionContext context) {
        init(context.<AivenExtensionProvider>provider().apiClientConfig());
    }

    private void init(@NotNull AivenApiClientConfig config) throws ConfigException {
        this.apiClientConfig = config;
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public ResourceList<V1KafkaQuota> listAll(@NotNull Configuration configuration,
                                              @NotNull Selector selector) {
        final AivenApiClient api = AivenApiClientFactory.create(apiClientConfig);
        try {
            ListKafkaQuotaResponse response = api.listKafkaQuotas();

            if (!response.errors().isEmpty()) {
                throw new AivenApiClientException(
                        String.format("failed to list kafka acl entries. %s (%s)",
                                response.message(),
                                response.errors()
                        )
                );
            }

            List<V1KafkaQuota> items = response.quotas()
                    .stream()
                    .map(KafkaQuotaAdapter::map)
                    .filter(selector::apply)
                    .collect(Collectors.toList());
            return new V1KafkaQuotaList.Builder().withItems(items).build();

        } catch (WebApplicationException e) {
            String response;
            try {
                response = Jackson.JSON_OBJECT_MAPPER.writerWithDefaultPrettyPrinter()
                        .writeValueAsString(e.getResponse().readEntity(JsonNode.class));
            } catch (JsonProcessingException ex) {
                response = e.getResponse().readEntity(String.class);
            }
            throw new AivenApiClientException(String.format(
                    "failed to list kafka quotas. %s:%n%s",
                    e.getLocalizedMessage(),
                    response
            ), e);
        } finally {
            api.close(); // make sure api is closed after catching exception
        }
    }
}
