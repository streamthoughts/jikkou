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
import io.streamthoughts.jikkou.core.exceptions.JikkouRuntimeException;
import io.streamthoughts.jikkou.core.extension.ExtensionContext;
import io.streamthoughts.jikkou.core.io.Jackson;
import io.streamthoughts.jikkou.core.models.ResourceList;
import io.streamthoughts.jikkou.core.reconciler.Collector;
import io.streamthoughts.jikkou.core.selector.Selector;
import io.streamthoughts.jikkou.extension.aiven.AivenExtensionProvider;
import io.streamthoughts.jikkou.extension.aiven.adapter.SchemaRegistryAclEntryAdapter;
import io.streamthoughts.jikkou.extension.aiven.api.AivenApiClient;
import io.streamthoughts.jikkou.extension.aiven.api.AivenApiClientConfig;
import io.streamthoughts.jikkou.extension.aiven.api.AivenApiClientException;
import io.streamthoughts.jikkou.extension.aiven.api.AivenApiClientFactory;
import io.streamthoughts.jikkou.extension.aiven.api.data.ListSchemaRegistryAclResponse;
import io.streamthoughts.jikkou.extension.aiven.collections.V1SchemaRegistryAclEntryList;
import io.streamthoughts.jikkou.extension.aiven.models.V1SchemaRegistryAclEntry;
import jakarta.ws.rs.WebApplicationException;
import java.util.List;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;

@SupportedResource(type = V1SchemaRegistryAclEntry.class)
public final class AivenSchemaRegistryAclEntryCollector implements Collector<V1SchemaRegistryAclEntry> {

    private AivenApiClientConfig apiClientConfig;

    /**
     * Creates a new {@link AivenSchemaRegistryAclEntryCollector} instance.
     */
    public AivenSchemaRegistryAclEntryCollector() {
    }

    /**
     * Creates a new {@link AivenSchemaRegistryAclEntryCollector} instance.
     *
     * @param apiClientConfig the configuration.
     */
    public AivenSchemaRegistryAclEntryCollector(AivenApiClientConfig apiClientConfig) {
        init(apiClientConfig);
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public void init(@NotNull final ExtensionContext context) {
        init(context.<AivenExtensionProvider>provider().apiClientConfig());
    }

    private void init(@NotNull AivenApiClientConfig apiClientConfig) throws ConfigException {
        this.apiClientConfig = apiClientConfig;
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public ResourceList<V1SchemaRegistryAclEntry> listAll(@NotNull Configuration configuration,
                                                          @NotNull Selector selector) {
        AivenApiClient api = AivenApiClientFactory.create(apiClientConfig);
        try {
            ListSchemaRegistryAclResponse response = api.listSchemaRegistryAclEntries();

            if (!response.errors().isEmpty()) {
                throw new AivenApiClientException(
                        String.format("failed to list kafka acl entries. %s (%s)",
                                response.message(),
                                response.errors()
                        )
                );
            }

            List<V1SchemaRegistryAclEntry> items = SchemaRegistryAclEntryAdapter.map(response.acl())
                    .stream()
                    .filter(selector::apply)
                    .collect(Collectors.toList());
            return new V1SchemaRegistryAclEntryList.Builder().withItems(items).build();

        } catch (WebApplicationException e) {
            String response;
            try {
                response = Jackson.JSON_OBJECT_MAPPER.writerWithDefaultPrettyPrinter()
                        .writeValueAsString(e.getResponse().readEntity(JsonNode.class));
            } catch (JsonProcessingException ex) {
                response = e.getResponse().readEntity(String.class);
            }
            throw new JikkouRuntimeException(String.format(
                    "failed to list schema registry acl entries. %s:%n%s",
                    e.getLocalizedMessage(),
                    response
            ), e);
        } finally {
            api.close(); // make sure api is closed after catching exception
        }
    }
}
